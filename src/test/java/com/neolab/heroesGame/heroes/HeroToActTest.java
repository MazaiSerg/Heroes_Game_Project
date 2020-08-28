package com.neolab.heroesGame.heroes;

import com.neolab.heroesGame.aditional.GetLegalTargetTest;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.heroes.factory.ArcherFactory;
import com.neolab.heroesGame.heroes.factory.HealerFactory;
import com.neolab.heroesGame.heroes.factory.MagicianFactory;
import com.neolab.heroesGame.heroes.factory.WarlordVampireFactory;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.spy;

public class HeroToActTest {

    @Test
    public void healerToActTest() throws HeroExceptions, IOException {
        final Hero healer = new HealerFactory().create();
        Map<SquareCoordinate, Integer> effect;
        final SquareCoordinate firstPosition = new SquareCoordinate(0, 0);
        final SquareCoordinate secondPosition = new SquareCoordinate(1, 1);
        final SquareCoordinate thirdPosition = new SquareCoordinate(2, 0);

        final Set<SquareCoordinate> coordHeroes1 = new HashSet<>();
        coordHeroes1.add(firstPosition);
        coordHeroes1.add(secondPosition);
        coordHeroes1.add(thirdPosition);
        final Army army = GetLegalTargetTest.getArmyByCoords(coordHeroes1);
        assertTrue(army.getHero(firstPosition).isPresent());
        assertTrue(army.getHero(secondPosition).isPresent());

        effect = healer.toAct(firstPosition, army);
        assertEquals(1, effect.size());
        assertNotNull(effect.get(firstPosition));
        assertEquals(army.getHero(firstPosition).get().getHpMax(), army.getHero(firstPosition).get().getHp());

        final Hero hero = army.getHero(secondPosition).get();
        final int damageForTest = 10;
        hero.setHp(hero.getHpMax() - healer.getDamage() - damageForTest);
        effect = healer.toAct(secondPosition, army);
        assertEquals(1, effect.size());
        assertNotNull(effect.get(secondPosition));
        assertEquals(healer.getDamage(), effect.get(secondPosition).intValue());
        assertEquals(hero.getHpMax() - damageForTest, hero.getHp());
    }

    @Test(expected = HeroExceptions.class)
    public void wrongTargetForHealer() throws HeroExceptions, IOException {
        final Hero healer = new HealerFactory().create();
        final SquareCoordinate firstPosition = new SquareCoordinate(0, 0);
        final SquareCoordinate secondPosition = new SquareCoordinate(1, 1);
        final SquareCoordinate thirdPosition = new SquareCoordinate(2, 0);

        final Set<SquareCoordinate> coordHeroes1 = new HashSet<>();
        coordHeroes1.add(firstPosition);
        coordHeroes1.add(secondPosition);
        coordHeroes1.add(thirdPosition);
        final Army army = GetLegalTargetTest.getArmyByCoords(coordHeroes1);
        healer.toAct(new SquareCoordinate(2, 2), army);
    }

    @Test
    public void soloTargetAttackToActTest() throws HeroExceptions, IOException {
        final Hero archer = spy(new ArcherFactory().create());
        final int archerDamage = archer.getDamage();
        Map<SquareCoordinate, Integer> effect;
        final SquareCoordinate firstPosition = new SquareCoordinate(0, 0);
        final SquareCoordinate secondPosition = new SquareCoordinate(1, 1);
        final SquareCoordinate thirdPosition = new SquareCoordinate(2, 0);
        Mockito.when(archer.isHit(anyFloat())).thenReturn(true);
        Mockito.when(archer.randomIncreaseDamage(anyInt())).thenReturn(archerDamage);

        final Set<SquareCoordinate> coordHeroes1 = new HashSet<>();
        coordHeroes1.add(firstPosition);
        coordHeroes1.add(secondPosition);
        coordHeroes1.add(thirdPosition);
        final Army army = GetLegalTargetTest.getArmyByCoords(coordHeroes1);
        assertTrue(army.getHero(firstPosition).isPresent());
        assertTrue(army.getHero(secondPosition).isPresent());

        Hero hero = army.getHero(firstPosition).get();
        effect = archer.toAct(firstPosition, army);
        assertEquals(1, effect.size());
        assertNotNull(effect.get(firstPosition));
        int expectedDamage = Math.round(archer.getDamage() - archer.getDamage() * hero.getArmor());
        assertEquals(expectedDamage, effect.get(firstPosition).intValue());

        hero = army.getHero(secondPosition).get();
        effect = archer.toAct(secondPosition, army);
        assertEquals(1, effect.size());
        assertNotNull(effect.get(secondPosition));
        expectedDamage = Math.round(archer.getDamage() - archer.getDamage() * hero.getArmor());
        assertEquals(expectedDamage, effect.get(secondPosition).intValue());
    }

