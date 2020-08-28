package com.neolab.heroesGame.heroes;

import com.fasterxml.jackson.annotation.*;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.enumerations.HeroErrorCode;
import com.neolab.heroesGame.errors.HeroExceptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Archer.class, name = "Archer"),
        @JsonSubTypes.Type(value = Footman.class, name = "Footman"),
        @JsonSubTypes.Type(value = Healer.class, name = "Healer"),
        @JsonSubTypes.Type(value = Magician.class, name = "Magician"),
        @JsonSubTypes.Type(value = WarlordFootman.class, name = "WarlordFootman"),
        @JsonSubTypes.Type(value = WarlordMagician.class, name = "WarlordMagician"),
        @JsonSubTypes.Type(value = WarlordVampire.class, name = "WarlordVampire")
}
)
public abstract class Hero implements Cloneable {
    private final int unitId;
    private final int hpDefault;
    private int hpMax;
    private int hp;
    private final int damageDefault;
    private int damage;
    private final float precision;
    private float armor;
    private final float armorDefault;
    private boolean defence = false;
    private static int nextId = 0;


    public Hero(final int hp, final int damage, final float precision, final float armor) {
        this.unitId = nextId++;
        this.hpDefault = hp;
        this.hpMax = hp;
        this.hp = hp;
        this.damage = damage;
        this.damageDefault = damage;
        this.precision = precision;
        this.armor = armor;
        this.armorDefault = armor;
    }

    @JsonCreator
    protected Hero(@JsonProperty("unitId") final int unitId, @JsonProperty("hpDefault") final int hpDefault,
                   @JsonProperty("hpMax") final int hpMax, @JsonProperty("hp") final int hp,
                   @JsonProperty("damageDefault") final int damageDefault, @JsonProperty("damage") final int damage,
                   @JsonProperty("precision") final float precision, @JsonProperty("armor") final float armor,
                   @JsonProperty("armorDefault") final float armorDefault,
                   @JsonProperty("defence") final boolean defence) {
        this.unitId = unitId;
        this.hpDefault = hpDefault;
        this.hpMax = hpMax;
        this.hp = hp;
        this.damageDefault = damageDefault;
        this.damage = damage;
        this.precision = precision;
        this.armor = armor;
        this.armorDefault = armorDefault;
        this.defence = defence;
    }

    public void setHpMax(final int hpMax) {
        this.hpMax = hpMax;
    }

    public int getDamageDefault() {
        return damageDefault;
    }

    public float getArmorDefault() {
        return armorDefault;
    }

    public int getHp() {
        return hp;
    }

    public int getUnitId() {
        return unitId;
    }

    public int getDamage() {
        return damage;
    }

    public float getPrecision() {
        return precision;
    }

    public float getArmor() {
        return armor;
    }

    public boolean isDefence() {
        return defence;
    }

    public void setHp(final int hp) {
        this.hp = hp;
    }

    public int getHpDefault() {
        return hpDefault;
    }

    public int getHpMax() {
        return hpMax;
    }

    public void setDamage(final int damage) {
        this.damage = damage;
    }

    public void setArmor(final float armor) {
        this.armor = armor;
    }

    public boolean isInjure() {
        return hp < hpMax;
    }

    public void setDefence() {
        if (!defence) {
            this.armor = this.armor + 0.5f;
            this.defence = true;
        }
    }

    public void cancelDefence() {
        if (defence) {
            this.armor = this.armor - 0.5f;
            this.defence = false;
        }
    }

    public abstract String getClassName();

    /**
     * Если герой погибает удаляем его из обеих коллекций
     *
     * @param position позиция героя
     * @param army     армия противника
     * @return возращается значение нанесенного урона и координата цели(ей)
     */
    public Map<SquareCoordinate, Integer> toAct(final SquareCoordinate position,
                                                final Army army) throws HeroExceptions {
        final Hero targetAttack = army.getHero(position).orElseThrow(() -> new HeroExceptions(HeroErrorCode.ERROR_TARGET_ATTACK));
        int damageDone = 0;
        if (isHit(this.getPrecision())) {
            damageDone = makeAction(targetAttack);
        }
        final Map<SquareCoordinate, Integer> enemyHeroPosDamage = new HashMap<>();
        enemyHeroPosDamage.put(position, damageDone);
        return enemyHeroPosDamage;
    }

    private int makeAction(final Hero target) {
        final int damage = calculateDamage(target);
        target.setHp(target.getHp() - damage);
        return damage;
    }

    public boolean isDead() {
        return this.hp <= 0;
    }

    protected int calculateDamage(final Hero targetAttack) {
        return Math.round(randomIncreaseDamage(this.getDamage()) * (1 - targetAttack.getArmor()));
    }

    protected int randomIncreaseDamage(final int damage) {
        final double probability = new Random().nextFloat();
        if (probability < 0.15f) {
            return damage - 5;
        } else if (probability >= 0.15f && probability < 0.70f) {
            return damage;
        }
        return damage + 5;
    }

    /**
     * подбрасываем монетку
     *
     * @param precision точность
     * @return boolean
     */
    protected boolean isHit(final float precision) {
        final Random rnd = new Random();
        final float number = rnd.nextFloat();
        return precision > number;
    }

    @JsonIgnore
    public Hero getCopy() {
        return clone();
    }

    public Hero clone() {
        try {
            return (Hero) super.clone();
        } catch (final CloneNotSupportedException ex) {
            //!!!!Никогда не возникнет. Исключение CloneNotSupported возникает если не cloneable
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Hero)) return false;
        final Hero hero = (Hero) o;
        return getUnitId() == hero.getUnitId() &&
                getHpDefault() == hero.getHpDefault() &&
                getHpMax() == hero.getHpMax() &&
                getHp() == hero.getHp() &&
                getDamageDefault() == hero.getDamageDefault() &&
                getDamage() == hero.getDamage() &&
                Float.compare(hero.getPrecision(), getPrecision()) == 0 &&
                Float.compare(hero.getArmor(), getArmor()) == 0 &&
                Float.compare(hero.getArmorDefault(), getArmorDefault()) == 0 &&
                isDefence() == hero.isDefence();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUnitId(), getHpDefault(), getHpMax(), getHp(), getDamageDefault(), getDamage(), getPrecision(), getArmor(), getArmorDefault(), isDefence());
    }
}
