package com.neolab.heroesGame.aditional;

import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.FactoryArmies;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.arena.StringArmyFactory;
import com.neolab.heroesGame.errors.HeroExceptions;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ArmyToStringTest {

    @Test
    public void armyToStringTest() throws HeroExceptions, IOException {
        final Army original = FactoryArmies.createRandomArmy();
        final String armyString = CommonFunction.ArmyCodeToString(original);
        final Army sameArmy = new StringArmyFactory(armyString).create();
        for (int j = 0; j < 2; ++j) {
            for (int i = 0; i < 3; ++i) {
                SquareCoordinate coordinate = new SquareCoordinate(i, j);
                assertEquals(original.getHero(coordinate).get().getClass(), sameArmy.getHero(coordinate).get().getClass());
            }
        }
    }

    @Test
    public void armyFromStringTest() throws HeroExceptions, IOException {
        final String army = "amhFff";
        final Army original = new StringArmyFactory(army).create();
        final String armyString = CommonFunction.ArmyCodeToString(original);
        assertEquals(army, armyString);
    }
}
