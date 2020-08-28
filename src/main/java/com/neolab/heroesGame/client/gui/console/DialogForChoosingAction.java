package com.neolab.heroesGame.client.gui.console;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.terminal.Terminal;
import com.neolab.heroesGame.aditional.CommonFunction;
import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.SquareCoordinate;
import com.neolab.heroesGame.enumerations.HeroActions;
import com.neolab.heroesGame.heroes.Hero;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.neolab.heroesGame.client.gui.console.AsciiGraphics.ARMY_WIDTH;

public class DialogForChoosingAction {
    private final Terminal term;
    private final TextGraphics textGraphics;
    private final int leftOffset;

    public DialogForChoosingAction(final Terminal term, final TextGraphics textGraphics) throws IOException {
        this.term = term;
        this.textGraphics = textGraphics;
        leftOffset = (term.getTerminalSize().getColumns() - ARMY_WIDTH) / 2;
    }

    /**
     * Формируем строки, которые увидет пользователь, из доступных для действия героев
     * counter используется только для обозначения номера строки
     * gui.getChoose() возвращает номер строки, которую выбрал пользователь.
     * Индекс строки на 1 больше индекса соответствующей координаты
     */
    public SquareCoordinate chooseUnit(final Army army, final int lastRow) throws IOException {
        final List<String> strings = new ArrayList<>();
        final List<SquareCoordinate> listVariationCoordinate = new ArrayList<>();
        final Map<SquareCoordinate, Hero> heroes = army.getAvailableHeroes();
        strings.add("Выберите юнита для действия:");
        int counter = 1;
        for (final SquareCoordinate key : heroes.keySet()) {
            strings.add(String.format("%d. %s", counter++, makeUnitChooseString(key, heroes.get(key))));
            listVariationCoordinate.add(key);
        }
        return listVariationCoordinate.get(AsciiGraphics.getChoose(strings, lastRow, leftOffset, term, textGraphics) - 1);
    }

    private String makeUnitChooseString(final SquareCoordinate coordinate, final Hero hero) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(hero.getClassName());
        if (coordinate.getX() == 0) {
            stringBuilder.append(" на левом фланге");
        } else if (coordinate.getX() == 1) {
            stringBuilder.append(" по центру");
        } else {
            stringBuilder.append(" на правом фланге");
        }
        return stringBuilder.toString();
    }

    /**
     * Формируем список строк, которые увидет пользователь. Смотрим его выбор и отправляем соответствующий результат
     */
    public HeroActions chooseActionForHero(final SquareCoordinate coordinate, final Hero hero,
                                           final int lastRow) throws IOException {
        final List<String> strings = new ArrayList<>();
        strings.add(String.format("Выберите действие для %s:", makeUnitChooseString(coordinate, hero)));
        strings.add("1. Защита");
        strings.add(CommonFunction.isUnitHealer(hero) ? "2. Лечить союзников" : "2. Атаковать");
        strings.add("3. Вернуться к выбору юнита");
        final int choose = AsciiGraphics.getChoose(strings, lastRow, leftOffset, term, textGraphics);
        if (choose == 3) {
            return null;
        } else if (choose == 1) {
            return HeroActions.DEFENCE;
        }
        return CommonFunction.isUnitHealer(hero) ? HeroActions.HEAL : HeroActions.ATTACK;
    }

    /**
     * Формируем сет доступных целей. Для лучника это вся вражеская армия, для лекаря - вся своя армия, для воинов
     * определяем доступные цели с помощью функции CommonFunction.getCorrectTargetForFootman()
     * Формируем строки, которые увидет пользователь. counter будет равен индексу последней строки
     * gui.getChoose() возвращает номер строки, которую выбрал пользователь. Если это последняя строка, то возвращаемся
     * к выбору героя. Иначе выбираем координаты из списка доступных.
     * Индекс строки на 1 больше индекса соответствующей координаты
     */
    public SquareCoordinate chooseTargetCoordinate(final SquareCoordinate activeHeroCoordinate, final Hero activeHero,
                                                   final Army army, final int lastRow) throws IOException {
        final Set<SquareCoordinate> legalTargetCoordinate;
        if (CommonFunction.isUnitArcher(activeHero) || CommonFunction.isUnitHealer(activeHero)) {
            legalTargetCoordinate = army.getHeroes().keySet();
        } else {
            legalTargetCoordinate = CommonFunction.getCorrectTargetForFootman(activeHeroCoordinate, army);
        }
        final List<String> strings = new ArrayList<>();
        final List<SquareCoordinate> listVariationCoordinate = new ArrayList<>();
        strings.add(String.format("Выберите цель %s: ", makeUnitChooseString(activeHeroCoordinate, activeHero)));

        int counter = 1;
        for (final SquareCoordinate key : legalTargetCoordinate) {
            strings.add(String.format("%d. %s", counter++, makeUnitChooseString(key, army.getHero(key).get())));
            listVariationCoordinate.add(key);
        }
        strings.add(String.format("%d. Вернуться к выбору юнита", counter));
        final int choose = AsciiGraphics.getChoose(strings, lastRow, leftOffset, term, textGraphics);
        return choose == counter ? null : listVariationCoordinate.get(choose - 1);
    }

}
