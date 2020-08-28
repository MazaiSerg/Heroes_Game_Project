package com.neolab.heroesGame.client.gui;

import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.client.dto.ExtendedServerResponse;
import com.neolab.heroesGame.enumerations.HeroActions;
import com.neolab.heroesGame.heroes.Hero;

import java.util.Map;

public class NullGraphics implements IGraphics {

    public NullGraphics() {

    }

    @Override
    public void showPosition(final ExtendedServerResponse response) {

    }

    @Override
    public void endGame(final ExtendedServerResponse response) {

    }

    @Override
    public SquareCoordinate chooseUnit(final Army army) {
        return null;
    }

    @Override
    public HeroActions chooseActionForHero(final SquareCoordinate coordinate, final Hero hero) {
        return null;
    }

    @Override
    public SquareCoordinate chooseTargetCoordinate(final SquareCoordinate activeHeroCoordinate, final Hero activeHero,
                                                   final Army army) {
        return null;
    }

    @Override
    public Hero getHeroChoose(final Map<Integer, Hero> army) {
        return null;
    }

    @Override
    public int getHeroPositionChoose(final Hero hero, final Map<Integer, Hero> army) {
        return 0;
    }

    @Override
    public boolean finishCreatingArmy(final Map<Integer, Hero> army) {
        return true;
    }

    @Override
    public Hero getHeroChoose(final Army enemyArmy, final Map<Integer, Hero> yourArmy) {
        return null;
    }

    @Override
    public int getHeroPositionChoose(final Hero hero, final Army enemyArmy, final Map<Integer, Hero> yourArmy) {
        return -1;
    }

    @Override
    public boolean finishCreatingArmy(final Army enemyArmy, final Map<Integer, Hero> yourArmy) {
        return false;
    }

}
