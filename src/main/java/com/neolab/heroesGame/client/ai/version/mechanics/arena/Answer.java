package com.neolab.heroesGame.client.ai.version.mechanics.arena;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.neolab.heroesGame.enumerations.HeroActions;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.neolab.heroesGame.client.ai.version.mechanics.arena.SquareCoordinate.coordinateDoesntMatters;
import static com.neolab.heroesGame.client.ai.version.mechanics.arena.SquareCoordinate.getSquareCoordinate;
import static com.neolab.heroesGame.enumerations.HeroActions.*;

public class Answer {

    static final private Map<SquareCoordinate, Answer> defenses;
    static final private Map<SquareCoordinate, Map<SquareCoordinate, Answer>> attack;
    static final private Map<SquareCoordinate, Map<SquareCoordinate, Answer>> heals;

    static final private List<Answer> listDefenses;
    static final private List<List<Answer>> listAttack;
    static final private List<List<Answer>> listHeals;

    static {
        defenses = new HashMap<>();
        attack = new HashMap<>();
        heals = new HashMap<>();
        for (int i = 0; i < 6; i++) {
            final SquareCoordinate activeUnit = getSquareCoordinate(i);
            defenses.put(activeUnit, new Answer(activeUnit, DEFENCE, coordinateDoesntMatters));

            final Map<SquareCoordinate, Answer> allAttacksForActiveUnit = new HashMap<>();
            for (int j = 0; j < 6; j++) {
                allAttacksForActiveUnit.put(getSquareCoordinate(j), new Answer(activeUnit, ATTACK, getSquareCoordinate(j)));
            }
            if (i < 3) {
                allAttacksForActiveUnit.put(coordinateDoesntMatters, new Answer(activeUnit, ATTACK, coordinateDoesntMatters));
            }
            attack.put(activeUnit, allAttacksForActiveUnit);

            if (i < 3) {
                final Map<SquareCoordinate, Answer> allHealsForActiveUnit = new HashMap<>();
                for (int j = 0; j < 6; j++) {
                    allHealsForActiveUnit.put(getSquareCoordinate(j), new Answer(activeUnit, HEAL, getSquareCoordinate(j)));
                }
                heals.put(activeUnit, allHealsForActiveUnit);
            }
        }


        listDefenses = new ArrayList<>();
        listAttack = new ArrayList<>();
        listHeals = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            final SquareCoordinate activeUnit = getSquareCoordinate(i);
            listDefenses.add(new Answer(activeUnit, DEFENCE, coordinateDoesntMatters));

            final List<Answer> allAttacksForActiveUnit = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                allAttacksForActiveUnit.add(new Answer(activeUnit, ATTACK, getSquareCoordinate(j)));
            }
            if (i < 3) {
                allAttacksForActiveUnit.add(new Answer(activeUnit, ATTACK, coordinateDoesntMatters));
            }
            listAttack.add(allAttacksForActiveUnit);

            if (i < 3) {
                final List<Answer> allHealsForActiveUnit = new ArrayList<>();
                for (int j = 0; j < 6; j++) {
                    allHealsForActiveUnit.add(new Answer(activeUnit, HEAL, getSquareCoordinate(j)));
                }
                listHeals.add(allHealsForActiveUnit);
            }
        }
    }


    private final SquareCoordinate activeHeroCoordinate;
    private final HeroActions action;
    private final SquareCoordinate targetUnitCoordinate;

    @JsonCreator
    private Answer(@JsonProperty("activeHeroCoordinate") final SquareCoordinate activeHeroCoordinate,
                   @JsonProperty("action") final HeroActions action,
                   @JsonProperty("targetUnitCoordinate") final SquareCoordinate targetUnitCoordinate) {
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

    public static @NotNull Answer getAnswer(final SquareCoordinate activeHeroCoordinate, final HeroActions action,
                                            final SquareCoordinate target) {
        return switch (action) {
            case DEFENCE -> defenses.get(activeHeroCoordinate);
            case HEAL -> heals.get(activeHeroCoordinate).get(target);
            case ATTACK -> attack.get(activeHeroCoordinate).get(target);
        };
    }

    public static @NotNull Answer getAnswerFromList(final SquareCoordinate activeHeroCoordinate, final HeroActions action,
                                                    final SquareCoordinate target) {
        return switch (action) {
            case DEFENCE -> listDefenses.get(activeHeroCoordinate.getIndex());
            case HEAL -> listHeals.get(activeHeroCoordinate.getIndex()).get(target.getIndex());
            case ATTACK -> listAttack.get(activeHeroCoordinate.getIndex()).get(target.getIndex());
        };
    }

    public static Answer getDefaultAnswer() {
        return defenses.get(getSquareCoordinate(0));
    }

    public com.neolab.heroesGame.server.answers.Answer getCommonAnswer(final int playerId) {
        return new com.neolab.heroesGame.server.answers.Answer(activeHeroCoordinate.toCommonCoordinate(), action,
                targetUnitCoordinate.toCommonCoordinate(), playerId);
    }
}