package com.neolab.heroesGame.aditional;

import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.heroes.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CommonFunction {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonFunction.class);

    final static public char EMPTY_UNIT = ' ';
    final static private String cleanLine = "            |";

    public static boolean isUnitMagician(final Hero hero) {
        return hero instanceof Magician;
    }

    public static boolean isUnitArcher(final Hero hero) {
        return hero instanceof Archer;
    }

    public static boolean isUnitHealer(final Hero hero) {
        return hero instanceof Healer;
    }

    /**
     * Ищем корректные цели для милишников. В цикле проверяем по рядам наличие целей. Если в первом ряду есть юниты,
     * то задний ряд не проверяем. Сперва смотрим это центральный юнит или фланговый, вызываем соответствующие функции
     * для соответствующего случая
     */
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

    /**
     * Сперва проверяем, наличие центрального юнита в армии врага, потом юнита на том же фланге. Если юнитов в армии
     * противника все еще не встретилось, то проверяем второй фланг
     */
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

    public static String printArmy(final Army army) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("________________________________________\n");
        for (int y = 0; y < 2; y++) {
            stringBuilder.append(getLineUnit(army, y));
            stringBuilder.append("|____________|____________|____________|\n");
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    /**
     * Формируем 3 строки - первая с названием класса, вторая с текущим/маскимальным хп, третья со статусом действия
     */
    private static String getLineUnit(final Army army, final int y) {
        final StringBuilder stringBuilder = new StringBuilder();
        final Map<Integer, Optional<Hero>> heroes = new HashMap<>();
        for (int x = 0; x < 3; x++) {
            heroes.put(x, army.getHero(new SquareCoordinate(x, y)));
        }
        stringBuilder.append("|");
        for (int x = 0; x < 3; x++) {
            stringBuilder.append(heroes.get(x).isPresent() ? classToString(heroes.get(x).get()) : cleanLine);
        }
        stringBuilder.append("\n|");
        for (int x = 0; x < 3; x++) {
            stringBuilder.append(heroes.get(x).isPresent() ? hpToString(heroes.get(x).get()) : cleanLine);
        }
        stringBuilder.append("\n|");
        for (int x = 0; x < 3; x++) {
            stringBuilder.append(heroes.get(x).isPresent() ? statusToString(heroes.get(x).get(), army) : cleanLine);
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    private static String statusToString(final Hero hero, final Army army) {
        final StringBuilder result = new StringBuilder();
        if (hero.isDefence()) {
            result.append("   D  ");
        } else {
            result.append("      ");
        }
        if (army.getAvailableHeroes().containsValue(hero)) {
            result.append("  CA  |");
        } else {
            result.append("   W  |");
        }
        return result.toString();
    }

    private static String hpToString(final Hero hero) {
        return String.format("  HP%3d/%3d |", hero.getHp(), hero.getHpMax());
    }

    private static String classToString(final Hero hero) {
        return String.format("%12s|", hero.getClassName());
    }

    public static List<String> getAllAvailableArmiesCode(final int armySize) {
        final char[] string = new char[6];
        final List<String> results = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            string[i] = EMPTY_UNIT;
        }
        createAllString(string, results, armySize, 0);
        return results;
    }

    private static void createAllString(final char[] currentString, final List<String> results,
                                        final int armySize, final int currentPositions) {
        //LOGGER.trace("{} {}", currentString, currentPositions);
        if (full(currentString, armySize) == 0) {
            //LOGGER.trace("{}  записываем строку", currentString);
            results.add(new String(currentString));
            return;
        }
        if (empty(currentString)) {
            addWarLord(currentString, results, armySize);
            return;
        }
        if (currentPositions < 3) {
            if (currentString[currentPositions] == EMPTY_UNIT) {
                currentString[currentPositions] = 'a';
                createAllString(currentString, results, armySize, currentPositions + 1);
                currentString[currentPositions] = 'h';
                createAllString(currentString, results, armySize, currentPositions + 1);
                currentString[currentPositions] = 'm';
                createAllString(currentString, results, armySize, currentPositions + 1);
                currentString[currentPositions] = EMPTY_UNIT;
            }
            createAllString(currentString, results, armySize, currentPositions + 1);
        } else {
            if (currentPositions < 6) {
                if (currentString[currentPositions] == EMPTY_UNIT) {
                    currentString[currentPositions] = 'f';
                    createAllString(currentString, results, armySize, currentPositions + 1);
                    currentString[currentPositions] = EMPTY_UNIT;
                }
                createAllString(currentString, results, armySize, currentPositions + 1);
            }
        }
    }

    public static String ArmyCodeToString(Army army) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 3; x++) {
                Optional<Hero> hero = army.getHero(new SquareCoordinate(x, y));
                stringBuilder.append(hero.map(CommonFunction::classCodeToString).orElse(" "));
            }
        }
        return stringBuilder.toString();
    }

    private static String classCodeToString(final Hero hero) {
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

    private static void addWarLord(final char[] currentString, final List<String> results, final int armySize) {
        for (int i = 0; i < 3; i++) {
            currentString[i] = 'M';
            createAllString(currentString, results, armySize, 0);
            currentString[i] = 'V';
            createAllString(currentString, results, armySize, 0);
            currentString[i] = EMPTY_UNIT;
        }
        for (int i = 3; i < 6; i++) {
            currentString[i] = 'F';
            createAllString(currentString, results, armySize, 0);
            currentString[i] = EMPTY_UNIT;
        }
    }

    private static boolean empty(final char[] currentString) {
        for (int i = 0; i < 6; i++) {
            if (currentString[i] != EMPTY_UNIT) {
                return false;
            }
        }
        return true;
    }

    private static int full(final char[] currentString, final int armySize) {
        int counter = 0;
        for (int i = 0; i < 6; i++) {
            if (currentString[i] != EMPTY_UNIT) {
                counter++;
            }
        }
        return counter - armySize;
    }

}
