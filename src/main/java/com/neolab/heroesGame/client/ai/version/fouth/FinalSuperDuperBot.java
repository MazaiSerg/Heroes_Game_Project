package com.neolab.heroesGame.client.ai.version.fouth;

import com.neolab.heroesGame.client.ai.version.basic.BasicMonteCarloBot;
import com.neolab.heroesGame.client.ai.version.mechanics.GameProcessor;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Answer;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Army;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.BattleArena;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.enumerations.HeroActions;
import com.neolab.heroesGame.errors.HeroExceptions;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.neolab.heroesGame.client.ai.enums.BotType.FINAL_BOT;

public class FinalSuperDuperBot extends BasicMonteCarloBot {
    private static final Logger LOGGER = LoggerFactory.getLogger(FinalSuperDuperBot.class);
    private static final int TIME_TO_THINK = 1000;
    private static final double LN_2D = Math.log(2d);
    private static final boolean USE_RANDOM = true;
    private static final int THREAD_COUNT = 3;
    private static List<Double> geneticCoefficients;

    static {
        geneticCoefficients = new ArrayList<>();
    }

    public FinalSuperDuperBot(final int id, final int timeToThink) {
        super(FINAL_BOT.toString(), timeToThink, id);
        geneticCoefficients = updateCoefficient();
    }

    public FinalSuperDuperBot(final int id) {
        super(FINAL_BOT.toString(), TIME_TO_THINK, id);
        geneticCoefficients = updateCoefficient();
    }

