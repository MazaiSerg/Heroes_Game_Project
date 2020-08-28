package com.neolab.heroesGame.enumerations;

public enum GameEvent {
    CLIENT_IS_CREATED(""),
    MAX_COUNT_PLAYERS(""),
    NEW_ROUND_STARTED(""),
    NEW_GAME_STARTED(""),
    GAME_END_WITH_A_TIE("Draw"),
    YOU_WIN_GAME("Win"),
    YOU_LOSE_GAME("Lose"),
    NOTHING_HAPPEN(""),
    NOW_YOUR_TURN(""),
    WAIT_ITS_NOT_YOUR_TURN(""),
    ARMY_IS_CREATED("Армия была успешно создана"),
    ERROR_ARMY_CREATED("Ошибка создания армии");

    private final String description;

    GameEvent(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
