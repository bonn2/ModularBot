package com.bonn2.modules.core.settings.types;

import com.bonn2.Bot;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

public class TextChannelSetting extends Setting {

    public final String id;

    public TextChannelSetting(@NotNull String id) {
        this.id = id;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive("%s:%s".formatted(Type.TEXT_CHANNEL.toString(), id));
    }

    @Override
    public String getDisplayString() {
        return "<#%s>".formatted(id);
    }

    @Override
    public TextChannel getAsTextChannel() {
        return Bot.jda.getTextChannelById(id);
    }
}
