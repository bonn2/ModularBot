package com.bonn2.modules.core.chatlog;

import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;
import net.dv8tion.jda.api.entities.TextChannel;

public class ChatLog extends Module {

    public enum Type {
        AUTO_KICK, KICK, BAN
    }

    public ChatLog() {
        version = "v1.0";
        priority = Priority.POST_JDA_HIGH;
        name = "ChatLog";
    }

    @Override
    public void registerSettings() {
        Settings.register(this, "log_channel", Setting.Type.TEXT_CHANNEL, Setting.Type.TEXT_CHANNEL.unset);
    }

    @Override
    public void load() {

    }

    public TextChannel getLogChannel() {
        return Settings.get(this, "log_channel").getAsTextChannel();
    }
}
