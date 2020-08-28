package com.neolab.heroesGame.client.ai.version.mechanics.nodes;

import com.neolab.heroesGame.client.ai.version.mechanics.arena.Answer;

import java.util.Objects;

public class PairAnswerHeuristic {
    private Answer answer;
    private int heuristicValue;

    public PairAnswerHeuristic(final Answer answer, final int heuristicValue) {
        this.answer = answer;
        this.heuristicValue = heuristicValue;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(final Answer answer) {
        this.answer = answer;
    }

    public int getHeuristicValue() {
        return heuristicValue;
    }

    public void setHeuristicValue(final int heuristicValue) {
        this.heuristicValue = heuristicValue;
    }

    public void setBest(final PairAnswerHeuristic secondOne) {
        if (heuristicValue < secondOne.heuristicValue) {
            answer = secondOne.answer;
            heuristicValue = secondOne.heuristicValue;
        }
    }

    public void setWorst(final PairAnswerHeuristic secondOne) {
        if (heuristicValue > secondOne.heuristicValue) {
            answer = secondOne.answer;
            heuristicValue = secondOne.heuristicValue;
        }
    }

    public boolean isBetter(final PairAnswerHeuristic secondOne) {
        return heuristicValue < secondOne.heuristicValue;
    }

    public boolean isWorst(final PairAnswerHeuristic secondOne) {
        return heuristicValue > secondOne.heuristicValue;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PairAnswerHeuristic that = (PairAnswerHeuristic) o;
        return heuristicValue == that.heuristicValue &&
                Objects.equals(answer, that.answer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(answer, heuristicValue);
    }
}
