package com.neolab.heroesGame.client.ai.version.third;

import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.aditional.StatisticWriter;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.arena.StringArmyFactory;
import com.neolab.heroesGame.client.ai.Player;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.server.answers.Answer;
import com.neolab.heroesGame.server.answers.AnswerProcessor;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.neolab.heroesGame.aditional.CommonFunction.EMPTY_UNIT;
import static java.lang.Thread.sleep;

public class GamingProcess {
    public static final Integer MAX_ROUND = 15;
    public static final Integer ARMY_SIZE = StatisticWriter.ARMY_SIZE;
    private static final Integer MAX_COUNT_GAME_ROOMS = 5;
    private static final Integer QUEUE_SIZE = 10;
    final static long startTime = System.currentTimeMillis();
    static int prevCounter = 0;

    public static void main(final String[] args) throws Exception {
        matches();
    }

    private static void matches() throws Exception {
        List<String> armies = CommonFunction.getAllAvailableArmiesCode(ARMY_SIZE);
        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(MAX_COUNT_GAME_ROOMS, MAX_COUNT_GAME_ROOMS,
                10_000L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(QUEUE_SIZE));

        Set<String[]> pairs = makePairs(armies);
        for (int i = 0; i < 100; i++) {
            for (String[] stringArmies : pairs) {
                final Army firstArmy = new StringArmyFactory(stringArmies[0]).create();
                final Army secondArmy = new StringArmyFactory(stringArmies[1]).create();
                final Map<Integer, Army> mapArmies = new HashMap<>();
                mapArmies.put(1, firstArmy.getCopy());
                mapArmies.put(2, secondArmy.getCopy());
                final BattleArena arena = new BattleArena(mapArmies);
                waitQueue(threadPoolExecutor);
                threadPoolExecutor.execute(new Room(arena));
            }
            System.out.println("i = " + (i + 1));
        }
    }

    private static Set<String[]> makePairs(List<String> armies) {
        Set<String> setThree = new HashSet<>();
        Set<String> setTwo = new HashSet<>();
        Set<String> setOne = new HashSet<>();
        for (String code : armies) {
            switch (decide(code)) {
                case 1 -> setOne.add(transformOneToThree(code));
                case 2 -> setTwo.add(code);
                case 3 -> setThree.add(transformThreeToOne(code));
            }
        }
        Set<String[]> pairs = new HashSet<>();
        pairs.addAll(createPairsThirdToThird(setThree));
        pairs.addAll(createPairsOneToOne(setOne));
        pairs.addAll(createPairsTwoToTwo(setTwo));
        pairs.addAll(createPairsTwoToOne(setTwo, setOne));
        pairs.addAll(createPairsTwoToThree(setTwo, setThree));
        pairs.addAll(createPairsOneToThree(setOne, setThree));
        return pairs;
    }

    private static Set<String[]> createPairsOneToThree(Set<String> setOne, Set<String> setThree) {
        Set<String> stringSetThree = simplifyThreeSet(setThree);
        Set<String[]> pairs = new HashSet<>();
        for (String one : setOne) {
            for (String anotherOne : stringSetThree) {
                pairs.add(new String[]{one, anotherOne});
                pairs.add(new String[]{anotherOne, one});
            }
        }
        return pairs;
    }

    private static Set<String[]> createPairsTwoToOne(Set<String> setTwo, Set<String> setOne) {
        Set<String> stringSetTwo = new HashSet<>();
        for (String code : setTwo) {
            char[] codes = sortSecondLine(code).toCharArray();
            for (int line = 0; line < 2; line++) {
                codes[line * 3] = codes[line * 3] == EMPTY_UNIT ? codes[2 + line * 3] : codes[line * 3];
                codes[1 + line * 3] = codes[1 + line * 3] == EMPTY_UNIT ? codes[2 + line * 3] : codes[1 + line * 3];
                codes[2 + line * 3] = EMPTY_UNIT;
            }
            stringSetTwo.add(String.valueOf(codes));
        }
        Set<String[]> pairs = new HashSet<>();
        for (String one : stringSetTwo) {
            for (String anotherOne : setOne) {
                pairs.add(new String[]{one, anotherOne});
                pairs.add(new String[]{anotherOne, one});
            }
        }
        return pairs;
    }

    private static Set<String[]> createPairsTwoToThree(Set<String> codeSetTwo, Set<String> codeSetThree) {
        Set<String> stringSetTwo = new HashSet<>();
        for (String code : codeSetTwo) {
            char[] codes = sortSecondLine(code).toCharArray();
            for (int line = 0; line < 2; line++) {
                codes[line * 3] = codes[line * 3] == EMPTY_UNIT ? codes[1 + line * 3] : codes[line * 3];
                codes[2 + line * 3] = codes[2 + line * 3] == EMPTY_UNIT ? codes[1 + line * 3] : codes[2 + line * 3];
                codes[1 + line * 3] = EMPTY_UNIT;
            }
            stringSetTwo.add(String.valueOf(codes));
        }
        Set<String> stringSetThree = simplifyThreeSet(codeSetThree);
        Set<String[]> pairs = new HashSet<>();
        for (String one : stringSetTwo) {
            for (String anotherOne : stringSetThree) {
                pairs.add(new String[]{one, anotherOne});
                pairs.add(new String[]{anotherOne, one});
            }
        }
        return pairs;
    }

