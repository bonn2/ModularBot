package com.bonn2.modules.settings;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.settings.types.Setting;
import com.bonn2.utils.StringUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;

public class SettingsCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("settings")) return;
        // Return list of all modules with registered settings
        if (event.getOption("module") == null) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Settings");
            embedBuilder.setColor(Color.CYAN);
            for (Module module : Bot.modules) {
                embedBuilder.addField(
                        module.name,
                        "Setting nodes: %s".formatted(Settings.getRegisteredSettings(module.name).keySet().size()),
                        true
                );
            }
            event.replyEmbeds(embedBuilder.build()).queue();
            return;
        }
        // Attempt to get the module
        Module module = Bot.getModuleIgnoreCase(Objects.requireNonNull(event.getOption("module")).getAsString());
        if (module == null) {
            event.reply("That module does not exist!").setEphemeral(true).queue();
            return;
        }
        // Check if module has settings
        if (Settings.registeredSettingsCount(module) == 0) {
            event.reply("That module does not have any settings!").setEphemeral(true).queue();
            return;
        }
        // Return a list of all registered settings for the module
        if (event.getOption("setting") == null) {
            event.replyEmbeds(getModuleSettingEmbed(module)).queue();
            return;
        }
        String key = Objects.requireNonNull(event.getOption("setting"))
                .getAsString()
                .toLowerCase()
                .replaceAll(" ", "_");
        // Unset the value
        if (event.getOption("default") != null && Objects.requireNonNull(event.getOption("default")).getAsBoolean()) {
            if (Settings.hasSetting(module, key)) {
                Settings.unSet(module, key);
                event.replyEmbeds(getModuleSettingEmbed(module)).queue();
            } else {
                event.reply("The module %s does not have a setting %s!".formatted(module.name, key))
                        .setEphemeral(true)
                        .queue();
            }
        }
        // If multiple values are provided
        else if (event.getOption("values") != null) {
            if (Settings.hasSetting(module, key)) {
                switch (Settings.getRegisteredSettingType(module, key)) {
                    case ROLE_LIST -> {
                        String[] splitValues = Objects.requireNonNull(event.getOption("values")).getAsString().split(" ");
                        List<Role> roles = new LinkedList<>();
                        for (String value : splitValues) {
                            // Check if passed value is a role
                            if (!value.matches("<@&[0-9]{18}>")) continue;
                            Role role = Bot.jda.getRoleById(value.substring(3, 21));
                            if (role == null) continue;
                            roles.add(role);
                        }
                        StringBuilder valueToSet = new StringBuilder();
                        for (Role role : roles) {
                            valueToSet.append(role.getId());
                            valueToSet.append(",");
                        }
                        // Remove trailing ,
                        if (!valueToSet.isEmpty())
                            valueToSet.deleteCharAt(valueToSet.length() - 1);

                        Settings.set(module, key, valueToSet.toString());
                        event.replyEmbeds(getModuleSettingEmbed(module)).queue();
                    }
                    case TEXT_CHANNEL_LIST -> {
                        String[] splitValues = Objects.requireNonNull(event.getOption("values")).getAsString().split(" ");
                        List<TextChannel> channels = new LinkedList<>();
                        for (String value : splitValues) {
                            if (!value.matches("<#[0-9]{18}>")) continue;
                            TextChannel channel = Bot.guild.getTextChannelById(value.substring(2, 20));
                            if (channel == null) continue;
                            channels.add(channel);
                        }
                        StringBuilder valueToSet = new StringBuilder();
                        for (TextChannel channel : channels) {
                            valueToSet.append(channel.getId());
                            valueToSet.append(",");
                        }
                        // Remove trailing ,
                        if (!valueToSet.isEmpty())
                            valueToSet.deleteCharAt(valueToSet.length() - 1);

                        Settings.set(module, key, valueToSet.toString());
                        event.replyEmbeds(getModuleSettingEmbed(module)).queue();
                    }
                    default -> event.reply("This setting does not support multiple values!")
                            .setEphemeral(true)
                            .queue();
                }
            } else {
                event.reply("The module %s does not have a setting %s!".formatted(module.name, key))
                        .setEphemeral(true)
                        .queue();
            }
        }
        // Ask user for a value
        else if (event.getOption("value") == null) {
            if (Settings.hasSetting(module, key)) {
                event.reply("You must provide a value to set %s to.".formatted(StringUtil.capitalize(key)))
                        .setEphemeral(true)
                        .queue();
            } else {
                event.reply("The module %s does not have a setting %s!".formatted(module.name, key))
                        .setEphemeral(true)
                        .queue();
            }
        }
        // One value was provided
        else {
            String value = Objects.requireNonNull(event.getOption("value")).getAsString();
            value = value.trim();
            // Attempt to set the value
            if (Settings.hasSetting(module, key)) {
                switch (Settings.getRegisteredSettingType(module, key)) {
                    // Handle mentionables
                    case ROLE, ROLE_LIST -> {
                        // Check if passed value is a role
                        if (!value.matches("<@&[0-9]{18}>")) {
                            event.reply("`%s` is not a role! Make sure you tab complete the role rather than just typing the name!"
                                            .formatted(value))
                                    .setEphemeral(true)
                                    .queue();
                            return;
                        }
                        Role role = Bot.jda.getRoleById(value.substring(3, 21));
                        if (role == null) {
                            event.reply("Could not find that role!")
                                    .setEphemeral(true)
                                    .queue();
                            return;
                        }
                        Settings.set(module, key, role.getId());
                        event.replyEmbeds(getModuleSettingEmbed(module)).queue();
                    }
                    case TEXT_CHANNEL, TEXT_CHANNEL_LIST -> {
                        // Check if text channel was provided
                        if (!value.matches("<#[0-9]{18}>")) {
                            event.reply("`%s` is not a channel! Make sure you tab complete the channel, rather than just typing the name!"
                                            .formatted(value))
                                    .setEphemeral(true)
                                    .queue();
                            return;
                        }
                        TextChannel textChannel = Bot.jda.getTextChannelById(value.substring(2, 20));
                        if (textChannel == null) {
                            event.reply("Could not find that channel!")
                                    .setEphemeral(true)
                                    .queue();
                            return;
                        }
                        Settings.set(module, key, textChannel.getId());
                        event.replyEmbeds(getModuleSettingEmbed(module)).queue();
                    }
                    // Handle everything else
                    default -> {
                        if (Settings.set(module, key, value)) {
                            event.replyEmbeds(getModuleSettingEmbed(module)).queue();
                        } else {
                            event.reply("That is not a valid value!")
                                    .setEphemeral(true)
                                    .queue();
                        }
                    }
                }
            } else {
                event.reply("The module %s does not have a setting %s!".formatted(module.name, key))
                        .setEphemeral(true)
                        .queue();
            }
        }
    }

    private @NotNull MessageEmbed getModuleSettingEmbed(@NotNull Module module) {
        // Get registered settings for the module
        Map<String, Setting.Type> registeredSettings = Settings.getRegisteredSettings(module.name);
        // Get setting descriptions
        Map<String, String> descriptions = Settings.getDescriptions(module.name);
        // Create embed
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("%s settings".formatted(module.name));
        embedBuilder.setColor(Color.CYAN);
        for (String key : registeredSettings.keySet()) {
            embedBuilder.addField(
                    StringUtil.capitalize(key),
                    "%s\n**Type:** %s\n**Value:** %s".formatted(
                            descriptions.get(key),
                            StringUtil.capitalize(registeredSettings.get(key).toString().toLowerCase()),
                            Objects.requireNonNull(Settings.get(module, key)).getDisplayString()
                    ),
                    false
            );
        }
        return embedBuilder.build();
    }
}
