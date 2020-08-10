package com.neolab.heroesGame.client.ai.version.mechanics.heroes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.neolab.heroesGame.aditional.HeroConfigManager;
import com.neolab.heroesGame.aditional.PropertyUtils;

import java.io.IOException;
import java.util.Properties;

public class Footman extends Hero {
    static private Properties prop = null;
    static private final Integer hpDefault;
    static private final Integer damageDefault;
    static private final Float armorDefault;

    static {
        try {
            prop = HeroConfigManager.getHeroConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
        hpDefault = PropertyUtils.getIntegerFromProperty(prop, "hero.footman.hp");
        damageDefault = PropertyUtils.getIntegerFromProperty(prop, "hero.footman.damage");
        armorDefault = PropertyUtils.getFloatFromProperty(prop, "hero.footman.armor");
    }

    @JsonCreator
    protected Footman(@JsonProperty("unitId") final int unitId,
                      @JsonProperty("hpMax") final int hpMax, @JsonProperty("hp") final int hp,
                      @JsonProperty("damage") final int damage,
                      @JsonProperty("armor") final float armor,
                      @JsonProperty("defence") final boolean defence) {
        super(unitId, hpMax, hp, damage, armor, defence);
    }

    @Override
    public String getClassName() {
        return "Мечник";
    }

    @Override
    public float getArmorDefault() {
        return armorDefault;
    }

    @Override
    public int getHpDefault() {
        return hpDefault;
    }

    @Override
    public int getDamageDefault() {
        return damageDefault;
    }
}