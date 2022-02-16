package com.bonn2.modules.core.settings.types;

import com.bonn2.Bot;
import com.google.gson.JsonElement;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class Setting {

    public enum Type {
        INT("0"), DOUBLE("0"), FLOAT("0"), ROLE("0"), NULL("0"),
        ROLE_LIST("0"), TEXT_CHANNEL("0"), STRING("");

        public final String unset;

        Type(String unset) {
            this.unset = unset;
        }

        public static Type fromString(@NotNull String string) {
            return switch (string.toUpperCase()) {
                case "INT" -> INT;
                case "DOUBLE" -> DOUBLE;
                case "FLOAT" -> FLOAT;
                case "ROLE" -> ROLE;
                case "ROLE_LIST" -> ROLE_LIST;
                case "TEXT_CHANNEL" -> TEXT_CHANNEL;
                case "STRING" -> STRING;
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
                case ROLE -> new RoleSetting(value);
                case ROLE_LIST -> new RoleListSetting(value);
                case TEXT_CHANNEL -> new TextChannelSetting(value);
                case STRING -> new StringSetting(value);
                default -> null;
            };
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public abstract String getDisplayString();

    public double getAsDouble() {
        Bot.logger.warn("Getting unimplemented setting value!");
        return Double.parseDouble(Type.DOUBLE.unset);
    }

    public int getAsInt() {
        Bot.logger.warn("Getting unimplemented setting value!");
        return Integer.parseInt(Type.INT.unset);
    }

    public float getAsFloat() {
        Bot.logger.warn("Getting unimplemented setting value!");
        return Float.parseFloat(Type.FLOAT.unset);
    }

    public Role getAsRole() {
        Bot.logger.warn("Getting unimplemented setting value!");
        return null;
    }

    public List<Role> getAsRoleList() {
        Bot.logger.warn("Getting unimplemented setting value!");
        return new ArrayList<>();
    }

    public List<String> getAsRoleIdList() {
        Bot.logger.warn("Getting unimplemented setting value!");
        return new ArrayList<>();
    }

    public TextChannel getAsTextChannel() {
        Bot.logger.warn("Getting unimplemented setting value!");
        return null;
    }

    public String getAsString() {
        Bot.logger.warn("Getting unimplemented setting value!");
        return "";
    }
}
