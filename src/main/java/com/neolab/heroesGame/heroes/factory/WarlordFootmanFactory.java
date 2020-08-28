package com.neolab.heroesGame.heroes.factory;

import com.neolab.heroesGame.aditional.HeroConfigManager;
import com.neolab.heroesGame.aditional.PropertyUtils;
import com.neolab.heroesGame.heroes.Hero;
import com.neolab.heroesGame.heroes.WarlordFootman;

import java.io.IOException;
import java.util.Properties;

public class WarlordFootmanFactory implements HeroFactory {

    private final Properties prop;

    public WarlordFootmanFactory() throws IOException {
        prop = HeroConfigManager.getHeroConfig();
    }

    @Override
    public Hero create() {
        final int hp = PropertyUtils.getIntegerFromProperty(prop, "warlord.footman.hp");
        final int damage = PropertyUtils.getIntegerFromProperty(prop, "warlord.footman.damage");
        final float armor = PropertyUtils.getFloatFromProperty(prop, "warlord.footman.armor");
        final float precision = PropertyUtils.getFloatFromProperty(prop, "warlord.footman.precision");
        return new WarlordFootman(hp, damage, precision, armor);
    }
}
