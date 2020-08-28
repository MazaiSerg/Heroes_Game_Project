package com.neolab.heroesGame.arena;

import com.neolab.heroesGame.errors.HeroExceptions;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class FabricArmiesTest {

    @Test
    public void testCreateBattleArena() throws HeroExceptions, IOException {
        final Map<Integer, Army> armies = FactoryArmies.generateArmies(1, 2);
        assertEquals(6, armies.get(1).getHeroes().size());
        assertEquals(6, armies.get(2).getHeroes().size());
        assertNotNull(armies.get(1).getWarlord());
        assertNotNull(armies.get(2).getWarlord());
    }
}
