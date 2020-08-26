package com.neolab.heroesGame.client.ai;

import com.neolab.heroesGame.client.ai.enums.BotType;
import com.neolab.heroesGame.client.ai.version.first.MinMaxWithoutTree;
import com.neolab.heroesGame.client.ai.version.first.MonteCarloBot;
import com.neolab.heroesGame.client.ai.version.fouth.FinalSuperDuperBot;
import com.neolab.heroesGame.client.ai.version.fouth.MultiArmedWithoutRecursive;
import com.neolab.heroesGame.client.ai.version.second.SuperDuperManyArmed;
import com.neolab.heroesGame.client.ai.version.third.MultiArmedWIthCoefficient;

public final class PlayerFactory {
    private PlayerFactory() {
    }

    public static Player createPlayerBot(final BotType type, final int id) {
        return switch (type) {
            case RANDOM -> new PlayerBot(id);
            case MONTE_CARLO -> new MonteCarloBot(id);
            case SUPER_DUPER_MANY_ARMED -> new SuperDuperManyArmed(id);
            case MIN_MAX_WITHOUT_TREE -> new MinMaxWithoutTree(id);
            case MULTI_ARMED_WITH_COEFFICIENTS -> new MultiArmedWIthCoefficient(id);
            case MULTI_ARMED_WITHOUT_RECURSIVE -> new MultiArmedWithoutRecursive(id);
            case FINAL_BOT -> new FinalSuperDuperBot(id);
        };
    }

    public static Player createMonteCarloBotWithTimeLimit(final BotType type, final int id, final int timeToThink) {
        return switch (type) {
            case MONTE_CARLO -> new MonteCarloBot(id, timeToThink);
            case SUPER_DUPER_MANY_ARMED -> new SuperDuperManyArmed(id, timeToThink);
            case MULTI_ARMED_WITH_COEFFICIENTS -> new MultiArmedWIthCoefficient(id, timeToThink);
            case MULTI_ARMED_WITHOUT_RECURSIVE -> new MultiArmedWithoutRecursive(id, timeToThink);
            case RANDOM -> new PlayerBot(id);
            case MIN_MAX_WITHOUT_TREE -> new MinMaxWithoutTree(id);
            case FINAL_BOT -> new FinalSuperDuperBot(id);
        };
    }
}