    /**
     * Массив коэффициентов приорететов действий юнитов:
     * 0 - защита юнитов второй линии
     * 1 - защита юнитов первой линии
     * 2 - модификатор атаки от урона
     * 3 - модификатор хила
     * Коэффициент выбирается по строке следующим образом: A - 0.1, B - 0.2, C - 0.5, D - 1, E - 2, F - 3 etc
     *
     * @return Массив коэффициентов приорететов действий юнитов
     */
    private List<Double> updateCoefficient() {
        final List<Double> coefficients = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            final String genotype = "BIQK";
            final int value = genotype.charAt(i) - 'A';
            switch (value) {
                case 0 -> coefficients.add(0.1);
                case 1 -> coefficients.add(0.2);
                case 2 -> coefficients.add(0.5);
                default -> coefficients.add((double) (value - 2));
            }
        }
        return coefficients;
    }

    @Override
    public com.neolab.heroesGame.server.answers.Answer getAnswer(
            final com.neolab.heroesGame.arena.@NotNull BattleArena board) {

        final long startTime = System.currentTimeMillis();
        if (board.getArmy(getId()).getHeroes().size() == board.getArmy(getId()).getAvailableHeroes().size()) {
            increaseCurrentRound();
        }
        final BattleArena arena = BattleArena.getCopyFromOriginalClass(board);
        final List<Answer> actions = arena.getAllActionForPlayer(getId());
        final int[] simulationsCounter = new int[actions.size()];
        final double[] scores = new double[actions.size()];
        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT,
                10L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(3));
        for (int i = 0; i < scores.length; i++) {
            simulationsCounter[i] = 0;
            scores[i] = modify(actions.get(i));
        }
        for (int i = 0; ; ) {
            if (System.currentTimeMillis() - startTime > TIME_TO_THINK) {
                LOGGER.warn("Количество симуляций за {}мс: {}", System.currentTimeMillis() - startTime, i);
                break;
            }
            final double[] priorityFunction = countPriorityFunction(scores, simulationsCounter, i);
            final int[] indexes = new int[THREAD_COUNT];
            List<AtomicInteger> results = new ArrayList<>(THREAD_COUNT);
            for (int j = 0; j < THREAD_COUNT; j++) {
                final int index = chooseAction(priorityFunction);
                indexes[j] = index;
                results.add(new AtomicInteger(0));
                final GameProcessor processor = new GameProcessor(getId(), arena.getCopy(), getCurrentRound(), USE_RANDOM);
                threadPoolExecutor.execute(new Room(processor, actions.get(index), results.get(j), getId()));
            }
            try {
                while (threadPoolExecutor.getActiveCount() != 0 || threadPoolExecutor.getQueue().size() != 0) {
                    Thread.sleep(0l, 5);
                }
            } catch (InterruptedException ex) {
                LOGGER.error(ex.getMessage());
            }
            i += 3;
            for (int j = 0; j < THREAD_COUNT; j++) {
                scores[indexes[j]] = (scores[indexes[j]] * simulationsCounter[indexes[j]] + results.get(j).get())
                        / (simulationsCounter[indexes[j]] + 1);
                simulationsCounter[indexes[j]]++;
            }
        }
        return actions.get(findBest(scores)).getCommonAnswer(getId());
    }

    private int findBest(final double[] scores) {
        int max = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[max] < scores[i]) {
                max = i;
            }
        }
        return max;
    }

    private double[] countPriorityFunction(final double[] scores, final int[] simulationsCounter, final int counter) {
        final double[] priorityFunction = new double[scores.length];
        if (counter == 0) {
            priorityFunction[0] = scores[0];
            for (int i = 1; i < scores.length; i++) {
                priorityFunction[i] = priorityFunction[i - 1] + scores[i];
            }
        } else {
            priorityFunction[0] = scores[0] + (simulationsCounter[0] == 0 ? 0
                    : Math.sqrt(2 * Math.log(counter) / simulationsCounter[0] / LN_2D));
            for (int i = 1; i < scores.length; i++) {
                priorityFunction[i] = priorityFunction[i - 1] + scores[i]
                        + (simulationsCounter[i] == 0 ? 0
                        : Math.sqrt(2 * Math.log(counter) / simulationsCounter[i] / LN_2D));
            }
        }
        return priorityFunction;
    }

    private static double modify(final Answer answer) {
        if (answer.getAction() == HeroActions.DEFENCE) {
            if (answer.getActiveHeroCoordinate().getY() == 0) {
                return geneticCoefficients.get(0);
            } else {
                return geneticCoefficients.get(1);
            }
        } else if (answer.getAction() == HeroActions.ATTACK) {
            return geneticCoefficients.get(2);
        }
        return geneticCoefficients.get(3);
    }

    private static class Room implements Runnable {
        private final GameProcessor processor;
        private final Answer action;
        private final AtomicInteger score;
        private final int botId;

        public Room(final GameProcessor processor, final Answer action, final AtomicInteger score, final int botId) {
            this.processor = processor;
            this.action = action;
            this.score = score;
            this.botId = botId;
        }

        @Override
        public void run() {
            Answer currentAction = action;
            while (true) {
                try {
                    processor.handleAnswer(currentAction);
                } catch (HeroExceptions ex) {
                    LOGGER.error(ex.getMessage());
                    throw new AssertionError();
                }
                final GameEvent event = processor.matchOver();

                if (event == GameEvent.NOTHING_HAPPEN) {
                    final List<Answer> actions = processor.getAllActionsForCurrentPlayer();
                    final int index = chooseAction(createActionsPriority(actions));
                    currentAction = actions.get(index);
                } else {
                    score.set(calculateHeuristic(processor.getBoard()));
                    break;
                }
            }
        }

        private double[] createActionsPriority(@NotNull final List<Answer> actions) {
            final double[] actionPriority = new double[actions.size()];
            actionPriority[0] = modify(actions.get(0));
            for (int i = 1; i < actionPriority.length; i++) {
                actionPriority[i] = actionPriority[i - 1] + modify(actions.get(i));
            }
            return actionPriority;
        }

        private int calculateHeuristic(final BattleArena arena) {
            final Army botArmy = arena.getArmy(botId);
            final Army enemyArmy = arena.getEnemyArmy(botId);

            return 12 + botArmy.getHeroes().size() - 2 * enemyArmy.getHeroes().size();
        }
    }
}
