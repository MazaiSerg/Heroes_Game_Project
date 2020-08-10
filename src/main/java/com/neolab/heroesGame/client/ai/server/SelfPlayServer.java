package com.neolab.heroesGame.client.ai.server;

import com.neolab.heroesGame.aditional.CommonFunction;
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
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;


public class SelfPlayServer {
    public static final Integer NUMBER_TRIES = 10;
    public static final Integer DIFFERENT_ARMIES = 20;
    public static final Integer ARMY_SIZE = 6;
    private static final long SEED = 87941;
    private static final Random RANDOM = new Random(SEED);
    private static final AtomicInteger countGame = new AtomicInteger(0);
    private static final AtomicInteger countEndGame = new AtomicInteger(0);
    private static final Integer MAX_COUNT_GAME_ROOMS = 5;
    final static long startTime = System.currentTimeMillis();

    /**
     * Стравливаем двух ботов по NUMBER_TRIES каждой из DIFFERENT_ARMIES различных армий
     * Боты сражаются одинаковыми армиями
     * Право первого хода постоянно передается друг другу
     * Всего боты сыграют 2 * NUMBER_TRIES * DIFFERENT_ARMIES партий, каждый будет ходить первым ровно в половине случаев
     * Типы ботов задаются один раз до цикла
     */
    public static void main(final String[] args) throws Exception {
        final ThreadGroup threadGroup = new ThreadGroup("matching");
        final BotType firstType = BotType.MIN_MAX;
        final BotType secondType = BotType.RANDOM;
        for (int j = 0; j < DIFFERENT_ARMIES; j++) {
            final BattleArena arena = CreateBattleArena();
            for (int i = 0; i < NUMBER_TRIES; i++) {
                waitQueue(threadGroup);
                final Player firstPlayer1 = PlayerFactory.createPlayerBot(firstType, 1);
                final Player secondPlayer1 = PlayerFactory.createPlayerBot(secondType, 2);
                new Thread(threadGroup, new SelfPlayRoom(arena.getCopy(), firstPlayer1, secondPlayer1)).start();

                waitQueue(threadGroup);
                final Player firstPlayer2 = PlayerFactory.createPlayerBot(secondType, 2);
                final Player secondPlayer2 = PlayerFactory.createPlayerBot(firstType, 1);
                new Thread(threadGroup, new SelfPlayRoom(arena.getCopy(), firstPlayer2, secondPlayer2)).start();
            }
        }
        waitEnd(threadGroup);
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
     * Ожидаем пока не освободится один из занятых потоков.
     *
     * @param threadGroup группа, в которой создаются потоки
     */
    private static void waitQueue(final ThreadGroup threadGroup) throws Exception {
        while (threadGroup.activeCount() >= MAX_COUNT_GAME_ROOMS) {
            sleep(2000);
        }
        printTimeInformation(threadGroup.activeCount());
        countGame.incrementAndGet();
    }

    /**
     * Ожидаем пока не освободятся все потоки
     *
     * @param threadGroup группа, в которой создаются потоки
     */
    private static void waitEnd(final ThreadGroup threadGroup) throws Exception {
        while (threadGroup.activeCount() > 0) {
            sleep(2000);
            printTimeInformation(threadGroup.activeCount());
        }
    }

    private static void printTimeInformation(final int activeThreadCount) {
        if (countGame.get() > activeThreadCount) {
            countEndGame.incrementAndGet();
            countGame.decrementAndGet();
            final long endTime = System.currentTimeMillis();
            final long timeNeed = (((endTime - startTime) / countEndGame.get())
                    * (2 * DIFFERENT_ARMIES * NUMBER_TRIES - countEndGame.get())) / 1000;
            final int timeFromStart = (int) ((endTime - startTime) / 1000);
            System.out.printf("Прошло %d испытаний из %d. Прошло: %d секунд. Примерно осталось : %d секунд\n",
                    countEndGame.get(), 2 * DIFFERENT_ARMIES * NUMBER_TRIES, timeFromStart, timeNeed);
        }
    }
}
