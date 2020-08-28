package com.neolab.heroesGame.client.ai;

import com.neolab.heroesGame.arena.Army;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.errors.HeroExceptions;
import com.neolab.heroesGame.server.answers.Answer;

import java.io.IOException;
import java.util.Objects;

public abstract class Player {

    private int id;

    private String name;

    public Player(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    public abstract Answer getAnswer(final BattleArena board) throws HeroExceptions, IOException;

    public abstract String getStringArmyFirst(final int armySize) throws IOException;

    public abstract String getStringArmySecond(final int armySize, final Army army) throws IOException;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Player player = (Player) o;
        return id == player.id &&
                Objects.equals(name, player.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
