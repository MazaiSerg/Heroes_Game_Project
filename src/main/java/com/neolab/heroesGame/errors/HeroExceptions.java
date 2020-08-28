package com.neolab.heroesGame.errors;

import com.neolab.heroesGame.enumerations.HeroErrorCode;

public class HeroExceptions extends Exception {
    private final HeroErrorCode heroErrorCode;

    public HeroExceptions(final HeroErrorCode heroErrorCode) {
        this.heroErrorCode = heroErrorCode;
    }

    public HeroErrorCode getHeroErrorCode() {
        return heroErrorCode;
    }
}
