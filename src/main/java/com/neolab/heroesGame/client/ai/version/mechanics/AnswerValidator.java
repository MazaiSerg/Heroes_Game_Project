package com.neolab.heroesGame.client.ai.version.mechanics;

import com.neolab.heroesGame.client.ai.version.mechanics.arena.Army;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.SquareCoordinate;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static com.neolab.heroesGame.client.ai.version.mechanics.arena.SquareCoordinate.getSquareCoordinate;

public class AnswerValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnswerValidator.class);

    public static @NotNull Set<SquareCoordinate> getCorrectTargetForFootman(final @NotNull SquareCoordinate activeUnit,
                                                                            final @NotNull Army enemyArmy) {
        final Set<SquareCoordinate> validateTarget = new HashSet<>();
        final Set<SquareCoordinate> enemies = enemyArmy.getHeroes().keySet();
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

    private static Set<SquareCoordinate> getTargetForFlankUnit(final int activeUnitX, final Set<SquareCoordinate> enemies, final int y) {
        final Set<SquareCoordinate> validateTarget = new HashSet<>();
        if (enemies.contains(getSquareCoordinate(1, y))) {
            validateTarget.add(getSquareCoordinate(1, y));
        }
        if (enemies.contains(getSquareCoordinate(activeUnitX, y))) {
            validateTarget.add(getSquareCoordinate(activeUnitX, y));
        }
        if (validateTarget.isEmpty()) {
            final int x = activeUnitX == 2 ? 0 : 2;
            if (enemies.contains(getSquareCoordinate(x, y))) {
                validateTarget.add(getSquareCoordinate(x, y));
            }
        }
        return validateTarget;
    }

    /**
     * Проверяем всю линию на наличие юнитов в армии противника
     */
    private static Set<SquareCoordinate> getTargetForCentralUnit(final Set<SquareCoordinate> enemies, final Integer line) {
        final Set<SquareCoordinate> validateTarget = new HashSet<>();
        for (int x = 0; x < 3; x++) {
            final SquareCoordinate coordinate = getSquareCoordinate(x, line);
            if (enemies.contains(coordinate)) {
                validateTarget.add(coordinate);
            }
        }
        return validateTarget;
    }

}
