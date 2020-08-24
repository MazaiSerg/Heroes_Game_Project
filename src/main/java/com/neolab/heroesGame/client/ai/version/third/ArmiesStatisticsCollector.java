package com.neolab.heroesGame.client.ai.version.third;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.aditional.StatisticWriter;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.arena.StringArmyFactory;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.client.ai.version.basic.AbstractServer;
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

import static com.neolab.heroesGame.aditional.CommonFunction.EMPTY_UNIT;

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
            switch (decide(code)) {
                case 1 -> setOne.add(transformOneToThree(code));
                case 2 -> setTwo.add(code);
                case 3 -> setThree.add(transformThreeToOne(code));
            }
        }
        final Set<String[]> pairs = new HashSet<>();
        pairs.addAll(createPairsThirdToThird(setThree));
        pairs.addAll(createPairsOneToOne(setOne));
        pairs.addAll(createPairsTwoToTwo(setTwo));
        pairs.addAll(createPairsTwoToOne(setTwo, setOne));
        pairs.addAll(createPairsTwoToThree(setTwo, setThree));
        pairs.addAll(createPairsOneToThree(setOne, setThree));
        return pairs;
    }

    private static Set<String[]> createPairsOneToThree(final Set<String> setOne, final Set<String> setThree) {
        final Set<String> stringSetThree = simplifyThreeSet(setThree);
        final Set<String[]> pairs = new HashSet<>();
        for (final String one : setOne) {
            for (final String anotherOne : stringSetThree) {
                pairs.add(new String[]{one, anotherOne});
                pairs.add(new String[]{anotherOne, one});
            }
        }
        return pairs;
    }

    private static Set<String[]> createPairsTwoToOne(final Set<String> setTwo, final Set<String> setOne) {
        final Set<String> stringSetTwo = new HashSet<>();
        for (final String code : setTwo) {
            final char[] codes = sortSecondLine(code).toCharArray();
            for (int line = 0; line < 2; line++) {
                codes[line * 3] = codes[line * 3] == EMPTY_UNIT ? codes[2 + line * 3] : codes[line * 3];
                codes[1 + line * 3] = codes[1 + line * 3] == EMPTY_UNIT ? codes[2 + line * 3] : codes[1 + line * 3];
                codes[2 + line * 3] = EMPTY_UNIT;
            }
            stringSetTwo.add(String.valueOf(codes));
        }
        final Set<String[]> pairs = new HashSet<>();
        for (final String one : stringSetTwo) {
            for (final String anotherOne : setOne) {
                pairs.add(new String[]{one, anotherOne});
                pairs.add(new String[]{anotherOne, one});
            }
        }
        return pairs;
    }

    private static Set<String[]> createPairsTwoToThree(final Set<String> codeSetTwo, final Set<String> codeSetThree) {
        final Set<String> stringSetTwo = new HashSet<>();
        for (final String code : codeSetTwo) {
            final char[] codes = sortSecondLine(code).toCharArray();
            for (int line = 0; line < 2; line++) {
                codes[line * 3] = codes[line * 3] == EMPTY_UNIT ? codes[1 + line * 3] : codes[line * 3];
                codes[2 + line * 3] = codes[2 + line * 3] == EMPTY_UNIT ? codes[1 + line * 3] : codes[2 + line * 3];
                codes[1 + line * 3] = EMPTY_UNIT;
            }
            stringSetTwo.add(String.valueOf(codes));
        }
        final Set<String> stringSetThree = simplifyThreeSet(codeSetThree);
        final Set<String[]> pairs = new HashSet<>();
        for (final String one : stringSetTwo) {
            for (final String anotherOne : stringSetThree) {
                pairs.add(new String[]{one, anotherOne});
                pairs.add(new String[]{anotherOne, one});
            }
        }
        return pairs;
    }

    private static Set<String[]> createPairsTwoToTwo(final Set<String> codesSet) {
        final Set<String> stringSet = new HashSet<>();
        for (final String code : codesSet) {
            final char[] codes = sortSecondLine(code).toCharArray();
            for (int line = 0; line < 2; line++) {
                codes[line * 3] = codes[line * 3] == EMPTY_UNIT ? codes[1 + line * 3] : codes[line * 3];
                codes[2 + line * 3] = codes[2 + line * 3] == EMPTY_UNIT ? codes[1 + line * 3] : codes[2 + line * 3];
                codes[1 + line * 3] = EMPTY_UNIT;
            }
            stringSet.add(String.valueOf(codes));
        }
        final Set<String[]> pairs = new HashSet<>();
        for (final String one : stringSet) {
            for (final String anotherOne : stringSet) {
                pairs.add(new String[]{one, anotherOne});
            }
        }
        return pairs;
    }

    private static Set<String[]> createPairsOneToOne(final Set<String> codesSet) {
        final Set<String[]> pairs = new HashSet<>();
        for (final String one : codesSet) {
            for (final String anotherOne : codesSet) {
                pairs.add(new String[]{one, anotherOne});
            }
        }
        return pairs;
    }

    private static Set<String[]> createPairsThirdToThird(final Set<String> codesSet) {
        final Set<String> stringSet = simplifyThreeSet(codesSet);
        final Set<String[]> pairs = new HashSet<>();
        for (final String one : stringSet) {
            for (final String anotherOne : stringSet) {
                pairs.add(new String[]{one, anotherOne});
            }
        }
        return pairs;
    }

    private static Set<String> simplifyThreeSet(final Set<String> codesSet) {
        final Set<String> stringSet = new HashSet<>();
        for (final String code : codesSet) {
            stringSet.add(sortSecondLine(code));
        }
        return stringSet;
    }

    private static String sortSecondLine(final String code) {
        final char[] codes = code.toCharArray();
        for (int i = 0; i < 2; i++) {
            for (int j = i + 1; j < 3; j++) {
                if (codes[i] > codes[j]) {
                    final char temp = codes[j];
                    codes[j] = codes[i];
                    codes[i] = temp;
                }
            }
        }
        return String.valueOf(codes);
    }

    private static String transformThreeToOne(final String code) {
        final char[] codes = code.toCharArray();
        char temp = EMPTY_UNIT;
        for (int i = 3; i < 6; i++) {
            if (codes[i] != EMPTY_UNIT) {
                temp = codes[i];
                codes[i] = EMPTY_UNIT;
            }
        }
        codes[4] = temp;
        return String.valueOf(codes);
    }

    private static String transformOneToThree(final String code) {
        final char[] codes = code.toCharArray();
        char temp = EMPTY_UNIT;
        for (int i = 0; i < 3; i++) {
            if (codes[i] != EMPTY_UNIT) {
                temp = codes[i];
                codes[i] = EMPTY_UNIT;
            }
        }
        codes[1] = temp;
        return String.valueOf(codes);
    }

    private static int decide(final String code) {
        final char[] codes = code.toCharArray();
        int counter = 0;
        for (int i = 0; i < 3; i++) {
            if (codes[i] != EMPTY_UNIT) {
                counter++;
            }
        }
        return counter;
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
            final String firstArmy = CommonFunction.ArmyCodeToString(battleArena.getArmy(
                    currentPlayer.getId()));
            final String secondArmy = CommonFunction.ArmyCodeToString(battleArena.getArmy(
                    waitingPlayer.getId()));
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

