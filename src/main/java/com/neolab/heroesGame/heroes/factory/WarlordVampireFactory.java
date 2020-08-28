package com.neolab.heroesGame.heroes.factory;

import com.neolab.heroesGame.aditional.HeroConfigManager;
import com.neolab.heroesGame.aditional.PropertyUtils;
import com.neolab.heroesGame.heroes.Hero;
import com.neolab.heroesGame.heroes.WarlordVampire;

import java.io.IOException;
import java.util.Properties;

public class WarlordVampireFactory implements HeroFactory {

    private final Properties prop;

    public WarlordVampireFactory() throws IOException {
        prop = HeroConfigManager.getHeroConfig();
    }

    @Override
    public Hero create() {
        final int hp = PropertyUtils.getIntegerFromProperty(prop, "warlord.vampire.hp");
        final int damage = PropertyUtils.getIntegerFromProperty(prop, "warlord.vampire.damage");
        final float armor = PropertyUtils.getFloatFromProperty(prop, "warlord.vampire.armor");
        final float precision = PropertyUtils.getFloatFromProperty(prop, "warlord.vampire.precision");
        return new WarlordVampire(hp, damage, precision, armor);
    }
}
