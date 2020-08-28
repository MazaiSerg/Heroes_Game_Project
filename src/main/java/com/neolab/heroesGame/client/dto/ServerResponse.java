package com.neolab.heroesGame.client.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neolab.heroesGame.arena.BattleArena;
import com.neolab.heroesGame.server.ActionEffect;

/**
 * класс который берет из запроса сервера новую доску и эффефект предыдущего ответа и преобразует в объекты
 */
public class ServerResponse {
    public final BattleArena board;
    public final ActionEffect actionEffect;

    public ServerResponse(final String boardJson, final String actionEffectJson) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        board = mapper.readValue(boardJson, BattleArena.class);
        actionEffect = mapper.readValue(actionEffectJson, ActionEffect.class);
    }

    public ServerResponse(final String boardJson) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        board = mapper.readValue(boardJson, BattleArena.class);
        actionEffect = null;
    }
}
