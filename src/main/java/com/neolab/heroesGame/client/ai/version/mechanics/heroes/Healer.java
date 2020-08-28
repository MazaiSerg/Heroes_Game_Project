package com.neolab.heroesGame.client.ai.version.mechanics.heroes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.neolab.heroesGame.aditional.HeroConfigManager;
import com.neolab.heroesGame.aditional.PropertyUtils;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Army;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.SquareCoordinate;
import com.neolab.heroesGame.enumerations.HeroErrorCode;
import com.neolab.heroesGame.errors.HeroExceptions;

import java.io.IOException;
import java.util.Properties;

public class Healer extends Hero {
    static private Properties prop = null;
    static private final Integer hpDefault;
    static private final Integer damageDefault;

    static {
        try {
            prop = HeroConfigManager.getHeroConfig();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        hpDefault = PropertyUtils.getIntegerFromProperty(prop, "hero.healer.hp");
        damageDefault = PropertyUtils.getIntegerFromProperty(prop, "hero.healer.heal");
    }

    @JsonCreator
    protected Healer(@JsonProperty("hpMax") final int hpMax, @JsonProperty("hp") final int hp,
                     @JsonProperty("damage") final int damage,
                     @JsonProperty("armor") final float armor,
                     @JsonProperty("defence") final boolean defence) {
        super(hpMax, hp, damage, armor, defence);
    }

    /**
     * Невозможно превысить максимальный запас здоровья
     *
     * @param position позиция героя
     * @param army     армия союзников
     */
    @Override
    public void toAct(final SquareCoordinate position, final Army army) throws HeroExceptions {
        final Hero targetHeal = army.getHero(position).orElseThrow(() -> new HeroExceptions(HeroErrorCode.ERROR_TARGET_HEAL));
        final int healing = targetHeal.getHp() + getDamage();
        targetHeal.setHp(Math.min(healing, targetHeal.getHpMax()));
    }

    @Override
    public String getClassName() {
        return "Лекарь";
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
        return 100;
    }
}