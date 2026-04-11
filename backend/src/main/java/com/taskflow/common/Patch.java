package com.taskflow.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Objects;

public final class Patch<T> {

    private static final Patch<?> ABSENT = new Patch<>(false, null);

    private final boolean present;
    private final T value;

    private Patch(boolean present, T value) {
        this.present = present;
        this.value = value;
    }

    // Called by Jackson when field is present in JSON (even if value is null)
    @JsonCreator
    public static <T> Patch<T> of(T value) {
        return new Patch<>(true, value);
    }

    // Used as default when field is absent from JSON entirely
    public static <T> Patch<T> absent() {
        @SuppressWarnings("unchecked")
        Patch<T> absent = (Patch<T>) ABSENT;
        return absent;
    }

    public boolean isPresent() { return present; }
    public boolean isAbsent()  { return !present; }
    public T get()             { return value; }        // can return null — that means "clear"
    public boolean isNull()    { return present && value == null; }

    @Override
    public String toString() {
        return present ? "Patch[" + value + "]" : "Patch.absent";
    }
}