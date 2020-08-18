package com.neolab.heroesGame.client.ai;

import com.neolab.heroesGame.client.ai.enums.BotType;
import com.neolab.heroesGame.client.ai.version.first.MinMaxBot;
import com.neolab.heroesGame.client.ai.version.first.MinMaxWithoutTree;
import com.neolab.heroesGame.client.ai.version.first.MonteCarloBot;
import com.neolab.heroesGame.client.ai.version.second.ManyArmedBandit;
import com.neolab.heroesGame.client.ai.version.second.ManyArmedWithoutTree;
import com.neolab.heroesGame.client.ai.version.second.SuperDuperManyArmed;
import com.neolab.heroesGame.client.ai.version.third.MultiArmedWIthCoefficient;

public final class PlayerFactory {
    private PlayerFactory() {
    }

    public static Player createPlayerBot(final BotType type, final int id) {
        return switch (type) {
            case RANDOM -> new PlayerBot(id);
            case MIN_MAX -> new MinMaxBot(id);
            case MONTE_CARLO -> new MonteCarloBot(id);
            case MANY_ARMED_BANDIT -> new ManyArmedBandit(id);
            case MANY_ARMED_BANDIT_WITH_RANDOM -> new ManyArmedWithoutTree(id);
            case SUPER_DUPER_MANY_ARMED -> new SuperDuperManyArmed(id);
            case MIN_MAX_WITHOUT_TREE -> new MinMaxWithoutTree(id);
            case MULTI_ARMED_WITH_COEFFICIENTS -> new MultiArmedWIthCoefficient(id);
        };
    }
}
