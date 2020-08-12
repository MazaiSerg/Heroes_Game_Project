package com.neolab.heroesGame.client.ai.version.first;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.version.mechanics.GameProcessor;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Answer;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Army;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.BattleArena;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.IWarlord;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Magician;
import com.neolab.heroesGame.client.ai.version.mechanics.nodes.PairAnswerHeuristic;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.errors.HeroExceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class MinMaxWithoutTree extends Player {
    private static final String BOT_NAME = "Mazaev_v_MinMaxWithoutTree";
    private static final Logger LOGGER = LoggerFactory.getLogger(MinMaxWithoutTree.class);
    private final long SEED = 5916;
    private static final int MAX_DEPTH = 6;
    private static final int MAX_TIME = 1000;
    private int maxDepthForFastWork;
    private final Random RANDOM = new Random(SEED);
    private int currentDepth;

    public MinMaxWithoutTree(final int id) {
        super(id, BOT_NAME);
    }

    @Override
    public com.neolab.heroesGame.server.answers.Answer getAnswer(
            final com.neolab.heroesGame.arena.BattleArena board) throws HeroExceptions {
        final long startTime = System.currentTimeMillis();
        final BattleArena arena = BattleArena.getCopyFromOriginalClass(board);
        final GameProcessor processor = new GameProcessor(getId(), arena.getCopy());
        countMaxDepthForFastWork(arena);
        currentDepth = 0;
        final PairAnswerHeuristic bestAction = recursiveSimulation(processor,
                new PairAnswerHeuristic(Answer.getDefaultAnswer(), Integer.MAX_VALUE));
        if (System.currentTimeMillis() - startTime > MAX_TIME) {
            LOGGER.info("На обход дерева глубиной {} потрачено {}мс. Размер армии - {}, количество доступных юнитов - {}",
                    maxDepthForFastWork, System.currentTimeMillis() - startTime, arena.getArmy(getId()).getHeroes().size(),
                    arena.getArmy(getId()).getAvailableHeroes().size());
        }
        return bestAction.getAnswer().getCommonAnswer(getId());
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
     * @return эвристику либо терминального узла, либо узла с максимальной глубины
     */
    private PairAnswerHeuristic recursiveSimulation(final GameProcessor processor,
                                                    final PairAnswerHeuristic prevBest) throws HeroExceptions {
        if (currentDepth == maxDepthForFastWork || processor.matchOver() != GameEvent.NOTHING_HAPPEN) {
            return new PairAnswerHeuristic(Answer.getDefaultAnswer(), calculateHeuristic(processor.getBoard()));
        }
        final boolean isItThisBot = processor.getActivePlayerId() == getId();
        final List<Answer> actions = processor.getAllActionsForCurrentPlayer();
        final PairAnswerHeuristic currentBest = new PairAnswerHeuristic(actions.get(0),
                isItThisBot ? Integer.MIN_VALUE : Integer.MAX_VALUE);

        for (final Answer action : actions) {
            final PairAnswerHeuristic currentPair = new PairAnswerHeuristic(action, currentBest.getHeuristicValue());
            currentPair.setHeuristicValue(goDownToChild(processor, currentPair).getHeuristicValue());
            if (isItThisBot ? prevBest.isBetter(currentPair) : prevBest.isWorst(currentPair)) {
                return currentPair;
            }
            if (isItThisBot) {
                currentBest.setBest(currentPair);
            } else {
                currentBest.setWorst(currentPair);
            }
        }
        return currentBest;
    }

    /**
     * В функции спускаемся вниз в следующий узел. Перед этим выполняем действие, которое приводит к этому узлу
     * Если после возвращения назад id игрока отличается, то меняем текущего и ждущего игроков
     */
    private PairAnswerHeuristic goDownToChild(final GameProcessor processor,
                                              final PairAnswerHeuristic prevBest) throws HeroExceptions {
        final BattleArena arena = processor.getBoard();
        final int currentRound = processor.getRoundCounter();
        final int currentPlayerId = processor.getActivePlayerId();
        processor.setBoard(arena.getLightCopy(prevBest.getAnswer(), currentPlayerId));
        processor.handleAnswer(prevBest.getAnswer());
        currentDepth++;
        final PairAnswerHeuristic childPair = recursiveSimulation(processor, prevBest);
        currentDepth--;
        if (currentPlayerId != processor.getActivePlayerId()) {
            processor.swapActivePlayer();
        }
        processor.setBoard(arena);
        processor.setRoundCounter(currentRound);
        return childPair;
    }

    /**
     * вычисление эвристики:
     * ценность юнита представляет собой сумму текущего здоровья юнита, умноженную на ожидаемый урон
     * для повышения ценности убийства юнитов и ценности сохранения юнита в живых добавляем его максимальное здоровье,
     * умноженное на его урон;
     */
    private int calculateHeuristic(final BattleArena arena) {
        final Army botArmy = arena.getArmy(getId());
        final Army enemyArmy = arena.getEnemyArmy(getId());
        if (botArmy.getHeroes().isEmpty()) {
            return Integer.MIN_VALUE / currentDepth;
        }
        if (enemyArmy.getHeroes().isEmpty()) {
            return Integer.MAX_VALUE / currentDepth;
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
            maxDepthForFastWork = 6;
            return;
        }

        maxDepthForFastWork = 8;
    }
}
