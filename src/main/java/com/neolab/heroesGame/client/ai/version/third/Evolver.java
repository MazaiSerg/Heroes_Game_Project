package com.neolab.heroesGame.client.ai.version.third;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.aditional.StatisticWriter;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.arena.StringArmyFactory;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.PlayerFactory;
import com.neolab.heroesGame.client.ai.enums.BotType;
import com.neolab.heroesGame.client.ai.server.elo.RatingElo;
import com.neolab.heroesGame.client.ai.server.elo.SelfPlayRoomFroCorrectingElo;
import com.neolab.heroesGame.errors.HeroExceptions;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class Evolver {
    public static final Integer ARMY_SIZE = StatisticWriter.ARMY_SIZE;
    private static final long SEED = 156517;
    private static final Random RANDOM = new Random(SEED);
    private static final Integer MAX_COUNT_GAME_ROOMS = 5;
    private static final Integer STEP_NUMBERS = 5;
    private static final Integer MUTATE_STRENGTH = 4;
    private static long startTime;
    private static final String START_GENOME = "ADMH";
    private static final BotType BOT_TYPE = BotType.MULTI_ARMED_WITH_COEFFICIENTS;

    public static void main(final String[] args) throws Exception {
        List<String> genomes = new ArrayList<>(6);
        genomes.add(START_GENOME);
        genomes.add(mutate(START_GENOME));
        genomes.add(mutate(START_GENOME));
        genomes.add(mutate(START_GENOME));
        genomes.add(mutate(START_GENOME));
        genomes.add(mutate(START_GENOME));
        for (int i = 0; i < 2; i++) {
            RatingElo ratingElo = RatingElo.createRatingElo(genomes);
            for (int stepCounter = 0; stepCounter < STEP_NUMBERS; stepCounter++) {
                Map<String, List<String>> matching = new HashMap<>();
                for (String genome : genomes) {
                    matching.put(genome, ratingElo.getOpponents(genome));
                }
                startTime = System.currentTimeMillis();
                final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(MAX_COUNT_GAME_ROOMS, MAX_COUNT_GAME_ROOMS,
                        0L, TimeUnit.SECONDS, getQueue(matching, ratingElo));
                threadPoolExecutor.prestartAllCoreThreads();
                waitEnd(threadPoolExecutor);
                System.out.printf("На %d потрачено %dс\n", stepCounter + 1, (System.currentTimeMillis() - startTime) / 1000);
            }
            ratingElo.printRating();
            genomes = evolveGenomes(ratingElo.getTwoBest());
        }
        return;
    }

    private static List<String> evolveGenomes(String[] twoBest) {
        List<String> genomes = new ArrayList<>(6);
        genomes.add(twoBest[1]);
        genomes.add(twoBest[0]);
        genomes.add(mutate(twoBest[1]));
        genomes.add(mutate(twoBest[0]));
        genomes.add(makeHybrid(twoBest[1], twoBest[0]));
        genomes.add(makeHybrid(twoBest[0], twoBest[1]));
        return genomes;
    }

    private static String makeHybrid(String firstOne, String SecondOne) {
        char[] newOne = new char[4];
        for (int i = 0; i < 2; i++) {
            newOne[i] = firstOne.charAt(i);
            newOne[2 + i] = SecondOne.charAt(2 + i);
        }
        return mutate(String.valueOf(newOne));
    }

    private static String mutate(String original) {
        char[] newOne = new char[4];
        for (int i = 0; i < 4; i++) {
            char temp = (char) (original.charAt(i) + (RANDOM.nextInt(MUTATE_STRENGTH * 2 + 1) - MUTATE_STRENGTH));
            newOne[i] = (temp < 'A' || temp > 'Z') ? (temp < 'A' ? 'A' : 'Z') : temp;
        }
        return String.valueOf(newOne);
    }

    private static BlockingQueue<Runnable> getQueue(final Map<String, List<String>> matching,
                                                    RatingElo ratingElo) throws Exception {
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(2100);
        for (String firstGenome : matching.keySet()) {
            for (String secondGenome : matching.get(firstGenome)) {
                final BattleArena arena = CreateBattleArena();

                final Player firstPlayer1 = PlayerFactory.createPlayerBot(BOT_TYPE, 1);
                ((MultiArmedWIthCoefficient) firstPlayer1).setGenotype(firstGenome);
                final Player secondPlayer1 = PlayerFactory.createPlayerBot(BOT_TYPE, 2);
                ((MultiArmedWIthCoefficient) secondPlayer1).setGenotype(secondGenome);
                queue.add(new SelfPlayRoomFroCorrectingElo(arena.getCopy(), firstPlayer1, secondPlayer1, ratingElo));

                final Player firstPlayer2 = PlayerFactory.createPlayerBot(BOT_TYPE, 2);
                ((MultiArmedWIthCoefficient) firstPlayer2).setGenotype(secondGenome);
                final Player secondPlayer2 = PlayerFactory.createPlayerBot(BOT_TYPE, 1);
                ((MultiArmedWIthCoefficient) secondPlayer2).setGenotype(firstGenome);
                queue.add(new SelfPlayRoomFroCorrectingElo(arena.getCopy(), firstPlayer2, secondPlayer2, ratingElo));
            }
        }
        return queue;
    }

    /**
     * создаем арену со случайными армиями, для этого:
     * формируем все возможные армии заданного размера
     * выбираем одну из них случайным образом
     *
     * @return созданная арена со случайными армиями
     */
    private static BattleArena CreateBattleArena() throws IOException, HeroExceptions {
        final List<String> armies = CommonFunction.getAllAvailableArmiesCode(ARMY_SIZE);
        final String stringArmy = armies.get(RANDOM.nextInt(armies.size()));
        final Army army = new StringArmyFactory(stringArmy).create();
        final Map<Integer, Army> mapArmies = new HashMap<>();
        mapArmies.put(1, army.getCopy());
        mapArmies.put(2, army);
        return new BattleArena(mapArmies);
    }

    /**
     * Ожидаем пока не освободятся все потоки
     *
     * @param threadPoolExecutor группа, в которой создаются потоки
     */
    private static void waitEnd(final ThreadPoolExecutor threadPoolExecutor) throws Exception {
        while (threadPoolExecutor.getActiveCount() > 0) {
            sleep(5000);
            printTimeInformation(threadPoolExecutor);
        }
    }

    private static void printTimeInformation(final ThreadPoolExecutor threadPoolExecutor) {
        final long endTime = System.currentTimeMillis();
        long completed = threadPoolExecutor.getCompletedTaskCount();
        if (completed == 0) {
            return;
        }
        final long timeNeed = (((endTime - startTime) / completed)
                * (threadPoolExecutor.getTaskCount() - completed)) / 1000;
        final int timeFromStart = (int) ((endTime - startTime) / 1000);
        System.out.printf("Прошло %d испытаний из %d. Прошло: %d секунд. Примерно осталось : %d секунд\n",
                completed, threadPoolExecutor.getTaskCount(), timeFromStart, timeNeed);
    }
}
