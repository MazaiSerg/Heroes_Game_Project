package com.neolab.heroesGame.heroes.factory;

import com.neolab.heroesGame.aditional.HeroConfigManager;
import com.neolab.heroesGame.aditional.PropertyUtils;
import com.neolab.heroesGame.heroes.Archer;
import com.neolab.heroesGame.heroes.Hero;

import java.io.IOException;
import java.util.Properties;

public class ArcherFactory implements HeroFactory {

    private final Properties prop;

    public ArcherFactory() throws IOException {
        prop = HeroConfigManager.getHeroConfig();
    }

    @Override
    public Hero create() {
        final int hp = PropertyUtils.getIntegerFromProperty(prop, "hero.archer.hp");
        final int damage = PropertyUtils.getIntegerFromProperty(prop, "hero.archer.damage");
        final float armor = PropertyUtils.getFloatFromProperty(prop, "hero.archer.armor");
        final float precision = PropertyUtils.getFloatFromProperty(prop, "hero.archer.precision");
        return new Archer(hp, damage, precision, armor);
    }
}
