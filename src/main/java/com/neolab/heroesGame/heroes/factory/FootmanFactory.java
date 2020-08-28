package com.neolab.heroesGame.heroes.factory;

import com.neolab.heroesGame.aditional.HeroConfigManager;
import com.neolab.heroesGame.aditional.PropertyUtils;
import com.neolab.heroesGame.heroes.Footman;
import com.neolab.heroesGame.heroes.Hero;

import java.io.IOException;
import java.util.Properties;

public class FootmanFactory implements HeroFactory {

    private final Properties prop;

    public FootmanFactory() throws IOException {
        prop = HeroConfigManager.getHeroConfig();
    }

    @Override
    public Hero create() {
        final int hp = PropertyUtils.getIntegerFromProperty(prop, "hero.footman.hp");
        final int damage = PropertyUtils.getIntegerFromProperty(prop, "hero.footman.damage");
        final float armor = PropertyUtils.getFloatFromProperty(prop, "hero.footman.armor");
        final float precision = PropertyUtils.getFloatFromProperty(prop, "hero.footman.precision");
        return new Footman(hp, damage, precision, armor);
    }
}
