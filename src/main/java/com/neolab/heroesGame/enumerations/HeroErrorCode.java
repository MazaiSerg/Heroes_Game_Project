package com.neolab.heroesGame.enumerations;

public enum HeroErrorCode {
    ERROR_TARGET_ATTACK("Невозможно установить цель атаки"),
    ERROR_TARGET_HEAL("Невозможно установить цель лечения"),
    ERROR_ACTIVE_UNIT("Невозможно выбрать данного юнита"),
    ERROR_UNIT_ATTACK("Этот юнит не может атаковать"),
    ERROR_UNIT_HEAL("Этот юнит не может лечить"),
    ERROR_ON_BATTLE_ARENA("Произошло что-то непредвиденное"),
    ERROR_ANSWER("Невозможно установить ход"),
    ERROR_SECOND_WARLORD_ON_ARMY("В армии два варлорда. Должен остаться только один"),
    ERROR_EVENT("Неизвестное событие"),
    ERROR_EMPTY_WARLORD("Армия не может быть создана без варлорда");

    private final String errorString;

    HeroErrorCode(final String errorString) {
        this.errorString = errorString;
    }

    public String getErrorString() {
        return errorString;
    }
}
