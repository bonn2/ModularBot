package com.bonn2.modules.core.settings.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class FloatSetting extends Setting {

    public float value;

    public FloatSetting(float number) {
        value = number;
    }

    @Override
    public float getAsFloat() {
        return value;
    }

    @Override
    public String getDisplayString() {
        return String.valueOf(value);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive("%s:%s".formatted(Type.FLOAT.toString(), value));
    }
}
