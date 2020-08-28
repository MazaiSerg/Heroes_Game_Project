package com.neolab.heroesGame.client.ai.version.first;

import com.neolab.heroesGame.client.ai.version.basic.BasicMonteCarloBot;
import com.neolab.heroesGame.client.ai.version.mechanics.GameProcessor;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Answer;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.BattleArena;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Hero;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Magician;
import com.neolab.heroesGame.client.ai.version.mechanics.trees.SimulationsTree;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.enumerations.HeroActions;
import com.neolab.heroesGame.errors.HeroExceptions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.neolab.heroesGame.client.ai.enums.BotType.MONTE_CARLO;

/**
 * Бот, принимающий решение на основе многочисленных симуляций, в который выбор действия был случайным неравновероятным
 * На вероятность выбора действия влияет текущий винрейт, множитель действия и его эффективность.
 * Для защиты эффективность считается равной 1, для атаки - потенциально наносимый урон, для хила - потенциальный отхил
 * эффективность атаки по единичной цели будет увеличена, если атака добивает противника
 * Случайных характер попадания и урона не учитывается
 * Максимальное время должно быть немного ниже реально доступного времени, чтобы гарантированно укладываться
 */
public class MonteCarloBot extends BasicMonteCarloBot {
    private static final int TIME_TO_THINK = 100;
    private final List<Double> geneticCoefficients;

    public MonteCarloBot(final int id, final int timeToThink) {
        super(MONTE_CARLO.toString(), timeToThink, id);
        geneticCoefficients = createCoefficient();
    }

    public MonteCarloBot(final int id) {
        super(MONTE_CARLO.toString(), TIME_TO_THINK, id);
        geneticCoefficients = createCoefficient();
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
    private List<Double> createCoefficient() {
        final List<Double> coefficients = new ArrayList<>();
        coefficients.add(0.1);
        coefficients.add(1.0);
        coefficients.add(10.0);
        coefficients.add(5.0);
        return coefficients;
    }

    @Override
    public com.neolab.heroesGame.server.answers.Answer getAnswer(
            final com.neolab.heroesGame.arena.@NotNull BattleArena board) throws HeroExceptions {

        final long startTime = System.currentTimeMillis();
        if (board.getArmy(getId()).getHeroes().size() == board.getArmy(getId()).getAvailableHeroes().size()) {
            increaseCurrentRound();
        }
        final BattleArena arena = BattleArena.getCopyFromOriginalClass(board);
        final SimulationsTree tree = new SimulationsTree();
        while (System.currentTimeMillis() - startTime < getTimeToThink()) {
            final GameProcessor processor = new GameProcessor(getId(), arena.getCopy(), getCurrentRound());
            recursiveSimulation(processor, tree);
            tree.toRoot();
        }
        return tree.getBestAction().getCommonAnswer(getId());
    }

    /**
     * Рекурсивная функция для построение симуляционного дерева
     *
     * @param processor процессор, который моделирует поведение игрового движка
     * @param tree      симуляционное дерево
     * @return возвращаем результат партии для текущего бота
     */
    private GameEvent recursiveSimulation(final GameProcessor processor, final SimulationsTree tree) throws HeroExceptions {
        if (tree.isNodeNew()) {
            final List<Answer> actions = processor.getAllActionsForCurrentPlayer();
            tree.fieldNewNode(actions, calculateActionPriority(actions, processor));
        }
        final int actionNumber = chooseAction(tree.getActionPriority());
        processor.handleAnswer(tree.getAnswer(actionNumber));
        final GameEvent event = processor.matchOver();

        if (event == GameEvent.NOTHING_HAPPEN) {
            tree.downToChild(actionNumber);
            final GameEvent result = recursiveSimulation(processor, tree);
            tree.upToParent();
            tree.increase(processor.getActivePlayerId() == getId() ? result : whatReturn(result));
            return result;
        } else {
            tree.increase(event);
            return processor.getActivePlayerId() == getId() ? event : whatReturn(event);
        }
    }

    /**
     * меняем результат с победы на поражение, чтобы разные игроки имели разные значения в узлах
     */
    private GameEvent whatReturn(final GameEvent event) {
        return switch (event) {
            case YOU_WIN_GAME -> GameEvent.YOU_LOSE_GAME;
            case YOU_LOSE_GAME -> GameEvent.YOU_WIN_GAME;
            default -> GameEvent.GAME_END_WITH_A_TIE;
        };
    }

    private @NotNull double[] calculateActionPriority(@NotNull final List<Answer> actions,
                                                      @NotNull final GameProcessor processor) {
        final double[] actionPriority = new double[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            actionPriority[i] = modify(actions.get(i), processor);
        }
        return actionPriority;
    }

    /**
     * Задаем базовую "важность" действия
     */
    private double modify(final @NotNull Answer answer, final GameProcessor processor) {
        if (answer.getAction() == HeroActions.DEFENCE) {
            if (answer.getActiveHeroCoordinate().getY() == 0) {
                return geneticCoefficients.get(0);
            } else {
                return geneticCoefficients.get(1);
            }
        } else if (answer.getAction() == HeroActions.ATTACK) {
            return geneticCoefficients.get(2) * calculateDamage(answer, processor);
        }
        return geneticCoefficients.get(3) * calculateHeal(answer, processor);
    }

    /**
     * Если у юнита осталось мало хп, то полечить его важно
     * Если у юнита нормально хп, то не очень важно
     */
    private double calculateHeal(final Answer answer, final GameProcessor processor) {
        final Hero hero = processor.getAlliesHero(answer.getActiveHeroCoordinate());
        final Hero target = processor.getAlliesHero(answer.getTargetUnitCoordinate());
        if (target.getHp() < hero.getDamage()) {
            return (target.getHpMax() * 1.0 / target.getHp() * target.getDamage() * hero.getDamage());
        }
        return Math.min(hero.getDamage(), target.getHpMax() - target.getHp());
    }

    /**
     * определяем важность атаки по урону, который нанесем
     * Если юнит после атаки умрем - удваеваем важность атаки
     */
    private double calculateDamage(final Answer answer, final GameProcessor processor) {
        final Hero hero = processor.getAlliesHero(answer.getActiveHeroCoordinate());
        if (hero instanceof Magician) {
            return (double) hero.getDamage() * processor.getEnemyArmySize();
        }
        if (hero.getDamage() > processor.getEnemyHero(answer.getTargetUnitCoordinate()).getHp()) {
            return hero.getDamage() * 2d;
        }
        return hero.getDamage();
    }
}

