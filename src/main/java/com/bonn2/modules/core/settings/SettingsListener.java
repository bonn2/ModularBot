package com.bonn2.modules.core.settings;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.permissions.PermissionLevel;
import com.bonn2.modules.core.permissions.Permissions;
import com.bonn2.modules.core.settings.types.Setting;
import com.bonn2.utils.StringUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
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
        Module module = Bot.getModuleIgnoreCase(Objects.requireNonNull(event.getOption("module")).getAsString());
        if (module == null) {
            event.reply("That module does not exist!").setEphemeral(true).queue();
            return;
        }
        Map<String, Setting.Type> registeredSettings = Settings.getRegisteredSettings(module.name);
        if (registeredSettings == null) {
            event.reply("That module does not have any settings!").setEphemeral(true).queue();
            return;
        }
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
        String key = Objects.requireNonNull(event.getOption("setting")).getAsString();
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
        if (Settings.hasSetting(module, key)) {
            Settings.set(module, key, value);
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
            event.reply("The module %s does not have a setting %s!".formatted(module.name, key))
                    .setEphemeral(true)
                    .queue();
        }

    }
}
