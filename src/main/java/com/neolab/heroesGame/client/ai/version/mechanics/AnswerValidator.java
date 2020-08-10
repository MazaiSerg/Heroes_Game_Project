package com.neolab.heroesGame.client.ai.version.mechanics;

import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Army;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.BattleArena;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Archer;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Healer;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Hero;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Magician;
import com.neolab.heroesGame.enumerations.HeroActions;
import com.neolab.heroesGame.enumerations.HeroErrorCode;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.server.answers.Answer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class AnswerValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnswerValidator.class);

    public static boolean isAnswerValidate(final Answer answer, final BattleArena arena) throws HeroExceptions {
        final Army thisBotArmy = arena.getArmy(answer.getPlayerId());
        final Army enemyArmy = arena.getEnemyArmy(answer.getPlayerId());
        final Hero hero = thisBotArmy.getHero(answer.getActiveHeroCoordinate())
                .orElseThrow(() -> {
                    LOGGER.error(arena.toString());
                    LOGGER.error(answer.toString());
                    return new HeroExceptions(HeroErrorCode.ERROR_ACTIVE_UNIT);
                });

        if (isErrorActiveHero(hero, thisBotArmy)) {
            throw new HeroExceptions(HeroErrorCode.ERROR_ACTIVE_UNIT);
        }
        if (answer.getAction() == HeroActions.DEFENCE) {
            return true;
        }

        if (isHealerCorrect(hero, answer, thisBotArmy)) {
            return true;
        }

        if (answer.getAction() == HeroActions.HEAL) {
            throw new HeroExceptions(HeroErrorCode.ERROR_UNIT_HEAL);
        }
        if (hero instanceof Magician) {
            return true;
        }
        if (hero instanceof Archer) {
            if (enemyArmy.getHero(answer.getTargetUnitCoordinate()).isEmpty()) {
                throw new HeroExceptions(HeroErrorCode.ERROR_TARGET_ATTACK);
            }
            return true;
        }

        footmanTargetCheck(answer.getActiveHeroCoordinate(), answer.getTargetUnitCoordinate(), enemyArmy);
        return true;
    }

    private static void footmanTargetCheck(final SquareCoordinate activeUnit, final SquareCoordinate target, final Army army) throws HeroExceptions {
        final Set<SquareCoordinate> validateTarget = getCorrectTargetForFootman(activeUnit, army);
        if (validateTarget.isEmpty()) {
            throw new HeroExceptions(HeroErrorCode.ERROR_ON_BATTLE_ARENA);
        }
        if (!validateTarget.contains(target)) {
            throw new HeroExceptions(HeroErrorCode.ERROR_TARGET_ATTACK);
        }
    }

    private static boolean isErrorActiveHero(final Hero hero, final Army thisBotArmy) {
        return !thisBotArmy.getAvailableHeroes().containsValue(hero);
    }

    private static boolean isHealerCorrect(final Hero hero, final Answer answer, final Army thisBotArmy) throws HeroExceptions {
        if (hero instanceof Healer) {
            if (answer.getAction() == HeroActions.ATTACK) {
                throw new HeroExceptions(HeroErrorCode.ERROR_UNIT_ATTACK);
            }
            if (thisBotArmy.getHero(answer.getTargetUnitCoordinate()).isEmpty()) {
                throw new HeroExceptions(HeroErrorCode.ERROR_TARGET_HEAL);
            }
            return true;
        }
        return false;
    }

    public static @NotNull Set<SquareCoordinate> getCorrectTargetForFootman(final @NotNull SquareCoordinate activeUnit,
                                                                            final @NotNull Army enemyArmy) {
        final Set<SquareCoordinate> validateTarget = new HashSet<>();
        for (int y = 1; y >= 0; y--) {
            if (activeUnit.getX() == 1) {
                validateTarget.addAll(getTargetForCentralUnit(enemyArmy, y));
            } else {
                validateTarget.addAll(getTargetForFlankUnit(activeUnit.getX(), enemyArmy, y));
            }
            if (!validateTarget.isEmpty()) {
                break;
            }
        }
        return validateTarget;
    }


    private static Set<SquareCoordinate> getTargetForFlankUnit(final int activeUnitX, final Army enemyArmy, final int y) {
        final Set<SquareCoordinate> validateTarget = new HashSet<>();
        if (enemyArmy.getHero(new SquareCoordinate(1, y)).isPresent()) {
            validateTarget.add(new SquareCoordinate(1, y));
        }
        if (enemyArmy.getHero(new SquareCoordinate(activeUnitX, y)).isPresent()) {
            validateTarget.add(new SquareCoordinate(activeUnitX, y));
        }
        if (validateTarget.isEmpty()) {
            final int x = activeUnitX == 2 ? 0 : 2;
            if (enemyArmy.getHero(new SquareCoordinate(x, y)).isPresent()) {
                validateTarget.add(new SquareCoordinate(x, y));
            }
        }
        return validateTarget;
    }

    /**
     * Проверяем всю линию на наличие юнитов в армии противника
     */
    private static Set<SquareCoordinate> getTargetForCentralUnit(final Army enemyArmy, final Integer line) {
        final Set<SquareCoordinate> validateTarget = new HashSet<>();
        for (int x = 0; x < 3; x++) {
            final SquareCoordinate coordinate = new SquareCoordinate(x, line);
            final Optional<Hero> hero = enemyArmy.getHero(coordinate);
            if (hero.isPresent()) {
                validateTarget.add(coordinate);
            }
        }
        return validateTarget;
    }

}
