package com.neolab.heroesGame.client.ai.version.mechanics.heroes;

import com.fasterxml.jackson.annotation.*;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Army;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.BattleArena;
import com.neolab.heroesGame.enumerations.HeroErrorCode;
import com.neolab.heroesGame.errors.HeroExceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(BattleArena.class);
    private final int unitId;
    private int hpMax;
    private int hp;
    private int damage;
    private float armor;
    private boolean defence;

    @JsonCreator
    protected Hero(@JsonProperty("unitId") final int unitId,
                   @JsonProperty("hpMax") final int hpMax, @JsonProperty("hp") final int hp,
                   @JsonProperty("damage") final int damage,
                   @JsonProperty("armor") final float armor,
                   @JsonProperty("defence") final boolean defence) {
        this.unitId = unitId;
        this.hpMax = hpMax;
        this.hp = hp;
        this.damage = damage;
        this.armor = armor;
        this.defence = defence;
    }

    public void setHpMax(final int hpMax) {
        this.hpMax = hpMax;
    }

    abstract public int getDamageDefault();

    public float getArmorDefault() {
        return 0f;
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

    public float getArmor() {
        return armor;
    }

    public boolean isDefence() {
        return defence;
    }

    public void setHp(final int hp) {
        this.hp = hp;
    }

    abstract public int getHpDefault();

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
     */
    public void toAct(final SquareCoordinate position,
                      final Army army) throws HeroExceptions {
        final Hero targetAttack = army.getHero(position).orElseThrow(() -> {
            LOGGER.error("position: ({}, {})", position.getX(), position.getY());
            LOGGER.error(army.toString());
            return new HeroExceptions(HeroErrorCode.ERROR_TARGET_ATTACK);
        });
        targetAttack.setHp(targetAttack.hp - calculateDamage(targetAttack));
    }

    public boolean isDead() {
        return this.hp <= 0;
    }

    protected int calculateDamage(final Hero targetAttack) {
        return Math.round(this.damage * (1 - targetAttack.armor));
    }

    public static Hero getCopyFromOriginalClasses(com.neolab.heroesGame.heroes.Hero hero) {
        return switch (hero.getClassName()) {
            case "Лучник" -> new Archer(hero.getUnitId(), hero.getHpMax(), hero.getHp(),
                    hero.getDamage(), hero.getArmor(),
                    hero.isDefence());
            case "Лекарь" -> new Healer(hero.getUnitId(), hero.getHpMax(), hero.getHp(),
                    hero.getDamage(), hero.getArmor(),
                    hero.isDefence());
            case "Мечник" -> new Footman(hero.getUnitId(), hero.getHpMax(), hero.getHp(),
                    hero.getDamage(), hero.getArmor(),
                    hero.isDefence());
            case "Маг" -> new Magician(hero.getUnitId(), hero.getHpMax(), hero.getHp(),
                    hero.getDamage(), hero.getArmor(),
                    hero.isDefence());
            case "Вампир" -> new WarlordVampire(hero.getUnitId(), hero.getHpMax(), hero.getHp(),
                    hero.getDamage(), hero.getArmor(),
                    hero.isDefence());
            case "Генерал" -> new WarlordFootman(hero.getUnitId(), hero.getHpMax(), hero.getHp(),
                    hero.getDamage(), hero.getArmor(),
                    hero.isDefence());
            case "Архимаг" -> new WarlordMagician(hero.getUnitId(), hero.getHpMax(), hero.getHp(),
                    hero.getDamage(), hero.getArmor(),
                    hero.isDefence());
            default -> null;
        };
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
                Float.compare(hero.getArmor(), getArmor()) == 0 &&
                Float.compare(hero.getArmorDefault(), getArmorDefault()) == 0 &&
                isDefence() == hero.isDefence();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUnitId(), getHpDefault(), getHpMax(), getHp(), getDamageDefault(),
                getDamage(), getArmor(), getArmorDefault(), isDefence());
    }
}
