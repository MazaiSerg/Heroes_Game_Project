package com.neolab.heroesGame.arena;

import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.heroes.Hero;
import com.neolab.heroesGame.heroes.factory.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StringArmyFactory implements ArmyFactory {

    private final String army;

    private final HeroFactory archerFactory = new ArcherFactory();
    private final HeroFactory footmanFactory = new FootmanFactory();
    private final HeroFactory healerFactory = new HealerFactory();
    private final HeroFactory magicianFactory = new MagicianFactory();
    private final HeroFactory warlordFootmanFactory = new WarlordFootmanFactory();
    private final HeroFactory warlordMagicianFactory = new WarlordMagicianFactory();
    private final HeroFactory warlordVampireFactory = new WarlordVampireFactory();

    public StringArmyFactory(final String army) throws IOException {
        this.army = army;
    }

    @Override
    public Army create() throws HeroExceptions {
        final Map<SquareCoordinate, Hero> armyMap = new HashMap<>();
        int k = 0;
        for (int j = 0; j < 2; ++j) {
            for (int i = 0; i < 3; ++i) {
                switch (army.charAt(k)) {
                    case 'a' -> armyMap.put(new SquareCoordinate(i, j), archerFactory.create());
                    case 'f' -> armyMap.put(new SquareCoordinate(i, j), footmanFactory.create());
                    case 'h' -> armyMap.put(new SquareCoordinate(i, j), healerFactory.create());
                    case 'm' -> armyMap.put(new SquareCoordinate(i, j), magicianFactory.create());
                    case 'F' -> armyMap.put(new SquareCoordinate(i, j), warlordFootmanFactory.create());
                    case 'M' -> armyMap.put(new SquareCoordinate(i, j), warlordMagicianFactory.create());
                    case 'V' -> armyMap.put(new SquareCoordinate(i, j), warlordVampireFactory.create());
                    case ' ' -> {
                    }
                    default -> throw new IllegalArgumentException("Неверно сформирована строка армии");
                }
                ++k;
            }
        }

        return new Army(armyMap);
    }
}
