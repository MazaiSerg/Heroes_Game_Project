package com.neolab.heroesGame.aditional;

import com.neolab.heroesGame.aditional.plotters.DynamicPlotter;

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
        strings.add("0. Выйти");
        final int option = readAction(strings);
        if (option == 1) {
            analyzer.showAnomalisticResults();
            return false;
        }
        if (option == 2) {
            workWithPair(analyzer);
            return false;
        }
        if (option == 3) {
            plotGraphicsForPair(analyzer);
            return false;
        }
        if (option == 4) {
            workWithAllPair(analyzer);
            return false;
        }
        return true;
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
            System.out.printf("%-16s%10s%10s%10s\n", name, "win", "draw", "lose");
            final List<Double> winRate = info.get(name);
            if (!winRate.isEmpty()) {
                System.out.printf("%16s%9.0f%%%9.0f%%%9.0f%% %10d матчей\n", "", winRate.get(0),
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
