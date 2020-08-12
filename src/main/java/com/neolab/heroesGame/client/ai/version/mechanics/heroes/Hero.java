package com.neolab.heroesGame.client.ai.version.mechanics.heroes;

import com.fasterxml.jackson.annotation.*;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.Army;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.BattleArena;
import com.neolab.heroesGame.client.ai.version.mechanics.arena.SquareCoordinate;
import com.neolab.heroesGame.enumerations.HeroErrorCode;
import com.neolab.heroesGame.errors.HeroExceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Random RANDOM = new Random();
    private static final Logger LOGGER = LoggerFactory.getLogger(BattleArena.class);
    private int hpMax;
    private int hp;
    private int damage;
    private float armor;
    private boolean defence;

    @JsonCreator
    protected Hero(@JsonProperty("hpMax") final int hpMax, @JsonProperty("hp") final int hp,
                   @JsonProperty("damage") final int damage,
                   @JsonProperty("armor") final float armor,
                   @JsonProperty("defence") final boolean defence) {
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

    abstract public int getPrecision();

    public boolean isInjure() {
        return hp < hpMax;
    }

    public void setDefence() {
        if (!defence) {
            armor = armor + 0.5f;
            defence = true;
        }
    }

    public void cancelDefence() {
        if (defence) {
            armor = armor - 0.5f;
            defence = false;
        }
    }

    public abstract String getClassName();

    public void toAct(final SquareCoordinate position,
                      final Army army) throws HeroExceptions {
        final Hero targetAttack = army.getHero(position).orElseThrow(() -> {
            LOGGER.error("position: ({}, {})", position.getX(), position.getY());
            LOGGER.error(army.toString());
            return new HeroExceptions(HeroErrorCode.ERROR_TARGET_ATTACK);
        });
        targetAttack.setHp(targetAttack.hp - calculateDamage(targetAttack));
    }

    public void toActWithPrecision(final SquareCoordinate position,
                                   final Army army) throws HeroExceptions {
        final Hero targetAttack = army.getHero(position).orElseThrow(() -> {
            LOGGER.error("position: ({}, {})", position.getX(), position.getY());
            LOGGER.error(army.toString());
            return new HeroExceptions(HeroErrorCode.ERROR_TARGET_ATTACK);
        });
        targetAttack.setHp(targetAttack.hp - calculateDamageWithPrecision(targetAttack));
    }

    public int calculateDamageWithPrecision(final Hero targetAttack) {
        if (RANDOM.nextInt(100) >= getPrecision()) {
            return 0;
        }
        int additionalDamage = 0;
        int random = RANDOM.nextInt(100);
        if (random < 15) {
            additionalDamage = -5;
        }
        if (random >= 85) {
            additionalDamage = 5;
        }
        return Math.round((damage + additionalDamage) * (1 - targetAttack.armor));
    }

    public boolean isDead() {
        return hp <= 0;
    }

    protected int calculateDamage(final Hero targetAttack) {
        return Math.round(damage * (1 - targetAttack.armor));
    }

    public static Hero getCopyFromOriginalClasses(final com.neolab.heroesGame.heroes.Hero hero) {
        if (hero instanceof com.neolab.heroesGame.heroes.Archer) {
            return new Archer(hero.getHpMax(), hero.getHp(),
                    hero.getDamage(), hero.getArmor(), hero.isDefence());

        } else if (hero instanceof com.neolab.heroesGame.heroes.Healer) {
            return new Healer(hero.getHpMax(), hero.getHp(),
                    hero.getDamage(), hero.getArmor(), hero.isDefence());

        } else if (hero instanceof com.neolab.heroesGame.heroes.WarlordVampire) {
            return new WarlordVampire(hero.getHpMax(), hero.getHp(),
                    hero.getDamage(), hero.getArmor(), hero.isDefence());

        } else if (hero instanceof com.neolab.heroesGame.heroes.WarlordFootman) {
            return new WarlordFootman(hero.getHpMax(), hero.getHp(),
                    hero.getDamage(), hero.getArmor(), hero.isDefence());

        } else if (hero instanceof com.neolab.heroesGame.heroes.WarlordMagician) {
            return new WarlordMagician(hero.getHpMax(), hero.getHp(),
                    hero.getDamage(), hero.getArmor(), hero.isDefence());

        } else if (hero instanceof com.neolab.heroesGame.heroes.Footman) {
            return new Footman(hero.getHpMax(), hero.getHp(),
                    hero.getDamage(), hero.getArmor(), hero.isDefence());

        } else if (hero instanceof com.neolab.heroesGame.heroes.Magician) {
            return new Magician(hero.getHpMax(), hero.getHp(),
                    hero.getDamage(), hero.getArmor(), hero.isDefence());
        }
        //Никогда не должно возникать
        throw new AssertionError();
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
        return getHpDefault() == hero.getHpDefault() &&
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
        return Objects.hash(getHpDefault(), getHpMax(), getHp(), getDamageDefault(), getDamage(), getArmor(), getArmorDefault(), isDefence());
    }
}
