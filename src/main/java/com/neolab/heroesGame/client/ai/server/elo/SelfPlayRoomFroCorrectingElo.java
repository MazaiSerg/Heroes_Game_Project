package com.neolab.heroesGame.client.ai.server.elo;

import com.neolab.heroesGame.aditional.StatisticWriter;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.server.answers.Answer;
import com.neolab.heroesGame.server.answers.AnswerProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

public class SelfPlayRoomFroCorrectingElo extends Thread {
    public static final Integer MAX_ROUND = 15;
    private static final Logger LOGGER = LoggerFactory.getLogger(com.neolab.heroesGame.client.ai.server.SelfPlayRoom.class);
    private final Player firstPlayer;
    private final Player secondPlayer;
    private Player currentPlayer;
    private Player waitingPlayer;
    private final AnswerProcessor answerProcessor;
    private final BattleArena battleArena;
    private int counter;
    private int timeForFirst = 0;
    private int timeForSecond = 0;
    private final RatingElo ratingElo;

    /**
     * @param arena        арена, в которой содержатся армии с правильным id
     * @param firstPlayer  игрок, который походит первым в этот матче
     * @param secondPlayer походит вторым
     * @param ratingElo    текущий рейтинг Эло
     */
    public SelfPlayRoomFroCorrectingElo(final BattleArena arena, final Player firstPlayer,
                                        final Player secondPlayer, final RatingElo ratingElo) {
        this.firstPlayer = firstPlayer;
        this.secondPlayer = secondPlayer;
        currentPlayer = firstPlayer;
        waitingPlayer = secondPlayer;
        battleArena = arena;
        answerProcessor = new AnswerProcessor(firstPlayer.getId(), secondPlayer.getId(), battleArena);
        counter = 0;
        this.ratingElo = ratingElo;
    }

    @Override
    public void run() {
        Optional<Player> whoIsWin;
        LOGGER.info("Игрок {} - {}; Игрок {} - {}",
                firstPlayer.getId(), firstPlayer.getName(),
                secondPlayer.getId(), secondPlayer.getName());
        while (true) {
            whoIsWin = someoneWhoWin();
            if (whoIsWin.isPresent()) {
                break;
            }
            if (!battleArena.canSomeoneAct()) {
                counter++;
                if (counter > MAX_ROUND) {
                    break;
                }
                battleArena.endRound();
            }
            if (checkCanMove(currentPlayer.getId())) {
                try {
                    final long start = System.currentTimeMillis();
                    askPlayerProcess();
                    if (currentPlayer == firstPlayer) {
                        timeForFirst += System.currentTimeMillis() - start;
                    } else {
                        timeForSecond += System.currentTimeMillis() - start;
                    }
                } catch (final Exception e) {
                    LOGGER.error(e.toString());
                    //throw new IllegalThreadStateException();
                }
            }
            changeCurrentAndWaitingPlayers();
        }
        endMatchActivity();
    }

    /**
     * Логируем арену, ответ игрока и еффект от его действия.
     * Запрашиваем у игрока действие и приводим его в исполнение
     *
     * @throws HeroExceptions Проблемы с выбором действия (не должны встречаться), неправильный ответ игрока
     * @throws IOException    Проблемы с отображением картинки для пользователя
     */
    private void askPlayerProcess() throws HeroExceptions, IOException {
        final Answer answer = currentPlayer.getAnswer(battleArena);
        answerProcessor.handleAnswer(answer);
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

    /**
     * записываем результаты действия в лог и файл для сбора статистики
     * обновляем рейтинг игроков
     */
    private void endMatchActivity() {
        final Optional<Player> whoIsWin = someoneWhoWin();
        final GameEvent endMatch;
        if (whoIsWin.isEmpty()) {
            endMatch = GameEvent.GAME_END_WITH_A_TIE;
        } else if (whoIsWin.get().getId() == firstPlayer.getId()) {
            endMatch = GameEvent.YOU_WIN_GAME;
        } else {
            endMatch = GameEvent.YOU_LOSE_GAME;
        }
        ratingElo.refreshRating(firstPlayer.getName(), secondPlayer.getName(), endMatch);
        try {
            StatisticWriter.writePlayerAnyStatistic(firstPlayer.getName(), secondPlayer.getName(), endMatch);
            LOGGER.info("{} vs {} = {}", firstPlayer.getName(), secondPlayer.getName(), endMatch.getDescription());
            LOGGER.info("{} - {}ms; Игрок {} - {}ms",
                    firstPlayer.getName(), timeForFirst,
                    secondPlayer.getName(), timeForSecond);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }
}
