package com.neolab.heroesGame.client.ai.version.mechanics.arena;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.neolab.heroesGame.enumerations.HeroActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Answer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Answer.class);
    private final SquareCoordinate activeHeroCoordinate;
    private final HeroActions action;
    private final SquareCoordinate targetUnitCoordinate;
    private final int playerId;

    @JsonCreator
    public Answer(@JsonProperty("activeHeroCoordinate") final SquareCoordinate activeHeroCoordinate,
                  @JsonProperty("action") final HeroActions action,
                  @JsonProperty("targetUnitCoordinate") final SquareCoordinate targetUnitCoordinate,
                  @JsonProperty("playerId") final int playerId) {
        this.playerId = playerId;
        this.action = action;
        this.activeHeroCoordinate = activeHeroCoordinate;
        this.targetUnitCoordinate = targetUnitCoordinate;
    }

    public SquareCoordinate getActiveHeroCoordinate() {
        return activeHeroCoordinate;
    }

    public HeroActions getAction() {
        return action;
    }

    public SquareCoordinate getTargetUnitCoordinate() {
        return targetUnitCoordinate;
    }

    public int getPlayerId() {
        return playerId;
    }

    public com.neolab.heroesGame.server.answers.Answer getCommonAnswer() {
        return new com.neolab.heroesGame.server.answers.Answer(activeHeroCoordinate.toCommonCoordinate(), action,
                targetUnitCoordinate.toCommonCoordinate(), playerId);
    }

    @Override
    public String toString() {
        final StringBuilder log = new StringBuilder();
        log.append(String.format("Игрок <%d> запросил действие %s юнитом на позиции (%d, %d)",
                playerId, action, activeHeroCoordinate.getX(), activeHeroCoordinate.getY()));
        if (action != HeroActions.DEFENCE) {
            log.append(String.format(" на юнита на позиции (%d, %d)", targetUnitCoordinate.getX(), targetUnitCoordinate.getY()));
        }
        return log.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Answer answer = (Answer) o;
        return playerId == answer.playerId &&
                Objects.equals(activeHeroCoordinate, answer.activeHeroCoordinate) &&
                action == answer.action &&
                Objects.equals(targetUnitCoordinate, answer.targetUnitCoordinate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activeHeroCoordinate, action, targetUnitCoordinate, playerId);
    }

    public void toLog() {
        LOGGER.info(this.toString());
    }
}