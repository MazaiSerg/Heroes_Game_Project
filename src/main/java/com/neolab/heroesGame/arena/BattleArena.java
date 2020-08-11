package com.neolab.heroesGame.arena;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.neolab.heroesGame.aditional.CommonFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BattleArena {

    private static final Logger LOGGER = LoggerFactory.getLogger(BattleArena.class);
    private final Map<Integer, Army> armies;

    @JsonCreator
    public BattleArena(@JsonProperty("armies") final Map<Integer, Army> armies) {
        this.armies = armies;
    }

    public static BattleArena createBattleArena(final int firstId, final Army firstArmy,
                                                final int secondId, final Army secondArmy) {
        final Map<Integer, Army> armies = new HashMap<>();
        armies.put(firstId, firstArmy);
        armies.put(secondId, secondArmy);
        return new BattleArena(armies);
    }

    public Map<Integer, Army> getArmies() {
        return armies;
    }

    public boolean isArmyDied(final int playerId) {
        return armies.get(playerId).getHeroes().isEmpty();
    }

    public void diedArmy(final int playerId) {
        armies.get(playerId).getHeroes().clear();
    }

    public Army getArmy(final int playerId) {
        return armies.get(playerId);
    }

    public boolean haveAvailableHeroByArmyId(final Integer id) {
        return armies.get(id).getAvailableHeroes().isEmpty();
    }

    public void removeUsedHeroesById(final int heroId, final int armyId) {
        armies.get(armyId).removeAvailableHeroById(heroId);
    }

    public void endRound() {
        armies.values().forEach(Army::roundIsOver);
    }

    public Army getEnemyArmy(final int playerId) {
        final Integer botArmyId = armies.keySet().stream()
                .filter(id -> id != playerId).findFirst().get();

        return armies.get(botArmyId);
    }

    public boolean canSomeoneAct() {
        for (final Army army : armies.values()) {
            if (army.canSomeOneAct()) {
                return true;
            }
        }
        return false;
    }

    public Integer getEnemyId(final Integer playerId) {
        return armies.keySet().stream().filter(id -> !id.equals(playerId)).findFirst().get();
    }

    public void toLog() {
        LOGGER.info(this.toString());
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n");
        for (final Integer key : armies.keySet()) {
            stringBuilder.append(String.format("Армия игрока <%d>: \n", key));
            stringBuilder.append(CommonFunction.printArmy(armies.get(key)));
        }
        return stringBuilder.toString();
    }

    @JsonIgnore
    public BattleArena getCopy() {
        final Map<Integer, Army> clone = new HashMap<>();
        armies.keySet().forEach(key -> clone.put(key, armies.get(key).getCopy()));
        return new BattleArena(clone);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final BattleArena arena = (BattleArena) o;
        return Objects.equals(armies, arena.armies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(armies);
    }
}
