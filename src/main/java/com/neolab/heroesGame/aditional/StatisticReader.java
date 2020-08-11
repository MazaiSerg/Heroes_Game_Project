package com.neolab.heroesGame.aditional;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class StatisticReader {
    static public final Integer ARMY_SIZE;
    public static final String PLAYER_STATISTIC_FILE_PATH;
    public static final String ARMY_STATISTIC_FILE_PATH;

    static {
        Properties prop = null;
        try {
            prop = HeroConfigManager.getHeroConfig();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        ARMY_SIZE = PropertyUtils.getIntegerFromProperty(prop, "hero.army.size");
        PLAYER_STATISTIC_FILE_PATH = String.format("src/main/resources/playerStatistic%d.csv", ARMY_SIZE);
        ARMY_STATISTIC_FILE_PATH = "src/main/resources/armyStatistic.csv";
    }

    public static Optional<List<String[]>> readArmiesWinStatistic() {
        final File csvInputFile = new File(ARMY_STATISTIC_FILE_PATH);
        if (!csvInputFile.exists()) {
            return Optional.empty();
        }
        List<String[]> result = Collections.emptyList();
        try (final CSVReader reader = new CSVReader(new FileReader(csvInputFile))) {
            result = reader.readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(result);
    }

    public static Optional<List<String[]>> readPlayersWinStatistic() {
        final File csvInputFile = new File(PLAYER_STATISTIC_FILE_PATH);
        if (!csvInputFile.exists()) {
            return Optional.empty();
        }
        List<String[]> result = Collections.emptyList();
        try (final CSVReader reader = new CSVReader(new BufferedReader(new FileReader(csvInputFile)))) {
            result = reader.readAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(result);
    }
}
