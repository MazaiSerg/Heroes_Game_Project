package com.neolab.heroesGame.arena;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.neolab.heroesGame.aditional.SquareCoordinateKeyDeserializer;
import com.neolab.heroesGame.aditional.SquareCoordinateKeySerializer;
import com.neolab.heroesGame.enumerations.HeroErrorCode;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.heroes.Hero;
import com.neolab.heroesGame.heroes.IWarlord;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Army {

    @JsonSerialize(keyUsing = SquareCoordinateKeySerializer.class)
    @JsonDeserialize(keyUsing = SquareCoordinateKeyDeserializer.class)
    private final Map<SquareCoordinate, Hero> heroes;
    private IWarlord warlord;
    @JsonSerialize(keyUsing = SquareCoordinateKeySerializer.class)
    @JsonDeserialize(keyUsing = SquareCoordinateKeyDeserializer.class)
    private Map<SquareCoordinate, Hero> availableHeroes;

    public Army(final Map<SquareCoordinate, Hero> heroes) throws HeroExceptions {
        this.heroes = heroes;
        this.warlord = findWarlord();
        roundIsOver();
        improveAllies();
    }

    @JsonCreator
    public Army(@JsonProperty("heroes") final Map<SquareCoordinate, Hero> heroes,
                @JsonProperty("warlord") final IWarlord warlord,
                @JsonProperty("availableHeroes") final Map<SquareCoordinate, Hero> availableHeroes) {
        this.heroes = heroes;
        this.warlord = warlord;
        this.availableHeroes = availableHeroes;
    }

    private IWarlord findWarlord() throws HeroExceptions {
        IWarlord iWarlord = null;
        for (final Hero hero : heroes.values()) {
            if (hero instanceof IWarlord) {
                if (iWarlord != null) {
                    throw new HeroExceptions(HeroErrorCode.ERROR_SECOND_WARLORD_ON_ARMY);
                }
                iWarlord = (IWarlord) hero;
            }
        }
        if (iWarlord == null) {
            throw new HeroExceptions(HeroErrorCode.ERROR_EMPTY_WARLORD);
        }
        return iWarlord;
    }

    public Map<SquareCoordinate, Hero> getHeroes() {
        return heroes;
    }

    public Map<SquareCoordinate, Hero> getAvailableHeroes() {
        return availableHeroes;
    }

    public Optional<Hero> getHero(final SquareCoordinate coordinate) {
        return Optional.ofNullable(heroes.get(coordinate));
    }

    public void roundIsOver() {
        this.availableHeroes = new HashMap<>(heroes);
    }

    public void killHero(final SquareCoordinate coordinate) {
        if (warlord != null && heroes.get(coordinate) instanceof IWarlord) {
            cancelImprove();
        }
        availableHeroes.remove(coordinate);
        heroes.remove(coordinate);
    }

    public void tryToKill(final SquareCoordinate coordinate) {
        if (heroes.get(coordinate) != null && heroes.get(coordinate).isDead()) {
            killHero(coordinate);
        }
    }

    public void setWarlord(final IWarlord warlord) {
        this.warlord = warlord;
    }

    public void removeAvailableHeroById(final int heroId) {
        availableHeroes.values().removeIf(value -> value.getUnitId() == heroId);
    }

    private void improveAllies() {
        heroes.values().forEach(this::improve);
    }

    public IWarlord getWarlord() {
        return this.warlord;
    }

    private void improve(final Hero hero) {
        int value = hero.getHpMax() + Math.round((float) hero.getHpMax() * warlord.getImproveCoefficient());
        hero.setHpMax(value);
        hero.setHp(value);
        value = hero.getDamageDefault() + Math.round((float) hero.getDamageDefault() * warlord.getImproveCoefficient());
        hero.setDamage(value);
        final float armor = hero.getArmorDefault() + warlord.getImproveCoefficient();
        hero.setArmor(armor);
    }

    protected void cancelImprove() {
        heroes.values().forEach(this::cancel);
    }

    private void cancel(final Hero hero) {
        hero.setArmor(hero.getArmor() - warlord.getImproveCoefficient());
        hero.setHpMax(hero.getHpDefault());
        hero.setHp(Math.min(hero.getHp(), hero.getHpDefault()));
        hero.setDamage(hero.getDamageDefault());
    }

    @JsonIgnore
    public Army getCopy() {
        final Hero warlord = (Hero) this.warlord;
        final IWarlord cloneWarlord = (IWarlord) warlord.getCopy();
        final Map<SquareCoordinate, Hero> heroes = getCloneMap(getHeroes());
        final Map<SquareCoordinate, Hero> availableHeroes = getCloneAvailableMap(getAvailableHeroes(), heroes);
        return new Army(heroes, cloneWarlord, availableHeroes);
    }

    private static Map<SquareCoordinate, Hero> getCloneAvailableMap(final Map<SquareCoordinate, Hero> availableHeroes,
                                                                    final Map<SquareCoordinate, Hero> heroes) {
        final Map<SquareCoordinate, Hero> clone = new HashMap<>();
        availableHeroes.keySet().forEach((key) -> clone.put(key, heroes.get(key)));
        return clone;
    }

    private static Map<SquareCoordinate, Hero> getCloneMap(final Map<SquareCoordinate, Hero> heroes) {
        final Map<SquareCoordinate, Hero> clone = new HashMap<>();
        heroes.keySet().forEach((key) -> clone.put(key, heroes.get(key).getCopy()));
        return clone;
    }

    public boolean canSomeOneAct() {
        return !availableHeroes.isEmpty();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Army army = (Army) o;
        return Objects.equals(heroes, army.heroes) &&
                Objects.equals(warlord, army.warlord) &&
                Objects.equals(availableHeroes, army.availableHeroes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(heroes, warlord, availableHeroes);
    }
}
