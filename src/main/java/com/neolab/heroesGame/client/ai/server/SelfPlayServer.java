package com.neolab.heroesGame.client.ai.server;

import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.client.ai.enums.BotType;
import com.neolab.heroesGame.client.ai.version.basic.AbstractServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;


public class SelfPlayServer extends AbstractServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelfPlayServer.class);
    public final int numberTries;
    public final int differentArmies;
    private final AtomicInteger countGame = new AtomicInteger(0);
    private final AtomicInteger countEndGame = new AtomicInteger(0);

    public SelfPlayServer(final int MAX_COUNT_GAME_ROOMS, final int QUEUE_SIZE) {
        this(MAX_COUNT_GAME_ROOMS, QUEUE_SIZE, 5, 6);
    }

    public SelfPlayServer(final int MAX_COUNT_GAME_ROOMS, final int QUEUE_SIZE,
                          final int numberTries, final int differentArmies) {
        super(MAX_COUNT_GAME_ROOMS, QUEUE_SIZE);
        this.numberTries = numberTries;
        this.differentArmies = differentArmies;
    }

    @Override
    public void matching() throws Exception {
        final ThreadPoolExecutor threadPoolExecutor = createThreadPoolExecutor(new ArrayBlockingQueue<>(getQueueSize()));
        final BotType firstType = BotType.MONTE_CARLO;
        final BotType secondType = BotType.MONTE_CARLO;
        for (int j = 0; j < differentArmies; j++) {
            final BattleArena arena = CreateBattleArena();
            for (int i = 0; i < numberTries; i++) {
                waitQueue(threadPoolExecutor);
                threadPoolExecutor.execute(createRoom(arena.getCopy(), firstType, secondType));

                waitQueue(threadPoolExecutor);
                threadPoolExecutor.execute(createRoom(arena.getCopy(), secondType, firstType));
            }
        }
        waitEnd(threadPoolExecutor);
    }

    @Override
    protected void waitQueue(final ThreadPoolExecutor threadPoolExecutor) throws Exception {
        while (threadPoolExecutor.getActiveCount() >= getMaxCountGameRoom()) {
            sleep(2000);
        }
        printTimeInformation(threadPoolExecutor.getActiveCount());
        countGame.incrementAndGet();
    }


    @Override
    protected void waitEnd(final ThreadPoolExecutor threadPoolExecutor) throws Exception {
        while (threadPoolExecutor.getActiveCount() > 0) {
            sleep(2000);
            printTimeInformation(threadPoolExecutor.getActiveCount());
        }
    }

    private void printTimeInformation(final int activeThreadCount) {
        if (countGame.get() > activeThreadCount) {
            countEndGame.incrementAndGet();
            countGame.decrementAndGet();
            final long endTime = System.currentTimeMillis();
            final long timeNeed = (((endTime - getStartTime()) / countEndGame.get())
                    * (2 * differentArmies * numberTries - countEndGame.get())) / 1000;
            final int timeFromStart = (int) ((endTime - getStartTime()) / 1000);
            LOGGER.info("Прошло {} испытаний из {}. Прошло: {}} секунд. Примерно осталось : {} секунд\n",
                    countEndGame.get(), 2 * differentArmies * numberTries, timeFromStart, timeNeed);
        }
    }
}
