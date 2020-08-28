package com.neolab.heroesGame.client.gui.console;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.terminal.Terminal;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.heroes.Footman;
import com.neolab.heroesGame.heroes.Hero;
import com.neolab.heroesGame.heroes.IWarlord;
import com.neolab.heroesGame.heroes.factory.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.neolab.heroesGame.client.gui.console.AsciiGraphics.ARMY_WIDTH;
import static com.neolab.heroesGame.client.gui.console.AsciiGraphics.putToTheCenter;

public class DialogForCreatingArmies {
    private final Terminal term;
    private final TextGraphics textGraphics;
    private final int leftOffset;
    private int lastRow;

    public DialogForCreatingArmies(final Terminal term, final TextGraphics textGraphics) throws IOException {
        this.term = term;
        this.textGraphics = textGraphics;
        leftOffset = (term.getTerminalSize().getColumns() - ARMY_WIDTH) / 2;
    }

    public Hero getHeroChoose(final Map<Integer, Hero> army) throws IOException {
        term.clearScreen();
        lastRow = 0;
        if (army.size() > 0) {
            showCurrentArmy(army);
            lastRow = 15;
        }
        if (army.isEmpty()) {
            return chooseWarlord();
        }
        return chooseUnit(army);
    }

    public int getHeroPositionChoose(final Hero hero, final Map<Integer, Hero> army) throws IOException {
        term.clearScreen();
        lastRow = 0;
        setBlackOnWhiteText();
        showCurrentArmy(army);
        lastRow = 15;
        final int offset = (AsciiGraphics.TERMINAL_WIDTH - 14) / 2;
        showHeroStats(hero, offset, lastRow);
        lastRow += 8;
        return choosePosition(hero, army);
    }

    public boolean finishCreatingArmy(final Map<Integer, Hero> army) throws IOException {
        term.clearScreen();
        lastRow = 0;
        showCurrentArmy(army);
        lastRow = 15;
        final List<String> strings = new ArrayList<>();
        strings.add("Вы уверены, что хотите закончить создание своей армии?");
        strings.add("1. Я уверен, что хочу закончить формировать армию");
        strings.add("2. Я еще не готов. Отменяем последнее действие");
        setWhiteOnBlackText();
        return AsciiGraphics.getChoose(strings, lastRow, leftOffset, term, textGraphics) == 1;
    }

    public Hero getHeroChoose(final Map<SquareCoordinate, Hero> enemyArmy,
                              final Map<Integer, Hero> yourArmy) throws IOException {
        term.clearScreen();
        lastRow = 0;
        showEnemy(enemyArmy);
        lastRow = 15;
        if (yourArmy.size() > 0) {
            showCurrentArmy(yourArmy);
            lastRow = 30;
        }
        if (yourArmy.isEmpty()) {
            return chooseWarlord();
        }
        return chooseUnit(yourArmy);
    }

    public int getHeroPositionChoose(final Hero hero, final Map<SquareCoordinate, Hero> enemyArmy,
                                     final Map<Integer, Hero> yourArmy) throws IOException {
        term.clearScreen();
        lastRow = 0;
        showEnemy(enemyArmy);
        lastRow = 15;
        showCurrentArmy(yourArmy);
        lastRow = 30;
        final int offset = (AsciiGraphics.TERMINAL_WIDTH - 14) / 2;
        showHeroStats(hero, offset, lastRow);
        lastRow += 8;
        return choosePosition(hero, yourArmy);
    }

    public boolean finishCreatingArmy(final Map<SquareCoordinate, Hero> enemyArmy,
                                      final Map<Integer, Hero> yourArmy) throws IOException {
        term.clearScreen();
        lastRow = 0;
        showEnemy(enemyArmy);
        lastRow = 15;
        showCurrentArmy(yourArmy);
        lastRow = 30;
        final List<String> strings = new ArrayList<>();
        strings.add("Вы уверены, что хотите закончить создание своей армии?");
        strings.add("1. Я уверен, что хочу закончить формировать армию");
        strings.add("2. Я еще не готов. Отменяем последнее действие");
        setWhiteOnBlackText();
        return AsciiGraphics.getChoose(strings, lastRow, leftOffset, term, textGraphics) == 1;
    }

    private void showCurrentArmy(final Map<Integer, Hero> army) {
        setWhiteOnBlackText();
        final String head = "Твоя текущая армия";
        final int headOffset = (70 - head.length()) / 2;
        textGraphics.putString(headOffset, lastRow, head, SGR.BOLD);
        setBlackOnWhiteText();
        final int currentLeftOffset = 15;
        for (int i = 0; i < 6; i++) {
            final int widthOffset = currentLeftOffset + (i % 3) * 15;
            final int heightOffset = i < 3 ? lastRow + 8 : lastRow + 1;
            final Hero hero = army.get(i);
            if (hero != null) {
                showHeroStats(hero, widthOffset, heightOffset);
            } else {
                fillTheField(widthOffset, heightOffset);
            }
        }
    }

    private void showEnemy(final Map<SquareCoordinate, Hero> army) {
        setWhiteOnBlackText();
        final String head = "Твой враг";
        final int headOffset = (70 - head.length()) / 2;
        textGraphics.putString(headOffset, 0, head, SGR.BOLD);
        setBlackOnWhiteText();
        final int currentLeftOffset = 15;
        for (int y = 0; y < 2; y++) {
            final int heightOffset = y == 0 ? lastRow + 1 : lastRow + 8;
            for (int x = 0; x < 3; x++) {
                final int widthOffset = currentLeftOffset + x * 15;
                final Hero hero = army.get(new SquareCoordinate(x, y));
                if (hero != null) {
                    showHeroStats(hero, widthOffset, heightOffset);
                } else {
                    fillTheField(widthOffset, heightOffset);
                }
            }
        }
    }

