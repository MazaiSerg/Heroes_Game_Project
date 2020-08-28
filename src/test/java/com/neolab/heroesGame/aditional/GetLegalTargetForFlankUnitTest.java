package com.neolab.heroesGame.aditional;

import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.heroes.Hero;
import com.neolab.heroesGame.heroes.factory.FootmanFactory;
import com.neolab.heroesGame.heroes.factory.WarlordFootmanFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GetLegalTargetForFlankUnitTest {

    @Test
    public void testGetCorrectTargetForFootman1() throws HeroExceptions, IOException {
        final SquareCoordinate activeUnit = new SquareCoordinate(0, 1);
        final Map<SquareCoordinate, Hero> heroes = new HashMap<>();

        heroes.put(new SquareCoordinate(0, 1), new FootmanFactory().create());
        heroes.put(new SquareCoordinate(1, 1), new WarlordFootmanFactory().create());
        heroes.put(new SquareCoordinate(2, 1), new FootmanFactory().create());
        final Army enemy = new Army(heroes);
        final Set<SquareCoordinate> legalTarget = CommonFunction.getCorrectTargetForFootman(activeUnit, enemy);
        assertTrue(legalTarget.contains(new SquareCoordinate(0, 1)));
        assertTrue(legalTarget.contains(new SquareCoordinate(1, 1)));
        assertEquals(2, legalTarget.size());
    }

    @Test
    public void testGetCorrectTargetForFootman2() throws HeroExceptions, IOException {
        final SquareCoordinate activeUnit = new SquareCoordinate(0, 1);
        final Map<SquareCoordinate, Hero> heroes = new HashMap<>();

        heroes.put(new SquareCoordinate(1, 1), new FootmanFactory().create());
        heroes.put(new SquareCoordinate(2, 1), new WarlordFootmanFactory().create());
        final Army enemy = new Army(heroes);
        final Set<SquareCoordinate> legalTarget = CommonFunction.getCorrectTargetForFootman(activeUnit, enemy);
        assertTrue(legalTarget.contains(new SquareCoordinate(1, 1)));
        assertEquals(1, legalTarget.size());
    }

    @Test
    public void testGetCorrectTargetForFootman3() throws HeroExceptions, IOException {
        final SquareCoordinate activeUnit = new SquareCoordinate(0, 1);
        final Map<SquareCoordinate, Hero> heroes = new HashMap<>();

        heroes.put(new SquareCoordinate(2, 1), new WarlordFootmanFactory().create());
        final Army enemy = new Army(heroes);
        final Set<SquareCoordinate> legalTarget = CommonFunction.getCorrectTargetForFootman(activeUnit, enemy);
        assertTrue(legalTarget.contains(new SquareCoordinate(2, 1)));
        assertEquals(1, legalTarget.size());
    }


    @Test
    public void testGetCorrectTargetForFootman4() throws HeroExceptions, IOException {
        final SquareCoordinate activeUnit = new SquareCoordinate(0, 1);
        final Map<SquareCoordinate, Hero> heroes = new HashMap<>();

        heroes.put(new SquareCoordinate(0, 1), new WarlordFootmanFactory().create());
        heroes.put(new SquareCoordinate(2, 1), new FootmanFactory().create());
        final Army enemy = new Army(heroes);
        final Set<SquareCoordinate> legalTarget = CommonFunction.getCorrectTargetForFootman(activeUnit, enemy);
        assertTrue(legalTarget.contains(new SquareCoordinate(0, 1)));
        assertEquals(1, legalTarget.size());
    }

    @Test
    public void testGetCorrectTargetForFootman5() throws HeroExceptions, IOException {
        final SquareCoordinate activeUnit = new SquareCoordinate(0, 1);
        final Map<SquareCoordinate, Hero> heroes = new HashMap<>();

        heroes.put(new SquareCoordinate(0, 0), new WarlordFootmanFactory().create());
        heroes.put(new SquareCoordinate(1, 0), new FootmanFactory().create());
        heroes.put(new SquareCoordinate(2, 0), new FootmanFactory().create());
        final Army enemy = new Army(heroes);
        final Set<SquareCoordinate> legalTarget = CommonFunction.getCorrectTargetForFootman(activeUnit, enemy);
        assertTrue(legalTarget.contains(new SquareCoordinate(0, 0)));
        assertTrue(legalTarget.contains(new SquareCoordinate(1, 0)));
        assertEquals(2, legalTarget.size());
    }

    @Test
    public void testGetCorrectTargetForFootman6() throws HeroExceptions, IOException {
        final SquareCoordinate activeUnit = new SquareCoordinate(0, 1);
        final Map<SquareCoordinate, Hero> heroes = new HashMap<>();

        heroes.put(new SquareCoordinate(1, 0), new WarlordFootmanFactory().create());
        heroes.put(new SquareCoordinate(2, 0), new FootmanFactory().create());
        final Army enemy = new Army(heroes);
        final Set<SquareCoordinate> legalTarget = CommonFunction.getCorrectTargetForFootman(activeUnit, enemy);
        assertTrue(legalTarget.contains(new SquareCoordinate(1, 0)));
        assertEquals(1, legalTarget.size());
    }


    @Test
    public void testGetCorrectTargetForFootman7() throws HeroExceptions, IOException {
        final SquareCoordinate activeUnit = new SquareCoordinate(0, 1);
        final Map<SquareCoordinate, Hero> heroes = new HashMap<>();

        heroes.put(new SquareCoordinate(2, 0), new WarlordFootmanFactory().create());
        final Army enemy = new Army(heroes);
        final Set<SquareCoordinate> legalTarget = CommonFunction.getCorrectTargetForFootman(activeUnit, enemy);
        assertTrue(legalTarget.contains(new SquareCoordinate(2, 0)));
        assertEquals(1, legalTarget.size());
    }


    @Test
    public void testGetCorrectTargetForFootman8() throws HeroExceptions, IOException {
        final SquareCoordinate activeUnit = new SquareCoordinate(0, 1);
        final Map<SquareCoordinate, Hero> heroes = new HashMap<>();

        heroes.put(new SquareCoordinate(0, 0), new WarlordFootmanFactory().create());
        heroes.put(new SquareCoordinate(2, 0), new FootmanFactory().create());
        final Army enemy = new Army(heroes);
        final Set<SquareCoordinate> legalTarget = CommonFunction.getCorrectTargetForFootman(activeUnit, enemy);
        assertTrue(legalTarget.contains(new SquareCoordinate(0, 0)));
        assertEquals(1, legalTarget.size());
    }
}
