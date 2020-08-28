package com.neolab.heroesGame;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.aditional.StatisticWriter;
import com.neolab.heroesGame.aditional.plotters.DynamicPlotter;
import com.neolab.heroesGame.aditional.plotters.MathDraw;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.arena.FactoryArmies;
import com.neolab.heroesGame.arena.StringArmyFactory;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.PlayerBot;
import com.neolab.heroesGame.client.ai.version.first.MonteCarloBot;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.server.answers.Answer;
import com.neolab.heroesGame.server.answers.AnswerProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class GamingProcess {
    public static final Integer MAX_ROUND = 15;
    public static final Integer NUMBER_TRIES = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(GamingProcess.class);
    private static final long SEED = 876;
    private static final Random RANDOM = new Random(SEED);
    private Player currentPlayer;
    private Player waitingPlayer;
    private final AnswerProcessor answerProcessor;
    private final BattleArena battleArena;
    private int counter;

    public GamingProcess() throws HeroExceptions, IOException {
        currentPlayer = new PlayerBot(1, "Bot1");
        waitingPlayer = new PlayerBot(2, "Bot2");
        battleArena = new BattleArena(FactoryArmies.generateArmies(1, 2));
        answerProcessor = new AnswerProcessor(1, 2, battleArena);
        counter = 0;
    }

    public GamingProcess(final BattleArena arena) {
        currentPlayer = new MonteCarloBot(1);
        waitingPlayer = new PlayerBot(2);
        battleArena = arena;
        answerProcessor = new AnswerProcessor(1, 2, battleArena);
        counter = 0;
    }

    public GamingProcess(GamingProcess game) {
        this.currentPlayer = game.currentPlayer;
        this.waitingPlayer = game.waitingPlayer;
        this.battleArena = game.battleArena.getCopy();
        this.answerProcessor = new AnswerProcessor(game.currentPlayer.getId(),
                game.waitingPlayer.getId(), this.battleArena);
        counter = 0;
    }

    private void changeCurrentAndWaitingPlayers() {
        final Player temp = currentPlayer;
        currentPlayer = waitingPlayer;
        waitingPlayer = temp;
        setAnswerProcessorPlayerId(currentPlayer, waitingPlayer);
    }

    private void setAnswerProcessorPlayerId(final Player currentPlayer, final Player waitingPlayer) {
        answerProcessor.setActivePlayerId(currentPlayer.getId());
        answerProcessor.setWaitingPlayerId(waitingPlayer.getId());
    }

    public static void main(final String[] args) throws Exception {
        final boolean isPlotter = false;
        if (isPlotter) {
            GamingProcess.plotter();
        } else {
            GamingProcess.matches();
        }
    }

    private static void matches() throws Exception {
        List<String> armies = CommonFunction.getAllAvailableArmiesCode(6);
        int counter = 0;
        final int numbers = armies.size();
        final long startTime = System.currentTimeMillis();


        for (int j = 0; j < 5; j++) {
            String army = armies.get(RANDOM.nextInt(armies.size()));
            final Army firstArmy = new StringArmyFactory(army).create();
            final Army secondArmy = new StringArmyFactory(army).create();
            final Map<Integer, Army> mapArmies = new HashMap<>();
            mapArmies.put(1, firstArmy);
            mapArmies.put(2, secondArmy);
            final BattleArena arena = new BattleArena(mapArmies);
            final GamingProcess process = new GamingProcess(arena);
            Player firstPlayer = process.currentPlayer;
            Player secondPlayer = process.waitingPlayer;
            for (int i = 0; i < NUMBER_TRIES; i++) {
                process.setActiveAndWaitingPlayer(firstPlayer, secondPlayer);
                GameEvent endMatch = process.match();
                StatisticWriter.writePlayerAnyStatistic(firstPlayer.getName(), secondPlayer.getName(), endMatch);

                process.setActiveAndWaitingPlayer(secondPlayer, firstPlayer);
                endMatch = process.match();
                StatisticWriter.writePlayerAnyStatistic(secondPlayer.getName(), firstPlayer.getName(), endMatch);
                System.out.printf("Выполнено %3d испытаний из %3d.\n", i + 1, NUMBER_TRIES);
            }

            counter++;
            final long endTime = System.currentTimeMillis();
            final long timeNeed = (((endTime - startTime) / counter) * (numbers - counter)) / 60000;
            final int timeFromStart = (int) ((endTime - startTime) / 60000);
            System.out.printf("Выполнено %3d испытаний из %3d. Прошло: %d минут. Примерно осталось : %d минут\n",
                    counter, 5, timeFromStart, timeNeed);
        }
    }

    private void setActiveAndWaitingPlayer(Player secondPlayer, Player firstPlayer) {
        this.currentPlayer = firstPlayer;
        this.waitingPlayer = secondPlayer;
    }

    private static void plotter() {
        try {
            final GamingProcess finalGame = new GamingProcess();
            final String firstArmy = CommonFunction.ArmyCodeToString(finalGame.battleArena.getArmy(
                    finalGame.currentPlayer.getId()));
            final String secondArmy = CommonFunction.ArmyCodeToString(finalGame.battleArena.getArmy(
                    finalGame.waitingPlayer.getId()));
            final DynamicPlotter dynamicDraw = new DynamicPlotter(firstArmy, secondArmy);
            for (int i = 0; i < NUMBER_TRIES; i++) {
                GameEvent endMatch = finalGame.match();
                StatisticWriter.writeArmiesWinStatistic(firstArmy, secondArmy, endMatch);
                dynamicDraw.plotDynamicInfo(firstArmy, endMatch);
                //dynamicDraw.dynamicHistogramPlot();

                finalGame.changeCurrentAndWaitingPlayers();
                endMatch = finalGame.match();
                StatisticWriter.writeArmiesWinStatistic(secondArmy, firstArmy, endMatch);
                dynamicDraw.plotDynamicInfo(secondArmy, endMatch);
                //dynamicDraw.dynamicHistogramPlot();

                finalGame.changeCurrentAndWaitingPlayers();
            }

            MathDraw mathDraw = MathDraw.getMathDrawWithDataFromFile();
            mathDraw.plotOldInfoForTwo(firstArmy, secondArmy);
            mathDraw.oldInfoHistogramPlot(firstArmy, secondArmy);
        } catch (final Exception ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    private GameEvent match() throws Exception {
        final GamingProcess gamingProcess = new GamingProcess(this);
        Optional<Player> whoIsWin;
        while (true) {
            //Определение победы
            //gamingProcess.battleArena.toLog();
            whoIsWin = gamingProcess.someoneWhoWin();
            if (whoIsWin.isPresent()) {
                break;
            }
            //Начало нового раунда, прерывание игры из-за ничьей
            if (!gamingProcess.battleArena.canSomeoneAct()) {
                gamingProcess.counter++;
                if (gamingProcess.counter > MAX_ROUND) {
                    break;
                }
                gamingProcess.battleArena.endRound();
            }
            //Ход игрока
            if (gamingProcess.checkCanMove(gamingProcess.currentPlayer.getId())) {
                gamingProcess.askPlayerProcess();
            }
            //смена активного игрока
            gamingProcess.changeCurrentAndWaitingPlayers();
        }
        GameEvent endMatch;
        if (whoIsWin.isEmpty()) {
            endMatch = GameEvent.GAME_END_WITH_A_TIE;
        } else if (whoIsWin.get().getId() == this.currentPlayer.getId()) {
            endMatch = GameEvent.YOU_WIN_GAME;
        } else {
            endMatch = GameEvent.YOU_LOSE_GAME;
        }
        return endMatch;
    }

    private void askPlayerProcess() throws HeroExceptions, IOException {
        final Answer answer = currentPlayer.getAnswer(battleArena);
        //answer.toLog();
        answerProcessor.handleAnswer(answer);
        //answerProcessor.getActionEffect().toLog();
    }

    private Optional<Player> someoneWhoWin() {
        Player isWinner = battleArena.isArmyDied(getCurrentPlayerId()) ? waitingPlayer : null;
        if (isWinner == null) {
            isWinner = battleArena.isArmyDied(getWaitingPlayerId()) ? currentPlayer : null;
        }
        return Optional.ofNullable(isWinner);
    }

    private boolean checkCanMove(final Integer id) {
        return !battleArena.haveAvailableHeroByArmyId(id);
    }

    private int getCurrentPlayerId() {
        return currentPlayer.getId();
    }

    private int getWaitingPlayerId() {
        return waitingPlayer.getId();
    }
}

