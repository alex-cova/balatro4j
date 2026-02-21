package com.balatro.jackson;

import com.balatro.api.Item;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ItemSerializer extends JsonSerializer<Item> {
    @Override
    public void serialize(@NotNull Item value, @NotNull JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.getName());
    }
}
