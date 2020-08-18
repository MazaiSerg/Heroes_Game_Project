package com.neolab.heroesGame.client.ai.server.elo;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.aditional.StatisticWriter;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.arena.StringArmyFactory;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.PlayerFactory;
import com.neolab.heroesGame.client.ai.enums.BotType;
import com.neolab.heroesGame.errors.HeroExceptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.neolab.heroesGame.client.ai.enums.BotType.*;
import static java.lang.Thread.sleep;

public class ServerForCorrectingElo {
    public static final Integer ARMY_SIZE = StatisticWriter.ARMY_SIZE;
    private static final long SEED = 156517;
    private static final Random RANDOM = new Random(SEED);
    private static final Integer MAX_COUNT_GAME_ROOMS = 3;
    private static final Integer STEP_NUMBERS = 5;
    private static long startTime;

    public static void main(final String[] args) throws Exception {
        RatingElo ratingElo = RatingElo.createRatingEloForBot();
        BotType[] bots = {MONTE_CARLO,
                MANY_ARMED_BANDIT,
                MANY_ARMED_BANDIT_WITH_RANDOM,
                SUPER_DUPER_MANY_ARMED,
                MULTI_ARMED_WITH_COEFFICIENTS,
                MIN_MAX,
                MIN_MAX_WITHOUT_TREE,
                BotType.RANDOM};
        for (int stepCounter = 0; stepCounter < STEP_NUMBERS; stepCounter++) {
            Map<BotType, List<String>> matching = new HashMap<>();
            for (BotType type : bots) {
                matching.put(type, ratingElo.getOpponents(type.toString()));
            }
            startTime = System.currentTimeMillis();
            final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(MAX_COUNT_GAME_ROOMS, MAX_COUNT_GAME_ROOMS,
                    0L, TimeUnit.SECONDS, getQueue(matching, ratingElo));
            threadPoolExecutor.prestartAllCoreThreads();
            waitEnd(threadPoolExecutor);
            ratingElo.saveRating();
            System.out.printf("На %d потрачено %dс\n", stepCounter + 1, (System.currentTimeMillis() - startTime) / 1000);
        }
        return;
    }

    private static BlockingQueue<Runnable> getQueue(final Map<BotType, List<String>> matching,
                                                    RatingElo ratingElo) throws Exception {
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(2100);
        for (BotType type : matching.keySet()) {
            for (String name : matching.get(type)) {
                final BattleArena arena = CreateBattleArena();

                final Player firstPlayer1 = PlayerFactory.createPlayerBot(type, 1);
                final Player secondPlayer1 = PlayerFactory.createPlayerBot(BotType.valueOf(name), 2);
                queue.add(new SelfPlayRoomFroCorrectingElo(arena.getCopy(), firstPlayer1, secondPlayer1, ratingElo));

                final Player firstPlayer2 = PlayerFactory.createPlayerBot(BotType.valueOf(name), 2);
                final Player secondPlayer2 = PlayerFactory.createPlayerBot(type, 1);
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
