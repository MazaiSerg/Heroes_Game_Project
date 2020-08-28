package com.neolab.heroesGame.server.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neolab.heroesGame.server.answers.Answer;


/**
 * класс который принимает ответ клиента в формате JSON и преобразует в объект
 */
public class ClientResponse {
    private final Answer answer;

    public ClientResponse(final String jsonString) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        this.answer = mapper.readValue(jsonString, Answer.class);
    }

    public Answer getAnswer() {
        return answer;
    }
}
