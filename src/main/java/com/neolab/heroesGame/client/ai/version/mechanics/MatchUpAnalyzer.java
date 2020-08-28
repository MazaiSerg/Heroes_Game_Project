package com.neolab.heroesGame.client.ai.version.mechanics;

import com.neolab.heroesGame.aditional.StatisticReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MatchUpAnalyzer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatchUpAnalyzer.class);
    private final Map<String, Map<String, Double>> armiesPriority;
    private final Map<String, Double> bestArmies;

    private MatchUpAnalyzer(final Map<String, Map<String, Double>> armiesPriority, final Map<String, Double> bestArmies) {
        this.armiesPriority = armiesPriority;
        this.bestArmies = bestArmies;
    }

    public static MatchUpAnalyzer createMatchUpAnalyzer() {
        final List<String[]> dataFromFile = StatisticReader.readMatchUpsStatistic();
        final Map<String, Map<String, Double>> armiesPriority = convertArrayToMap(dataFromFile);
        final Map<String, Double> bestArmies = sortArmies(armiesPriority);
        return new MatchUpAnalyzer(armiesPriority, bestArmies);
    }

    public String getBestArmy() {
        String result = getDefaultArmy(4);
        for (String key : bestArmies.keySet()) {
            if (bestArmies.get(key) > bestArmies.get(result)) {
                result = key;
            }
        }
        return result;
    }

    public String getOneOfBestArmy() {
        final List<String> armies = new ArrayList<>(bestArmies.size());
        final List<Double> priorities = new ArrayList<>(bestArmies.size());
        double adding = 0;
        for (final String key : bestArmies.keySet()) {
            adding += bestArmies.get(key);
            priorities.add(adding);
            armies.add(key);
        }
        final double random = new Random().nextDouble() * adding;
        for (int i = 0; i < priorities.size(); i++) {
            if (priorities.get(i) > random) {
                return armies.get(i);
            }
        }
        LOGGER.error("Что-то пошло не так");
        return getBestArmy();
    }

    public String getBestResponse(final String enemy) {
        String convertedEnemy = ConverterForFourSizeArmy.convertToCluster(enemy);
        String result = getDefaultArmy(4);
        for (String key : armiesPriority.get(convertedEnemy).keySet()) {
            if (armiesPriority.get(convertedEnemy).get(key) < armiesPriority.get(convertedEnemy).get(result)) {
                result = key;
            }
        }
        return result;
    }

    public String getDefaultArmy(final int armySize) {
        return switch (armySize) {
            case 1 -> "    F ";
            case 2 -> " m  F ";
            case 3 -> " m f F";
            case 4 -> "mmm F ";
            case 5 -> "mmmF f";
            case 6 -> "Mmmfff";
            default -> throw new IllegalStateException("Unexpected value: " + armySize);
        };
    }

    private static Map<String, Double> sortArmies(final Map<String, Map<String, Double>> armiesPriority) {
        final Map<String, Double> expectedResults = new HashMap<>();
        for (final String key : armiesPriority.keySet()) {
            double expectedResult = 0;
            final List<Double> values = new ArrayList<>();
            for (final String secondKey : armiesPriority.get(key).keySet()) {
                values.add(armiesPriority.get(key).get(secondKey));
            }
            values.sort(Double::compareTo);
            for (int i = 0; i < 10; i++) {
                final double value = values.get(i) - 50;
                expectedResult += value < 0 ? -0.1 * value * value : 0.1 * value;
            }
            if (expectedResult > 0) {
                expectedResults.put(key, expectedResult);
            }
        }
        return expectedResults;
    }

    private static Map<String, Map<String, Double>> convertArrayToMap(final List<String[]> dataFromFile) {
        final List<String> armies = new ArrayList<>(dataFromFile.get(0).length - 1);
        armies.addAll(Arrays.asList(dataFromFile.get(0)).subList(1, dataFromFile.get(0).length));
        final Map<String, Map<String, Double>> result = new HashMap<>(armies.size());
        for (int i = 1; i < dataFromFile.size(); i++) {
            final String army = dataFromFile.get(i)[0];
            final Map<String, Double> matchUps = new HashMap<>();
            for (int j = 1; j < dataFromFile.get(i).length; j++) {
                matchUps.put(armies.get(j - 1), Double.parseDouble(dataFromFile.get(i)[j]));
            }
            result.put(army, matchUps);
        }
        return result;
    }
}
