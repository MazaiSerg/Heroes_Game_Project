package com.neolab.heroesGame.heroes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.SquareCoordinate;

import java.util.HashMap;
import java.util.Map;

public class Magician extends Hero {

    public Magician(final int hp, final int damage, final float precision, final float armor) {
        super(hp, damage, precision, armor);
    }

    @JsonCreator
    protected Magician(@JsonProperty("unitId") final int unitId, @JsonProperty("hpDefault") final int hpDefault,
                       @JsonProperty("hpMax") final int hpMax, @JsonProperty("hp") final int hp,
                       @JsonProperty("damageDefault") final int damageDefault, @JsonProperty("damage") final int damage,
                       @JsonProperty("precision") final float precision, @JsonProperty("armor") final float armor,
                       @JsonProperty("armorDefault") final float armorDefault,
                       @JsonProperty("defence") final boolean defence) {
        super(unitId, hpDefault, hpMax, hp, damageDefault, damage, precision, armor, armorDefault, defence);
    }

    @Override
    public Map<SquareCoordinate, Integer> toAct(final SquareCoordinate position, final Army army) {
        final Map<SquareCoordinate, Integer> enemyHeroPosDamage = new HashMap<>();

        army.getHeroes().keySet().forEach(coordinate -> {
            final Hero h = army.getHero(coordinate).orElseThrow();
            int damageDone = 0;
            if (isHit(this.getPrecision())) {
                damageDone = calculateDamage(h);
                h.setHp(h.getHp() - damageDone);
            }
            enemyHeroPosDamage.put(coordinate, damageDone);
        });

        return enemyHeroPosDamage;
    }

    @Override
    public String getClassName() {
        return "Маг";
    }
}
