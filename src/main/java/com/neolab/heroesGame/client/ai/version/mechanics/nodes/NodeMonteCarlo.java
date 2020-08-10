package com.neolab.heroesGame.client.ai.version.mechanics.nodes;

import com.neolab.heroesGame.client.ai.version.mechanics.arena.Answer;
import com.neolab.heroesGame.enumerations.GameEvent;

import java.util.Arrays;
import java.util.Objects;

public class NodeMonteCarlo extends ANode {
    private int winCounter;
    private int simulationsCounter;
    private int tiesCounter;
    private double[] basicActionPriority;

    public NodeMonteCarlo(final Answer prevAnswer, final ANode parent) {
        super(prevAnswer, parent);
        winCounter = 0;
        simulationsCounter = 0;
        tiesCounter = 0;
    }

    public void setActionPriority(final double[] actionPriority) {
        basicActionPriority = actionPriority;
    }

    @Override
    public ANode createChild(final Answer prevAnswer, final ANode aNode) {
        final ANode temp = new NodeMonteCarlo(prevAnswer, aNode);
        getChildren().add(temp);
        return temp;
    }

    public void increase(final GameEvent event) {
        if (event == GameEvent.YOU_WIN_GAME) {
            winCounter++;
        } else if (event == GameEvent.GAME_END_WITH_A_TIE) {
            tiesCounter++;
        }
        simulationsCounter++;
    }

    /**
     * @return Возвращает функцию распределения для доступных в этом узле действий: a[i] = a[i-1]
     */
    public double[] getActionPriorityForChoose() {
        final double[] actionPriority = getActionPriority();
        for (int i = 1; i < basicActionPriority.length; i++) {
            actionPriority[i] += actionPriority[i - 1];
        }
        return actionPriority;
    }

    /**
     * Функция рассчитывает текущие вероятности для доступных действий опираясь на базовые значения вероятностей, которые
     * задаются ботом через функцию setActionPriority.
     * Чтобы избежать лишних рассчетов базовые вероятности рассчитываются один раз при первом заходе в узел
     *
     * @return возвращает распределение вероятностей для доступных действий
     */
    public double[] getActionPriority() {
        final double[] actionPriority = new double[basicActionPriority.length];
        for (int i = 0; i < basicActionPriority.length; i++) {
            actionPriority[i] = basicActionPriority[i] + basicActionPriority[i] / 100
                    * ((NodeMonteCarlo) getChildren().get(i)).getScorePercent();
        }
        return actionPriority;
    }

    public int getScorePercent() {
        return simulationsCounter != 0 ? ((100 * (winCounter + tiesCounter / 2)) / simulationsCounter) : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NodeMonteCarlo that = (NodeMonteCarlo) o;
        return winCounter == that.winCounter &&
                simulationsCounter == that.simulationsCounter &&
                tiesCounter == that.tiesCounter &&
                Arrays.equals(basicActionPriority, that.basicActionPriority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), winCounter, simulationsCounter, tiesCounter, basicActionPriority);
    }
}
