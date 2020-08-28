package com.neolab.heroesGame.aditional.plotters;

import com.neolab.heroesGame.aditional.StatisticReader;
import org.math.plot.Plot2DPanel;
import org.math.plot.plotObjects.BaseLabel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class MathDraw {
    private final Map<String, Map<String, List<String>>> statisticMap;

    private MathDraw(final Map<String, Map<String, List<String>>> statisticMap) {
        this.statisticMap = statisticMap;
    }

    public static MathDraw getMathDrawWithDataFromFile() {
        final Optional<List<String[]>> statistic = StatisticReader.readArmiesWinStatistic();
        final Map<String, Map<String, List<String>>> statisticMap = new HashMap<>();
        statistic.ifPresent(list ->
                list.forEach(val -> {
                    Map<String, List<String>> temp = Optional.ofNullable(statisticMap.get(val[0])).orElse(new HashMap<>());
                    List<String> listTemp = Optional.ofNullable(temp.get(val[1])).orElse(new ArrayList<>());
                    listTemp.add(val[2]);
                    temp.put(val[1], listTemp);
                    statisticMap.put(val[0], temp);
                })
        );
        return new MathDraw(statisticMap);
    }

    public void plotOldInfoForTwo(final String firstOne, final String secondOne) {
        final Map<String, List<String>> infoAboutFirstOne = Optional.ofNullable(statisticMap.get(firstOne))
                .orElse(Collections.emptyMap());
        final List<String> resultFirstOne = Optional.ofNullable(infoAboutFirstOne.get(secondOne))
                .orElse(Collections.emptyList());

        final Map<String, List<String>> infoAboutSecondOne = Optional.ofNullable(statisticMap.get(secondOne))
                .orElse(Collections.emptyMap());
        final List<String> resultSecondOne = Optional.ofNullable(infoAboutSecondOne.get(firstOne))
                .orElse(Collections.emptyList());

        Plot2DPanel plot = new Plot2DPanel();
        plot.addLegend("SOUTH");

        if (!resultFirstOne.isEmpty()) {
            plot.addLinePlot(String.format("Вероятность победы для %s против %s при своем первом ходе",
                    firstOne, secondOne), getYPoint(resultFirstOne), getXPoint(resultFirstOne));
        }
        if (!resultSecondOne.isEmpty()) {
            plot.addLinePlot(String.format("Вероятность победы для %s против %s при своем первом ходе",
                    secondOne, firstOne), getYPoint(resultSecondOne), getXPoint(resultSecondOne));
        }

        JFrame frame = new JFrame("a plot panel");
        frame.setSize(600, 600);
        frame.setContentPane(plot);
        frame.setVisible(true);
    }

    public void oldInfoHistogramPlot(final String firstOne, final String secondOne) {
        final Map<String, List<String>> infoAboutFirstOne = Optional.ofNullable(statisticMap.get(firstOne))
                .orElse(Collections.emptyMap());
        final List<String> resultFirstOne = Optional.ofNullable(infoAboutFirstOne.get(secondOne))
                .orElse(Collections.emptyList());

        final Map<String, List<String>> infoAboutSecondOne = Optional.ofNullable(statisticMap.get(secondOne))
                .orElse(Collections.emptyMap());
        final List<String> resultSecondOne = Optional.ofNullable(infoAboutSecondOne.get(firstOne))
                .orElse(Collections.emptyList());

        Plot2DPanel plot = new Plot2DPanel();
        plot.addLegend("SOUTH");

        plotHistogram(plot, resultFirstOne, firstOne, secondOne, Color.RED, 0.8);
        plotHistogram(plot, resultSecondOne, secondOne, firstOne, Color.BLUE, 0.75);

        plot.setFixedBounds(0, 0, 2);

        JFrame frame = new JFrame("a histo panel");
        frame.setSize(800, 900);
        frame.setContentPane(plot);
        frame.setVisible(true);
    }

    private void plotHistogram(final Plot2DPanel plot, final List<String> results, final String current,
                               final String another, final Color color, final double height) {
        double[] numericalResults = new double[results.size()];
        int counterWin = 0;
        int counterDraw = 0;
        int counterLose = 0;
        for (int i = 0; i < numericalResults.length; i++) {
            if (results.get(i).equals("Win")) {
                numericalResults[i] = 2;
                counterWin++;
            } else if (results.get(i).equals("Draw")) {
                numericalResults[i] = 1;
                counterDraw++;
            } else {
                numericalResults[i] = 0;
                counterLose++;
            }
        }
        if (!results.isEmpty()) {
            plot.addHistogramPlot(String.format("Распределение результатов для %s против %s при своем первом ходе",
                    current, another), color, numericalResults, 0d, 2.01d, 3);
        }

        BaseLabel title = new BaseLabel(String.format("Победы %.2f%%", counterWin * 100.0 / results.size()),
                color, 0.8, height);
        title.setFont(new Font("Wins", Font.BOLD, 20));
        plot.addPlotable(title);
        title = new BaseLabel(String.format("Ничьи %.2f%%", counterDraw * 100.0 / results.size()),
                color, 0.5, height);
        title.setFont(new Font("Draws", Font.BOLD, 20));
        plot.addPlotable(title);
        title = new BaseLabel(String.format("Поражения %.2f%%", counterLose * 100.0 / results.size()),
                color, 0.15, height);
        title.setFont(new Font("Loses", Font.BOLD, 20));
        plot.addPlotable(title);
    }

    protected static double[] getYPoint(final List<String> list) {
        final double[] result = new double[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = i + 1;
        }
        return result;
    }

    protected static double[] getXPoint(final List<String> list) {
        final double[] result = new double[list.size()];
        int counter = 0;
        for (int i = 0; i < result.length; i++) {
            counter += list.get(i).equals("Win") ? 1 : 0;
            result[i] = ((double) counter) / (i + 1);
        }
        return result;
    }
}
