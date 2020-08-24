package com.neolab.heroesGame.client.ai.version.basic;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.aditional.StatisticWriter;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.arena.StringArmyFactory;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.PlayerFactory;
import com.neolab.heroesGame.client.ai.enums.BotType;
import com.neolab.heroesGame.client.ai.server.SelfPlayRoom;
import com.neolab.heroesGame.errors.HeroExceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public abstract class AbstractServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServer.class);
    public static final int ARMY_SIZE = StatisticWriter.ARMY_SIZE;
    private final long SEED = 456123;
    private final Random RANDOM = new Random(SEED);
    private final int maxCountGameRoom;
    private final int queueSize;
    private int prevCounter = 0;
    private long startTime = System.currentTimeMillis();
    private static final List<String> armies = CommonFunction.getAllAvailableArmiesCode(ARMY_SIZE);

    protected AbstractServer(final int MAX_COUNT_GAME_ROOMS, final int QUEUE_SIZE) {
        maxCountGameRoom = MAX_COUNT_GAME_ROOMS;
        queueSize = QUEUE_SIZE;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }

    public void setPrevCounter(int prevCounter) {
        this.prevCounter = prevCounter;
    }

    public int getMaxCountGameRoom() {
        return maxCountGameRoom;
    }

    /**
     * Стравливаем двух ботов по NUMBER_TRIES каждой из DIFFERENT_ARMIES различных армий
     * Боты сражаются одинаковыми армиями
     * Право первого хода постоянно передается друг другу
     * Всего боты сыграют 2 * NUMBER_TRIES * DIFFERENT_ARMIES партий, каждый будет ходить первым ровно в половине случаев
     * Типы ботов задаются один раз до цикла
     */
    public abstract void matching() throws Exception;

    protected ThreadPoolExecutor createThreadPoolExecutor(final BlockingQueue<Runnable> queue) {
        return new ThreadPoolExecutor(maxCountGameRoom, maxCountGameRoom,
                1_000L, TimeUnit.SECONDS, queue);
    }

    /**
     * выбираем одну из всех возможных армий одну случайным образом
     *
     * @return созданная арена со случайными армиями
     */
    protected BattleArena CreateBattleArena() throws IOException, HeroExceptions {
        final String stringArmy = armies.get(RANDOM.nextInt(armies.size()));
        final Army army = new StringArmyFactory(stringArmy).create();
        final Map<Integer, Army> mapArmies = new HashMap<>();
        mapArmies.put(1, army.getCopy());
        mapArmies.put(2, army);
        return new BattleArena(mapArmies);
    }

    protected static Runnable createRoom(final BattleArena arena, final BotType firstBotType,
                                         final BotType secondBotType) {
        final Player firstPlayer = PlayerFactory.createPlayerBot(firstBotType, 1);
        final Player secondPlayer = PlayerFactory.createPlayerBot(secondBotType, 2);
        return new SelfPlayRoom(arena.getCopy(), firstPlayer, secondPlayer);
    }

    /**
     * Ожидаем пока не освободится один из занятых потоков.
     *
     * @param threadPoolExecutor группа, в которой создаются потоки
     */
    protected void waitQueue(final ThreadPoolExecutor threadPoolExecutor) throws Exception {
        while (threadPoolExecutor.getQueue().size() >= queueSize - 3) {
            sleep(100);
        }
        printTimeInformation(threadPoolExecutor);
    }

    /**
     * Ожидаем пока не освободятся все потоки
     *
     * @param threadPoolExecutor группа, в которой создаются потоки
     */
    protected void waitEnd(final ThreadPoolExecutor threadPoolExecutor) throws Exception {
        while (threadPoolExecutor.getActiveCount() > 0) {
            sleep(100);
            printTimeInformation(threadPoolExecutor);
        }
    }

    protected void printTimeInformation(final ThreadPoolExecutor threadPoolExecutor) {
        if ((threadPoolExecutor.getCompletedTaskCount() - prevCounter >= 20)) {
            prevCounter = (int) threadPoolExecutor.getCompletedTaskCount();
            final long currentTime = System.currentTimeMillis();
            LOGGER.info("прошло {} игр. C начала прошло {}с\n",
                    prevCounter, (currentTime - startTime) / 1000);
        }
    }
}
