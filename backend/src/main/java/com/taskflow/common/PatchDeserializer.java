package com.taskflow.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;

import java.io.IOException;

public class PatchDeserializer extends JsonDeserializer<Patch<?>> implements ContextualDeserializer {

    private JavaType valueType;

    public PatchDeserializer() {}

    private PatchDeserializer(JavaType valueType) {
        this.valueType = valueType;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctx, BeanProperty property) {
        // Extract the generic type T from Patch<T>
        JavaType wrapperType = property.getType();
        JavaType innerType = wrapperType.containedType(0);
        return new PatchDeserializer(innerType);
    }

    @Override
    public Patch<?> deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        // Field is present in JSON — deserialize the value (may be null)
        Object value = ctx.readValue(p, valueType);
        return Patch.of(value);
    }

    @Override
    public Patch<?> getNullValue(DeserializationContext ctx) {
        // null value in JSON → {"assigneeId": null} → Patch.of(null) means "clear"
        return Patch.of(null);
    }

    @Override
    public Patch<?> getAbsentValue(DeserializationContext ctx) {
        // field missing from JSON entirely → Patch.absent() means "don't touch"
        return Patch.absent();
    }
}