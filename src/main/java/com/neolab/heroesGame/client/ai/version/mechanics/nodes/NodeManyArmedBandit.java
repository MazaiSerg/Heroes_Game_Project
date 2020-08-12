package com.neolab.heroesGame.client.ai.version.mechanics.nodes;

import com.neolab.heroesGame.client.ai.version.mechanics.arena.Answer;

public class NodeManyArmedBandit extends ANode {
    private final int depth;
    private int simulationsCounter = 0;
    private double score = 0d;

    public NodeManyArmedBandit(final Answer prevAnswer, final ANode parent) {
        super(prevAnswer, parent);
        depth = ((NodeManyArmedBandit) parent).depth + 1;
    }

    public NodeManyArmedBandit() {
        super(null, null);
        depth = 0;
    }

    @Override
    public ANode createChild(final Answer prevAnswer, final ANode aNode) {
        return new NodeManyArmedBandit(prevAnswer, aNode);
    }

    public int getDepth() {
        return depth;
    }

    public double getScore() {
        return score;
    }

    public void increase(final double score) {
        this.score = (this.score * simulationsCounter + score) / (simulationsCounter + 1);
        simulationsCounter++;
    }

    public double[] getActionPriorityForChoose() {
        final double[] actionPriority = getActionPriority();
        for (int i = 1; i < actionPriority.length; i++) {
            actionPriority[i] += actionPriority[i - 1];
        }
        return actionPriority;
    }

    public double[] getActionPriority() {
        final double[] actionPriority = new double[getChildren().size()];
        for (int i = 0; i < actionPriority.length; i++) {
            final NodeManyArmedBandit child = (NodeManyArmedBandit) getChild(i);
            actionPriority[i] = child.simulationsCounter == 0
                    ? 5
                    : child.score + Math.sqrt(2 * Math.log(simulationsCounter) / child.simulationsCounter / Math.log(2d));
        }
        return actionPriority;
    }
}
