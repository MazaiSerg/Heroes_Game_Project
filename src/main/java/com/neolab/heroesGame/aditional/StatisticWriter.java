package com.neolab.heroesGame.aditional;

import com.neolab.heroesGame.enumerations.GameEvent;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public final class StatisticWriter {

    private StatisticWriter() throws Exception {
        throw new Exception();
    }

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
        if (Files.exists(Paths.get("src/main/resources/"))) {
            PLAYER_STATISTIC_FILE_PATH = String.format("src/main/resources/playerStatistic%d.csv", ARMY_SIZE);
            ARMY_STATISTIC_FILE_PATH = "src/main/resources/armyStatistic.csv";
        } else {
            PLAYER_STATISTIC_FILE_PATH = String.format("resources/playerStatistic%d.csv", ARMY_SIZE);
            ARMY_STATISTIC_FILE_PATH = "resources/armyStatistic.csv";
        }
    }

    public static void writePlayerWinStatistic(final String winnerPlayerName, final String looserPlayerName) throws IOException {
        final File csvOutputFile = new File(PLAYER_STATISTIC_FILE_PATH);
        if (!csvOutputFile.exists()) {
            initPlayerStatistic();
        }
        final String[] data = {winnerPlayerName, looserPlayerName, "1.0", "0.0"};
        write(data, PLAYER_STATISTIC_FILE_PATH);
    }

    public static void writePlayerAnyStatistic(final String firstPlayerName, final String secondPlayerName,
                                               final GameEvent event) throws IOException {
        final File csvOutputFile = new File(PLAYER_STATISTIC_FILE_PATH);
        if (!csvOutputFile.exists()) {
            initPlayerStatistic();
        }
        final String[] data = {firstPlayerName, secondPlayerName, event.getDescription()};
        write(data, PLAYER_STATISTIC_FILE_PATH);
    }

    public static void writePlayerDrawStatistic(final String player1, final String player2) throws IOException {
        final File csvOutputFile = new File(PLAYER_STATISTIC_FILE_PATH);
        if (!csvOutputFile.exists()) {
            initPlayerStatistic();
        }
        final String[] data = {player1, player2, "0.5", "0.5"};
        write(data, PLAYER_STATISTIC_FILE_PATH);
    }

    private static void initPlayerStatistic() throws IOException {
        final File csvOutputFile = new File(PLAYER_STATISTIC_FILE_PATH);
        final String[] data = {"Player 1", "Player 2", "Player1Point", "Player2Point"};
        final CSVWriter writer = new CSVWriter(new PrintWriter(new FileWriter(PLAYER_STATISTIC_FILE_PATH)));
        writer.writeNext(data);
        writer.close();
    }

    public static void writeArmiesWinStatistic(final String firstMoveArmy, final String secondMoveArmy,
                                               final GameEvent winner) throws IOException {
        final File csvOutputFile = new File(ARMY_STATISTIC_FILE_PATH);
        if (!csvOutputFile.exists()) {
            initArmyStatistic();
        }
        final String[] data = {firstMoveArmy, secondMoveArmy, winner.getDescription()};
        write(data, ARMY_STATISTIC_FILE_PATH);
    }

    private static void initArmyStatistic() throws IOException {
        final File csvOutputFile = new File(ARMY_STATISTIC_FILE_PATH);
        final String[] data = {"Player 1", "Player 2", "Result"};
        final CSVWriter writer = new CSVWriter(new PrintWriter(new FileWriter(ARMY_STATISTIC_FILE_PATH)));
        writer.writeNext(data);
        writer.close();
    }

    private static void write(final String[] data, final String filePath) throws IOException {
        final CSVWriter writer = new CSVWriter(new PrintWriter(new FileWriter(filePath, true)));
        writer.writeNext(data);
        writer.close();
    }
}
