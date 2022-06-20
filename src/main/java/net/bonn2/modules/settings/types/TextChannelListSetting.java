package net.bonn2.modules.settings.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TextChannelListSetting extends Setting {

    List<String> ids;

    public TextChannelListSetting(@NotNull String string) {
        String[] splitString = string.split(",");
        ids = new ArrayList<>();
        for (String subStr : splitString)
            ids.add(subStr.trim());
    }

    public TextChannelListSetting(@NotNull List<TextChannel> channels) {
        ids = new ArrayList<>();
        for (TextChannel channel : channels)
            ids.add(channel.getId());
    }

    @Override
    public JsonElement toJson() {
        // Output: "TEXT_CHANNEL_LIST:24151132,41231241,421421352,125352435,51351421,125134123"
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("%s:".formatted(Type.TEXT_CHANNEL_LIST));
        for (String id : ids)
            stringBuilder.append("%s,".formatted(id));
        if (stringBuilder.toString().endsWith(","))
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return new JsonPrimitive(stringBuilder.toString());
    }

    @Override
    public String getDisplayString() {
        StringBuilder builder = new StringBuilder();
        for (String id : ids)
            builder.append("<#%s> ".formatted(id));
        return builder.toString().trim();
    }

    @Override
    public List<TextChannel> getAsTextChannelList(Guild guild) {
        List<TextChannel> channels = new LinkedList<>();
        for (String id : ids)
            if (!id.equals("0"))
                channels.add(guild.getTextChannelById(id));
        return channels;
    }

    @Override
    public List<String> getAsTextChannelIdList() {
        return ids;
    }
}
