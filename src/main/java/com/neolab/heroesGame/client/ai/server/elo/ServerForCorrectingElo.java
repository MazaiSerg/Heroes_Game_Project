package com.neolab.heroesGame.client.ai.server.elo;

import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.PlayerFactory;
import com.neolab.heroesGame.client.ai.enums.BotType;
import com.neolab.heroesGame.client.ai.version.basic.AbstractServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static com.neolab.heroesGame.client.ai.enums.BotType.*;

public class ServerForCorrectingElo extends AbstractServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerForCorrectingElo.class);
    private final int numberStep;
    private static final Map<String, Integer> timeToThinkForBot;
    private static final Map<String, BotType> typeBotByName;

    static {
        timeToThinkForBot = new HashMap<>();
        typeBotByName = new HashMap<>();
        final int[] times = new int[]{100, 500, 1000, 1300};
        timeToThinkForBot.put(BotType.RANDOM.toString(), 0);
        typeBotByName.put(BotType.RANDOM.toString(), BotType.RANDOM);
        timeToThinkForBot.put(MIN_MAX_WITHOUT_TREE.toString(), 0);
        typeBotByName.put(MIN_MAX_WITHOUT_TREE.toString(), MIN_MAX_WITHOUT_TREE);
        for (final int time : times) {
            timeToThinkForBot.put(String.format("%s_%d", MONTE_CARLO.toString(), time), time);
            timeToThinkForBot.put(String.format("%s_%d", SUPER_DUPER_MANY_ARMED.toString(), time), time);
            timeToThinkForBot.put(String.format("%s_%d", MULTI_ARMED_WITH_COEFFICIENTS.toString(), time), time);

            typeBotByName.put(String.format("%s_%d", MONTE_CARLO.toString(), time), MONTE_CARLO);
            typeBotByName.put(String.format("%s_%d", SUPER_DUPER_MANY_ARMED.toString(), time), SUPER_DUPER_MANY_ARMED);
            typeBotByName.put(String.format("%s_%d", MULTI_ARMED_WITH_COEFFICIENTS.toString(), time),
                    MULTI_ARMED_WITH_COEFFICIENTS);
        }
    }

    public ServerForCorrectingElo(final int MAX_COUNT_GAME_ROOMS, final int QUEUE_SIZE) {
        this(MAX_COUNT_GAME_ROOMS, QUEUE_SIZE, 20);
    }

    public ServerForCorrectingElo(final int MAX_COUNT_GAME_ROOMS, final int QUEUE_SIZE,
                                  final int numberStep) {
        super(MAX_COUNT_GAME_ROOMS, QUEUE_SIZE);
        this.numberStep = numberStep;
    }

    @Override
    public void matching() throws Exception {
        final RatingElo ratingElo = RatingElo.createRatingEloForBot(typeBotByName.keySet());
        for (int stepCounter = 0; stepCounter < numberStep; stepCounter++) {
            final Map<String, List<String>> matching = new HashMap<>();
            for (final String type : typeBotByName.keySet()) {
                matching.put(type, ratingElo.getOpponents(type));
            }
            setStartTime(System.currentTimeMillis());
            final ThreadPoolExecutor threadPoolExecutor = createThreadPoolExecutor(getQueue(matching, ratingElo));
            threadPoolExecutor.prestartAllCoreThreads();
            waitEnd(threadPoolExecutor);
            ratingElo.saveRating();
            LOGGER.info("На {} потрачено {}с\n", stepCounter + 1, (System.currentTimeMillis() - getStartTime()) / 1000);
            threadPoolExecutor.shutdown();
        }
    }

    /**
     * Формируем очередь задач. Для каждой пары создаем две комнаты с одинаковыми стартовыми услвиями. В разной комнате
     * разный игрок ходит первым
     *
     * @param matching  набор соперников для каждого бота
     * @param ratingElo текущийй рейтинг Эло
     * @return ArrayBlockingQueue с комнатами для всех матчей
     * @throws Exception может появиться при создании армий
     */
    private BlockingQueue<Runnable> getQueue(final Map<String, List<String>> matching,
                                             final RatingElo ratingElo) throws Exception {
        final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(2100);
        for (final String firstBotName : matching.keySet()) {
            for (final String secondBotName : matching.get(firstBotName)) {
                final BattleArena arena = CreateBattleArena();
                queue.add(createRoom(arena, firstBotName, secondBotName, ratingElo));
                queue.add(createRoom(arena, secondBotName, firstBotName, ratingElo));
            }
        }
        return queue;
    }

    private Runnable createRoom(final BattleArena arena, final String firstBotName,
                                final String secondBotName, final RatingElo ratingElo) {
        final Player firstPlayer = PlayerFactory.createMonteCarloBotWithTimeLimit(typeBotByName.get(firstBotName),
                1, timeToThinkForBot.get(firstBotName));
        final Player secondPlayer = PlayerFactory.createMonteCarloBotWithTimeLimit(typeBotByName.get(secondBotName),
                2, timeToThinkForBot.get(secondBotName));
        return new SelfPlayRoomFroCorrectingElo(arena.getCopy(), firstPlayer, secondPlayer, ratingElo);
    }
}
