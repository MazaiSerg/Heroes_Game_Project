package com.neolab.heroesGame.arena;

import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.heroes.Hero;
import com.neolab.heroesGame.heroes.WarlordFootman;
import com.neolab.heroesGame.heroes.factory.*;

import java.io.IOException;
import java.util.*;

public final class FactoryArmies {

    private static final long SEED = 1700;
    private static final Random RANDOM = new Random(SEED);

    private FactoryArmies() {
    }

    public static Map<Integer, Army> generateArmies(final Integer firstPlayerId,
                                                    final Integer secondPlayerId) throws HeroExceptions, IOException {
        final Map<Integer, Army> armies = new HashMap<>();
        armies.put(firstPlayerId, createRandomArmy());
        armies.put(secondPlayerId, createRandomArmy());
        return armies;
    }

    /**
     * Генерируем армию. Варлорд: с шансом 50% файтер, по 25% на магов
     * Первую линию забивает Footman
     * Вторую линию забивает Healer, Magician, Archer с равным шансом
     * Место для варлорда выбирает Set.iterator().next()
     */
    public static Army createRandomArmy() throws HeroExceptions, IOException {
        final Map<SquareCoordinate, Hero> heroes = new HashMap<>();
        final Hero warlord = createWarlord();
        final Set<SquareCoordinate> firstLine = makeLine(1);
        final Set<SquareCoordinate> secondLine = makeLine(0);
        if (warlord instanceof WarlordFootman) {
            addWarlord(heroes, firstLine, warlord);
        } else {
            addWarlord(heroes, secondLine, warlord);
        }
        for (final SquareCoordinate key : firstLine) {
            heroes.put(key, new FootmanFactory().create());
        }
        for (final SquareCoordinate key : secondLine) {
            heroes.put(key, createSecondLineUnit());
        }
        return new Army(heroes);
    }

    private static void addWarlord(final Map<SquareCoordinate, Hero> heroes,
                                   final Set<SquareCoordinate> line, final Hero warlord) {
        final SquareCoordinate temp = line.iterator().next();
        heroes.put(temp, warlord);
        line.remove(temp);
    }

    private static Hero createWarlord() throws IOException {
        final int switcher = RANDOM.nextInt(4);
        if (switcher == 0) {
            return new WarlordMagicianFactory().create();
        } else if (switcher == 1) {
            return new WarlordVampireFactory().create();
        } else {
            return new WarlordFootmanFactory().create();
        }
    }

    private static Hero createSecondLineUnit() throws IOException {
        final int switcher = RANDOM.nextInt(3) % 3;
        if (switcher == 0) {
            return new ArcherFactory().create();
        } else if (switcher == 1) {
            return new MagicianFactory().create();
        } else {
            return new HealerFactory().create();
        }
    }

    private static Set<SquareCoordinate> makeLine(final Integer y) {
        final Set<SquareCoordinate> line = new HashSet<>();
        for (int x = 0; x <= 2; x++) {
            line.add(new SquareCoordinate(x, y));
        }
        return line;
    }
}
