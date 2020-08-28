package com.neolab.heroesGame.client.ai.server;

import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.PlayerFactory;
import com.neolab.heroesGame.client.ai.enums.BotType;
import com.neolab.heroesGame.client.ai.version.basic.AbstractServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import static com.neolab.heroesGame.client.ai.enums.BotType.*;
import static java.lang.Thread.sleep;

public class SelfPlayServerForNight extends AbstractServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SelfPlayServerForNight.class);
    public final int numberTries;
    public final int differentArmies;
    private final AtomicInteger countGame = new AtomicInteger(0);
    private final AtomicInteger countEndGame = new AtomicInteger(0);
    private int matchNumbers;

    public SelfPlayServerForNight(final int MAX_COUNT_GAME_ROOMS, final int QUEUE_SIZE) {
        this(MAX_COUNT_GAME_ROOMS, QUEUE_SIZE, 5, 6);
    }

    public SelfPlayServerForNight(final int MAX_COUNT_GAME_ROOMS, final int QUEUE_SIZE,
                                  final int numberTries, final int differentArmies) {
        super(MAX_COUNT_GAME_ROOMS, QUEUE_SIZE);
        this.numberTries = numberTries;
        this.differentArmies = differentArmies;
    }

    @Override
    public void matching() throws Exception {
        final ThreadPoolExecutor threadPoolExecutor = createThreadPoolExecutor(new ArrayBlockingQueue<>(getQueueSize()));
        final List<BotType> botTypeForTest = Arrays.asList(
                MONTE_CARLO,
                SUPER_DUPER_MANY_ARMED,
                MIN_MAX_WITHOUT_TREE,
                MULTI_ARMED_WITH_COEFFICIENTS);
        matchNumbers = 2 * numberTries * differentArmies * botTypeForTest.size() * (botTypeForTest.size() - 1);
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

    private void onePairMatching(final BotType firstType, final BotType secondType,
                                 final ThreadPoolExecutor threadPoolExecutor) throws Exception {
        for (int j = 0; j < differentArmies; j++) {
            final BattleArena arena = CreateBattleArena();
            for (int i = 0; i < numberTries; i++) {
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

    @Override
    protected void waitQueue(final ThreadPoolExecutor threadPoolExecutor) throws Exception {
        while (threadPoolExecutor.getActiveCount() >= getMaxCountGameRoom()) {
            sleep(100);
        }
        printTimeInformation(threadPoolExecutor.getActiveCount());
        countGame.incrementAndGet();
    }

    @Override
    protected void waitEnd(final ThreadPoolExecutor threadPoolExecutor) throws Exception {
        while (threadPoolExecutor.getActiveCount() > 0) {
            sleep(100);
            printTimeInformation(threadPoolExecutor.getActiveCount());
        }
    }

    private void printTimeInformation(final int activeThreadCount) {
        if (countGame.get() > activeThreadCount) {
            countEndGame.incrementAndGet();
            countGame.decrementAndGet();
            final long endTime = System.currentTimeMillis();
            final long timeNeed = (((endTime - getStartTime()) / countEndGame.get())
                    * (matchNumbers - countEndGame.get())) / 1000;
            final int timeFromStart = (int) ((endTime - getStartTime()) / 1000);
            LOGGER.info("Прошло {} испытаний из {}. Прошло: {}} секунд. Примерно осталось : {} секунд\n",
                    countEndGame.get(), matchNumbers, timeFromStart, timeNeed);
        }
    }
}
