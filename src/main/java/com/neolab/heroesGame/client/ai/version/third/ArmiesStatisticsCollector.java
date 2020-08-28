package com.neolab.heroesGame.client.ai.version.third;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.aditional.StatisticWriter;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.arena.StringArmyFactory;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.version.basic.AbstractServer;
import com.neolab.heroesGame.client.ai.version.mechanics.ConverterForFourSizeArmy;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.server.answers.Answer;
import com.neolab.heroesGame.server.answers.AnswerProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class ArmiesStatisticsCollector extends AbstractServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArmiesStatisticsCollector.class);

    public ArmiesStatisticsCollector(final int MAX_COUNT_GAME_ROOMS, final int QUEUE_SIZE) {
        super(MAX_COUNT_GAME_ROOMS, QUEUE_SIZE);
    }

    @Override
    public void matching() throws Exception {
        final List<String> armies = CommonFunction.getAllAvailableArmiesCode(ARMY_SIZE);
        final ThreadPoolExecutor threadPoolExecutor = createThreadPoolExecutor(new ArrayBlockingQueue<>(getQueueSize()));

        final Set<String[]> pairs = makePairs(armies);
        for (int i = 0; i < 100; i++) {
            for (final String[] stringArmies : pairs) {
                final Army firstArmy = new StringArmyFactory(stringArmies[0]).create();
                final Army secondArmy = new StringArmyFactory(stringArmies[1]).create();
                final Map<Integer, Army> mapArmies = new HashMap<>();
                mapArmies.put(1, firstArmy.getCopy());
                mapArmies.put(2, secondArmy.getCopy());
                final BattleArena arena = new BattleArena(mapArmies);
                waitQueue(threadPoolExecutor);
                threadPoolExecutor.execute(new Room(arena));
            }
            LOGGER.info("Закончен {} круг ", (i + 1));
        }
    }

    private static Set<String[]> makePairs(final List<String> armies) {
        final Set<String> setThree = new HashSet<>();
        final Set<String> setTwo = new HashSet<>();
        final Set<String> setOne = new HashSet<>();
        for (final String code : armies) {
            switch (ConverterForFourSizeArmy.decide(code)) {
                case 1 -> setOne.add(ConverterForFourSizeArmy.transformFirstType(code));
                case 2 -> setTwo.add(ConverterForFourSizeArmy.transformSecondType(code));
                case 3 -> setThree.add(ConverterForFourSizeArmy.transformThirdType(code));
            }
        }
        final Set<String[]> pairs = new HashSet<>();
        pairs.addAll(createPairs(setThree));
        pairs.addAll(createPairs(setOne));
        pairs.addAll(createPairs(ConverterForFourSizeArmy.setDispositionSecondTypeVersusFirstType(setTwo)));
        pairs.addAll(createPairs(ConverterForFourSizeArmy.setDispositionSecondTypeVersusFirstType(setTwo), setOne));
        pairs.addAll(createPairs(ConverterForFourSizeArmy.setDispositionSecondTypeVersusThirdType(setTwo), setThree));
        pairs.addAll(createPairs(setOne, setThree));
        return pairs;
    }

    private static Set<String[]> createPairs(final Set<String> firstCodeSet, final Set<String> secondCodeSet) {
        final Set<String[]> pairs = new HashSet<>();
        for (final String one : firstCodeSet) {
            for (final String anotherOne : secondCodeSet) {
                pairs.add(new String[]{one, anotherOne});
                pairs.add(new String[]{anotherOne, one});
            }
        }
        return pairs;
    }

    private static Set<String[]> createPairs(final Set<String> singletonCodeSet) {
        final Set<String[]> pairs = new HashSet<>();
        for (final String one : singletonCodeSet) {
            for (final String anotherOne : singletonCodeSet) {
                pairs.add(new String[]{one, anotherOne});
            }
        }
        return pairs;
    }

    private static class Room implements Runnable {
        private Player currentPlayer;
        private Player waitingPlayer;
        private final AnswerProcessor answerProcessor;
        private final BattleArena battleArena;
        private int counter = 0;
        public final int maxRound = 15;

        public Room(final BattleArena arena) {
            currentPlayer = new MultiArmedWIthCoefficient(1);
            waitingPlayer = new MultiArmedWIthCoefficient(2);
            battleArena = arena;
            answerProcessor = new AnswerProcessor(1, 2, battleArena);
        }

        private void changeCurrentAndWaitingPlayers() {
            final Player temp = currentPlayer;
            currentPlayer = waitingPlayer;
            waitingPlayer = temp;
            setAnswerProcessorPlayerId(currentPlayer, waitingPlayer);
        }

        private void setAnswerProcessorPlayerId(final Player currentPlayer, final Player waitingPlayer) {
            answerProcessor.setActivePlayerId(currentPlayer.getId());
            answerProcessor.setWaitingPlayerId(waitingPlayer.getId());
        }

        public void run() {
            Optional<Player> whoIsWin;
            final String firstArmy = ConverterForFourSizeArmy.convertToCluster(CommonFunction
                    .ArmyCodeToString(battleArena.getArmy(currentPlayer.getId())));
            final String secondArmy = ConverterForFourSizeArmy.convertToCluster(CommonFunction
                    .ArmyCodeToString(battleArena.getArmy(waitingPlayer.getId())));
            while (true) {
                whoIsWin = someoneWhoWin();
                if (whoIsWin.isPresent()) {
                    break;
                }
                if (!battleArena.canSomeoneAct()) {
                    counter++;
                    if (counter > maxRound) {
                        break;
                    }
                    battleArena.endRound();
                }
                if (checkCanMove(currentPlayer.getId())) {
                    try {
                        askPlayerProcess();
                    } catch (final Exception ex) {
                        ex.printStackTrace();
                    }
                }
                changeCurrentAndWaitingPlayers();
            }
            final GameEvent endMatch;
            final int firstPlayerId = 1;
            if (whoIsWin.isEmpty()) {
                endMatch = GameEvent.GAME_END_WITH_A_TIE;
            } else if (whoIsWin.get().getId() == firstPlayerId) {
                endMatch = GameEvent.YOU_WIN_GAME;
            } else {
                endMatch = GameEvent.YOU_LOSE_GAME;
            }
            try {
                StatisticWriter.writeArmiesWinStatistic(firstArmy, secondArmy, endMatch);
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        }

        private void askPlayerProcess() throws HeroExceptions, IOException {
            final Answer answer = currentPlayer.getAnswer(battleArena);
            answerProcessor.handleAnswer(answer);
        }

        private Optional<Player> someoneWhoWin() {
            Player isWinner = battleArena.isArmyDied(getCurrentPlayerId()) ? waitingPlayer : null;
            if (isWinner == null) {
                isWinner = battleArena.isArmyDied(getWaitingPlayerId()) ? currentPlayer : null;
            }
            return Optional.ofNullable(isWinner);
        }

        private boolean checkCanMove(final Integer id) {
            return !battleArena.haveAvailableHeroByArmyId(id);
        }

        private int getCurrentPlayerId() {
            return currentPlayer.getId();
        }

        private int getWaitingPlayerId() {
            return waitingPlayer.getId();
        }
    }
}

