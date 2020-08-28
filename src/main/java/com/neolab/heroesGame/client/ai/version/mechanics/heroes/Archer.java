package com.neolab.heroesGame.client.ai.version.mechanics.heroes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.neolab.heroesGame.aditional.HeroConfigManager;
import com.neolab.heroesGame.aditional.PropertyUtils;

import java.io.IOException;
import java.util.Properties;

public class Archer extends Hero {
    static private Properties prop = null;
    static private final int hpDefault;
    static private final int damageDefault;
    static private final int precision;

    static {
        try {
            prop = HeroConfigManager.getHeroConfig();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        hpDefault = PropertyUtils.getIntegerFromProperty(prop, "hero.archer.hp");
        damageDefault = PropertyUtils.getIntegerFromProperty(prop, "hero.archer.damage");
        precision = (int) (100 * PropertyUtils.getFloatFromProperty(prop, "hero.archer.precision"));
    }

    @JsonCreator
    protected Archer(@JsonProperty("hpMax") final int hpMax, @JsonProperty("hp") final int hp,
                     @JsonProperty("damage") final int damage,
                     @JsonProperty("armor") final float armor,
                     @JsonProperty("defence") final boolean defence) {
        super(hpMax, hp, damage, armor, defence);
    }

    @Override
    public String getClassName() {
        return "Лучник";
    }

    @Override
    public int getHpDefault() {
        return hpDefault;
    }

    @Override
    public int getDamageDefault() {
        return damageDefault;
    }

    @Override
    public int getPrecision() {
        return precision;
    }
}
