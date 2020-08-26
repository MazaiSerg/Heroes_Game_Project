package com.neolab.heroesGame.validators;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StringArmyValidator {
    final static private char emptyUnit = ' ';

    private StringArmyValidator() {
    }

    public static boolean validateArmyString(final String army, final int armySize) {
        char[] characters = new char[6];
        army.getChars(0, 6, characters, 0);
        return !(containWrongCharacter(characters)
                || full(characters, armySize) > 0
                || containNotOneWarlord(characters));
    }

    private static boolean containNotOneWarlord(char[] characters) {
        Set<Character> warlordCharacters = new HashSet<>(Arrays.asList('F', 'M', 'V'));
        boolean alreadyMet = false;
        for (int i = 0; i < 6; i++) {
            if (warlordCharacters.contains(characters[i])) {
                if (alreadyMet) {
                    return true;
                }
                alreadyMet = true;
            }
        }
        return !alreadyMet;
    }

    private static boolean containWrongCharacter(char[] army) {
        Set<Character> correctCharactersBackLine = new HashSet<>(Arrays.asList('a', 'm', 'h', 'M', 'V', emptyUnit));
        Set<Character> correctCharactersFrontLine = new HashSet<>(Arrays.asList('f', 'F', emptyUnit));
        for (int i = 0; i < 3; i++) {
            if (!correctCharactersBackLine.contains(army[i])) {
                return true;
            }
        }
        for (int i = 3; i < 6; i++) {
            if (!correctCharactersFrontLine.contains(army[i])) {
                return true;
            }
        }
        return false;
    }

    private static int full(final char[] currentString, final int armySize) {
        int counter = 0;
        for (int i = 0; i < 6; i++) {
            if (currentString[i] != emptyUnit) {
                counter++;
            }
        }
        return counter - armySize;
    }
}
