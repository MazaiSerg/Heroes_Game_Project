package com.neolab.heroesGame.client.ai.version.mechanics.arena;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.neolab.heroesGame.client.ai.version.mechanics.AnswerValidator;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Archer;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Healer;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Hero;
import com.neolab.heroesGame.client.ai.version.mechanics.heroes.Magician;

import java.util.*;

import static com.neolab.heroesGame.client.ai.version.mechanics.arena.SquareCoordinate.coordinateDoesntMatters;
import static com.neolab.heroesGame.client.ai.version.mechanics.arena.SquareCoordinate.getSquareCoordinate;
import static com.neolab.heroesGame.enumerations.HeroActions.*;

public class BattleArena {
    private final Map<Integer, Army> armies;

    @JsonCreator
    public BattleArena(@JsonProperty("armies") final Map<Integer, Army> armies) {
        this.armies = armies;
    }

    public static BattleArena getCopyFromOriginalClass(final com.neolab.heroesGame.arena.BattleArena arena) {
        final Map<Integer, Army> clone = new HashMap<>();
        arena.getArmies().keySet().forEach(key -> clone.put(key, Army.getCopyFromOriginalClasses(arena.getArmy(key))));
        return new BattleArena(clone);
    }

    public Map<Integer, Army> getArmies() {
        return armies;
    }

    public boolean isArmyDied(final int playerId) {
        return armies.get(playerId).getHeroes().isEmpty();
    }

    public Army getArmy(final int playerId) {
        return armies.get(playerId);
    }

    public boolean haveAvailableHeroByArmyId(final Integer id) {
        return !armies.get(id).getAvailableHeroes().isEmpty();
    }

    public void removeUsedHeroesById(final int heroId, final int armyId) {
        armies.get(armyId).removeAvailableHeroById(heroId);
    }
    public void endRound() {
        armies.values().forEach(Army::roundIsOver);
    }

    public Army getEnemyArmy(final int playerId) {
        final Integer botArmyId = armies.keySet().stream()
                .filter(id -> id != playerId).findFirst().orElseThrow();

        return armies.get(botArmyId);
    }

    public Integer getEnemyId(final Integer playerId) {
        return armies.keySet().stream().filter(id -> !id.equals(playerId)).findFirst().orElseThrow();
    }

    public boolean noOneCanAct() {
        for (final Army army : armies.values()) {
            if (army.canSomeOneAct()) {
                return false;
            }
        }
        return true;
    }

    public List<Answer> getAllActionForPlayer(final int playerId) {
        final List<Answer> actions = new ArrayList<>();
        getArmy(playerId).getAvailableHeroes().forEach((key, hero) -> actions.addAll(getHeroAction(key, hero, playerId)));
        return actions;
    }

    /**
     * Находит все доступные цели для каждого типа юнитов (для хиллера берутся только рененные союзники)
     *
     * @param activeHeroCoordinate координаты юнита, для которого ищутся доступные дествия
     * @param activeHero           юнит, для которого ищутся доступные дествия
     * @param activePlayerId       id текущего игрока
     * @return список доступных ходов
     */
    private List<Answer> getHeroAction(final SquareCoordinate activeHeroCoordinate,
                                       final Hero activeHero, final int activePlayerId) {
        final List<Answer> answers = new ArrayList<>();
        answers.add(new Answer(activeHeroCoordinate, DEFENCE, coordinateDoesntMatters, activePlayerId));

        if (activeHero instanceof Magician) {
            answers.add(new Answer(activeHeroCoordinate, ATTACK, coordinateDoesntMatters, activePlayerId));

        } else if (activeHero instanceof Archer) {
            answers.addAll(getArcherAction(activeHeroCoordinate, activePlayerId));

        } else if (activeHero instanceof Healer) {
            answers.addAll(getHealAction(activeHeroCoordinate, activePlayerId));

        } else {
            answers.addAll(getFootFighterAction(activeHeroCoordinate, activePlayerId));
        }
        return answers;
    }

    /**
     * Формируем ответы для атаки каждого из вражеских юнитов
     */
    private List<Answer> getArcherAction(final SquareCoordinate activeHeroCoordinate, final int activePlayerId) {
        final List<Answer> answers = new ArrayList<>();
        getEnemyArmy(activePlayerId).getHeroes().keySet()
                .forEach(target -> answers
                        .add(new Answer(activeHeroCoordinate, ATTACK, target, activePlayerId)));
        return answers;
    }

    /**
     * Формируем ответы для лечения каждого раненного юнита
     */
    private List<Answer> getHealAction(final SquareCoordinate activeHeroCoordinate, final int activePlayerId) {
        final List<Answer> answers = new ArrayList<>();
        getArmy(activePlayerId).getHeroes().forEach((key, value) -> {
            if (value.isInjure()) {
                answers.add(new Answer(activeHeroCoordinate, HEAL, key, activePlayerId));
            }
        });
        return answers;
    }

    /**
     * Формируем ответы для атаки ближайщих вражеских юнитов
     */
    private List<Answer> getFootFighterAction(final SquareCoordinate activeHeroCoordinate, final int activePlayerId) {
        final List<Answer> answers = new ArrayList<>();
        AnswerValidator.getCorrectTargetForFootman(activeHeroCoordinate, getEnemyArmy(activePlayerId))
                .forEach((target -> answers
                        .add(new Answer(activeHeroCoordinate, ATTACK, target, activePlayerId))));
        return answers;
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n");
        for (final Integer key : armies.keySet()) {
            stringBuilder.append(String.format("Армия игрока <%d>: \n", key));
            stringBuilder.append(printArmy(armies.get(key)));
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

    public static String printArmy(final Army army) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("________________________________________\n");
        for (int y = 0; y < 2; y++) {
            stringBuilder.append(getLineUnit(army, y));
            stringBuilder.append("|____________|____________|____________|\n");
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    /**
     * Формируем 3 строки - первая с названием класса, вторая с текущим/маскимальным хп, третья со статусом действия
     */
    private static String getLineUnit(final Army army, final int y) {
        final String cleanLine = "            |";
        final StringBuilder stringBuilder = new StringBuilder();
        final Map<Integer, Optional<Hero>> heroes = new HashMap<>();
        for (int x = 0; x < 3; x++) {
            heroes.put(x, army.getHero(getSquareCoordinate(x, y)));
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
            stringBuilder.append(heroes.get(x).isPresent() ? statusToString(heroes.get(x).get(), army) : cleanLine);
        }
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    private static String statusToString(final Hero hero, final Army army) {
        final StringBuilder result = new StringBuilder();
        if (hero.isDefence()) {
            result.append("   D  ");
        } else {
            result.append("      ");
        }
        if (army.getAvailableHeroes().containsValue(hero)) {
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
