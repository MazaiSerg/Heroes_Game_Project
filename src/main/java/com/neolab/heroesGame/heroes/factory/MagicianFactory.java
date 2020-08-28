package com.neolab.heroesGame.heroes.factory;

import com.neolab.heroesGame.aditional.HeroConfigManager;
import com.neolab.heroesGame.aditional.PropertyUtils;
import com.neolab.heroesGame.heroes.Hero;
import com.neolab.heroesGame.heroes.Magician;

import java.io.IOException;
import java.util.Properties;

public class MagicianFactory implements HeroFactory {

    private final Properties prop;

    public MagicianFactory() throws IOException {
        prop = HeroConfigManager.getHeroConfig();
    }

    @Override
    public Hero create() {
        final int hp = PropertyUtils.getIntegerFromProperty(prop, "hero.magician.hp");
        final int damage = PropertyUtils.getIntegerFromProperty(prop, "hero.magician.damage");
        final float armor = PropertyUtils.getFloatFromProperty(prop, "hero.magician.armor");
        final float precision = PropertyUtils.getFloatFromProperty(prop, "hero.magician.precision");
        return new Magician(hp, damage, precision, armor);
    }
}
