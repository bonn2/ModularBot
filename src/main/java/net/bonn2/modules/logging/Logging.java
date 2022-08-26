package net.bonn2.modules.logging;

import net.bonn2.Bot;
import net.bonn2.modules.Module;
import net.bonn2.modules.settings.Settings;
import net.bonn2.modules.settings.types.Setting;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Logging extends Module {

    private static Map<String, List<String>> channels = new HashMap<>();
    private static Logging instance;

    public Logging() {
        name = "Logging";
        version = "1.0";
        instance = this;
    }

    /**
     * Register a logging channel to the logger
     * @param module The module requesting the registration
     * @param channel The name of the channel
     */
    public static void register(@NotNull Module module, String channel) {
        List<String> currentModules = channels.getOrDefault(channel, new ArrayList<>());
        currentModules.add(module.getName());
        channels.put(channel, currentModules);
    }

    /**
     * Log a message to a configured channel to a specific guild
     * @param channel The registered channel
     * @param guild   The guild to send the message to
     * @param message The message to send
     */
    public static void log(String channel, Guild guild, Message message) {
        if (instance == null) {
            Bot.logger.warn("A message was logged before the logger was initialized!");
            return;
        }
        if (!Settings.hasSetting(instance, channel)) {
            Bot.logger.warn("A message was logged to a channel that was not registered!");
            return;
        }
        List<TextChannel> textChannels = Settings.get(instance, guild.getId(), channel).getAsTextChannelList(guild);
        for (TextChannel textChannel : textChannels)
            textChannel.sendMessage(message).queue();
    }

    /**
     * Log a message to a configured channel in <b>all</b> guilds that the bot is in.
     * @param channel The registered channel
     * @param message The message to send
     */
    public static void log(String channel, Message message) {
        for (Guild guild : Bot.jda.getGuilds())
            log(channel, guild, message);
    }

    /**
     * Log up to 10 message embeds to a configured channel in a specific guild
     * @param channel The registered channel
     * @param guild   The guild to send the message to
     * @param embeds  The embeds to send (10 max)
     */
    public static void log(String channel, Guild guild, MessageEmbed @NotNull ... embeds) {
        if (embeds.length > 10) {
            Bot.logger.warn("A message was logged with too many embeds! Max 10");
            return;
        }
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setEmbeds(embeds);
        log(channel, guild, messageBuilder.build());
    }

    /**
     * Log up to 10 message embeds to a configured channel in <b>all</b> guilds the bot is in
     * @param channel The registered channel
     * @param embeds  The embeds to send (10 max)
     */
    public static void log(String channel, MessageEmbed @NotNull ... embeds) {
        for (Guild guild : Bot.jda.getGuilds())
            log(channel, guild, embeds);
    }

    @Override
    public void registerLoggingChannels() {
        register(this, "startup");
    }

    @Override
    public void registerSettings() {
        for (String channel : channels.keySet()) {
            StringBuilder description = new StringBuilder("Used by: ");
            for (String module : channels.get(channel))
                description.append(module);
            Settings.register(this, channel, Setting.Type.TEXT_CHANNEL_LIST, Setting.Type.TEXT_CHANNEL_LIST.unset,
                    description.toString());
        }
    }

    @Override
    public void load() {

    }

    @Override
    public CommandData[] getCommands() {
        return new CommandData[0];
    }
}
