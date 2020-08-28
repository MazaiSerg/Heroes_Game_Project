package com.neolab.heroesGame.client.ai.version.mechanics.heroes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.neolab.heroesGame.aditional.HeroConfigManager;
import com.neolab.heroesGame.aditional.PropertyUtils;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Army;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.SquareCoordinate;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class WarlordVampire extends Magician implements IWarlord {
    static private Properties prop = null;
    static private final Integer hpDefault;
    static private final Integer damageDefault;
    static private final Float armorDefault;
    private static final Float improveCoefficient = 0.05f;
    static private final int precision;

    static {
        try {
            prop = HeroConfigManager.getHeroConfig();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        hpDefault = PropertyUtils.getIntegerFromProperty(prop, "warlord.vampire.hp");
        damageDefault = PropertyUtils.getIntegerFromProperty(prop, "warlord.vampire.damage");
        armorDefault = PropertyUtils.getFloatFromProperty(prop, "warlord.vampire.armor");
        precision = (int) (100 * PropertyUtils.getFloatFromProperty(prop, "warlord.vampire.precision"));
    }

    @JsonCreator
    protected WarlordVampire(@JsonProperty("hpMax") final int hpMax, @JsonProperty("hp") final int hp,
                             @JsonProperty("damage") final int damage,
                             @JsonProperty("armor") final float armor,
                             @JsonProperty("defence") final boolean defence) {
        super(hpMax, hp, damage, armor, defence);
    }

    public float getImproveCoefficient() {
        return improveCoefficient;
    }

    @Override
    public String getClassName() {
        return "Вампир";
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

    @Override
    public void toAct(final SquareCoordinate position, final Army army) {
        final AtomicInteger heal = new AtomicInteger(getHp());
        army.getHeroes().keySet().forEach(coordinate -> {
            final Hero h = army.getHero(coordinate).orElseThrow();
            final int damage = calculateDamage(h);
            h.setHp(h.getHp() - damage);
            heal.addAndGet(damage);
        });
        setHp(Math.min(heal.get(), getHpMax()));
    }

    @Override
    public void toActWithPrecision(final SquareCoordinate position, final Army army) {
        final AtomicInteger heal = new AtomicInteger(getHp());
        army.getHeroes().keySet().forEach(coordinate -> {
            final Hero h = army.getHero(coordinate).orElseThrow();
            final int damage = calculateDamageWithPrecision(h);
            h.setHp(h.getHp() - damage);
            heal.addAndGet(damage);
        });
        setHp(Math.min(heal.get(), getHpMax()));
    }

    @Override
    public int getPrecision() {
        return precision;
    }
}
