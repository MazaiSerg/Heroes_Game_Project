package com.neolab.heroesGame.server.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.enumerations.GameEvent;
import com.neolab.heroesGame.server.ActionEffect;

import java.util.Objects;

public class ExtendedServerRequest {
    @JsonProperty
    public final GameEvent event;
    @JsonProperty
    public final BattleArena arena;
    @JsonProperty
    public final ActionEffect effect;

    @JsonCreator
    private ExtendedServerRequest(@JsonProperty("arena") final BattleArena arena,
                                  @JsonProperty("effect") final ActionEffect effect,
                                  @JsonProperty("event") final GameEvent event) {
        this.event = event;
        this.arena = arena;
        this.effect = effect;
    }

    public static ExtendedServerRequest getRequestFromString(final String request) throws JsonProcessingException {
        return new ObjectMapper().readValue(request, ExtendedServerRequest.class);
    }

    public static String getRequestString(final GameEvent event, final BattleArena arena,
                                          final ActionEffect effect) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(new ExtendedServerRequest(arena, effect, event));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ExtendedServerRequest that = (ExtendedServerRequest) o;
        return event == that.event &&
                Objects.equals(arena, that.arena) &&
                Objects.equals(effect, that.effect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, arena, effect);
    }
}
