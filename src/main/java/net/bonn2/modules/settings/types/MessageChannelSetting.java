package net.bonn2.modules.settings.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jetbrains.annotations.NotNull;

public class MessageChannelSetting extends Setting {

    public final String id;

    public MessageChannelSetting(@NotNull String id) {
        this.id = id;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive("%s:%s".formatted(Type.MESSAGE_CHANNEL.toString(), id));
    }

    @Override
    public String getDisplayString() {
        return "<#%s>".formatted(id);
    }

    @Override
    public MessageChannel getAsMessageChannel(Guild guild) {
        return guild.getTextChannelById(id);
    }
}
