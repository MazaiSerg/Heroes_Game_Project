package com.neolab.heroesGame.arena;

import com.neolab.heroesGame.errors.HeroExceptions;

public interface ArmyFactory {
    Army create() throws HeroExceptions;
}
