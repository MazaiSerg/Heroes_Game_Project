package com.neolab.heroesGame.client.ai.version.mechanics.arena;

import java.util.ArrayList;
import java.util.List;

public class SquareCoordinate {

    private final int x;
    private final int y;
    public static final SquareCoordinate coordinateDoesntMatters = new SquareCoordinate(-1, -1);
    public static List<SquareCoordinate> allCoordinates;

    static {
        allCoordinates = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            allCoordinates.add(new SquareCoordinate(i % 3, i / 3));
        }
        allCoordinates.add(coordinateDoesntMatters);
    }

    private SquareCoordinate(final int x,
                             final int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public static SquareCoordinate getSquareCoordinate(final int index) {
        return allCoordinates.get(index);
    }

    public static SquareCoordinate getSquareCoordinate(final int x, final int y) {
        return allCoordinates.get(x + y * 3);
    }

    public int getIndex() {
        if (x == -1) {
            return 6;
        }
        return x + y * 3;
    }

    public static SquareCoordinate getSquareCoordinate(final com.neolab.heroesGame.arena.SquareCoordinate coordinate) {
        return allCoordinates.get(coordinate.getX() + coordinate.getY() * 3);
    }

    public com.neolab.heroesGame.arena.SquareCoordinate toCommonCoordinate() {
        return new com.neolab.heroesGame.arena.SquareCoordinate(x, y);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SquareCoordinate that = (SquareCoordinate) o;
        return x == that.x &&
                y == that.y;
    }

    @Override
    public int hashCode() {
        return 1 + x + 10 * (y + 1);
    }
}
