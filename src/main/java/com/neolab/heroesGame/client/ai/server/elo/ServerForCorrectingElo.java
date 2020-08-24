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
    private static final Integer MAX_COUNT_GAME_ROOMS = 5;
    private static final Integer STEP_NUMBERS = 20;
    private static long startTime;
    private static final List<String> armies = CommonFunction.getAllAvailableArmiesCode(ARMY_SIZE);
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
            typeBotByName.put(String.format("%s_%d", MULTI_ARMED_WITH_COEFFICIENTS.toString(), time), MULTI_ARMED_WITH_COEFFICIENTS);
        }
    }

    /**
     * 1. Загружаем рейтинг Эло
     * 2. Формируем список ботов, для которых будут подбираться пары (в качестве пары может быть выбран и бот вне списка)
     * пары могут повторяться, если игроки стоят рядом по рейтингу Эло и оба присутствуют в списке ботов, для которых
     * подбираются соперники
     * 3. Проводим STEP_NUMBERS итераций корректировки рейтинга
     * 3.1. подбираем оппонентов для всех ботов из списка (пункт 2)
     * 3.2. Формируем очередь задач с помощью функции getQueue()
     * 3.3. проводим все матчи из очереди
     *
     * @param args не использует входные параметры
     */
    public static void main(final String[] args) throws Exception {
        final RatingElo ratingElo = RatingElo.createRatingEloForBot(typeBotByName.keySet());
        for (int stepCounter = 0; stepCounter < STEP_NUMBERS; stepCounter++) {
            final Map<String, List<String>> matching = new HashMap<>();
            for (final String type : typeBotByName.keySet()) {
                matching.put(type, ratingElo.getOpponents(type));
            }
            startTime = System.currentTimeMillis();
            final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(MAX_COUNT_GAME_ROOMS,
                    MAX_COUNT_GAME_ROOMS, 0L, TimeUnit.SECONDS, getQueue(matching, ratingElo));
            threadPoolExecutor.prestartAllCoreThreads();
            waitEnd(threadPoolExecutor);
            ratingElo.saveRating();
            System.out.printf("На %d потрачено %dс\n", stepCounter + 1, (System.currentTimeMillis() - startTime) / 1000);
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
    private static BlockingQueue<Runnable> getQueue(final Map<String, List<String>> matching,
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

    private static Runnable createRoom(final BattleArena arena, final String firstBotName,
                                       final String secondBotName, final RatingElo ratingElo) {
        final Player firstPlayer = PlayerFactory.createMonteCarloBotWithTimeLimit(typeBotByName.get(firstBotName),
                1, timeToThinkForBot.get(firstBotName));
        final Player secondPlayer = PlayerFactory.createMonteCarloBotWithTimeLimit(typeBotByName.get(secondBotName),
                2, timeToThinkForBot.get(secondBotName));
        return new SelfPlayRoomFroCorrectingElo(arena.getCopy(), firstPlayer, secondPlayer, ratingElo);
    }

    /**
     * создаем арену со случайными армиями, для этого:
     * выбираем одну из армий случайным образом из всех доступных
     *
     * @return созданная арена со случайными одинаковыми армиями
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

    /**
     * раз в некоторое время маякуем в консоль, что все работает. Показываем информацию сколько сделано, сколько осталось
     */
    private static void printTimeInformation(final ThreadPoolExecutor threadPoolExecutor) {
        final long endTime = System.currentTimeMillis();
        final long completed = threadPoolExecutor.getCompletedTaskCount();
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
