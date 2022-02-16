package com.bonn2.modules.core.settings.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class StringSetting extends Setting {

    public String value;

    public StringSetting(String string) {
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
