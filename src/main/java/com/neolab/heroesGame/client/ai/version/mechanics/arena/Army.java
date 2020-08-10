package com.neolab.heroesGame.client.ai.version.mechanics.arena;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.neolab.heroesGame.aditional.SquareCoordinateKeyDeserializer;
import com.neolab.heroesGame.aditional.SquareCoordinateKeySerializer;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Hero;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.IWarlord;
import com.neolab.heroesGame.enumerations.HeroErrorCode;
import com.neolab.heroesGame.errors.HeroExceptions;

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

    public void returnHeroToAvailable(SquareCoordinate activeHeroCoordinate) {
        availableHeroes.put(activeHeroCoordinate, heroes.get(activeHeroCoordinate));
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

    public static Army getCopyFromOriginalClasses(com.neolab.heroesGame.arena.Army army) {
        final com.neolab.heroesGame.heroes.Hero warlord = (com.neolab.heroesGame.heroes.Hero) army.getWarlord();
        final IWarlord cloneWarlord = (IWarlord) Hero.getCopyFromOriginalClasses(warlord);
        final Map<SquareCoordinate, Hero> heroes = getCloneMapFromOriginalClass(army.getHeroes());
        final Map<SquareCoordinate, Hero> availableHeroes = getCloneAvailableMapFromOriginalClass(army.getAvailableHeroes(), heroes);
        return new Army(heroes, cloneWarlord, availableHeroes);
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

    private static Map<SquareCoordinate, Hero> getCloneAvailableMapFromOriginalClass(
            final Map<SquareCoordinate, com.neolab.heroesGame.heroes.Hero> availableHeroes,
            final Map<SquareCoordinate, Hero> heroes) {

        final Map<SquareCoordinate, Hero> clone = new HashMap<>();
        availableHeroes.keySet().forEach((key) -> clone.put(key, heroes.get(key)));
        return clone;
    }

    private static Map<SquareCoordinate, Hero> getCloneMapFromOriginalClass(final Map<SquareCoordinate,
            com.neolab.heroesGame.heroes.Hero> heroes) {

        final Map<SquareCoordinate, Hero> clone = new HashMap<>();
        heroes.keySet().forEach((key) -> clone.put(key, Hero.getCopyFromOriginalClasses(heroes.get(key))));
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

    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("________________________________________\n");
        for (int y = 0; y < 2; y++) {
            stringBuilder.append(getLineUnit(y));
            stringBuilder.append("|____________|____________|____________|\n");
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    /**
     * Формируем 3 строки - первая с названием класса, вторая с текущим/маскимальным хп, третья со статусом действия
     */
    private String getLineUnit(final int y) {
        final String cleanLine = "            |";
        final StringBuilder stringBuilder = new StringBuilder();
        final Map<Integer, Optional<Hero>> heroes = new HashMap<>();
        for (int x = 0; x < 3; x++) {
            heroes.put(x, getHero(new SquareCoordinate(x, y)));
        }
        stringBuilder.append("|");
        for (int x = 0; x < 3; x++) {
            stringBuilder.append(heroes.get(x).isPresent() ? classToString(heroes.get(x).get()) : cleanLine);
        }
        stringBuilder.append("\n|");
        for (int x = 0; x < 3; x++) {
            stringBuilder.append(heroes.get(x).isPresent() ? hpToString(heroes.get(x).get()) : cleanLine);
        }
        stringBuilder.append("\n|");
        for (int x = 0; x < 3; x++) {
            stringBuilder.append(heroes.get(x).isPresent() ? statusToString(heroes.get(x).get()) : cleanLine);
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    private String statusToString(final Hero hero) {
        final StringBuilder result = new StringBuilder();
        if (hero.isDefence()) {
            result.append("   D  ");
        } else {
            result.append("      ");
        }
        if (getAvailableHeroes().containsValue(hero)) {
            result.append("  CA  |");
        } else {
            result.append("   W  |");
        }
        return result.toString();
    }

    private static String hpToString(final Hero hero) {
        return String.format("  HP%3d/%3d |", hero.getHp(), hero.getHpMax());
    }

    private static String classToString(final Hero hero) {
        return String.format("%12s|", hero.getClassName());
    }
}