    private void fillTheField(final int widthOffset, final int heightOffset) {
        for (int i = 0; i < 6; i++) {
            textGraphics.putString(widthOffset, heightOffset + i, "             ");
        }
    }

    private void show(final List<Hero> heroes) {
        setBlackOnWhiteText();
        final int stringSize = 15;
        int widthOffset = (70 - (stringSize * heroes.size())) / 2;
        for (final Hero hero : heroes) {
            final int heightOffset = lastRow;
            showHeroStats(hero, widthOffset, heightOffset);
            widthOffset += stringSize;
        }
        lastRow += 8;
    }

    private void showHeroStats(final Hero hero, final int widthOffset, int heightOffset) {
        textGraphics.putString(widthOffset, heightOffset++, putToTheCenter(hero.getClassName(), 13));
        textGraphics.putString(widthOffset, heightOffset++, String.format("HP %3d       ", hero.getHpDefault()));
        textGraphics.putString(widthOffset, heightOffset++, String.format("Damage %2d    ", hero.getDamageDefault()));
        textGraphics.putString(widthOffset, heightOffset++, String.format("Armor %2d     ", (int) (hero.getArmorDefault() * 100)));
        textGraphics.putString(widthOffset, heightOffset++, String.format("Precision %3d", (int) (hero.getPrecision() * 100)));
        if (hero instanceof IWarlord) {
            final IWarlord iWarlord = (IWarlord) hero;
            textGraphics.putString(widthOffset, heightOffset, String.format("Improve %2d   ", (int) (iWarlord.getImproveCoefficient() * 100)));
        } else {
            textGraphics.putString(widthOffset, heightOffset, "             ");
        }
    }

    private int choosePosition(final Hero hero, final Map<Integer, Hero> army) throws IOException {
        final List<String> strings = new ArrayList<>();
        final int line;
        int index = line = hero instanceof Footman ? 3 : 0;
        strings.add("Выберите позицию для юнита");
        int counter = 1;
        if (Optional.ofNullable(army.get(index++)).isEmpty()) {
            strings.add(String.format("%d. Поместить юнита на левый фланг", counter++));
        }
        if (Optional.ofNullable(army.get(index++)).isEmpty()) {
            strings.add(String.format("%d. Поместить юнита по центру", counter++));
        }
        if (Optional.ofNullable(army.get(index)).isEmpty()) {
            strings.add(String.format("%d. Поместить юнита на правый фланг", counter++));
        }
        strings.add(String.format("%d. Вернуться к выбору юнита", counter));
        setWhiteOnBlackText();
        final int choose = AsciiGraphics.getChoose(strings, lastRow, leftOffset, term, textGraphics);
        counter = 0;
        for (int i = 0; i < 3; i++) {
            if (Optional.ofNullable(army.get(line + i)).isEmpty()) {
                counter++;
            }
            if (counter == choose) {
                return line + i;
            }
        }
        return -1;
    }

    private Hero chooseWarlord() throws IOException {
        final List<Hero> warlords = new ArrayList<>();
        warlords.add(new WarlordFootmanFactory().create());
        warlords.add(new WarlordMagicianFactory().create());
        warlords.add(new WarlordVampireFactory().create());
        final List<String> strings = new ArrayList<>();
        strings.add("Выберите своего полководца");
        strings.add("1. Выбрать Генерала");
        strings.add("2. Выбрать Архимага");
        strings.add("3. Выбрать Вампира");
        show(warlords);
        term.flush();
        setWhiteOnBlackText();
        final int choose = AsciiGraphics.getChoose(strings, lastRow, leftOffset, term, textGraphics);
        return warlords.get(choose - 1);
    }

    private Hero chooseUnit(final Map<Integer, Hero> army) throws IOException {
        final List<Hero> units = new ArrayList<>();
        final List<String> strings = new ArrayList<>();
        int counter = 1;
        strings.add("Выберите юнита: ");
        if (canChooseMeleeFighter(army)) {
            strings.add(String.format("%d. Выбрать воина", counter++));
            units.add(new FootmanFactory().create());
        }
        if (canChooseRangeFighter(army)) {
            strings.add(String.format("%d. Выбрать мага", counter++));
            strings.add(String.format("%d. Выбрать лучника", counter++));
            strings.add(String.format("%d. Выбрать лекаря", counter++));
            units.add(new MagicianFactory().create());
            units.add(new ArcherFactory().create());
            units.add(new HealerFactory().create());
        }
        strings.add(String.format("%d. Отменить предыдущий выбор", counter));
        show(units);
        term.flush();
        setWhiteOnBlackText();
        final int choose = AsciiGraphics.getChoose(strings, lastRow, leftOffset, term, textGraphics);
        if (choose == counter) {
            return null;
        }
        return units.get(choose - 1);
    }

    private boolean canChooseRangeFighter(final Map<Integer, Hero> army) {
        for (int i = 0; i < 3; i++) {
            if (!army.containsKey(i)) {
                return true;
            }
        }
        return false;
    }

    private boolean canChooseMeleeFighter(final Map<Integer, Hero> army) {
        for (int i = 3; i < 6; i++) {
            if (!army.containsKey(i)) {
                return true;
            }
        }
        return false;
    }

    private void setBlackOnWhiteText() {
        textGraphics.setBackgroundColor(TextColor.ANSI.WHITE);
        textGraphics.setForegroundColor(TextColor.ANSI.BLACK);
    }

    private void setWhiteOnBlackText() {
        textGraphics.setBackgroundColor(TextColor.ANSI.BLACK);
        textGraphics.setForegroundColor(TextColor.ANSI.WHITE);
    }
}
