package com.neolab.heroesGame.arena;

import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.heroes.*;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class ArmyFactoryTest {
    @Test
    public void stringArmyFactoryTest() throws HeroExceptions, IOException {
        final String armyStr = "affVmh";
        final ArmyFactory factory = new StringArmyFactory(armyStr);
        final Army army = factory.create();
        assertTrue(army.getHero(new SquareCoordinate(0, 0)).get() instanceof Archer);
        assertTrue(army.getHero(new SquareCoordinate(1, 0)).get() instanceof Footman);
        assertTrue(army.getHero(new SquareCoordinate(2, 0)).get() instanceof Footman);
        assertTrue(army.getHero(new SquareCoordinate(0, 1)).get() instanceof WarlordVampire);
        assertTrue(army.getHero(new SquareCoordinate(1, 1)).get() instanceof Magician);
        assertTrue(army.getHero(new SquareCoordinate(2, 1)).get() instanceof Healer);

    }
}
