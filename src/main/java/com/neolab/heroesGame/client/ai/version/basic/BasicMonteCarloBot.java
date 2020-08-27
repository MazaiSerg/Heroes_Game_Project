package com.neolab.heroesGame.client.ai.version.basic;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.version.mechanics.AnswerValidator;
import com.neolab.heroesGame.client.ai.version.mechanics.MatchUpAnalyzer;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.server.answers.Answer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class BasicMonteCarloBot extends Player {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicMonteCarloBot.class);
    private static final MatchUpAnalyzer analyzer;
    private static final Random RANDOM = new Random();
    private final int timeToThink;
    private int currentRound = -1;

    static {
        AnswerValidator.initializeHashTable();
        analyzer = MatchUpAnalyzer.createMatchUpAnalyzer();
    }

    protected BasicMonteCarloBot(final String basicName, final int timeToThink, final int id) {
        super(id, String.format("%s_%d", basicName, timeToThink));
        this.timeToThink = timeToThink;
    }

    @Override
    public Answer getAnswer(final BattleArena board) throws HeroExceptions {
        return null;
    }

    @Override
    public String getStringArmyFirst(final int armySize) {
        if (armySize == 4) {
            return analyzer.getOneOfBestArmy();
        } else {
            return analyzer.getDefaultArmy(armySize);
        }
    }

    @Override
    public String getStringArmySecond(final int armySize, final Army army) {
        if (armySize == 4) {
            return analyzer.getBestResponse(CommonFunction.ArmyCodeToString(army));
        } else {
            return analyzer.getDefaultArmy(armySize);
        }
    }

    public final int getTimeToThink() {
        return timeToThink;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void increaseCurrentRound() {
        currentRound++;
    }

    public void restartCurrentRound() {
        currentRound = -1;
    }

    /**
     * Выбираем случайное действие с учетом приоретета действий
     */
    protected static int chooseAction(@NotNull final double[] actionPriority) {
        final double random = RANDOM.nextDouble() * actionPriority[actionPriority.length - 1];
        for (int i = 0; i < actionPriority.length; i++) {
            if (actionPriority[i] > random) {
                return i;
            }
        }
        LOGGER.error("WTF with priority!!!");
        for (final double aDouble : actionPriority) {
            LOGGER.trace("RANDOM: {}, Action: {}", random, aDouble);
        }
        return 0;
    }
}
