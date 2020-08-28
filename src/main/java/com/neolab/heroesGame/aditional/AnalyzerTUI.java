package com.neolab.heroesGame.aditional;

import com.neolab.heroesGame.aditional.plotters.DynamicPlotter;
import com.neolab.heroesGame.client.ai.version.mechanics.ConverterForFourSizeArmy;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class AnalyzerTUI {

    private AnalyzerTUI() {
    }

    public static void main(final String[] args) {
        final Analyzer analyzer = chooseStatistic();
        if (analyzer == null) {
            return;
        }
        while (true) {
            if (makeAction(analyzer)) {
                break;
            }
        }
    }

    private static Analyzer chooseStatistic() {
        final List<String> strings = new ArrayList<>();
        strings.add("Какую статистику загрузить?");
        strings.add("1. Статистику по никнеймам");
        strings.add("2. Статистику по армиям");
        strings.add("0. Выйти");
        final int option = readAction(strings);
        if (option == 1) {
            return Analyzer.createAnalyserForPlayers();
        }
        if (option == 2) {
            return Analyzer.createAnalyserForArmies();
        }
        return null;
    }

    private static boolean makeAction(final Analyzer analyzer) {
        final List<String> strings = new ArrayList<>();
        strings.add("Что сделать?");
        strings.add("1. Показать аномальные результаты");
        strings.add("2. Показать информацию о паре");
        strings.add("3. Построить графики пары игроков");
        strings.add("4. Показать информацию о всех парах");
        strings.add("5. Создать csv таблицу (старая будет перезаписана)");
        strings.add("0. Выйти");
        final int option = readAction(strings);
        switch (option) {
            case 1:
                analyzer.showAnomalisticResults();
                break;
            case 2:
                workWithPair(analyzer);
                break;
            case 3:
                plotGraphicsForPair(analyzer);
                break;
            case 4:
                workWithAllPair(analyzer);
                break;
            case 5:
                createCSV(analyzer);
                break;
            default:
                return true;
        }

        return false;
    }

    private static void createCSV(final Analyzer analyzer) {
        try (final CSVWriter writer = new CSVWriter(new PrintWriter(new FileWriter("table.csv")))) {
            final List<String> allName = createListNames(analyzer);
            armySort(allName);
            final String[] init = new String[allName.size() + 1];
            init[0] = "";
            for (int i = 0; i < allName.size(); i++) {
                init[i + 1] = allName.get(i);
            }
            writer.writeNext(init);

            for (final String firstName : allName) {
                final String[] newLine = new String[allName.size() + 1];
                newLine[0] = firstName;
                for (int i = 0; i < allName.size(); i++) {
                    final List<Double> info = analyzer.getInformationAboutMatchUp(firstName, allName.get(i));
                    if (info.isEmpty()) {
                        newLine[i + 1] = "";
                    } else {
                        newLine[i + 1] = String.valueOf(
                                (info.get(0) * info.get(3) * 3 + info.get(1) * info.get(3)) / (3 * info.get(3)));
                    }
                }
                writer.writeNext(newLine);
            }
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void armySort(List<String> armies) {
        for (int i = 0; i < armies.size() - 1; i++) {
            for (int j = i + 1; j < armies.size(); j++) {
                if (compare(armies.get(i), armies.get(j))) {
                    String temp = armies.get(i);
                    armies.set(i, armies.get(j));
                    armies.set(j, temp);
                }
            }
        }
    }

    private static boolean compare(String firstArmy, String secondArmy) {
        char firstWarlord = getWarlord(firstArmy);
        char secondWarlord = getWarlord(secondArmy);
        if (firstWarlord != secondWarlord) {
            return firstWarlord > secondWarlord;
        }
        int firstType = ConverterForFourSizeArmy.decide(firstArmy);
        int secondType = ConverterForFourSizeArmy.decide(secondArmy);
        if (firstType != secondType) {
            return firstType > secondType;
        }
        return firstArmy.compareTo(secondArmy) > 0;
    }

    private static char getWarlord(String army) {
        for (int i = 0; i < army.length(); i++) {
            if (army.charAt(i) >= 'A' && army.charAt(i) <= 'Z') {
                return army.charAt(i);
            }
        }
        return ' ';
    }

    private static void plotGraphicsForPair(final Analyzer analyzer) {
        final List<String> allName = createListNames(analyzer);
        final String first = chooseName(allName);
        if (first.isEmpty()) {
            return;
        }
        final String second = chooseName(allName);
        if (second.isEmpty()) {
            return;
        }
        final Map<String, List<String>> info = analyzer.getInfoAboutPairPlayers(first, second);
        DynamicPlotter.plotLoadedInformation(first, second, info);
    }

    private static void workWithPair(final Analyzer analyzer) {
        final List<String> allName = createListNames(analyzer);
        final String first = chooseName(allName);
        if (first.isEmpty()) {
            return;
        }
        final String second = chooseName(allName);
        if (second.isEmpty()) {
            return;
        }

        final Map<String, List<Double>> info = analyzer.getAnalyzedInfoAboutPairPlayers(first, second);
        for (final String name : info.keySet()) {
            final String anotherName = name.equals(first) ? second : first;
            System.out.printf("%-30s vs%10s%10s%10s\n", name, "win", "draw", "lose");
            final List<Double> winRate = info.get(name);
            if (!winRate.isEmpty()) {
                System.out.printf("%33s%9.0f%%%9.0f%%%9.0f%% %10d матчей\n\n", anotherName, winRate.get(0),
                        winRate.get(1), winRate.get(2), winRate.get(3).longValue());
            }
        }
    }

    private static void workWithAllPair(final Analyzer analyzer) {
        final List<String> allName = createListNames(analyzer);
        final List<String> anotherAllName = createListNames(analyzer);
        for (final String firstName : allName) {
            for (final String secondName : anotherAllName) {
                final Map<String, List<Double>> info = analyzer.getAnalyzedInfoAboutPairPlayers(firstName, secondName);
                for (final String name : info.keySet()) {
                    if (info.get(name).isEmpty()) {
                        continue;
                    }
                    final String anotherName = name.equals(firstName) ? secondName : firstName;
                    System.out.printf("%-30s vs%10s%10s%10s\n", name, "win", "draw", "lose");
                    final List<Double> winRate = info.get(name);
                    if (!winRate.isEmpty()) {
                        System.out.printf("%33s%9.0f%%%9.0f%%%9.0f%% %10d матчей\n\n", anotherName, winRate.get(0),
                                winRate.get(1), winRate.get(2), winRate.get(3).longValue());
                    }
                }

            }
            anotherAllName.remove(firstName);
        }
    }

    private static List<String> createListNames(final Analyzer analyzer) {
        final Set<String> allName = new HashSet<>();
        allName.addAll(analyzer.getFirstArmies());
        allName.addAll(analyzer.getSecondArmies());
        return new ArrayList<>(allName);
    }

    private static int readAction(final List<String> strings) {
        final Scanner in = new Scanner(System.in);
        for (final String string : strings) {
            System.out.println(string);
        }

        while (true) {
            final int option;
            try {
                final String temp = in.nextLine();
                option = Integer.parseInt(temp);
            } catch (final Exception e) {
                System.out.println("Ввод символов, отличных от цифр, недопустим");
                continue;
            }
            if (option >= 0 && option < strings.size()) {
                return option;
            }
        }
    }

    private static String chooseName(final List<String> allName) {
        final List<String> expertInter = new ArrayList<>();
        expertInter.add("1. Ввести имя самостоятельно");
        expertInter.add("0. Выбрать имя из списка");
        while (true) {
            final int read = readAction(expertInter);
            if (read == 0) {
                break;
            }
            final Scanner in = new Scanner(System.in);
            final String name = in.nextLine();
            if (allName.contains(name)) {
                return name;
            }
            System.out.println("Такого имени не найдено");
        }
        final List<String> strings = new ArrayList<>();
        strings.add("Введите номер игрока:");
        for (int i = 0; i < allName.size(); i++) {
            strings.add(String.format("%d. %s", i + 1, allName.get(i)));
        }

        final int first = readAction(strings) - 1;
        return first >= 0 ? allName.get(first) : "";
    }
}
