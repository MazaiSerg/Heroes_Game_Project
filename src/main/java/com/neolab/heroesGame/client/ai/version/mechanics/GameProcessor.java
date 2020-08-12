package com.neolab.heroesGame.client.ai.version.mechanics;

import com.neolab.heroesGame.client.ai.version.mechanics.arena.Answer;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Army;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.BattleArena;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.SquareCoordinate;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Hero;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.enumerations.HeroErrorCode;
import com.neolab.heroesGame.errors.HeroExceptions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.neolab.heroesGame.client.ai.version.mechanics.arena.SquareCoordinate.coordinateDoesntMatters;

public class GameProcessor {
    private int roundCounter = 0;
    private static final int MAX_ROUND = 15;
    private int waitingPlayerId;
    private int activePlayerId;
    private BattleArena board;
    private final boolean useRandom;

    public GameProcessor(final int activePlayerId, final BattleArena board) {
        waitingPlayerId = board.getEnemyId(activePlayerId);
        this.activePlayerId = activePlayerId;
        this.board = board;
        useRandom = false;
    }

    public GameProcessor(final int activePlayerId, final BattleArena board,
                         final int roundCounter) {
        waitingPlayerId = board.getEnemyId(activePlayerId);
        this.activePlayerId = activePlayerId;
        this.board = board;
        this.roundCounter = roundCounter;
        useRandom = false;
    }

    public GameProcessor(final int activePlayerId, final BattleArena board,
                         final int roundCounter, final boolean useRandom) {
        waitingPlayerId = board.getEnemyId(activePlayerId);
        this.activePlayerId = activePlayerId;
        this.board = board;
        this.roundCounter = roundCounter;
        this.useRandom = useRandom;
    }

    public int getRoundCounter() {
        return roundCounter;
    }

    public void setRoundCounter(final int roundCounter) {
        this.roundCounter = roundCounter;
    }

    public BattleArena getBoard() {
        return board;
    }

    public void setBoard(final BattleArena board) {
        this.board = board;
    }

    public Army getActivePlayerArmy() {
        return board.getArmy(activePlayerId);
    }

    public Army getWaitingPlayerArmy() {
        return board.getArmy(waitingPlayerId);
    }

    public int getActivePlayerId() {
        return activePlayerId;
    }

    public void swapActivePlayer() {
        final int temp = activePlayerId;
        activePlayerId = waitingPlayerId;
        waitingPlayerId = temp;
    }

    public void handleAnswer(final Answer answer) throws HeroExceptions {
        toAct(answer);
        if (board.haveAvailableHeroByArmyId(waitingPlayerId)) {
            swapActivePlayer();
        } else if (board.noOneCanAct()) {
            board.endRound();
            roundCounter++;
            swapActivePlayer();
        }
    }

    /**
     * Применяем выбранное игроком действие
     */
    private void toAct(final Answer answer) throws HeroExceptions {
        final Hero activeHero = board.getArmy(activePlayerId).getHero(answer.getActiveHeroCoordinate())
                .orElseThrow(() -> new HeroExceptions(HeroErrorCode.ERROR_ACTIVE_UNIT));

        if (activeHero.isDefence()) {
            activeHero.cancelDefence();
        }

        switch (answer.getAction()) {
            case DEFENCE -> activeHero.setDefence();
            case ATTACK -> {
                if (useRandom) {
                    activeHero.toActWithPrecision(answer.getTargetUnitCoordinate(), board.getArmy(waitingPlayerId));
                } else {
                    activeHero.toAct(answer.getTargetUnitCoordinate(), board.getArmy(waitingPlayerId));
                }
                if (answer.getTargetUnitCoordinate().equals(coordinateDoesntMatters)) {
                    tryToKillAll(board.getArmy(waitingPlayerId));
                } else {
                    tryToKill(answer.getTargetUnitCoordinate(), board.getArmy(waitingPlayerId));
                }
            }
            case HEAL -> activeHero.toAct(answer.getTargetUnitCoordinate(), board.getArmy(activePlayerId));
        }
        removeUsedHero(activePlayerId, answer.getActiveHeroCoordinate());
    }

    public List<Answer> getAllActionsForCurrentPlayer() {
        return board.getAllActionForPlayer(activePlayerId);
    }

    private void tryToKillAll(final Army army) {
        final Set<SquareCoordinate> keys = new HashSet<>(army.getHeroes().keySet());
        keys.forEach(army::tryToKill);
    }

    private void tryToKill(final SquareCoordinate target, final Army army) {
        army.tryToKill(target);
    }

    public GameEvent matchOver() {
        if (board.isArmyDied(waitingPlayerId)) {
            return GameEvent.YOU_WIN_GAME;
        } else if (board.isArmyDied(activePlayerId)) {
            return GameEvent.YOU_LOSE_GAME;
        } else if (roundCounter >= MAX_ROUND) {
            return GameEvent.GAME_END_WITH_A_TIE;
        }
        return GameEvent.NOTHING_HAPPEN;
    }

    private void removeUsedHero(final int activePlayerId, final SquareCoordinate activeUnitCoordinate) {
        board.removeUsedHeroesById(activeUnitCoordinate, activePlayerId);
    }

}
