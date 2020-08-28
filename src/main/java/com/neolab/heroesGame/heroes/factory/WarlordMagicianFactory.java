package com.neolab.heroesGame.heroes.factory;

import com.neolab.heroesGame.aditional.HeroConfigManager;
import com.neolab.heroesGame.aditional.PropertyUtils;
import com.neolab.heroesGame.heroes.Hero;
import com.neolab.heroesGame.heroes.WarlordMagician;

import java.io.IOException;
import java.util.Properties;

public class WarlordMagicianFactory implements HeroFactory {

    private final Properties prop;

    public WarlordMagicianFactory() throws IOException {
        prop = HeroConfigManager.getHeroConfig();
    }

    @Override
    public Hero create() {
        final int hp = PropertyUtils.getIntegerFromProperty(prop, "warlord.magician.hp");
        final int damage = PropertyUtils.getIntegerFromProperty(prop, "warlord.magician.damage");
        final float armor = PropertyUtils.getFloatFromProperty(prop, "warlord.magician.armor");
        final float precision = PropertyUtils.getFloatFromProperty(prop, "warlord.magician.precision");
        return new WarlordMagician(hp, damage, precision, armor);
    }
}
