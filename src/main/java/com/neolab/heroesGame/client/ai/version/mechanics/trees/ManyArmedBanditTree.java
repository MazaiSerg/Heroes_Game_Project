package com.neolab.heroesGame.client.ai.version.mechanics.trees;

import com.neolab.heroesGame.client.ai.version.mechanics.arena.Answer;
import com.neolab.heroesGame.client.ai.version.mechanics.nodes.ANode;
import com.neolab.heroesGame.client.ai.version.mechanics.nodes.NodeManyArmedBandit;
import com.neolab.heroesGame.enumerations.GameEvent;

import java.util.List;
import java.util.Optional;

public class ManyArmedBanditTree extends ATree {
    private int maxDepth = 0;
    private GameEvent resultForBot = GameEvent.NOTHING_HAPPEN;
    private boolean canWinNextMove = false;
    private ANode winnable = null;

    public ManyArmedBanditTree() {
        super(new NodeManyArmedBandit(), null);
        setCurrentNode(getRoot());
    }

    public void createAllChildren(final List<Answer> answers) {
        for (final Answer answer : answers) {
            getCurrentNode().createChild(answer);
        }
    }

    @Override
    public boolean downToChild(final int index) {
        if (super.downToChild(index)) {
            maxDepth++;
            return true;
        }
        return false;
    }

    @Override
    public void toRoot() {
        super.toRoot();
        maxDepth = 0;
        resultForBot = GameEvent.NOTHING_HAPPEN;
    }

    public int getCurrentDepth() {
        return ((NodeManyArmedBandit) getCurrentNode()).getDepth();
    }

    public GameEvent getResultForBot() {
        return resultForBot;
    }

    public void setResultForBot(GameEvent resultForBot) {
        this.resultForBot = resultForBot;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void checkWinFirstMove(final int index) {
        if (resultForBot == GameEvent.YOU_WIN_GAME && maxDepth == 0) {
            canWinNextMove = true;
            winnable = getCurrentNode().getChild(index);
        }
    }

    public boolean isWinnable() {
        return canWinNextMove;
    }

    public Answer getWinnableMove() {
        return winnable.getPrevAnswer();
    }

    public double[] getActionPriority() {
        return Optional.ofNullable(((NodeManyArmedBandit) getCurrentNode()).getActionPriorityForChoose()).orElseThrow();
    }

    /**
     * Ищем максимальное значение текущего приоретета среди доступных действий
     *
     * @return Answer для узла с наибольшим значением приоретета, даже если винрейт узла не максимальный
     */
    public Answer getBestAction() {
        final double[] actionPriority = ((NodeManyArmedBandit) getCurrentNode()).getActionPriority();
        int max = 0;
        for (int i = 0; i < actionPriority.length; i++) {
            if (actionPriority[max] < actionPriority[i]) {
                max = i;
            }
        }
        return getCurrentNode().getChild(max).getPrevAnswer();
    }

    public void increase(final double score) {
        ((NodeManyArmedBandit) getCurrentNode()).increase(score);
    }

}
