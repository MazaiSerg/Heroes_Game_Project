package com.neolab.heroesGame.client.ai.server;

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
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.neolab.heroesGame.client.ai.enums.BotType.*;
import static java.lang.Thread.sleep;

public class SelfPlayServerForNight {
    public static final Integer NUMBER_TRIES = 5;
    public static final Integer DIFFERENT_ARMIES = 5;
    public static final Integer ARMY_SIZE = StatisticWriter.ARMY_SIZE;
    private static final long SEED = 456123;
    private static final Random RANDOM = new Random(SEED);
    private static final AtomicInteger countGame = new AtomicInteger(0);
    private static final AtomicInteger countEndGame = new AtomicInteger(0);
    private static final Integer MAX_COUNT_GAME_ROOMS = 3;
    private static final Integer QUEUE_SIZE = 3;
    private static final Integer MATCH_NUMBERS = 2 * NUMBER_TRIES * DIFFERENT_ARMIES
            * BotType.values().length * (BotType.values().length - 1);
    private static final List<String> armies = CommonFunction.getAllAvailableArmiesCode(ARMY_SIZE);
    final static long startTime = System.currentTimeMillis();

    /**
     * Стравливаем двух ботов по NUMBER_TRIES каждой из DIFFERENT_ARMIES различных армий
     * Боты сражаются одинаковыми армиями
     * Право первого хода постоянно передается друг другу
     * Всего боты сыграют 2 * NUMBER_TRIES * DIFFERENT_ARMIES партий, каждый будет ходить первым ровно в половине случаев
     * Типы ботов задаются один раз до цикла
     */
    public static void main(final String[] args) throws Exception {
        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(0, MAX_COUNT_GAME_ROOMS,
                0L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(QUEUE_SIZE));
        final List<BotType> botTypeForTest = Arrays.asList(
                MONTE_CARLO,
                SUPER_DUPER_MANY_ARMED,
                MIN_MAX_WITHOUT_TREE,
                MULTI_ARMED_WITH_COEFFICIENTS);
        for (final BotType firstType : botTypeForTest) {
            for (final BotType secondType : botTypeForTest) {
                if (firstType.equals(secondType)) {
                    continue;
                }
                onePairMatching(firstType, secondType, threadPoolExecutor);
            }
        }
        waitEnd(threadPoolExecutor);
    }

    private static void onePairMatching(final BotType firstType, final BotType secondType,
                                        final ThreadPoolExecutor threadPoolExecutor) throws Exception {
        for (int j = 0; j < DIFFERENT_ARMIES; j++) {
            final BattleArena arena = CreateBattleArena();
            for (int i = 0; i < NUMBER_TRIES; i++) {
                waitQueue(threadPoolExecutor);
                final Player firstPlayer1 = PlayerFactory.createPlayerBot(firstType, 1);
                final Player secondPlayer1 = PlayerFactory.createPlayerBot(secondType, 2);
                threadPoolExecutor.execute(new SelfPlayRoom(arena.getCopy(), firstPlayer1, secondPlayer1));

                waitQueue(threadPoolExecutor);
                final Player firstPlayer2 = PlayerFactory.createPlayerBot(secondType, 2);
                final Player secondPlayer2 = PlayerFactory.createPlayerBot(firstType, 1);
                threadPoolExecutor.execute(new SelfPlayRoom(arena.getCopy(), firstPlayer2, secondPlayer2));
            }
        }
    }

    /**
     * выбираем одну из всех возможных армий одну случайным образом
     *
     * @return созданная арена со случайными армиями
     */
    private static BattleArena CreateBattleArena() throws IOException, HeroExceptions {
        final String stringArmy = armies.get(RANDOM.nextInt(armies.size()));
        final Army army = new StringArmyFactory(stringArmy).create();
        final Map<Integer, Army> mapArmies = new HashMap<>();
        mapArmies.put(1, army.getCopy());
        mapArmies.put(2, army);
        return new BattleArena(mapArmies);
    }

    /**
     * Ожидаем пока не освободится один из занятых потоков.
     *
     * @param threadPoolExecutor группа, в которой создаются потоки
     */
    private static void waitQueue(final ThreadPoolExecutor threadPoolExecutor) throws Exception {
        while (threadPoolExecutor.getActiveCount() >= MAX_COUNT_GAME_ROOMS) {
            sleep(100);
        }
        printTimeInformation(threadPoolExecutor.getActiveCount());
        countGame.incrementAndGet();
    }

    /**
     * Ожидаем пока не освободятся все потоки
     *
     * @param threadPoolExecutor группа, в которой создаются потоки
     */
    private static void waitEnd(final ThreadPoolExecutor threadPoolExecutor) throws Exception {
        while (threadPoolExecutor.getActiveCount() > 0) {
            sleep(100);
            printTimeInformation(threadPoolExecutor.getActiveCount());
        }
    }

    private static void printTimeInformation(final int activeThreadCount) {
        if (countGame.get() > activeThreadCount) {
            countEndGame.incrementAndGet();
            countGame.decrementAndGet();
            final long endTime = System.currentTimeMillis();
            final long timeNeed = (((endTime - startTime) / countEndGame.get())
                    * (MATCH_NUMBERS - countEndGame.get())) / 1000;
            final int timeFromStart = (int) ((endTime - startTime) / 1000);
            System.out.printf("Прошло %d испытаний из %d. Прошло: %d секунд. Примерно осталось : %d секунд\n",
                    countEndGame.get(), MATCH_NUMBERS, timeFromStart, timeNeed);
        }
    }
}
