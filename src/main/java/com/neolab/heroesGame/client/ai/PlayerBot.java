package com.neolab.heroesGame.client.ai;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.enumerations.HeroActions;
import com.neolab.heroesGame.enumerations.HeroErrorCode;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.heroes.Hero;
import com.neolab.heroesGame.server.answers.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PlayerBot extends Player {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerBot.class);
    private final long SEED = 5916;
    private final Random RANDOM = new Random(SEED);

    public PlayerBot(final int id, final String name) {
        super(id, name);
    }

    public PlayerBot(int i) {
        super(i, "Random_Bot");
    }

    /**
     * бот работает по следующим правилам:
     * 1. Действующий юнит выбирается случайно
     * 2. Маги и лучники всегда атакуют
     * 3. Все выбирают цель из доступных случайно
     * 4. Хилер защищается только если у всех союзников фул хп
     * 5. Если у нескольких союзников не полные хп, то цель выбирается случайно
     * 5. Файтер с 10% шансов встают в защиту.
     * 6. Для якобы случайного выбора используется Set.iterator.next()
     *
     * @param board принимаем текущую ситуацию на игровом поле
     * @return возвращаем Answer, который определяет действие игрока в текущем ходу
     * @throws HeroExceptions текущее исключение вообще не должно выбрасываться, но кто его знает
     */
    @Override
    public Answer getAnswer(final BattleArena board) throws HeroExceptions {

        final Army thisBotArmy = board.getArmy(getId());
        final Army enemyArmy = board.getEnemyArmy(getId());
        final SquareCoordinate activeHero = chooseUnit(thisBotArmy);
        final Hero hero = thisBotArmy.getHeroes().get(activeHero);

        if (CommonFunction.isUnitMagician(hero)) {
            return new Answer(activeHero, HeroActions.ATTACK, new SquareCoordinate(-1, -1), getId());
        }

        if (CommonFunction.isUnitArcher(hero)) {
            return new Answer(activeHero, HeroActions.ATTACK, chooseTargetByArcher(enemyArmy), getId());
        }

        final SquareCoordinate targetUnit;
        final HeroActions action;
        if (CommonFunction.isUnitHealer(hero)) {
            targetUnit = chooseTargetByHealer(thisBotArmy);
            action = HeroActions.HEAL;
        } else {
            targetUnit = chooseTargetByFootman(activeHero, enemyArmy);
            action = HeroActions.ATTACK;
        }

        if (targetUnit == null) {
            return new Answer(activeHero, HeroActions.DEFENCE, new SquareCoordinate(-1, -1), getId());
        }

        return new Answer(activeHero, action, targetUnit, getId());
    }

    public String getStringArmyFirst(final int armySize) {
        final List<String> armies = CommonFunction.getAllAvailableArmiesCode(armySize);
        return armies.get(RANDOM.nextInt(armies.size()));
    }

    public String getStringArmySecond(final int armySize, final Army army) {
        return getStringArmyFirst(armySize);
    }

    protected SquareCoordinate chooseUnit(final Army army) {
        final Set<SquareCoordinate> availableHeroes = army.getAvailableHeroes().keySet();
        return availableHeroes.iterator().next();
    }

    protected SquareCoordinate chooseTargetByArcher(final Army enemyArmy) {
        final Set<SquareCoordinate> availableHeroes = enemyArmy.getHeroes().keySet();
        return availableHeroes.iterator().next();
    }

    /**
     * Ищем раненых юнитов. Если таких нет возвразаем Null
     */
    protected SquareCoordinate chooseTargetByHealer(final Army army) {
        final Map<SquareCoordinate, Hero> heroes = army.getHeroes();
        final HashSet<SquareCoordinate> damagedHeroes = new HashSet<>();
        for (final SquareCoordinate key : heroes.keySet()) {
            final Optional<Hero> temp = army.getHero(key);
            if (temp.isPresent() && (temp.get().getHp() < temp.get().getHpMax())) {
                damagedHeroes.add(key);
            }
        }
        if (damagedHeroes.isEmpty()) {
            return null;
        }
        return damagedHeroes.iterator().next();
    }

    /**
     * Ищем легальную цель для милишника
     *
     * @throws HeroExceptions Цель должна существовать, иначе армия противника пустая и бой должен был завершиться.
     */
    protected SquareCoordinate chooseTargetByFootman(final SquareCoordinate activeUnit, final Army enemyArmy) throws HeroExceptions {
        if (RANDOM.nextInt(10) == 0) {
            return null;
        }
        final Set<SquareCoordinate> validateTarget = CommonFunction.getCorrectTargetForFootman(activeUnit, enemyArmy);
        if (validateTarget.isEmpty()) {
            LOGGER.error(String.format("Юнит (%d,%d) не может найти цель в армии:\n%s",
                    activeUnit.getX(), activeUnit.getY(), CommonFunction.printArmy(enemyArmy)));
            throw new HeroExceptions(HeroErrorCode.ERROR_ON_BATTLE_ARENA);
        }
        return validateTarget.iterator().next();
    }
}