    @Test(expected = HeroExceptions.class)
    public void wrongTargetForArcher() throws HeroExceptions, IOException {
        final Hero archer = new ArcherFactory().create();
        final SquareCoordinate firstPosition = new SquareCoordinate(0, 0);
        final SquareCoordinate secondPosition = new SquareCoordinate(1, 1);
        final SquareCoordinate thirdPosition = new SquareCoordinate(2, 0);

        final Set<SquareCoordinate> coordHeroes1 = new HashSet<>();
        coordHeroes1.add(firstPosition);
        coordHeroes1.add(secondPosition);
        coordHeroes1.add(thirdPosition);
        final Army army = GetLegalTargetTest.getArmyByCoords(coordHeroes1);
        archer.toAct(new SquareCoordinate(2, 2), army);
    }

    @Test
    public void magicianTargetAttackToActTest() throws HeroExceptions, IOException {
        final Hero magician = spy(new MagicianFactory().create());
        final int magicianDamage = magician.getDamage();
        final Map<SquareCoordinate, Integer> effect;
        final SquareCoordinate firstPosition = new SquareCoordinate(0, 0);
        final SquareCoordinate secondPosition = new SquareCoordinate(1, 1);
        final SquareCoordinate thirdPosition = new SquareCoordinate(2, 0);
        Mockito.when(magician.isHit(anyFloat())).thenReturn(true);
        Mockito.when(magician.randomIncreaseDamage(anyInt())).thenReturn(magicianDamage);

        final Set<SquareCoordinate> coordHeroes1 = new HashSet<>();
        coordHeroes1.add(firstPosition);
        coordHeroes1.add(secondPosition);
        coordHeroes1.add(thirdPosition);
        final Army army = GetLegalTargetTest.getArmyByCoords(coordHeroes1);
        assertTrue(army.getHero(firstPosition).isPresent());
        assertTrue(army.getHero(secondPosition).isPresent());

        effect = magician.toAct(firstPosition, army);
        assertEquals(3, effect.size());

        Hero hero = army.getHero(firstPosition).get();
        assertNotNull(effect.get(firstPosition));
        int expectedDamage = Math.round(magician.getDamage() - magician.getDamage() * hero.getArmor());
        assertEquals(expectedDamage, effect.get(firstPosition).intValue());

        hero = army.getHero(secondPosition).get();
        assertNotNull(effect.get(firstPosition));
        expectedDamage = Math.round(magician.getDamage() - magician.getDamage() * hero.getArmor());
        assertEquals(expectedDamage, effect.get(secondPosition).intValue());

        hero = army.getHero(thirdPosition).get();
        assertNotNull(effect.get(firstPosition));
        expectedDamage = Math.round(magician.getDamage() - magician.getDamage() * hero.getArmor());
        assertEquals(expectedDamage, effect.get(thirdPosition).intValue());
    }

    @Test
    public void vampireTargetAttackToActTest() throws HeroExceptions, IOException {
        final Hero vampire = spy(new WarlordVampireFactory().create());
        final int vampireDamage = vampire.getDamage();
        final Map<SquareCoordinate, Integer> effect;
        final SquareCoordinate firstPosition = new SquareCoordinate(0, 0);
        final SquareCoordinate secondPosition = new SquareCoordinate(1, 1);
        final SquareCoordinate thirdPosition = new SquareCoordinate(2, 0);
        Mockito.when(vampire.isHit(anyFloat())).thenReturn(true);
        Mockito.when(vampire.randomIncreaseDamage(anyInt())).thenReturn(vampireDamage);
        vampire.setHp(1);

        final Set<SquareCoordinate> coordHeroes1 = new HashSet<>();
        coordHeroes1.add(firstPosition);
        coordHeroes1.add(secondPosition);
        coordHeroes1.add(thirdPosition);
        final Army army = GetLegalTargetTest.getArmyByCoords(coordHeroes1);
        assertTrue(army.getHero(firstPosition).isPresent());
        assertTrue(army.getHero(secondPosition).isPresent());
        assertTrue(army.getHero(thirdPosition).isPresent());

        effect = vampire.toAct(firstPosition, army);
        int health = 1;
        for (final Integer value : effect.values()) {
            health += value;
        }
        assertEquals(3, effect.size());
        assertEquals(health, vampire.getHp());

        Hero hero = army.getHero(firstPosition).get();
        assertNotNull(effect.get(firstPosition));
        int expectedDamage = Math.round(vampire.getDamage() - vampire.getDamage() * hero.getArmor());
        assertEquals(expectedDamage, effect.get(firstPosition).intValue());

        hero = army.getHero(secondPosition).get();
        assertNotNull(effect.get(firstPosition));
        expectedDamage = Math.round(vampire.getDamage() - vampire.getDamage() * hero.getArmor());
        assertEquals(expectedDamage, effect.get(secondPosition).intValue());

        hero = army.getHero(thirdPosition).get();
        assertNotNull(effect.get(firstPosition));
        expectedDamage = Math.round(vampire.getDamage() - vampire.getDamage() * hero.getArmor());
        assertEquals(expectedDamage, effect.get(thirdPosition).intValue());
    }
}
