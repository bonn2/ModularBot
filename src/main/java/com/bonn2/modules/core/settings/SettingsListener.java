package com.bonn2.modules.core.settings;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.permissions.PermissionLevel;
import com.bonn2.modules.core.permissions.Permissions;
import com.bonn2.modules.core.settings.types.Setting;
import com.bonn2.utils.StringUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
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
                                Objects.requireNonNull(Settings.get(module, key)).getAsString()
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
                    // Check if passed value starts with an @
                    if (!value.startsWith("@")) {
                        event.reply("`%s` is not a role, all roles must start with `@`".formatted(value))
                                .setEphemeral(true)
                                .queue();
                        return;
                    }
                    List<Role> roles = Bot.jda.getRolesByName(value.substring(1), true);
                    switch (roles.size()) {
                        // Could not find a role
                        case 0 -> {
                            event.reply("Could not find a role by the name `%s`!".formatted(key))
                                    .setEphemeral(true)
                                    .queue();
                            return;
                        }
                        // Found one role
                        case 1 -> {
                            Settings.set(module, key, roles.get(0).getId());
                            EmbedBuilder embedBuilder = new EmbedBuilder();
                            embedBuilder.setTitle("%s settings".formatted(module.name));
                            embedBuilder.setColor(Color.CYAN);
                            for (String k : registeredSettings.keySet()) {
                                embedBuilder.addField(
                                        StringUtil.capitalize(k),
                                        "Type: %s\nValue: %s".formatted(
                                                StringUtil.capitalize(registeredSettings.get(k).toString().toLowerCase()),
                                                Objects.requireNonNull(Settings.get(module, k)).getAsString()
                                        ),
                                        true
                                );
                            }
                            event.replyEmbeds(embedBuilder.build()).queue();
                        }
                        // Multiple roles were found :(
                        default -> {
                            // TODO: 2/4/2022 Handle this somehow
                            event.reply("There were multiple roles found by that name.\n" +
                                    "Tbh idk if you should blame me or discord for this not being handled.")
                                    .setEphemeral(true)
                                    .queue();
                            return;
                        }
                    }
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
                                            Objects.requireNonNull(Settings.get(module, k)).getAsString()
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
