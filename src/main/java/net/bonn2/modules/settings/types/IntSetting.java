package net.bonn2.modules.settings.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class IntSetting extends Setting {

    public int value;

    public IntSetting(int number) {
        value = number;
    }

    @Override
    public int getAsInt() {
        return value;
    }

    @Override
    public String getDisplayString() {
        return String.valueOf(value);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive("%s:%s".formatted(Type.INT.toString(), value));
    }
}
