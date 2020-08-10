package com.neolab.heroesGame.client.ai.version.mechanics.heroes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.neolab.heroesGame.aditional.HeroConfigManager;
import com.neolab.heroesGame.aditional.PropertyUtils;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Army;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.SquareCoordinate;

import java.io.IOException;
import java.util.Properties;

public class Magician extends Hero {
    static private Properties prop = null;
    static private final Integer hpDefault;
    static private final Integer damageDefault;

    static {
        try {
            prop = HeroConfigManager.getHeroConfig();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        hpDefault = PropertyUtils.getIntegerFromProperty(prop, "hero.magician.hp");
        damageDefault = PropertyUtils.getIntegerFromProperty(prop, "hero.magician.damage");
    }

    @JsonCreator
    protected Magician(@JsonProperty("unitId") final int unitId,
                       @JsonProperty("hpMax") final int hpMax, @JsonProperty("hp") final int hp,
                       @JsonProperty("damage") final int damage,
                       @JsonProperty("armor") final float armor,
                       @JsonProperty("defence") final boolean defence) {
        super(unitId, hpMax, hp, damage, armor, defence);
    }

    @Override
    public void toAct(final SquareCoordinate position, final Army army) {
        army.getHeroes().keySet().forEach(coordinate -> {
            final Hero h = army.getHero(coordinate).orElseThrow();
            h.setHp(h.getHp() - calculateDamage(h));
        });
    }

    @Override
    public String getClassName() {
        return "Маг";
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
