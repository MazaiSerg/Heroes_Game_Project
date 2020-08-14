package com.neolab.heroesGame.client.ai.version.second;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.version.mechanics.AnswerValidator;
import com.neolab.heroesGame.client.ai.version.mechanics.GameProcessor;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Answer;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Army;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.BattleArena;
import com.neolab.heroesGame.client.ai.version.mechanics.trees.ManyArmedBanditTree;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.errors.HeroExceptions;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

public class ManyArmedBandit extends Player {
    private static final String BOT_NAME = "ManyArmedBanditBot";
    private static final Logger LOGGER = LoggerFactory.getLogger(ManyArmedBandit.class);
    private static final int TIME_TO_THINK = 900;
    private final long SEED = 5916;
    private final Random RANDOM = new Random(SEED);
    private int currentRound = -1;

    public ManyArmedBandit(final int id) {
        super(id, BOT_NAME);
        AnswerValidator.initializeHashMap();
    }

    @Override
    public com.neolab.heroesGame.server.answers.Answer getAnswer(
            final com.neolab.heroesGame.arena.@NotNull BattleArena board) throws HeroExceptions {

        final long startTime = System.currentTimeMillis();
        if (board.getArmy(getId()).getHeroes().size() == board.getArmy(getId()).getAvailableHeroes().size()) {
            currentRound++;
        }
        final BattleArena arena = BattleArena.getCopyFromOriginalClass(board);
        final ManyArmedBanditTree tree = new ManyArmedBanditTree();
        for (int i = 0; ; ) {
            if (System.currentTimeMillis() - startTime > TIME_TO_THINK) {
                LOGGER.info("Количество симуляций за {}мс: {}", System.currentTimeMillis() - startTime, i);
                break;
            }
            final GameProcessor processor = new GameProcessor(getId(), arena.getCopy(), currentRound);
            recursiveSimulation(processor, tree);
            tree.toRoot();
            i++;
            if (tree.isWinnable()) {
                LOGGER.info("Количество симуляций за {}мс: {}", System.currentTimeMillis() - startTime, i);
                return tree.getWinnableMove().getCommonAnswer(getId());
            }
        }
        return tree.getBestAction().getCommonAnswer(getId());
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

    /**
     * Рекурсивная функция для построение симуляционного дерева
     *
     * @param processor процессор, который моделирует поведение игрового движка
     * @param tree      симуляционное дерево
     */
    private void recursiveSimulation(final GameProcessor processor, final ManyArmedBanditTree tree) throws HeroExceptions {
        if (tree.isNodeNew()) {
            final List<Answer> actions = processor.getAllActionsForCurrentPlayer();
            tree.createAllChildren(actions);
        }
        final int actionNumber = chooseAction(tree.getActionPriority());
        processor.handleAnswer(tree.getAnswer(actionNumber));
        final GameEvent event = processor.matchOver();

        if (event == GameEvent.NOTHING_HAPPEN) {
            tree.downToChild(actionNumber);
            recursiveSimulation(processor, tree);
            tree.upToParent();
        } else {
            tree.setResultForBot(processor.getActivePlayerId() == getId() ? event
                    : (event == GameEvent.GAME_END_WITH_A_TIE ? event
                    : event == GameEvent.YOU_LOSE_GAME ? GameEvent.YOU_WIN_GAME : GameEvent.YOU_LOSE_GAME));
            tree.checkWinFirstMove(actionNumber);
        }

        //tree.increase(((processor.getActivePlayerId() == getId()) ? countScore(tree) : 15 - 1 * countScore(tree)));
        tree.increase(((processor.getActivePlayerId() == getId()) ? 1 : -1)
                * calculateHeuristic(processor.getBoard()) + 18);
    }

    /**
     * меняем результат с победы на поражение, чтобы разные игроки имели разные значения в узлах
     */
    private double countScore(final ManyArmedBanditTree tree) {
        final double score = 5d / (((tree.getMaxDepth() - tree.getCurrentDepth()) / 2) + 1);
        return switch (tree.getResultForBot()) {
            case YOU_WIN_GAME -> 10 + score;
            case YOU_LOSE_GAME -> 5 - score;
            default -> 5;
        };
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
