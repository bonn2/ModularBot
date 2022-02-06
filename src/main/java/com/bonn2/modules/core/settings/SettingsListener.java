package com.bonn2.modules.core.settings;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.permissions.PermissionLevel;
import com.bonn2.modules.core.permissions.Permissions;
import com.bonn2.modules.core.settings.types.RoleListSetting;
import com.bonn2.modules.core.settings.types.Setting;
import com.bonn2.utils.StringUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SettingsListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getCommandId().equals("settings")) return;
        if (!Permissions.hasPermissionReply(event.getInteraction(), PermissionLevel.ADMIN)) return;
        // Return list of all modules with registered settings
        if (event.getOption("module") == null) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Settings");
            embedBuilder.setColor(Color.CYAN);
            for (Module module : Bot.modules) {
                embedBuilder.addField(
                        module.name,
                        "Version %s".formatted(module.version),
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
        // Get registered settings for the module
        Map<String, Setting.Type> registeredSettings = Settings.getRegisteredSettings(module.name);
        if (registeredSettings == null) {
            event.reply("That module does not have any settings!").setEphemeral(true).queue();
            return;
        }
        // Return a list of all registered settings for the module
        if (event.getOption("setting") == null) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("%s settings".formatted(module.name));
            embedBuilder.setColor(Color.CYAN);
            for (String key : registeredSettings.keySet()) {
                embedBuilder.addField(
                        StringUtil.capitalize(key),
                        "Type: %s\nValue: %s".formatted(
                                StringUtil.capitalize(registeredSettings.get(key).toString().toLowerCase()),
                                Objects.requireNonNull(Settings.get(module, key)).getDisplayString()
                        ),
                        true
                );
            }
            event.replyEmbeds(embedBuilder.build()).queue();
            return;
        }
        String key = Objects.requireNonNull(event.getOption("setting"))
                .getAsString()
                .toLowerCase()
                .replaceAll(" ", "_");
        // Ask user for a value
        if (event.getOption("value") == null) {
            if (Settings.hasSetting(module, key)) {
                event.reply("You must provide a value to set %s to.".formatted(StringUtil.capitalize(key)))
                        .setEphemeral(true)
                        .queue();
            } else {
                event.reply("The module %s does not have a setting %s!".formatted(module.name, key))
                        .setEphemeral(true)
                        .queue();
            }
            return;
        }
        String value = Objects.requireNonNull(event.getOption("value")).getAsString();
        // Attempt to set the value
        if (Settings.hasSetting(module, key)) {
            switch (Settings.getRegisteredSettingType(module, key)) {
                // Handle mentionables
                case ROLE -> {
                    // Check if passed value is a role
                    if (!value.trim().matches("<@&[0-9]{18}>")) {
                        event.reply("%s is not a role! Make sure you tab complete the role, rather than just typing the name!"
                                .formatted(value))
                                .setEphemeral(true)
                                .queue();
                        return;
                    }
                    Role role = Bot.jda.getRoleById(value);
                    if (role == null) {
                        event.reply("Could not find that role!")
                                .setEphemeral(true)
                                .queue();
                        return;
                    }
                    Settings.set(module, key, role.getId());
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle("%s settings".formatted(module.name));
                    embedBuilder.setColor(Color.CYAN);
                    for (String k : registeredSettings.keySet()) {
                        embedBuilder.addField(
                                StringUtil.capitalize(k),
                                "Type: %s\nValue: %s".formatted(
                                        StringUtil.capitalize(registeredSettings.get(k).toString().toLowerCase()),
                                        Objects.requireNonNull(Settings.get(module, k)).getDisplayString()
                                ),
                                true
                        );
                    }
                    event.replyEmbeds(embedBuilder.build()).queue();
                }
                case ROLE_LIST -> {
                    List<Role> rolesToSet = new ArrayList<>();
                    StringBuilder replyString = new StringBuilder();
                    // TODO: 2/6/2022 Use regex.find
                    // Convert both cases : <@&893201496479502356> <@&801564242737627197>
                    //                    : <@&893201496479502356><@&801564242737627197>
                    // To this            : 893201496479502356 801564242737627197
                    value = value.replaceAll("><@&", " ");
                    value = value.replaceAll("<@&", "");
                    value = value.replaceAll(">", "");
                    // Split into array
                    String[] roleIds = value.split(" ");
                    for (String id : roleIds) {
                        // Ignore null strings that may have been generated from extra leading / trailing spaces
                        if (id.equals(""))
                            continue;
                        if (!id.matches("[0-9]+")) {
                            replyString.append("%s is not a role! Make sure you tab complete the role, rather than just typing the name!"
                                    .formatted(id));
                            continue;
                        }
                        rolesToSet.add(Bot.jda.getRoleById(id));
                    }

                    if (rolesToSet.size() > 0)
                        Settings.set(module, key, new RoleListSetting(rolesToSet));

                    MessageBuilder messageBuilder = new MessageBuilder();
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle("%s settings".formatted(module.name));
                    embedBuilder.setColor(Color.CYAN);
                    for (String k : registeredSettings.keySet()) {
                        embedBuilder.addField(
                                StringUtil.capitalize(k),
                                "Type: %s\nValue: %s".formatted(
                                        StringUtil.capitalize(registeredSettings.get(k).toString().toLowerCase()),
                                        Objects.requireNonNull(Settings.get(module, k)).getDisplayString()
                                ),
                                true
                        );
                    }
                    messageBuilder.setEmbeds(embedBuilder.build());
                    messageBuilder.setContent(replyString.toString());
                    event.reply(messageBuilder.build()).queue();
                }
                // Handle everything else
                default -> {
                    if (Settings.set(module, key, value)) {
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setTitle("%s settings".formatted(module.name));
                        embedBuilder.setColor(Color.CYAN);
                        for (String k : registeredSettings.keySet()) {
                            embedBuilder.addField(
                                    StringUtil.capitalize(k),
                                    "Type: %s\nValue: %s".formatted(
                                            StringUtil.capitalize(registeredSettings.get(k).toString().toLowerCase()),
                                            Objects.requireNonNull(Settings.get(module, k)).getDisplayString()
                                    ),
                                    true
                            );
                        }
                        event.replyEmbeds(embedBuilder.build()).queue();
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
