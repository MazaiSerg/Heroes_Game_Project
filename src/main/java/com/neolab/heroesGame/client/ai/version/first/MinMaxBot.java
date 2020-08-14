package com.neolab.heroesGame.client.ai.version.first;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.version.mechanics.AnswerValidator;
import com.neolab.heroesGame.client.ai.version.mechanics.GameProcessor;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Army;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.BattleArena;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.IWarlord;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Magician;
import com.neolab.heroesGame.client.ai.version.mechanics.nodes.ANode;
import com.neolab.heroesGame.client.ai.version.mechanics.trees.MinMaxTree;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.errors.HeroExceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class MinMaxBot extends Player {
    private static final String BOT_NAME = "Min Max";
    private static final Logger LOGGER = LoggerFactory.getLogger(MinMaxBot.class);
    private final long SEED = 5916;
    private static final int MAX_DEPTH = 6;
    private static final int MAX_TIME = 1000;
    private int maxDepthForFastWork;
    private final Random RANDOM = new Random(SEED);

    public MinMaxBot(final int id) {
        super(id, BOT_NAME);
        AnswerValidator.initializeHashMap();
    }

    @Override
    public com.neolab.heroesGame.server.answers.Answer getAnswer(
            final com.neolab.heroesGame.arena.BattleArena board) throws HeroExceptions {
        final long startTime = System.currentTimeMillis();
        final BattleArena arena = BattleArena.getCopyFromOriginalClass(board);
        final MinMaxTree tree = new MinMaxTree();
        final GameProcessor processor = new GameProcessor(getId(), arena.getCopy());
        countMaxDepthForFastWork(arena);
        recursiveSimulation(processor, tree, Integer.MAX_VALUE);
        if (System.currentTimeMillis() - startTime > MAX_TIME) {
            LOGGER.info("На обход дерева глубиной {} потрачено {}мс. Размер армии - {}, количество доступных юнитов - {}",
                    maxDepthForFastWork, System.currentTimeMillis() - startTime, arena.getArmy(getId()).getHeroes().size(),
                    arena.getArmy(getId()).getAvailableHeroes().size());
        }
        return tree.getBestHeuristicAnswer().getCommonAnswer(getId());
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
            final int heuristicValue = calculateHeuristic(processor.getBoard(), tree.getCurrentDepth());
            tree.setHeuristic(heuristicValue);
            return heuristicValue;
        }
        final boolean isItThisBot = processor.getActivePlayerId() == getId();
        tree.createAllChildren(processor.getAllActionsForCurrentPlayer());
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
        final BattleArena arena = processor.getBoard();
        final int currentRound = processor.getRoundCounter();
        processor.setBoard(arena.getCopy());
        processor.handleAnswer(child.getPrevAnswer());
        tree.downToChild(child);
        final int nodeHeuristicValue = recursiveSimulation(processor, tree, prevHeuristicValue);
        tree.upToParent();
        if (currentPlayerId != processor.getActivePlayerId()) {
            processor.swapActivePlayer();
        }
        processor.setBoard(arena);
        processor.setRoundCounter(currentRound);
        return nodeHeuristicValue;
    }

    /**
     * вычисление эвристики:
     * ценность юнита представляет собой сумму текущего здоровья юнита, умноженную на ожидаемый урон
     * для повышения ценности убийства юнитов и ценности сохранения юнита в живых добавляем его максимальное здоровье,
     * умноженное на его урон;
     */
    private int calculateHeuristic(final BattleArena arena, final int depth) {
        final Army botArmy = arena.getArmy(getId());
        final Army enemyArmy = arena.getEnemyArmy(getId());
        if (botArmy.getHeroes().isEmpty()) {
            return Integer.MIN_VALUE / (depth + 1);
        }
        if (enemyArmy.getHeroes().isEmpty()) {
            return Integer.MAX_VALUE / (depth + 1);
        }

        final AtomicInteger heuristic = new AtomicInteger(0);
        botArmy.getHeroes().values().forEach(hero -> {
            final int delta = hero.getDamage() * hero.getHp() * (hero instanceof Magician ? enemyArmy.getHeroes().size() : 1)
                    + hero.getHpMax() * hero.getDamage() / 4;
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

    private void countMaxDepthForFastWork(final BattleArena arena) {
        /*
        final long maxCountNode = 200_000_000L;
        final int numbersHero = Math.max(arena.getArmy(getId()).getHeroes().size(),
                arena.getEnemyArmy(getId()).getHeroes().size());
        int numbersAvailableHero = Math.max(arena.getArmy(getId()).getAvailableHeroes().size(),
                arena.getEnemyArmy(getId()).getAvailableHeroes().size());
        long countNode = 1;
        int numbersActionForOne = 2 + numbersAvailableHero / 3;
        maxDepthForFastWork = 0;
        while (true) {
            countNode *= numbersAvailableHero * numbersActionForOne * numbersAvailableHero * numbersActionForOne;
            if (countNode > maxCountNode) {
                break;
            }
            numbersAvailableHero = numbersAvailableHero == 1 ? numbersHero : numbersAvailableHero - 1;
            maxDepthForFastWork++;
            numbersActionForOne = 2 + numbersAvailableHero / 3;
        }
        maxDepthForFastWork = Math.max(maxDepthForFastWork, 4);
        maxDepthForFastWork += (6 - numbersHero);

         */
        final int numbersHero = arena.getArmy(getId()).getHeroes().size() +
                arena.getEnemyArmy(getId()).getHeroes().size();
        final int numbersAvailableHero = arena.getArmy(getId()).getAvailableHeroes().size() +
                arena.getEnemyArmy(getId()).getAvailableHeroes().size();
        if (numbersAvailableHero >= 7 || (numbersHero >= 10 && numbersAvailableHero <= 2)) {
            maxDepthForFastWork = 4;
            return;
        }

        if (numbersHero >= 9) {
            maxDepthForFastWork = 4;
            return;
        }

        maxDepthForFastWork = 4;
    }
}
