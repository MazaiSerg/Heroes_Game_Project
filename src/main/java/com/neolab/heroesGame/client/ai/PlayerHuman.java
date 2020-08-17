package com.neolab.heroesGame.client.ai;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.client.gui.IGraphics;
import com.neolab.heroesGame.client.gui.console.AsciiGraphics;
import com.neolab.heroesGame.enumerations.HeroActions;
import com.neolab.heroesGame.heroes.*;
import com.neolab.heroesGame.server.answers.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static com.neolab.heroesGame.client.ai.enums.BotType.RANDOM;

public class PlayerHuman extends Player {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerHuman.class);
    private final IGraphics gui;
    private final SquareCoordinate coordinateDoesntMatters = new SquareCoordinate(-1, -1);

    public PlayerHuman(final int id, final String name, final IGraphics gui) {
        super(id, name);
        this.gui = gui;
    }

    public PlayerHuman(final int id) throws IOException {
        this(id, "Serg", new AsciiGraphics(id));
    }

    /**
     * Выбираем героя в функции chooseUnit
     * Выбираем действие, если null возвращаемся к выбору героя
     * Выбираем цели: для мага и защиты цель не выбирается. Если null возвращаемся к выбору героя
     *
     * @param board принимаем текущую ситуацию на игровом поле
     * @return возвращаем Answer, который определяет действие игрока в текущем ходу
     */
    @Override
    public Answer getAnswer(final BattleArena board) throws IOException {

        final Army yourArmy = board.getArmy(getId());
        final Army enemyArmy = board.getEnemyArmy(getId());
        while (true) {
            final SquareCoordinate activeHeroCoordinate = gui.chooseUnit(yourArmy);
            final Hero activeHero = yourArmy.getHero(activeHeroCoordinate).orElseThrow();

            final HeroActions action = gui.chooseActionForHero(activeHeroCoordinate, activeHero);
            if (action == null) {
                continue;
            }

            final SquareCoordinate targetUnitCoordinate =
                    (action == HeroActions.DEFENCE || CommonFunction.isUnitMagician(activeHero)) ?
                            coordinateDoesntMatters :
                            gui.chooseTargetCoordinate(activeHeroCoordinate, activeHero,
                                    CommonFunction.isUnitHealer(activeHero) ? yourArmy : enemyArmy);

            if (targetUnitCoordinate == null) {
                continue;
            }

            return new Answer(activeHeroCoordinate, action, targetUnitCoordinate, getId());
        }
    }

    @Override
    public String getStringArmyFirst(final int armySize) throws IOException {
        final Map<Integer, Hero> army = new HashMap<>();
        final Stack<Integer> lastChoose = new Stack<>();
        while (true) {
            final Hero hero;
            hero = gui.getHeroChoose(army);
            if (hero == null) {
                army.remove(lastChoose.pop());
                LOGGER.trace("Unit - null");
                continue;
            }
            LOGGER.trace("Unit - {}", hero.getClassName());
            final int position = gui.getHeroPositionChoose(hero, army);
            LOGGER.trace("Unit  position- {}", position);
            if (position != -1) {
                army.put(position, hero);
                lastChoose.push(position);
            }
            if (army.size() == armySize) {
                if (gui.finishCreatingArmy(army)) {
                    break;
                }
                army.remove(lastChoose.pop());
            }
        }
        return makeStringArmiesFromMap(army);
    }

    @Override
    public String getStringArmySecond(final int armySize, final Army enemy) throws IOException {
        final Map<Integer, Hero> army = new HashMap<>();
        final Stack<Integer> lastChoose = new Stack<>();
        while (true) {
            final Hero hero;
            hero = gui.getHeroChoose(enemy, army);
            if (hero == null) {
                army.remove(lastChoose.pop());
                LOGGER.trace("Unit - null");
                continue;
            }
            LOGGER.trace("Unit - {}", hero.getClassName());
            final int position = gui.getHeroPositionChoose(hero, enemy, army);
            LOGGER.trace("Unit  position- {}", position);
            if (position != -1) {
                army.put(position, hero);
                lastChoose.push(position);
            }
            if (army.size() == armySize) {
                if (gui.finishCreatingArmy(enemy, army)) {
                    break;
                }
                army.remove(lastChoose.pop());
            }
        }
        return makeStringArmiesFromMap(army);
    }


    @Override
    public String getType() {
        return RANDOM.toString();
    }

    private String makeStringArmiesFromMap(final Map<Integer, Hero> army) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            stringBuilder.append(classCodeToString(army.get(i)));
        }
        return stringBuilder.toString();
    }

    private String classCodeToString(final Hero hero) {
        if (hero == null) {
            return String.valueOf(CommonFunction.EMPTY_UNIT);
        }
        final String result;
        if (hero.getClass() == Magician.class) {
            result = "m";
        } else if (hero instanceof WarlordMagician) {
            result = "M";
        } else if (hero instanceof WarlordVampire) {
            result = "V";
        } else if (hero instanceof Archer) {
            result = "a";
        } else if (hero instanceof Healer) {
            result = "h";
        } else if (hero.getClass() == Footman.class) {
            result = "f";
        } else if (hero instanceof WarlordFootman) {
            result = "F";
        } else {
            result = "u";
        }
        return result;
    }

}
