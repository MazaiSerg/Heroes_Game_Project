package com.neolab.heroesGame.client.ai.version.mechanics;

import com.neolab.heroesGame.client.ai.version.mechanics.arena.SquareCoordinate;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.neolab.heroesGame.client.ai.version.mechanics.arena.SquareCoordinate.getSquareCoordinate;

public class AnswerValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnswerValidator.class);
    private static final Map<SquareCoordinate, Map<Set<SquareCoordinate>, Set<SquareCoordinate>>> actionForFootman;
    private static boolean isInitialize = false;

    static {
        actionForFootman = new HashMap<>();
    }

    public static void initializeHashMap() {
        isInitialize = true;
        final List<Set<SquareCoordinate>> allAliveUnitVariations = new ArrayList<>();
        final boolean[] counter = new boolean[6];
        Arrays.fill(counter, Boolean.FALSE);
        do {
            int index = 6;
            do {
                index--;
                counter[index] = !counter[index];
            } while (!counter[index] && index != 0);
            final Set<SquareCoordinate> variation = new HashSet<>();
            for (int i = 0; i < 6; i++) {
                if (counter[i]) {
                    variation.add(getSquareCoordinate(i));
                }
            }
            allAliveUnitVariations.add(variation);
        } while (!(counter[0] && counter[1] && counter[2] && counter[3] && counter[4] && counter[5]));
        for (int i = 0; i < 3; i++) {
            final SquareCoordinate currentPosition = getSquareCoordinate(i, 1);
            final Map<Set<SquareCoordinate>, Set<SquareCoordinate>> targetVariations = new HashMap<>();
            for (final Set<SquareCoordinate> variation : allAliveUnitVariations) {
                targetVariations.put(variation, getCorrectTargetForFootman(currentPosition, variation));
            }
            actionForFootman.put(currentPosition, targetVariations);
        }
    }

    public static @NotNull Set<SquareCoordinate> getCorrectTargetForFootmanFromHashMap(
            final @NotNull SquareCoordinate activeUnit,
            final @NotNull Set<SquareCoordinate> enemies) {
        return actionForFootman.get(activeUnit).get(enemies);
    }

    public static @NotNull Set<SquareCoordinate> getCorrectTargetForFootman(
            final @NotNull SquareCoordinate activeUnit,
            final @NotNull Set<SquareCoordinate> enemies) {
        final Set<SquareCoordinate> validateTarget = new HashSet<>();
        for (int y = 1; y >= 0; y--) {
            if (activeUnit.getX() == 1) {
                validateTarget.addAll(getTargetForCentralUnit(enemies, y));
            } else {
                validateTarget.addAll(getTargetForFlankUnit(activeUnit.getX(), enemies, y));
            }
            if (!validateTarget.isEmpty()) {
                break;
            }
        }
        return validateTarget;
    }

    private static Set<SquareCoordinate> getTargetForFlankUnit(final int activeUnitX,
                                                               final Set<SquareCoordinate> enemies, final int y) {
        final Set<SquareCoordinate> validateTarget = new HashSet<>();
        SquareCoordinate coordinate = getSquareCoordinate(1, y);
        if (enemies.contains(coordinate)) {
            validateTarget.add(coordinate);
        }
        coordinate = getSquareCoordinate(activeUnitX, y);
        if (enemies.contains(coordinate)) {
            validateTarget.add(coordinate);
        }
        coordinate = getSquareCoordinate(activeUnitX == 2 ? 0 : 2, y);
        if (validateTarget.isEmpty()) {
            if (enemies.contains(coordinate)) {
                validateTarget.add(coordinate);
            }
        }
        return validateTarget;
    }

    /**
     * Проверяем всю линию на наличие юнитов в армии противника
     */
    private static Set<SquareCoordinate> getTargetForCentralUnit(final Set<SquareCoordinate> enemies,
                                                                 final int line) {
        final Set<SquareCoordinate> validateTarget = new HashSet<>();
        for (int x = 0; x < 3; x++) {
            final SquareCoordinate coordinate = getSquareCoordinate(x, line);
            if (enemies.contains(coordinate)) {
                validateTarget.add(coordinate);
            }
        }
        return validateTarget;
    }

    public static @NotNull Set<SquareCoordinate> getCorrectTargetForFootman(
            final @NotNull SquareCoordinate activeUnit,
            final @NotNull boolean[] enemy) {
        final Set<SquareCoordinate> validateTarget = new HashSet<>();
        for (int y = 1; y >= 0; y--) {
            if (activeUnit.getX() == 1) {
                validateTarget.addAll(getTargetForCentralUnit(enemy, y));
            } else {
                validateTarget.addAll(getTargetForFlankUnit(activeUnit.getX(), enemy, y));
            }
            if (!validateTarget.isEmpty()) {
                break;
            }
        }
        return validateTarget;
    }

    private static Set<SquareCoordinate> getTargetForFlankUnit(final int activeUnitX, final boolean[] enemies,
                                                               final int y) {
        final Set<SquareCoordinate> validateTarget = new HashSet<>();
        if (enemies[1 + 3 * y]) {
            validateTarget.add(getSquareCoordinate(1, y));
        }
        if (enemies[activeUnitX + 3 * y]) {
            validateTarget.add(getSquareCoordinate(activeUnitX, y));
        }
        if (validateTarget.isEmpty()) {
            final int x = activeUnitX == 2 ? 0 : 2;
            if (enemies[x + 3 * y]) {
                validateTarget.add(getSquareCoordinate(x, y));
            }
        }
        return validateTarget;
    }

    /**
     * Проверяем всю линию на наличие юнитов в армии противника
     */
    private static Set<SquareCoordinate> getTargetForCentralUnit(final boolean[] enemies, final int line) {
        final Set<SquareCoordinate> validateTarget = new HashSet<>();
        for (int x = 0; x < 3; x++) {
            if (enemies[x + 3 * line]) {
                validateTarget.add(getSquareCoordinate(x, line));
            }
        }
        return validateTarget;
    }

}
