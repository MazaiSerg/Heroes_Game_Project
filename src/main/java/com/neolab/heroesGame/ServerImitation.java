package com.neolab.heroesGame;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.aditional.StatisticWriter;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.arena.FactoryArmies;
import com.neolab.heroesGame.arena.StringArmyFactory;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.dto.ExtendedServerResponse;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.server.answers.Answer;
import com.neolab.heroesGame.server.answers.AnswerProcessor;
import com.neolab.heroesGame.server.dto.ClientResponse;
import com.neolab.heroesGame.server.dto.ExtendedServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ServerImitation {
    public static final Integer MAX_ROUND = 15;
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerImitation.class);
    private ClientPlayerImitation currentPlayer;
    private ClientPlayerImitation waitingPlayer;
    private AnswerProcessor answerProcessor;
    private BattleArena battleArena;
    private int counter;

    public ServerImitation() throws Exception {
        currentPlayer = ClientPlayerImitation.createSimpleBotWithoutRandom(1);
        waitingPlayer = ClientPlayerImitation.createHumanPlayerWithAsciiGraphics(2, "Serg");
        battleArena = new BattleArena(FactoryArmies.generateArmies(1, 2));
        answerProcessor = new AnswerProcessor(1, 2, battleArena);
        counter = 0;
    }

    public String getCurrentPlayerName() {
        return currentPlayer.getPlayerName();
    }

    public String getWaitingPlayerName() {
        return waitingPlayer.getPlayerName();
    }

    private void changeCurrentAndWaitingPlayers() {
        final ClientPlayerImitation temp = currentPlayer;
        currentPlayer = waitingPlayer;
        waitingPlayer = temp;
        setAnswerProcessorPlayerId(currentPlayer.getPlayer(), waitingPlayer.getPlayer());
    }

    private void setAnswerProcessorPlayerId(final Player currentPlayer, final Player waitingPlayer) {
        answerProcessor.setActivePlayerId(currentPlayer.getId());
        answerProcessor.setWaitingPlayerId(waitingPlayer.getId());
    }

    public static void main(final String[] args) {
        try {
            final ServerImitation serverImitation = new ServerImitation();
            final List<String> armiesCode = CommonFunction.getAllAvailableArmiesCode(6);

            final Army firstArmy = new StringArmyFactory(armiesCode.get(15)).create();
            final Army botArmy = firstArmy.getCopy();
            final Army playerArmy = firstArmy.getCopy();
            final Map<Integer, Army> armies = new HashMap<>();
            armies.put(1, botArmy);
            armies.put(2, playerArmy);
            serverImitation.battleArena = new BattleArena(armies);
            serverImitation.answerProcessor = new AnswerProcessor(1, 2, serverImitation.battleArena);

            LOGGER.info("-----------------Начинается великая битва---------------");
            while (true) {

                final Optional<ClientPlayerImitation> whoIsWin = serverImitation.someoneWhoWin();
                if (whoIsWin.isPresent()) {
                    serverImitation.someoneWin(whoIsWin.get());
                    break;
                }

                if (!serverImitation.battleArena.canSomeoneAct()) {
                    serverImitation.counter++;
                    if (serverImitation.counter > MAX_ROUND) {
                        StatisticWriter.writePlayerDrawStatistic(serverImitation.getCurrentPlayerName(),
                                serverImitation.getWaitingPlayerName());
                        LOGGER.info("Поединок закончился ничьей");
                        break;
                    }
                    LOGGER.info("-----------------Начинается раунд <{}>---------------", serverImitation.counter);
                    serverImitation.battleArena.endRound();
                }

                if (serverImitation.checkCanMove(serverImitation.currentPlayer.getPlayer().getId())) {
                    serverImitation.askPlayerProcess();
                }
                serverImitation.changeCurrentAndWaitingPlayers();
            }

        } catch (final Exception ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    private void askPlayerProcess() throws Exception {
        battleArena.toLog();
        waitingPlayer.sendInformation(ExtendedServerResponse.getResponseFromString(
                ExtendedServerRequest.getRequestString(
                        GameEvent.WAIT_ITS_NOT_YOUR_TURN, battleArena, answerProcessor.getActionEffect())));

        final String response = currentPlayer.getAnswer(ExtendedServerResponse.getResponseFromString(
                ExtendedServerRequest.getRequestString(
                        GameEvent.NOW_YOUR_TURN, battleArena, answerProcessor.getActionEffect())));

        final Answer answer = new ClientResponse(response).getAnswer();
        answer.toLog();
        answerProcessor.handleAnswer(answer);
        answerProcessor.getActionEffect().toLog();
    }

    private Optional<ClientPlayerImitation> someoneWhoWin() {
        ClientPlayerImitation isWinner = battleArena.isArmyDied(getCurrentPlayerId()) ? waitingPlayer : null;
        if (isWinner == null) {
            isWinner = battleArena.isArmyDied(getWaitingPlayerId()) ? currentPlayer : null;
        }
        return Optional.ofNullable(isWinner);
    }

    private boolean checkCanMove(final Integer id) {
        return !battleArena.haveAvailableHeroByArmyId(id);
    }

    private int getCurrentPlayerId() {
        return currentPlayer.getPlayerId();
    }

    private int getWaitingPlayerId() {
        return waitingPlayer.getPlayerId();
    }

    private void someoneWin(final ClientPlayerImitation winner) throws IOException {
        final ClientPlayerImitation loser = getLoser(winner);
        StatisticWriter.writePlayerWinStatistic(winner.getPlayerName(), loser.getPlayerName());
        LOGGER.info("Игрок<{}> выиграл это тяжкое сражение", winner.getPlayerId());

        winner.endGame(ExtendedServerResponse.getResponseFromString(ExtendedServerRequest.getRequestString(
                GameEvent.YOU_WIN_GAME, battleArena, answerProcessor.getActionEffect())));

        loser.endGame(ExtendedServerResponse.getResponseFromString(ExtendedServerRequest.getRequestString(
                GameEvent.YOU_LOSE_GAME, battleArena, answerProcessor.getActionEffect())));
    }

    private ClientPlayerImitation getLoser(final ClientPlayerImitation winner) {
        return winner.equals(currentPlayer) ? waitingPlayer : currentPlayer;
    }
}


