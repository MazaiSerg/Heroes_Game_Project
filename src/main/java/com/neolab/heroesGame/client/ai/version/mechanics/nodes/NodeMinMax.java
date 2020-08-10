package com.neolab.heroesGame.client.ai.version.mechanics.nodes;

import com.neolab.heroesGame.client.ai.version.mechanics.arena.Answer;

import java.util.Objects;

public class NodeMinMax extends ANode {
    private final int depth;
    private int heuristicValue = Integer.MIN_VALUE;

    public NodeMinMax(final Answer prevAnswer, final ANode parent) {
        super(prevAnswer, parent);
        depth = ((NodeMinMax) parent).depth + 1;
    }

    public NodeMinMax() {
        super(null, null);
        depth = 0;
    }

    @Override
    public ANode createChild(final Answer prevAnswer, final ANode aNode) {
        return new NodeMinMax(prevAnswer, aNode);
    }

    public int getDepth() {
        return depth;
    }

    public int getHeuristicValue() {
        return heuristicValue;
    }

    public void setHeuristicValue(final int heuristicValue) {
        this.heuristicValue = heuristicValue;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final NodeMinMax that = (NodeMinMax) o;
        return depth == that.depth &&
                heuristicValue == that.heuristicValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), depth, heuristicValue);
    }
}
