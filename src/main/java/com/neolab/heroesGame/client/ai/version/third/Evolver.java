package com.neolab.heroesGame.client.ai.version.third;

import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.PlayerFactory;
import com.neolab.heroesGame.client.ai.enums.BotType;
import com.neolab.heroesGame.client.ai.server.elo.RatingElo;
import com.neolab.heroesGame.client.ai.server.elo.SelfPlayRoomFroCorrectingElo;
import com.neolab.heroesGame.client.ai.version.basic.AbstractServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * 1. Формируем стартовый генофонд
 * 2. Определяем рейтинг Эло для этого генофонда
 * 3. Формируем новый генофонд
 * 3.1. Оставляем двух сильнейших
 * 3.2. Добавляем двух мутировавших сильнейших
 * 3.3. Объединяем гены сильнейших по правилу 2 первых гена одного + 2 последних гена второго
 * 3.4. Добавляем мутированных скрещенных
 */
public class Evolver extends AbstractServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Evolver.class);
    private final long SEED = 456123;
    private final Random RANDOM = new Random(SEED);
    private final int numberStep;
    private final int mutateStrength = 4;
    private final String startGenome = "ADMH";
    private final BotType BOT_TYPE = BotType.MULTI_ARMED_WITH_COEFFICIENTS;


    public Evolver(final int MAX_COUNT_GAME_ROOMS, final int QUEUE_SIZE) {
        this(MAX_COUNT_GAME_ROOMS, QUEUE_SIZE, 5);
    }

    public Evolver(final int MAX_COUNT_GAME_ROOMS, final int QUEUE_SIZE,
                   final int numberStep) {
        super(MAX_COUNT_GAME_ROOMS, QUEUE_SIZE);
        this.numberStep = numberStep;
    }

    @Override
    public void matching() throws Exception {
        List<String> genomes = new ArrayList<>(6);
        genomes.add(startGenome);
        genomes.add(mutate(startGenome));
        genomes.add(mutate(startGenome));
        genomes.add(mutate(startGenome));
        genomes.add(mutate(startGenome));
        genomes.add(mutate(startGenome));
        MultiArmedWIthCoefficient.startEvolve();
        for (int i = 0; i < 20; i++) {
            System.out.print(new Date(System.currentTimeMillis()) + " Genomes: ");
            System.out.println(String.join(" ", genomes));
            final RatingElo ratingElo = RatingElo.createRatingElo(genomes);
            for (int stepCounter = 0; stepCounter < numberStep; stepCounter++) {
                final Map<String, List<String>> matching = new HashMap<>();
                for (final String genome : genomes) {
                    matching.put(genome, ratingElo.getOpponents(genome));
                }
                setStartTime(System.currentTimeMillis());
                final ThreadPoolExecutor threadPoolExecutor = createThreadPoolExecutor(getQueue(matching, ratingElo));
                threadPoolExecutor.prestartAllCoreThreads();
                waitEnd(threadPoolExecutor);
                LOGGER.info("На {} потрачено {}с\n", stepCounter + 1,
                        (System.currentTimeMillis() - getStartTime()) / 1000);
                threadPoolExecutor.shutdown();
            }
            ratingElo.printRating();
            genomes = evolveGenomes(ratingElo.getTwoBest());
        }
    }

    /**
     * Оставляем двух сильнейших
     * Добавляем двух мутировавших сильнейших
     * Объединяем гены сильнейших по правилу 2 первых гена одного + 2 последних гена второго
     * Добавляем мутированных скрещенных
     *
     * @param twoBest гены двух сильнейших ботов
     * @return ArrayList с новым генофондом
     */
    private List<String> evolveGenomes(final String[] twoBest) {
        final List<String> genomes = new ArrayList<>(6);
        genomes.add(twoBest[1]);
        genomes.add(twoBest[0]);
        genomes.add(mutate(twoBest[1]));
        genomes.add(mutate(twoBest[0]));
        genomes.add(makeHybrid(twoBest[1], twoBest[0]));
        genomes.add(makeHybrid(twoBest[0], twoBest[1]));
        return genomes;
    }

    /**
     * берем два первых гена первого и два последних гена второго
     * Перед возвратом отправляем получившийся геном на мутацию
     *
     * @param firstOne  геном первого бота
     * @param SecondOne геном второго бота
     */
    private String makeHybrid(final String firstOne, final String SecondOne) {
        final char[] newOne = new char[4];
        for (int i = 0; i < 2; i++) {
            newOne[i] = firstOne.charAt(i);
            newOne[2 + i] = SecondOne.charAt(2 + i);
        }
        return mutate(String.valueOf(newOne));
    }

    /**
     * К коду каждого гена прибавляем случайное число в диапазоне [-MUTATE_STRENGTH; MUTATE_STRENGTH]
     * Значение гена должно быть в диапазоне ['A'; 'Z']
     *
     * @param original стартовый геном
     * @return мутировавший геном
     */
    private String mutate(final String original) {
        final char[] newOne = new char[4];
        for (int i = 0; i < 4; i++) {
            final char temp = (char) (original.charAt(i)
                    + (RANDOM.nextInt(mutateStrength * 2 + 1) - mutateStrength));
            newOne[i] = (temp < 'A' || temp > 'Z') ? (temp < 'A' ? 'A' : 'Z') : temp;
        }
        return String.valueOf(newOne);
    }

    /**
     * Формируем очередь матчей
     * Для ботов в том числе задается их геном
     */
    private BlockingQueue<Runnable> getQueue(final Map<String, List<String>> matching,
                                             final RatingElo ratingElo) throws Exception {
        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(2100);
        for (final String firstGenome : matching.keySet()) {
            for (final String secondGenome : matching.get(firstGenome)) {
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
}
