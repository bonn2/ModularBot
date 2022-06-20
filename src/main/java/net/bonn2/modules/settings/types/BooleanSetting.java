package net.bonn2.modules.settings.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

public class BooleanSetting extends Setting {

    public boolean value;

    public BooleanSetting(@NotNull String value) {
        this.value = value.equalsIgnoreCase("true");
    }

    @Override
    public boolean getAsBoolean() {
        return value;
    }

    @Override
    public String getDisplayString() {
        return String.valueOf(value);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive("%s:%s".formatted(Type.BOOLEAN.toString(), value));
    }
}
