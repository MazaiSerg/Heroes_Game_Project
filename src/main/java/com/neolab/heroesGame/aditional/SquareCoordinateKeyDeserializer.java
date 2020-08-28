package com.neolab.heroesGame.aditional;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neolab.heroesGame.arena.SquareCoordinate;

import java.io.IOException;

public class SquareCoordinateKeyDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(final String s,
                                 final DeserializationContext deserializationContext) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(s, SquareCoordinate.class);
    }
}
