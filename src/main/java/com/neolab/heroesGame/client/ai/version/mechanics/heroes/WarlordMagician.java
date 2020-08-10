package com.neolab.heroesGame.client.ai.version.mechanics.heroes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.neolab.heroesGame.aditional.HeroConfigManager;
import com.neolab.heroesGame.aditional.PropertyUtils;

import java.io.IOException;
import java.util.Properties;

public class WarlordMagician extends Magician implements IWarlord {
    static private Properties prop = null;
    static private final Integer hpDefault;
    static private final Integer damageDefault;
    static private final Float armorDefault;
    private static final Float improveCoefficient = 0.05f;

    static {
        try {
            prop = HeroConfigManager.getHeroConfig();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        hpDefault = PropertyUtils.getIntegerFromProperty(prop, "warlord.magician.hp");
        damageDefault = PropertyUtils.getIntegerFromProperty(prop, "warlord.magician.damage");
        armorDefault = PropertyUtils.getFloatFromProperty(prop, "warlord.magician.armor");
    }

    @JsonCreator
    protected WarlordMagician(@JsonProperty("unitId") final int unitId,
                              @JsonProperty("hpMax") final int hpMax, @JsonProperty("hp") final int hp,
                              @JsonProperty("damage") final int damage,
                              @JsonProperty("armor") final float armor,
                              @JsonProperty("defence") final boolean defence) {
        super(unitId, hpMax, hp, damage, armor, defence);
    }

    public float getImproveCoefficient() {
        return improveCoefficient;
    }

    @Override
    public int getUnitId() {
        return super.getUnitId();
    }

    @Override
    public String getClassName() {
        return "Архимаг";
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
