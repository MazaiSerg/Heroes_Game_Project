package com.neolab.heroesGame.heroes.factory;

import com.neolab.heroesGame.aditional.HeroConfigManager;
import com.neolab.heroesGame.aditional.PropertyUtils;
import com.neolab.heroesGame.heroes.Healer;
import com.neolab.heroesGame.heroes.Hero;

import java.io.IOException;
import java.util.Properties;

public class HealerFactory implements HeroFactory {

    private final Properties prop;

    public HealerFactory() throws IOException {
        prop = HeroConfigManager.getHeroConfig();
    }

    @Override
    public Hero create() {
        final int hp = PropertyUtils.getIntegerFromProperty(prop, "hero.healer.hp");
        final int heal = PropertyUtils.getIntegerFromProperty(prop, "hero.healer.heal");
        final float armor = PropertyUtils.getFloatFromProperty(prop, "hero.healer.armor");
        final float precision = PropertyUtils.getFloatFromProperty(prop, "hero.healer.precision");
        return new Healer(hp, heal, precision, armor);
    }
}
