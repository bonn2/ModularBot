package net.bonn2.modules.settings.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.dv8tion.jda.api.entities.Guild;
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
    public TextChannel getAsTextChannel(Guild guild) {
        return guild.getTextChannelById(id);
    }
}
