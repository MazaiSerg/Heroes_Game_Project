package com.neolab.heroesGame.client.ai.version.mechanics;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.aditional.StatisticWriter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.neolab.heroesGame.aditional.CommonFunction.EMPTY_UNIT;

public class ConverterForFourSizeArmy {
    private static final List<String> armies = CommonFunction.getAllAvailableArmiesCode(StatisticWriter.ARMY_SIZE);

    public static String convertToCluster(final String army) {
        return switch (decide(army)) {
            case 1 -> transformFirstType(army);
            case 2 -> transformSecondType(army);
            case 3 -> transformThirdType(army);
            default -> throw new IllegalStateException("Unexpected value: " + decide(army));
        };
    }

    public static int decide(final String code) {
        final char[] codes = code.toCharArray();
        int counter = 0;
        for (int i = 0; i < 3; i++) {
            if (codes[i] != EMPTY_UNIT) {
                counter++;
            }
        }
        return counter;
    }

    public static String setDispositionSecondTypeVersusFirstType(final String code) {
        final char[] codes = transformSecondType(code).toCharArray();
        for (int line = 0; line < 2; line++) {
            codes[line * 3] = codes[line * 3] == EMPTY_UNIT ? codes[2 + line * 3] : codes[line * 3];
            codes[1 + line * 3] = codes[1 + line * 3] == EMPTY_UNIT ? codes[2 + line * 3] : codes[1 + line * 3];
            codes[2 + line * 3] = EMPTY_UNIT;
        }
        return String.valueOf(codes);
    }

    public static String setDispositionSecondTypeVersusThirdType(final String code) {
        final char[] codes = transformSecondType(code).toCharArray();
        for (int line = 0; line < 2; line++) {
            codes[line * 3] = codes[line * 3] == EMPTY_UNIT ? codes[1 + line * 3] : codes[line * 3];
            codes[2 + line * 3] = codes[2 + line * 3] == EMPTY_UNIT ? codes[1 + line * 3] : codes[2 + line * 3];
            codes[1 + line * 3] = EMPTY_UNIT;
        }
        return String.valueOf(codes);
    }

    public static Set<String> setDispositionSecondTypeVersusFirstType(final Set<String> codes) {
        final Set<String> results = new HashSet<>();
        for (String code : codes) {
            results.add(setDispositionSecondTypeVersusFirstType(code));
        }
        return results;
    }

    public static Set<String> setDispositionSecondTypeVersusThirdType(final Set<String> codes) {
        final Set<String> results = new HashSet<>();
        for (String code : codes) {
            results.add(setDispositionSecondTypeVersusThirdType(code));
        }
        return results;
    }

    public static Set<String> getAllArmiesFromCluster(final String cluster) {
        Set<String> result = new HashSet<>();
        for (String army : armies) {
            String temp = convertToCluster(army);
            if (temp.equals(cluster)) {
                result.add(army);
            }
        }
        return result;
    }

    private static String sortLine(final String code, final int line) {
        final char[] codes = code.toCharArray();
        for (int i = line * 3; i < 2 + line * 3; i++) {
            for (int j = i + 1; j < 3 * (line + 1); j++) {
                if (codes[i] > codes[j]) {
                    final char temp = codes[j];
                    codes[j] = codes[i];
                    codes[i] = temp;
                }
            }
        }
        return String.valueOf(codes);
    }

    public static String transformThirdType(final String code) {
        final char[] codes = code.toCharArray();
        char temp = EMPTY_UNIT;
        for (int i = 3; i < 6; i++) {
            if (codes[i] != EMPTY_UNIT) {
                temp = codes[i];
                codes[i] = EMPTY_UNIT;
            }
        }
        codes[4] = temp;
        return sortLine(String.valueOf(codes), 0);
    }

    public static String transformSecondType(final String code) {
        return sortLine(sortLine(code, 0), 1);
    }

    public static String transformFirstType(final String code) {
        final char[] codes = code.toCharArray();
        char temp = EMPTY_UNIT;
        for (int i = 0; i < 3; i++) {
            if (codes[i] != EMPTY_UNIT) {
                temp = codes[i];
                codes[i] = EMPTY_UNIT;
            }
        }
        codes[1] = temp;
        return String.valueOf(codes);
    }
}