    private static Set<String[]> createPairsTwoToTwo(Set<String> codesSet) {
        Set<String> stringSet = new HashSet<>();
        for (String code : codesSet) {
            char[] codes = sortSecondLine(code).toCharArray();
            for (int line = 0; line < 2; line++) {
                codes[line * 3] = codes[line * 3] == EMPTY_UNIT ? codes[1 + line * 3] : codes[line * 3];
                codes[2 + line * 3] = codes[2 + line * 3] == EMPTY_UNIT ? codes[1 + line * 3] : codes[2 + line * 3];
                codes[1 + line * 3] = EMPTY_UNIT;
            }
            stringSet.add(String.valueOf(codes));
        }
        Set<String[]> pairs = new HashSet<>();
        for (String one : stringSet) {
            for (String anotherOne : stringSet) {
                pairs.add(new String[]{one, anotherOne});
            }
        }
        return pairs;
    }

    private static Set<String[]> createPairsOneToOne(Set<String> codesSet) {
        Set<String[]> pairs = new HashSet<>();
        for (String one : codesSet) {
            for (String anotherOne : codesSet) {
                pairs.add(new String[]{one, anotherOne});
            }
        }
        return pairs;
    }

    private static Set<String[]> createPairsThirdToThird(Set<String> codesSet) {
        Set<String> stringSet = simplifyThreeSet(codesSet);
        Set<String[]> pairs = new HashSet<>();
        for (String one : stringSet) {
            for (String anotherOne : stringSet) {
                pairs.add(new String[]{one, anotherOne});
            }
        }
        return pairs;
    }

    private static Set<String> simplifyThreeSet(Set<String> codesSet) {
        Set<String> stringSet = new HashSet<>();
        for (String code : codesSet) {
            stringSet.add(sortSecondLine(code));
        }
        return stringSet;
    }

    private static String sortSecondLine(String code) {
        char[] codes = code.toCharArray();
        for (int i = 0; i < 2; i++) {
            for (int j = i + 1; j < 3; j++) {
                if (codes[i] > codes[j]) {
                    char temp = codes[j];
                    codes[j] = codes[i];
                    codes[i] = temp;
                }
            }
        }
        return String.valueOf(codes);
    }

    private static String transformThreeToOne(String code) {
        char[] codes = code.toCharArray();
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

    private static String transformOneToThree(String code) {
        char[] codes = code.toCharArray();
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

    private static int decide(String code) {
        char[] codes = code.toCharArray();
        int counter = 0;
        for (int i = 0; i < 3; i++) {
            if (codes[i] != EMPTY_UNIT) {
                counter++;
            }
        }
        return counter;
    }


    /**
     * Ожидаем пока не освободится один из занятых потоков.
     *
     * @param threadPoolExecutor группа, в которой создаются потоки
     */
    private static void waitQueue(final ThreadPoolExecutor threadPoolExecutor) throws Exception {
        while (threadPoolExecutor.getQueue().size() >= QUEUE_SIZE - 3) {
            sleep(100);
        }
        printTimeInformation(threadPoolExecutor);
    }

    /**
     * Ожидаем пока не освободятся все потоки
     *
     * @param threadPoolExecutor группа, в которой создаются потоки
     */
    private static void waitEnd(final ThreadPoolExecutor threadPoolExecutor) throws Exception {
        while (threadPoolExecutor.getActiveCount() > 0) {
            sleep(100);
            printTimeInformation(threadPoolExecutor);
        }
    }

    private static void printTimeInformation(final ThreadPoolExecutor threadPoolExecutor) {
        if ((threadPoolExecutor.getCompletedTaskCount() - prevCounter >= 100)) {
            prevCounter = (int) threadPoolExecutor.getCompletedTaskCount();
            long currentTime = System.currentTimeMillis();
            System.out.printf("прошло %d игр. Текущее время %s, с начала прошло %dс\n",
                    prevCounter, new Date(currentTime), (currentTime - startTime) / 1000);
        }
    }

    private static class Room implements Runnable {
        private Player currentPlayer;
        private Player waitingPlayer;
        private final AnswerProcessor answerProcessor;
        private final BattleArena battleArena;
        private int counter = 0;

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
                    if (counter > MAX_ROUND) {
                        break;
                    }
                    battleArena.endRound();
                }
                if (checkCanMove(currentPlayer.getId())) {
                    try {
                        askPlayerProcess();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                changeCurrentAndWaitingPlayers();
            }
            GameEvent endMatch;
            int firstPlayerId = 1;
            if (whoIsWin.isEmpty()) {
                endMatch = GameEvent.GAME_END_WITH_A_TIE;
            } else if (whoIsWin.get().getId() == firstPlayerId) {
                endMatch = GameEvent.YOU_WIN_GAME;
            } else {
                endMatch = GameEvent.YOU_LOSE_GAME;
            }
            try {
                StatisticWriter.writeArmiesWinStatistic(firstArmy, secondArmy, endMatch);
            } catch (Exception ex) {
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

