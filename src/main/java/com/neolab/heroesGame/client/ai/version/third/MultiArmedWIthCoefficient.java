package com.neolab.heroesGame.client.ai.version.third;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.client.ai.Player;
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
import java.util.Random;

import static com.neolab.heroesGame.client.ai.version.mechanics.AnswerValidator.initializeHashMap;

public class MultiArmedWIthCoefficient extends Player {
    private static final String BOT_NAME = "Many Armed With Coefficients";
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiArmedWIthCoefficient.class);
    private static final int TIME_TO_THINK = 900;
    private static final double LN_2D = Math.log(2d);
    private static final boolean USE_RANDOM = true;
    private final long SEED = 5916;
    private final Random RANDOM = new Random(SEED);
    private int currentRound = -1;
    private List<Double> geneticCoefficients;
    private String genotype = "ADMH";

    public MultiArmedWIthCoefficient(final int id) {
        super(id, BOT_NAME);
        geneticCoefficients = updateCoefficient();
        initializeHashMap();
    }

    /**
     * Массив коэффициентов приорететов действий юнитов:
     * 0 - защита юнитов второй линии
     * 1 - защита юнитов первой линии
     * 2 - модификатор атаки от урона
     * 3 - модификатор хила
     *
     * @return Массив коэффициентов приорететов действий юнитов
     */
    private List<Double> updateCoefficient() {
        final List<Double> coefficients = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
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

    public String getGenotype() {
        return genotype;
    }

    public void setGenotype(final String genotype) {
        this.genotype = genotype;
        geneticCoefficients = updateCoefficient();
    }

    @Override
    public com.neolab.heroesGame.server.answers.Answer getAnswer(
            final com.neolab.heroesGame.arena.@NotNull BattleArena board) throws HeroExceptions {

        final long startTime = System.currentTimeMillis();
        if (board.getArmy(getId()).getHeroes().size() == board.getArmy(getId()).getAvailableHeroes().size()) {
            currentRound++;
        }
        final BattleArena arena = BattleArena.getCopyFromOriginalClass(board);
        final List<Answer> actions = arena.getAllActionForPlayer(getId());
        final int[] simulationsCounter = new int[actions.size()];
        final double[] scores = new double[actions.size()];
        for (int i = 0; i < scores.length; i++) {
            simulationsCounter[i] = 0;
            scores[i] = modify(actions.get(i));
        }
        for (int i = 0; ; ) {
            if (System.currentTimeMillis() - startTime > TIME_TO_THINK) {
                LOGGER.info("Количество симуляций за {}мс: {}", System.currentTimeMillis() - startTime, i);
                break;
            }
            final double[] priorityFunction = countPriorityFunction(scores, simulationsCounter, i);
            final int index = chooseAction(priorityFunction);
            final GameProcessor processor = new GameProcessor(getId(), arena.getCopy(), currentRound, USE_RANDOM);
            final double score = recursiveSimulation(processor, actions.get(index), 0);
            i++;
            scores[index] = (scores[index] * simulationsCounter[index] + score) / (simulationsCounter[index] + 1);
            simulationsCounter[index]++;
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

    @Override
    public String getStringArmyFirst(final int armySize) {
        final List<String> armies = CommonFunction.getAllAvailableArmiesCode(armySize);
        return armies.get(RANDOM.nextInt(armies.size()));
    }

    @Override
    public String getStringArmySecond(final int armySize, final com.neolab.heroesGame.arena.Army army) {
        return getStringArmyFirst(armySize);
    }

    @Override
    public String getType() {
        return getGenotype();
    }

    /**
     * Рекурсивная функция для построение симуляционного дерева
     *
     * @param processor процессор, который моделирует поведение игрового движка
     */
    private int recursiveSimulation(final GameProcessor processor, final Answer action, final int depth) throws HeroExceptions {

        processor.handleAnswer(action);
        final GameEvent event = processor.matchOver();

        if (event == GameEvent.NOTHING_HAPPEN) {
            final List<Answer> actions = processor.getAllActionsForCurrentPlayer();
            final int index = chooseAction(createActionsPriority(actions));
            return recursiveSimulation(processor, actions.get(index), depth + 1);
        } else {
            return calculateHeuristic(processor.getBoard());
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

    private double modify(final Answer answer) {
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

    private int calculateHeuristic(final BattleArena arena) {
        final Army botArmy = arena.getArmy(getId());
        final Army enemyArmy = arena.getEnemyArmy(getId());

        return 12 + botArmy.getHeroes().size() - 2 * enemyArmy.getHeroes().size();
    }

    /**
     * Выбираем случайное действие с учетом приоретета действий
     */
    private int chooseAction(@NotNull final double[] actionPriority) {
        final double random = RANDOM.nextDouble() * actionPriority[actionPriority.length - 1];
        for (int i = 0; i < actionPriority.length; i++) {
            if (actionPriority[i] > random) {
                return i;
            }
        }
        LOGGER.error("WTF!!!");
        for (final double aDouble : actionPriority) {
            LOGGER.trace("RANDOM: {}, Action: {}", random, aDouble);
        }
        return 0;
    }
}
