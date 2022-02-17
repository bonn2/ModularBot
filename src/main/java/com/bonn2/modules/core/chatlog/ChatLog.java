package com.bonn2.modules.core.chatlog;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.w3c.dom.Text;

public class ChatLog extends Module {

    public enum Type {
        AUTO_KICK, KICK, BAN
    }

    private static TextChannel logChannel;
    private static ChatLog instance;

    public ChatLog() {
        version = "v1.1";
        priority = Priority.POST_JDA_HIGH;
        name = "ChatLog";
    }

    @Override
    public void registerSettings() {
        Settings.register(this, "log_channel", Setting.Type.TEXT_CHANNEL, Setting.Type.TEXT_CHANNEL.unset,
                "The channel log messages are sent to.");
    }

    @Override
    public void load() {
        instance = this;
        Bot.logger.info("Getting channels...");
        logChannel = Settings.get(this, "log_channel").getAsTextChannel();
    }

    @Override
    public CommandData[] getCommands() {
        return new CommandData[0];
    }

    public static TextChannel getLogChannel() {
        TextChannel channelFromSettings = Settings.get(instance, "log_channel").getAsTextChannel();
        if (logChannel == null)
            logChannel = Settings.get(instance, "log_channel").getAsTextChannel();
        if (!logChannel.getId().equals(channelFromSettings.getId()))
            logChannel = channelFromSettings;
        return logChannel;
    }
}
