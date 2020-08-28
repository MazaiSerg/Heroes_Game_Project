package com.neolab.heroesGame.heroes;

import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.heroes.factory.ArcherFactory;
import com.neolab.heroesGame.heroes.factory.FootmanFactory;
import com.neolab.heroesGame.heroes.factory.WarlordFootmanFactory;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.spy;

public class HeroesTest {

    @Test
    public void heroesMethodsTest() throws HeroExceptions, IOException {
        final Hero targetHero = new WarlordFootmanFactory().create();
        final Hero activeHero = spy(new WarlordFootmanFactory().create());
        final int activeHeroDamage = activeHero.getDamage();
        final Map<SquareCoordinate, Hero> mapTarget = new HashMap<>();
        final SquareCoordinate targetCoord = new SquareCoordinate(0, 0);
        mapTarget.put(targetCoord, targetHero);
        final Army armyTarget = new Army(mapTarget);

        //testEquals
        assertNotEquals(activeHero, targetHero);

        // removeTargetTest
        final int oldSize = armyTarget.getHeroes().size();
        targetHero.setHp(0);
        armyTarget.killHero(targetCoord);
        final int newSize = armyTarget.getHeroes().size();
        assertNotEquals(oldSize, newSize);

        //calculateDamage
        final int damageByWarlordFootman = 51;
        final int damageByFootman = 54;
        final int damageByArcher = 60;
        Mockito.when(activeHero.randomIncreaseDamage(anyInt())).thenReturn(activeHeroDamage);
        final Hero footman = new FootmanFactory().create();
        final Hero archer = new ArcherFactory().create();
        assertEquals(damageByWarlordFootman, activeHero.calculateDamage(targetHero));
        assertEquals(damageByFootman, activeHero.calculateDamage(footman));
        assertEquals(damageByArcher, activeHero.calculateDamage(archer));

        //isHitTest
        assertTrue(activeHero.isHit(1.0f));
        assertFalse(activeHero.isHit(0));

        //setDefenseTest
        final float armor = activeHero.getArmor();
        final float armorDef = activeHero.getArmor() + 0.5f;
        activeHero.setDefence();
        assertTrue(activeHero.isDefence());
        assertEquals(Math.round(armorDef), Math.round(activeHero.getArmor()));
        activeHero.cancelDefence();
        assertFalse(activeHero.isDefence());
        assertEquals(Math.round(armor), Math.round(activeHero.getArmor()));

    }
}
