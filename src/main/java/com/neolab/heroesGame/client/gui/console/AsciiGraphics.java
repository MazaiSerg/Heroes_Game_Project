package com.neolab.heroesGame.client.gui.console;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.client.dto.ExtendedServerResponse;
import com.neolab.heroesGame.client.gui.IGraphics;
import com.neolab.heroesGame.enumerations.HeroActions;
import com.neolab.heroesGame.heroes.Hero;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public class AsciiGraphics implements IGraphics {
    public static final int TERMINAL_WIDTH = 70;
    public static final int TERMINAL_HEIGHT = 45;
    public static final int ARMY_WIDTH = 40;
    public static final int ENEMY_ARMY_ROW_START_AT = 0;
    public static final int YOUR_ARMY_ROW_START_AT = 20;
    public static final SquareCoordinate IS_AOE_EFFECT = new SquareCoordinate(-1, -1);
    private int lastRow = 0;
    private final DialogForCreatingArmies armiesDialog;
    private final DialogForChoosingAction actionDialog;
    private final ActionPresenter presenter;

    public AsciiGraphics(final int playerId) throws IOException {

        final Terminal term = new DefaultTerminalFactory().setInitialTerminalSize(
                new TerminalSize(TERMINAL_WIDTH, TERMINAL_HEIGHT)).createTerminal();
        final TextGraphics textGraphics = term.newTextGraphics();
        armiesDialog = new DialogForCreatingArmies(term, textGraphics);
        actionDialog = new DialogForChoosingAction(term, textGraphics);
        presenter = new ActionPresenter(playerId, term, textGraphics);
    }

    public void setPresenterPlayerId(final int id) {
        presenter.setPlayerId(id);
    }

    @Override
    public void showPosition(final ExtendedServerResponse response) throws IOException {
        lastRow = presenter.showPosition(response);
    }

    @Override
    public void endGame(final ExtendedServerResponse response) throws IOException {
        lastRow = presenter.endGame(response);
    }

    @Override
    public SquareCoordinate chooseUnit(final Army army) throws IOException {
        return actionDialog.chooseUnit(army, lastRow);
    }

    @Override
    public HeroActions chooseActionForHero(final SquareCoordinate coordinate, final Hero hero) throws IOException {
        return actionDialog.chooseActionForHero(coordinate, hero, lastRow);
    }

    @Override
    public SquareCoordinate chooseTargetCoordinate(final SquareCoordinate activeHeroCoordinate, final Hero activeHero,
                                                   final Army army) throws IOException {
        return actionDialog.chooseTargetCoordinate(activeHeroCoordinate, activeHero, army, lastRow);
    }

    @Override
    public Hero getHeroChoose(final Map<Integer, Hero> army) throws IOException {
        return armiesDialog.getHeroChoose(army);
    }

    @Override
    public int getHeroPositionChoose(final Hero hero, final Map<Integer, Hero> army) throws IOException {
        return armiesDialog.getHeroPositionChoose(hero, army);
    }

    @Override
    public boolean finishCreatingArmy(final Map<Integer, Hero> army) throws IOException {
        return armiesDialog.finishCreatingArmy(army);
    }

    @Override
    public Hero getHeroChoose(final Army enemyArmy, final Map<Integer, Hero> yourArmy) throws IOException {
        return armiesDialog.getHeroChoose(enemyArmy.getHeroes(), yourArmy);
    }

    @Override
    public int getHeroPositionChoose(final Hero hero, final Army enemyArmy, final Map<Integer, Hero> yourArmy) throws IOException {
        return armiesDialog.getHeroPositionChoose(hero, enemyArmy.getHeroes(), yourArmy);
    }

    @Override
    public boolean finishCreatingArmy(final Army enemyArmy, final Map<Integer, Hero> yourArmy) throws IOException {
        return armiesDialog.finishCreatingArmy(enemyArmy.getHeroes(), yourArmy);
    }

    public static int getChoose(final List<String> strings, final int lastRow, final int leftOffset,
                                final Terminal term, final TextGraphics textGraphics) throws IOException {
        while (true) {
            clearChooseSector(lastRow, term, textGraphics);
            textGraphics.putString(leftOffset, lastRow, strings.get(0), SGR.BOLD);
            for (int i = 1; i < strings.size(); i++) {
                textGraphics.putString(leftOffset, lastRow + i, strings.get(i));
            }
            term.flush();
            final String request = AsciiGraphics.readString(lastRow + strings.size(), term, textGraphics);
            try {
                final int result = Integer.parseInt(request);
                if (0 < result && result < strings.size()) {
                    return result;
                } else {
                    textGraphics.putString(leftOffset, lastRow + strings.size(),
                            "Введите номер одного из доступных выборов!!!!!");
                    term.flush();
                    term.readInput();
                }
            } catch (final NumberFormatException ex) {
                textGraphics.putString(leftOffset, lastRow + strings.size(), "Введите число!!!!!");
                term.flush();
                term.readInput();
            }
        }
    }

    private static String readString(final int currentRow, final Terminal term,
                                     final TextGraphics textGraphics) throws IOException {

        final StringBuilder stringBuilder = new StringBuilder();
        int offset = (term.getTerminalSize().getColumns() - ARMY_WIDTH) / 2;
        term.setCursorPosition(offset, currentRow);
        term.flush();
        while (true) {
            final KeyStroke key = term.readInput();

            if (key.getKeyType() == KeyType.Enter || key.getKeyType() == KeyType.EOF) {
                break;
            }
            if (key.getKeyType() == KeyType.Backspace) {
                textGraphics.putString(--offset, currentRow, " ");
                term.setCursorPosition(offset, currentRow);
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            } else {
                textGraphics.putString(offset++, currentRow, String.valueOf(key.getCharacter()));
                term.setCursorPosition(offset, currentRow);
                stringBuilder.append(key.getCharacter());
            }
            term.flush();
        }
        return stringBuilder.toString();
    }


    private static void clearChooseSector(final int lastRow, final Terminal term,
                                          final TextGraphics textGraphics) throws IOException {
        for (int i = lastRow; i < TERMINAL_HEIGHT; i++) {
            textGraphics.putString(0, i,
                    "                                                                     ");
        }
        term.flush();
    }

    public static String putToTheCenter(final String state, final int width) {
        final int offset = (width - state.length()) / 2;
        return " ".repeat(offset) + state + " ".repeat(width - offset - state.length());
    }

}
