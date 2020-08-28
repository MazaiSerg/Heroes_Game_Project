package com.neolab.heroesGame.client.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neolab.heroesGame.server.answers.Answer;

/**
 * класс который берет ответ пользоватля и преобразует в JSON
 */
public class ClientRequest {
    public final String jsonAnswer;

    public ClientRequest(final Answer answer) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        jsonAnswer = mapper.writeValueAsString(answer);
    }

}
