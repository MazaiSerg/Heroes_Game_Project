package com.neolab.heroesGame.heroes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.enumerations.HeroErrorCode;
import com.neolab.heroesGame.errors.HeroExceptions;

import java.util.HashMap;
import java.util.Map;

public class Healer extends Hero {

    public Healer(final int hp, final int healing, final float precision, final float armor) {
        super(hp, healing, precision, armor);
    }

    @JsonCreator
    protected Healer(@JsonProperty("unitId") final int unitId, @JsonProperty("hpDefault") final int hpDefault,
                     @JsonProperty("hpMax") final int hpMax, @JsonProperty("hp") final int hp,
                     @JsonProperty("damageDefault") final int damageDefault, @JsonProperty("damage") final int damage,
                     @JsonProperty("precision") final float precision, @JsonProperty("armor") final float armor,
                     @JsonProperty("armorDefault") final float armorDefault,
                     @JsonProperty("defence") final boolean defence) {
        super(unitId, hpDefault, hpMax, hp, damageDefault, damage, precision, armor, armorDefault, defence);
    }

    /**
     * Невозможно превысить максимальный запас здоровья
     *
     * @param position позиция героя
     * @param army     армия союзников
     */
    @Override
    public Map<SquareCoordinate, Integer> toAct(final SquareCoordinate position, final Army army) throws HeroExceptions {
        final Hero targetHeal = army.getHero(position).orElseThrow(() -> new HeroExceptions(HeroErrorCode.ERROR_TARGET_ATTACK));
        final int healing = targetHeal.getHp() + this.getDamage();
        targetHeal.setHp(Math.min(healing, targetHeal.getHpMax()));
        final Map<SquareCoordinate, Integer> allyHeroPosHeal = new HashMap<>();
        allyHeroPosHeal.put(position, this.getDamage());
        return allyHeroPosHeal;
    }

    @Override
    public String getClassName() {
        return "Лекарь";
    }
}