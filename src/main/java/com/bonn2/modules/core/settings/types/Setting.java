package com.bonn2.modules.core.settings.types;

import com.bonn2.Bot;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;

public abstract class Setting {

    public enum Type {
        INT, DOUBLE, FLOAT, NULL;

        public static Type fromString(String string) {
            return switch (string.toUpperCase()) {
                case "INT" -> INT;
                case "DOUBLE" -> DOUBLE;
                case "FLOAT" -> FLOAT;
                default -> NULL;
            };
        }
    }

    public abstract JsonElement toJson();

    /**
     * @param element Must be a string in the form `type:value`
     * @return A setting object of the correct type
     */
    public static Setting fromJson(@NotNull JsonElement element) {
        String elementString = element.getAsString();
        // Split elementString into an array where:
        // 0 : type
        // 1 : value
        String[] split = elementString.split(":", 2);
        return of(split[1], Type.fromString(split[0]));
    }

    /**
     * Creates a setting of type-type with the value-value
     * @param value A string representation of the value to set
     * @param type The type of setting to create
     * @return The setting or null if unknown type
     */
    public static Setting of(@NotNull String value, Type type) {
        try {
            return switch (type) {
                case INT -> new IntSetting(Integer.parseInt(value));
                case DOUBLE -> new DoubleSetting(Double.parseDouble(value));
                case FLOAT -> new FloatSetting(Float.parseFloat(value));
                default -> null;
            };
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public abstract String getAsString();

    public double getAsDouble() {
        Bot.logger.warn("Getting unimplemented setting value!");
        return 0;
    }

    public int getAsInt() {
        Bot.logger.warn("Getting unimplemented setting value!");
        return 0;
    }

    public float getAsFloat() {
        Bot.logger.warn("Getting unimplemented setting value!");
        return 0;
    }
}
