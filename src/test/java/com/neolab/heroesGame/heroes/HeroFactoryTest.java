package com.neolab.heroesGame.heroes;

import com.neolab.heroesGame.heroes.factory.*;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class HeroFactoryTest {
    private final Properties prop = new Properties();

    @Before
    public void initProp() throws IOException {
        final String propertyFileName = "src/main/resources/hero.config";
        try (final InputStream is = new FileInputStream(propertyFileName)) {
            prop.load(is);
        }
    }

    @Test
    public void archerFactoryTest() throws IOException {
        final HeroFactory factory = new ArcherFactory();
        final Hero h = factory.create();
        final int hp = Integer.parseInt(prop.getProperty("hero.archer.hp"));
        final int damage = Integer.parseInt(prop.getProperty("hero.archer.damage"));
        final float armor = Float.parseFloat(prop.getProperty("hero.archer.armor"));
        final float precision = Float.parseFloat(prop.getProperty("hero.archer.precision"));
        assertEquals(hp, h.getHp());
        assertEquals(damage, h.getDamage());
        assertEquals(armor, h.getArmor(), 0);
        assertEquals(precision, h.getPrecision(), 0);
    }

    @Test
    public void footmanFactoryTest() throws IOException {
        final HeroFactory factory = new FootmanFactory();
        final Hero h = factory.create();
        final int hp = Integer.parseInt(prop.getProperty("hero.footman.hp"));
        final int damage = Integer.parseInt(prop.getProperty("hero.footman.damage"));
        final float armor = Float.parseFloat(prop.getProperty("hero.footman.armor"));
        final float precision = Float.parseFloat(prop.getProperty("hero.footman.precision"));
        assertEquals(hp, h.getHp());
        assertEquals(damage, h.getDamage());
        assertEquals(armor, h.getArmor(), 0);
        assertEquals(precision, h.getPrecision(), 0);
    }

    @Test
    public void healerFactoryTest() throws IOException {
        final HeroFactory factory = new HealerFactory();
        final Hero h = factory.create();
        final int hp = Integer.parseInt(prop.getProperty("hero.healer.hp"));
        final int heal = Integer.parseInt(prop.getProperty("hero.healer.heal"));
        final float armor = Float.parseFloat(prop.getProperty("hero.healer.armor"));
        final float precision = Float.parseFloat(prop.getProperty("hero.healer.precision"));
        assertEquals(hp, h.getHp());
        assertEquals(heal, h.getDamage());
        assertEquals(armor, h.getArmor(), 0);
        assertEquals(precision, h.getPrecision(), 0);
    }

    @Test
    public void magicianFactoryTest() throws IOException {
        final HeroFactory factory = new MagicianFactory();
        final Hero h = factory.create();
        final int hp = Integer.parseInt(prop.getProperty("hero.magician.hp"));
        final int damage = Integer.parseInt(prop.getProperty("hero.magician.damage"));
        final float armor = Float.parseFloat(prop.getProperty("hero.magician.armor"));
        final float precision = Float.parseFloat(prop.getProperty("hero.magician.precision"));
        assertEquals(hp, h.getHp());
        assertEquals(damage, h.getDamage());
        assertEquals(armor, h.getArmor(), 0);
        assertEquals(precision, h.getPrecision(), 0);
    }

    @Test
    public void warlordFootmanFactoryTest() throws IOException {
        final HeroFactory factory = new WarlordFootmanFactory();
        final Hero h = factory.create();
        final int hp = Integer.parseInt(prop.getProperty("warlord.footman.hp"));
        final int damage = Integer.parseInt(prop.getProperty("warlord.footman.damage"));
        final float armor = Float.parseFloat(prop.getProperty("warlord.footman.armor"));
        final float precision = Float.parseFloat(prop.getProperty("warlord.footman.precision"));
        assertEquals(hp, h.getHp());
        assertEquals(damage, h.getDamage());
        assertEquals(armor, h.getArmor(), 0);
        assertEquals(precision, h.getPrecision(), 0);
    }

    @Test
    public void warlordMagicianFactoryTest() throws IOException {
        final HeroFactory factory = new WarlordMagicianFactory();
        final Hero h = factory.create();
        final int hp = Integer.parseInt(prop.getProperty("warlord.magician.hp"));
        final int damage = Integer.parseInt(prop.getProperty("warlord.magician.damage"));
        final float armor = Float.parseFloat(prop.getProperty("warlord.magician.armor"));
        final float precision = Float.parseFloat(prop.getProperty("warlord.magician.precision"));
        assertEquals(hp, h.getHp());
        assertEquals(damage, h.getDamage());
        assertEquals(armor, h.getArmor(), 0);
        assertEquals(precision, h.getPrecision(), 0);
    }

    @Test
    public void warlordVampireFactoryTest() throws IOException {
        final HeroFactory factory = new WarlordVampireFactory();
        final Hero h = factory.create();
        final int hp = Integer.parseInt(prop.getProperty("warlord.vampire.hp"));
        final int damage = Integer.parseInt(prop.getProperty("warlord.vampire.damage"));
        final float armor = Float.parseFloat(prop.getProperty("warlord.vampire.armor"));
        final float precision = Float.parseFloat(prop.getProperty("warlord.vampire.precision"));
        assertEquals(hp, h.getHp());
        assertEquals(damage, h.getDamage());
        assertEquals(armor, h.getArmor(), 0);
        assertEquals(precision, h.getPrecision(), 0);
    }
}
