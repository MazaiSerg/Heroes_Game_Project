package com.neolab.heroesGame.aditional;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.neolab.heroesGame.arena.SquareCoordinate;

import java.io.IOException;

public class SquareCoordinateKeySerializer extends JsonSerializer<SquareCoordinate> {

    @Override
    public void serialize(final SquareCoordinate coordinate, final JsonGenerator jsonGenerator,
                          final SerializerProvider serializerProvider) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        jsonGenerator.writeFieldName(mapper.writeValueAsString(coordinate));
    }
}
