package com.neolab.heroesGame;

import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.arena.FactoryArmies;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.heroes.*;
import com.neolab.heroesGame.heroes.factory.*;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class CloneTest {

    @Test
    public void cloneArcherTest() throws IOException {
        final Hero hero = new ArcherFactory().create();
        final Hero clone = hero.getCopy();
        assertEquals(hero, clone);
        assertNotSame(hero, clone);
        assertTrue(clone instanceof Archer);
    }

    @Test
    public void cloneFootmanTest() throws IOException {
        final Hero hero = new FootmanFactory().create();
        final Hero clone = hero.getCopy();
        assertEquals(hero, clone);
        assertNotSame(hero, clone);
        assertTrue(clone instanceof Footman);
    }

    @Test
    public void cloneHealerTest() throws IOException {
        final Hero hero = new HealerFactory().create();
        final Hero clone = hero.getCopy();
        assertEquals(hero, clone);
        assertNotSame(hero, clone);
        assertTrue(clone instanceof Healer);
    }

    @Test
    public void cloneMagicianTest() throws IOException {
        final Hero hero = new MagicianFactory().create();
        final Hero clone = hero.getCopy();
        assertEquals(hero, clone);
        assertNotSame(hero, clone);
        assertTrue(clone instanceof Magician);
    }

    @Test
    public void cloneWarlordFootmanTest() throws IOException {
        final Hero hero = new WarlordFootmanFactory().create();
        final Hero clone = hero.getCopy();
        assertEquals(hero, clone);
        assertNotSame(hero, clone);
        assertTrue(clone instanceof WarlordFootman);
    }

    @Test
    public void cloneWarlordMagicianTest() throws IOException {
        final Hero hero = new WarlordMagicianFactory().create();
        final Hero clone = hero.getCopy();
        assertEquals(hero, clone);
        assertNotSame(hero, clone);
        assertTrue(clone instanceof WarlordMagician);
    }

    @Test
    public void cloneWarlordVampireTest() throws IOException {
        final Hero hero = new WarlordVampireFactory().create();
        final Hero clone = hero.getCopy();
        assertEquals(hero, clone);
        assertNotSame(hero, clone);
        assertTrue(clone instanceof WarlordVampire);
    }

    @Test
    public void cloneArmyTest() throws HeroExceptions, IOException {
        final Army army = FactoryArmies.createRandomArmy();
        final Army clone = army.getCopy();
        assertEquals(army, clone);
        assertNotSame(army, clone);


        assertEquals(army.getHeroes(), clone.getHeroes());
        assertNotSame(army.getHeroes(), clone.getHeroes());
        assertEquals(army.getWarlord(), clone.getWarlord());
        assertNotSame(army.getWarlord(), clone.getWarlord());
        assertEquals(army.getAvailableHeroes(), clone.getAvailableHeroes());
        assertNotSame(army.getAvailableHeroes(), clone.getAvailableHeroes());

        army.getHeroes().keySet().forEach((key -> {
            assertEquals(army.getHero(key), clone.getHero(key));
            if (army.getHero(key).isPresent() && clone.getHero(key).isPresent()) {
                assertNotSame(army.getHero(key).get(), clone.getHero(key).get());
            } else if (army.getHero(key).isPresent() || clone.getHero(key).isPresent()) {
                fail();
            }
        }));

        army.getAvailableHeroes().keySet().forEach((key) -> {
            assertEquals(army.getAvailableHeroes().get(key), clone.getAvailableHeroes().get(key));
            assertNotSame(army.getAvailableHeroes().get(key), clone.getAvailableHeroes().get(key));
        });

        clone.getHeroes().values().forEach(val -> assertTrue(clone.getAvailableHeroes().containsValue(val)));
    }

    @Test
    public void cloneArenaTest() throws HeroExceptions, IOException {
        final BattleArena arena = new BattleArena(FactoryArmies.generateArmies(1, 2));
        final BattleArena clone = arena.getCopy();

        assertEquals(arena, clone);
        assertNotSame(arena, clone);

        arena.getArmies().keySet().forEach((key) -> {
            assertEquals(arena.getArmy(key), clone.getArmy(key));
            assertNotSame(arena.getArmy(key), clone.getArmy(key));
        });
    }
}
