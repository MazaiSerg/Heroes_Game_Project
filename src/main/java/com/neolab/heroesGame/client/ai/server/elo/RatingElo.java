package com.neolab.heroesGame.client.ai.server.elo;

import com.neolab.heroesGame.client.ai.enums.BotType;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.*;
import java.util.*;

import static com.neolab.heroesGame.enumerations.GameEvent.YOU_LOSE_GAME;
import static com.neolab.heroesGame.enumerations.GameEvent.YOU_WIN_GAME;

public class RatingElo {
    static final private String FILE_TO_SAVE = "src/main/resources/ratingElo.csv";
    static final private int START_RATING = 1200;
    Map<String, Integer> ratingMap;

    private RatingElo(final Map<String, Integer> ratingMap) {
        this.ratingMap = ratingMap;
    }

    /**
     * Создает карту рейтинга ботов, читая старые значения из файла для сохранения
     * Если среди BotType появился новый тип, то добавляет его с рейтингом START_RATING
     *
     * @return объект класса
     */
    static public RatingElo createRatingEloForBot() {
        final Map<String, Integer> ratingMap = readFromFile();
        for (final BotType type : BotType.values()) {
            if (!ratingMap.containsKey(type.toString())) {
                ratingMap.put(type.toString(), START_RATING);
            }
        }
        return new RatingElo(ratingMap);
    }

    static public RatingElo createRatingEloForBot(Set<String> botNames) {
        final Map<String, Integer> ratingMap = readFromFile();
        for (String botName : new HashSet<>(ratingMap.keySet())) {
            if (!botNames.contains(botName)) {
                ratingMap.remove(botName);
            }
        }
        for (final String botName : botNames) {
            if (!ratingMap.containsKey(botName)) {
                ratingMap.put(botName, START_RATING);
            }
        }
        return new RatingElo(ratingMap);
    }

    /**
     * Создает карту рейтинга по списку имен. Всем присваивается START_RATING
     *
     * @param names список имен, которые будут бороться друг с другом
     * @return объект класса
     */
    static public RatingElo createRatingElo(final List<String> names) {
        final Map<String, Integer> ratingMap = new HashMap<>();
        for (final String name : names) {
            ratingMap.put(name, START_RATING);
        }
        return new RatingElo(ratingMap);
    }

    private static Map<String, Integer> readFromFile() {
        final File csvInputFile = new File(FILE_TO_SAVE);
        final Map<String, Integer> ratingMap = new HashMap<>();
        if (!csvInputFile.exists()) {
            return ratingMap;
        }
        final List<String[]> result;
        try (final CSVReader reader = new CSVReader(new FileReader(csvInputFile))) {
            result = reader.readAll();
        } catch (final Exception e) {
            e.printStackTrace();
            return ratingMap;
        }
        for (final String[] pair : result) {
            ratingMap.put(pair[0], Integer.parseInt(pair[1]));
        }
        return ratingMap;
    }

    /**
     * Подбирает соперников для выбранного игрока. В соперники пойдут игроки:
     * с таким же рейтингом,
     * игрок, стоящий на 1 позицию ниже,
     * игрок, стоящий на 1 позицию выше
     * Для самого слабого и самого сильного игроков подбирается только 1 соперник с отличным рейтингом
     *
     * @param name имя игрока, которому ищем оппонентов
     * @return список имен игроков, коотрые подошли под условие отбора
     */
    public List<String> getOpponents(final String name) {
        if (!ratingMap.containsKey(name)) {
            ratingMap.put(name, START_RATING);
        }
        final int rating = ratingMap.get(name);
        String bottom = null;
        String upper = null;
        final List<String> result = new ArrayList<>();
        for (final String opponent : ratingMap.keySet()) {
            if (opponent.equals(name)) {
                continue;
            }
            if ((rating < ratingMap.get(opponent))
                    && (upper == null || ratingMap.get(upper) > ratingMap.get(opponent))) {
                upper = opponent;
            } else if ((rating > ratingMap.get(opponent))
                    && (bottom == null || ratingMap.get(bottom) < ratingMap.get(opponent))) {
                bottom = opponent;
            } else if (rating == ratingMap.get(opponent)) {
                result.add(opponent);
            }
        }
        if (bottom != null) {
            result.add(bottom);
        }
        if (upper != null) {
            result.add(upper);
        }
        return result;
    }

    public void saveRating() {
        try (final CSVWriter writer = new CSVWriter(new PrintWriter(new FileWriter(FILE_TO_SAVE, true)))) {
            for (final String name : ratingMap.keySet()) {
                final String[] data = {name, String.valueOf(ratingMap.get(name))};
                writer.writeNext(data);
            }
        } catch (final IOException ex) {
            ex.printStackTrace();
            throw new AssertionError();
        }
    }

    public void printRating() {
        for (final String name : ratingMap.keySet()) {
            System.out.printf("%s - %d\n", name, ratingMap.get(name));
        }
    }

    /**
     * Обновляем рейтинг по правилам, близким к Эло:
     * ожидаем результат рассчитываем как Эло
     * коэффициент берем по следующим правилам:
     * рейтинг > 2400 - 20
     * рейтинг (1500, 2400] - 30
     * рейтинг [600, 1500] - 40
     * рейтинг < 600 - 50
     * Очередность игроков не влияет на подсчет очков (пока не влияет!!)
     *
     * @param firstOne  один из игроков
     * @param secondOne второй из игроков
     * @param event     результат для первого(!) игрока
     */
    public synchronized void refreshRating(final String firstOne, final String secondOne, final GameEvent event) {
        if (!ratingMap.containsKey(firstOne)) {
            ratingMap.put(firstOne, START_RATING);
        }
        if (!ratingMap.containsKey(secondOne)) {
            ratingMap.put(secondOne, START_RATING);
        }
        final int firstRating = ratingMap.get(firstOne) + (int) (getCoefficient(firstOne)
                * ((event == YOU_LOSE_GAME ? 0 : event == YOU_WIN_GAME ? 1 : 0.5)
                - getExpectedPoints(firstOne, secondOne)));
        final int secondRating = ratingMap.get(secondOne) + (int) (getCoefficient(secondOne)
                * ((event == YOU_LOSE_GAME ? 1 : event == YOU_WIN_GAME ? 0 : 0.5)
                - getExpectedPoints(secondOne, firstOne)));
        ratingMap.put(firstOne, firstRating);
        ratingMap.put(secondOne, secondRating);
    }

    private double getExpectedPoints(final String firstOne, final String secondOne) {
        return 1d / (1 + Math.pow(10, ((double) (ratingMap.get(secondOne) - ratingMap.get(firstOne))) / 400));
    }

    private int getCoefficient(final String name) {
        final int rating = ratingMap.get(name);
        if (rating > 2400) {
            return 20;
        } else if (rating > 1500) {
            return 30;
        } else if (rating < 600) {
            return 50;
        }
        return 40;
    }

    public String[] getTwoBest() {
        final String[] twoBest = new String[2];
        for (final String name : ratingMap.keySet()) {
            if (twoBest[1] == null || ratingMap.get(twoBest[1]) < ratingMap.get(name)) {
                twoBest[0] = twoBest[1];
                twoBest[1] = name;
            } else if (twoBest[0] == null || ratingMap.get(twoBest[0]) < ratingMap.get(name)) {
                twoBest[0] = name;
            }
        }
        return twoBest;
    }
}
