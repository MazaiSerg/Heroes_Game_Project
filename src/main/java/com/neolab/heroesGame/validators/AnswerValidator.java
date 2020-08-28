package com.neolab.heroesGame.validators;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.enumerations.HeroActions;
import com.neolab.heroesGame.enumerations.HeroErrorCode;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.heroes.Hero;
import com.neolab.heroesGame.server.answers.Answer;

import java.util.Optional;
import java.util.Set;

public class AnswerValidator {

    public static boolean isAnswerValidate(final Answer answer, final BattleArena arena) throws HeroExceptions {
        final Army thisBotArmy = arena.getArmy(answer.getPlayerId());
        final Army enemyArmy = arena.getEnemyArmy(answer.getPlayerId());
        final Optional<Hero> heroOptional = thisBotArmy.getHero(answer.getActiveHeroCoordinate());
        final Hero hero;
        if (heroOptional.isPresent()) {
            hero = heroOptional.get();
        } else throw new HeroExceptions(HeroErrorCode.ERROR_ACTIVE_UNIT);

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
        if (CommonFunction.isUnitMagician(hero)) {
            return true;
        }
        if (CommonFunction.isUnitArcher(hero)) {
            if (enemyArmy.getHero(answer.getTargetUnitCoordinate()).isEmpty()) {
                throw new HeroExceptions(HeroErrorCode.ERROR_TARGET_ATTACK);
            }
            return true;
        }

        footmanTargetCheck(answer.getActiveHeroCoordinate(), answer.getTargetUnitCoordinate(), enemyArmy);
        return true;
    }

    private static void footmanTargetCheck(final SquareCoordinate activeUnit, final SquareCoordinate target, final Army army) throws HeroExceptions {
        final Set<SquareCoordinate> validateTarget = CommonFunction.getCorrectTargetForFootman(activeUnit, army);
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
        if (CommonFunction.isUnitHealer(hero)) {
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
}
