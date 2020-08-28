package com.neolab.heroesGame.samplesSockets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropsServerManager {
    //путь к нашему файлу конфигураций
    private static final String PATH_TO_PROPERTIES = "resources/server.properties";
    private static final String PATH_TO_PROPERTIES_2 = "src/main/resources/server.properties";
    private static final Logger LOGGER = LoggerFactory.getLogger(PropsServerManager.class);
    public int PORT = 8081;
    public int MAX_COUNT_PLAYERS;
    public int MAX_COUNT_GAME_ROOMS;
    public int MAX_COUNT_BATTLES;
    public int MAX_ANSWER_TIMEOUT;
    public final Map<Integer, String> mapIdNamePlayers = new HashMap<>();

    public PropsServerManager() {
        //инициализируем специальный объект Properties
        //типа Hashtable для удобной работы с данными
        final Properties prop = new Properties();

        try {
            //обращаемся к файлу и получаем данные
            //todo почему-то getResource не работает
            //prop.load(PropsServerManager.class.getResourceAsStream("resources/server.properties"));
            //prop.load(new FileInputStream(PATH_TO_PROPERTIES));
            File test = new File(PATH_TO_PROPERTIES_2);
            if (test.exists()) {
                prop.load(new FileInputStream(PATH_TO_PROPERTIES_2));
            } else {
                prop.load(new FileInputStream(PATH_TO_PROPERTIES));
            }
            PORT = Integer.parseInt(prop.getProperty("server.PORT"));
            MAX_COUNT_PLAYERS = Integer.parseInt(prop.getProperty("MAX_COUNT_PLAYERS"));
            MAX_COUNT_GAME_ROOMS = Integer.parseInt(prop.getProperty("MAX_COUNT_GAME_ROOMS"));
            MAX_COUNT_BATTLES = Integer.parseInt(prop.getProperty("MAX_COUNT_BATTLES"));
            MAX_ANSWER_TIMEOUT = Integer.parseInt(prop.getProperty("MAX_ANSWER_TIMEOUT"));
            final String playerOneName = prop.getProperty("playerOne.name");
            final String playerTwoName = prop.getProperty("playerTwo.name");

            mapIdNamePlayers.put(1, playerOneName);
            mapIdNamePlayers.put(2, playerTwoName);

        } catch (final IOException e) {
            LOGGER.error("Ошибка в программе: файл {} не обнаружен", PATH_TO_PROPERTIES);
        }
    }
}