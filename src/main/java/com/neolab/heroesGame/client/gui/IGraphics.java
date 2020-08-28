package com.neolab.heroesGame.client.gui;

import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.client.dto.ExtendedServerResponse;
import com.neolab.heroesGame.enumerations.HeroActions;
import com.neolab.heroesGame.heroes.Hero;

import java.io.IOException;
import java.util.Map;

public interface IGraphics {

    /**
     * Отображаем текущую позицию на арене и последнее сделанное действие.
     *
     * @param response - Используется для отображения арены, эффекта прошлого действия.
     */
    void showPosition(final ExtendedServerResponse response) throws IOException;

    void endGame(final ExtendedServerResponse response) throws IOException;

    SquareCoordinate chooseUnit(final Army army) throws IOException;

    HeroActions chooseActionForHero(final SquareCoordinate coordinate, final Hero hero) throws IOException;

    SquareCoordinate chooseTargetCoordinate(final SquareCoordinate activeHeroCoordinate, final Hero activeHero,
                                            final Army army) throws IOException;

    Hero getHeroChoose(Map<Integer, Hero> army) throws IOException;

    int getHeroPositionChoose(Hero hero, Map<Integer, Hero> army) throws IOException;

    boolean finishCreatingArmy(Map<Integer, Hero> army) throws IOException;

    Hero getHeroChoose(Army enemyArmy, Map<Integer, Hero> yourArmy) throws IOException;

    int getHeroPositionChoose(Hero hero, Army enemyArmy, Map<Integer, Hero> yourArmy) throws IOException;

    boolean finishCreatingArmy(Army enemyArmy, Map<Integer, Hero> yourArmy) throws IOException;
}
