package com.bonn2.modules.settings.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

public class StringSetting extends Setting {

    public String value;

    public StringSetting(@NotNull String string) {
        value = string;
    }

    @Override
    public String getAsString() {
        return value;
    }

    @Override
    public String getDisplayString() {
        return value;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive("%s:%s".formatted(Type.STRING.toString(), value));
    }
}
