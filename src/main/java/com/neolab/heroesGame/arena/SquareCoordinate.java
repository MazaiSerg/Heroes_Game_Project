package com.neolab.heroesGame.arena;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class SquareCoordinate {

    private final int x;
    private final int y;
    public static final SquareCoordinate coordinateDoesntMatters = new SquareCoordinate(-1, -1);

    @JsonCreator
    public SquareCoordinate(@JsonProperty("x") final int x,
                            @JsonProperty("y") final int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
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
        return Objects.hash(x, y);
    }
}
