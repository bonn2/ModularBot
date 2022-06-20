package net.bonn2.modules.settings.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class DoubleSetting extends Setting {

    public double value;

    public DoubleSetting(double number) {
        value = number;
    }

    @Override
    public double getAsDouble() {
        return value;
    }

    @Override
    public String getDisplayString() {
        return String.valueOf(value);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive("%s:%s".formatted(Type.DOUBLE.toString(), value));
    }
}
