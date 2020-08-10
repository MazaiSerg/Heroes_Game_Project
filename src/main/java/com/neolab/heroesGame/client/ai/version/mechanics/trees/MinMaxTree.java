package com.neolab.heroesGame.client.ai.version.mechanics.trees;

import com.neolab.heroesGame.client.ai.version.mechanics.arena.Answer;
import com.neolab.heroesGame.client.ai.version.mechanics.nodes.ANode;
import com.neolab.heroesGame.client.ai.version.mechanics.nodes.NodeMinMax;

import java.util.List;

public class MinMaxTree extends ATree {

    public MinMaxTree() {
        super(new NodeMinMax(), null);
        setCurrentNode(getRoot());
    }

    public void createAllChildren(final List<Answer> answers) {
        for (final Answer answer : answers) {
            getCurrentNode().createChild(answer);
        }
    }

    public boolean isMaxDepth(final int maxDepth) {
        return ((NodeMinMax) getCurrentNode()).getDepth() == maxDepth;
    }

    public int getCurrentDepth() {
        return ((NodeMinMax) getCurrentNode()).getDepth();
    }

    public void setHeuristic(final int heuristic) {
        ((NodeMinMax) getCurrentNode()).setHeuristic(heuristic);
    }

    /**
     * Изначально NodeMinMax temp имеет значение эвристики Integer.MIN
     *
     * @return Answer для узла с наивысшим значением эвристики
     */
    public Answer getBestHeuristicAnswer() {
        final List<ANode> children = getCurrentNode().getChildren();
        NodeMinMax temp = new NodeMinMax();
        for (final ANode child : children) {
            if (temp.getHeuristic() < ((NodeMinMax) child).getHeuristic()) {
                temp = ((NodeMinMax) child);
            }
        }
        return temp.getPrevAnswer();
    }
}
