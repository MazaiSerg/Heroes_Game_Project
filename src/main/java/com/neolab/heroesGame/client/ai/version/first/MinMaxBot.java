package com.neolab.heroesGame.client.ai.version.first;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.version.mechanics.GameProcessor;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Army;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.BattleArena;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.IWarlord;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Magician;
import com.neolab.heroesGame.client.ai.version.mechanics.nodes.ANode;
import com.neolab.heroesGame.client.ai.version.mechanics.trees.MinMaxTree;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.server.answers.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class MinMaxBot extends Player {
    private static final String BOT_NAME = "Mazaev_v_MinMax";
    private static final Logger LOGGER = LoggerFactory.getLogger(MinMaxBot.class);
    private final long SEED = 5916;
    private static final int MAX_DEPTH = 6;
    private final Random RANDOM = new Random(SEED);

    public MinMaxBot(final int id) {
        super(id, BOT_NAME);
    }

    @Override
    public Answer getAnswer(final com.neolab.heroesGame.arena.BattleArena board) throws HeroExceptions {
        final long startTime = System.currentTimeMillis();
        final BattleArena arena = BattleArena.getCopyFromOriginalClass(board);
        final MinMaxTree tree = new MinMaxTree();
        final GameProcessor processor = new GameProcessor(getId(), arena.getCopy());
        recursiveSimulation(processor, tree, Integer.MAX_VALUE);
        LOGGER.info("На обход дерева глубиной {} потрачено {}мс", MAX_DEPTH, System.currentTimeMillis() - startTime);
        return tree.getBestHeuristicAnswer();
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
     * Рекурсивная функция - строит симуляционное дерево заданной глубины.
     *
     * @param tree               текущее дерево
     * @param prevHeuristicValue текущее значение эвристики уровнем выше
     * @return эвристику либо терминального узла, либо узла с максимальной глубины
     */
    private int recursiveSimulation(final GameProcessor processor, final MinMaxTree tree,
                                    final int prevHeuristicValue) throws HeroExceptions {
        if (tree.isMaxDepth(MAX_DEPTH) || processor.matchOver() != GameEvent.NOTHING_HAPPEN) {
            final int heuristic = calculateHeuristic(processor.getBoard());
            tree.setHeuristic(heuristic);
            return heuristic;
        }

        final boolean isItThisBot = processor.getActivePlayerId() == getId();
        tree.createAllChildren(processor.getAllActionsForCurrentPlayer());
        final BattleArena arena = processor.getBoard().getCopy();
        int heuristicValue = isItThisBot ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (final ANode node : tree.getCurrentNode().getChildren()) {
            final int nodeHeuristicValue = goDownToChild(processor, tree, heuristicValue, node);
            if (isItThisBot ? prevHeuristicValue < nodeHeuristicValue : prevHeuristicValue > nodeHeuristicValue) {
                tree.setHeuristic(nodeHeuristicValue);
                return nodeHeuristicValue;
            }
            if (isItThisBot ? nodeHeuristicValue > heuristicValue : nodeHeuristicValue < heuristicValue) {
                heuristicValue = nodeHeuristicValue;
            }
            processor.setBoard(arena.getCopy());
        }

        tree.setHeuristic(heuristicValue);
        return heuristicValue;
    }

    /**
     * В функции спускаемся вниз в следующий узел. Перед этим выполняем действие, которое приводит к этому узлу
     * Если после возвращения назад id игрока отличается, то меняем текущего и ждущего игроков
     */
    private int goDownToChild(final GameProcessor processor, final MinMaxTree tree,
                              final int prevHeuristicValue, final ANode child) throws HeroExceptions {
        final int currentPlayerId = processor.getActivePlayerId();
        processor.handleAnswer(child.getPrevAnswer());
        tree.downToChild(child);
        final int nodeHeuristicValue = recursiveSimulation(processor, tree, prevHeuristicValue);
        tree.upToParent();
        if (currentPlayerId != processor.getActivePlayerId()) {
            processor.swapActivePlayer();
        }
        return nodeHeuristicValue;
    }

    /**
     * вычисление эвристики:
     * ценность юнита представляет собой сумму текущего здоровья юнита, умноженную на ожидаемый урон
     * для повышения ценности убийства юнитов и ценности сохранения юнита в живых добавляем его максимальное здоровье,
     * умноженное на его урон;
     * для варлордов даем еще сверху 4000 очков ценности
     */
    private int calculateHeuristic(final BattleArena arena) {
        final Army botArmy = arena.getArmy(getId());
        final Army enemyArmy = arena.getEnemyArmy(getId());
        if (botArmy.getHeroes().isEmpty()) {
            return Integer.MIN_VALUE;
        }
        if (enemyArmy.getHeroes().isEmpty()) {
            return Integer.MAX_VALUE;
        }

        final AtomicInteger heuristic = new AtomicInteger(0);
        botArmy.getHeroes().values().forEach(hero -> {
            final int delta = hero.getDamage() * hero.getHp() * (hero instanceof Magician ? enemyArmy.getHeroes().size() : 1)
                    + hero.getHpMax() * hero.getDamage()
                    + (hero instanceof IWarlord ? 4000 : 0);
            heuristic.addAndGet(delta);
        });

        enemyArmy.getHeroes().values().forEach(hero -> {
            final int delta = -hero.getDamage() * hero.getHp() * (hero instanceof Magician ? enemyArmy.getHeroes().size() : 1)
                    - hero.getHpMax() * hero.getDamage()
                    - (hero instanceof IWarlord ? 4000 : 0);
            heuristic.addAndGet(delta);
        });
        return heuristic.get();
    }
}
