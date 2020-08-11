package com.neolab.heroesGame.client.ai;

import com.neolab.heroesGame.client.ai.enums.BotType;
import com.neolab.heroesGame.client.ai.version.first.MinMaxBot;
import com.neolab.heroesGame.client.ai.version.first.MonteCarloBot;

public final class PlayerFactory {
    private PlayerFactory() {
    }

    public static Player createPlayerBot(final BotType type, final int id) {
        return switch (type) {
            case RANDOM -> new PlayerBot(id);
            case MIN_MAX -> new MinMaxBot(id);
            case MONTE_CARLO -> new MonteCarloBot(id);
        };
    }
}
