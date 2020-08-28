package com.neolab.heroesGame.aditional.plotters;

import com.neolab.heroesGame.aditional.Analyzer;
import com.neolab.heroesGame.enumerations.GameEvent;
import org.math.plot.Plot2DPanel;
import org.math.plot.plotObjects.BaseLabel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

import static com.neolab.heroesGame.aditional.plotters.MathDraw.getXPoint;
import static com.neolab.heroesGame.aditional.plotters.MathDraw.getYPoint;

public class DynamicPlotter {
    final private String firstName;
    final private String secondName;
    final private Color firstColor;
    final private Color secondColor;
    final private Map<String, List<String>> statistic;
    private int winCounter;
    private int numbersMatches;
    private String previousName;
    private final Plot2DPanel dynamicPlot;
    private final JFrame dynamicFrame;
    private final JFrame dynamicFrameHistogram;

    public DynamicPlotter(final String firstName, final String secondName) {
        this(firstName, secondName, new HashMap<>());
    }

    private DynamicPlotter(final String firstName, final String secondName, final Map<String, List<String>> statistic) {
        this.firstColor = Color.RED;
        this.secondColor = Color.BLUE;
        this.firstName = firstName;
        this.secondName = secondName;
        this.statistic = statistic;
        dynamicFrame = setFrame();
        dynamicFrameHistogram = new JFrame("a histogram panel");
        dynamicFrameHistogram.setVisible(true);
        dynamicPlot = setPlot();
        winCounter = 0;
        numbersMatches = 0;
        previousName = "";
    }

    public static DynamicPlotter createDynamicPlotterWithOldInformation(final String firstName, final String secondName) {
        Analyzer analyzer = Analyzer.createAnalyserForPlayers();
        DynamicPlotter plotter = new DynamicPlotter(firstName, secondName,
                analyzer.getInfoAboutPairPlayers(firstName, secondName));
        plotter.oldStatisticPlot();
        plotter.dynamicHistogramPlot();
        return plotter;
    }

    public static void plotLoadedInformation(final String firstName, final String secondName,
                                             final Map<String, List<String>> statistic) {
        DynamicPlotter plotter = new DynamicPlotter(firstName, secondName, statistic);
        plotter.oldStatisticPlot();
        plotter.dynamicHistogramPlot();
    }

    private void oldStatisticPlot() {
        if (!Optional.ofNullable(statistic.get(firstName)).orElse(Collections.emptyList()).isEmpty()) {
            dynamicPlot.addLinePlot("", firstColor, getYPoint(statistic.get(firstName)),
                    getXPoint(statistic.get(firstName)));
        }
        if (!Optional.ofNullable(statistic.get(secondName)).orElse(Collections.emptyList()).isEmpty()) {
            dynamicPlot.addLinePlot("", secondColor, getYPoint(statistic.get(secondName)),
                    getXPoint(statistic.get(secondName)));
        }
        dynamicFrame.setContentPane(dynamicPlot);
        dynamicFrame.setVisible(true);
    }

    public void plotDynamicInfo(final String name, final GameEvent event) throws Exception {
        double[] oldPoint = new double[2];
        double[] newPoint = new double[2];
        if (!previousName.equals(name)) {
            previousName = name;
            winCounter = countWin(name);
            numbersMatches = countMatches(name);
        }
        oldPoint[0] = numbersMatches;
        oldPoint[1] = (1.0 * winCounter) / (numbersMatches != 0 ? numbersMatches : 1);
        winCounter += event.getDescription().equals("Win") ? 1 : 0;
        numbersMatches++;
        newPoint[0] = numbersMatches;
        newPoint[1] = (1.0 * winCounter) / (numbersMatches != 0 ? numbersMatches : 1);
        addInfo(name, event);
        dynamicPlot.addLinePlot("add", firstName.equals(name) ? firstColor : secondColor, newPoint, oldPoint);
        dynamicFrame.setContentPane(dynamicPlot);
        dynamicFrame.setVisible(true);
        Thread.sleep(20);
    }

    public void dynamicHistogramPlot() {
        Plot2DPanel plot = new Plot2DPanel();
        plot.addLegend("SOUTH");

        plotHistogram(plot, Optional.ofNullable(statistic.get(firstName)).orElse(Collections.emptyList()),
                firstName, secondName, Color.RED, 0.8);
        plotHistogram(plot, Optional.ofNullable(statistic.get(secondName)).orElse(Collections.emptyList()),
                secondName, firstName, Color.BLUE, 0.75);

        plot.setFixedBounds(0, 0, 2);

        dynamicFrameHistogram.setSize(800, 900);
        dynamicFrameHistogram.setContentPane(plot);
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
    }

    private int countMatches(final String name) {
        List<String> listTemp = Optional.ofNullable(statistic.get(name)).orElse(new ArrayList<>());
        return listTemp.size();
    }

    private int countWin(final String name) {
        List<String> listTemp = Optional.ofNullable(statistic.get(name)).orElse(new ArrayList<>());
        int counter = 0;
        for (String val : listTemp) {
            counter += val.equals("Win") ? 1 : 0;
        }
        return counter;
    }

    public void addInfo(final String name, final GameEvent event) {
        List<String> listTemp = Optional.ofNullable(statistic.get(name)).orElse(new ArrayList<>());
        listTemp.add(event.getDescription());
        statistic.put(name, listTemp);
    }

    private Plot2DPanel setPlot() {
        Plot2DPanel plot = new Plot2DPanel();

        BaseLabel title = new BaseLabel(String.format("Вероятность победы для %s против %s в зависимости от количества партий",
                firstName, secondName), firstColor, 0.5, 1.15);
        title.setFont(new Font("FirstPlayer", Font.BOLD, 20));
        plot.addPlotable(title);

        title = new BaseLabel(String.format("Вероятность победы для %s против %s в зависимости от количества партий",
                secondName, firstName), secondColor, 0.5, 1.08);
        title.setFont(new Font("SecondPlayer", Font.BOLD, 20));
        plot.addPlotable(title);

        return plot;
    }

    private JFrame setFrame() {
        JFrame frame = new JFrame("a plot panel");
        frame.setSize(1200, 600);
        frame.setVisible(true);
        return frame;
    }

}
