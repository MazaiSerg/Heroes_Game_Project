package com.neolab.heroesGame.samplesSockets;

import com.neolab.heroesGame.aditional.StatisticWriter;
import com.neolab.heroesGame.aditional.plotters.DynamicPlotter;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.server.serverNetwork.GameServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Queue;

/**
 * класс реализующий функционал игровой комнты для двух игроков
 */
public class GameRoom extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameRoom.class);
    private final PlayerSocket playerOne;
    private final PlayerSocket playerTwo;
    private final int countBattles;
    private final boolean usePlots;

    public GameRoom(final Queue<PlayerSocket> queuePlayers, final int countBattles) {
        this(queuePlayers, countBattles, false);
    }

    public GameRoom(final Queue<PlayerSocket> queuePlayers, final int countBattles, final boolean usePlots) {
        playerOne = queuePlayers.poll();
        playerTwo = queuePlayers.poll();
        this.countBattles = countBattles;
        this.usePlots = usePlots;
    }

    @Override
    public void run() {
        try {
            final long start = System.nanoTime();
            DynamicPlotter plotter = null;
            if (usePlots) {
                plotter = DynamicPlotter.createDynamicPlotterWithOldInformation(playerOne.getPlayerName(),
                        playerTwo.getPlayerName());
            }

            for (int i = 0; i < countBattles; i++) {
                if (usePlots) {
                    matchingWithPlots(playerOne, playerTwo, plotter);
                    matchingWithPlots(playerTwo, playerOne, plotter);
                } else {
                    matchingWithoutPlots(playerOne, playerTwo);
                    matchingWithoutPlots(playerTwo, playerOne);
                }
            }
            final long end = System.nanoTime();
            //1 second = 1__000__000__000 nano seconds
            final double elapsedTimeInSecond = (double) (end - start) / 1__000__000__000;
            LOGGER.warn("Игра длилась {}", elapsedTimeInSecond);

            Server.getCountGameRooms().decrementAndGet();
        } catch (final InterruptedException | HeroExceptions | IOException e) {
            LOGGER.warn(e.getMessage());
            //throw new IllegalStateException("Игра прервана, внутренняя ошибка сервера");
        }
    }

    private GameEvent matchingWithoutPlots(final PlayerSocket firstPlayer,
                                           final PlayerSocket secondPlayer)
            throws IOException, HeroExceptions, InterruptedException {

        final GameServer server = new GameServer(firstPlayer, secondPlayer);
        final PlayerSocket failedPlayer = server.prepareForBattle();
        if (failedPlayer != null) {
            LOGGER.warn("Игрок {} трижды прислал неверную армию", failedPlayer.getPlayerName());
            return failedPlayer.equals(firstPlayer) ? GameEvent.YOU_LOSE_GAME : GameEvent.YOU_WIN_GAME;
        }
        final GameEvent event = server.gameProcess();
        StatisticWriter.writePlayerAnyStatistic(firstPlayer.getPlayerName(),
                secondPlayer.getPlayerName(), event);
        return event;
    }

    private void matchingWithPlots(final PlayerSocket firstPlayer,
                                   final PlayerSocket secondPlayer,
                                   final DynamicPlotter plotter)
            throws InterruptedException, HeroExceptions, IOException {

        final GameEvent event = matchingWithoutPlots(firstPlayer, secondPlayer);

        try {
            plotter.plotDynamicInfo(firstPlayer.getPlayerName(), event);
            plotter.dynamicHistogramPlot();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
