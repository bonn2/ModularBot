package com.bonn2.modules.core.settings;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.utils.StringUtil;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import javax.annotation.Nonnull;
import java.util.*;

public class SettingsTabComplete extends ListenerAdapter {

    public void onCommandAutoCompleteInteraction(@Nonnull CommandAutoCompleteInteractionEvent event) {
        // Only reply to /settings
        if (!event.getName().equals("settings")) return;

        // Reply differently for each focused option
        switch (event.getFocusedOption().getName()) {
            case "module" -> {
                List<Command.Choice> choices = new ArrayList<>();
                for (Module module : Bot.modules)
                    if (module.name.toLowerCase(Locale.ROOT).startsWith(Objects.requireNonNull(event.getOption("module")).getAsString().toLowerCase(Locale.ROOT))
                            && module.hasSettings())
                        choices.add(new Command.Choice(module.name, module.name));
                event.replyChoices(choices).queue();
            }
            case "setting" -> {
                // Only reply if module is valid
                OptionMapping moduleOption = event.getOption("module");
                if (moduleOption == null) {
                    event.replyChoices().queue();
                    return;
                }
                Module module = Bot.getModuleIgnoreCase(moduleOption.getAsString());
                if (module == null) {
                    event.replyChoices().queue();
                    return;
                }
                Set<String> settings = Settings.getSettings(module);
                List<Command.Choice> choices = new ArrayList<>();
                for (String setting : settings) {
                    if (setting.toLowerCase(Locale.ROOT).startsWith(Objects.requireNonNull(event.getOption("setting")).getAsString().trim().replaceAll(" ", "_"))) {
                        String capitalizedSetting = StringUtil.capitalize(setting);
                        choices.add(new Command.Choice(capitalizedSetting, capitalizedSetting));
                    }
                }
                event.replyChoices(choices).queue();
            }
            case "value" -> {
                // Only reply if module is valid
                OptionMapping moduleOption = event.getOption("module");
                if (moduleOption == null) {
                    event.replyChoices().queue();
                    return;
                }
                Module module = Bot.getModuleIgnoreCase(moduleOption.getAsString());
                if (module == null) {
                    event.replyChoices().queue();
                    return;
                }
                // Only reply if setting is valid
                OptionMapping settingOption = event.getOption("setting");
                if (settingOption == null) {
                    event.replyChoices().queue();
                    return;
                }
                String setting = settingOption.getAsString().trim().replaceAll(" ", "_").toLowerCase(Locale.ROOT);
                if (!Settings.hasSetting(module, setting)) {
                    event.replyChoices().queue();
                    return;
                }
                List<Command.Choice> choices = new ArrayList<>();
                String value = Objects.requireNonNull(event.getOption("value")).getAsString().trim().toLowerCase(Locale.ROOT);
                switch (Settings.getRegisteredSettingType(module, setting)) {
                    case TEXT_CHANNEL -> {
                        for (TextChannel channel : Objects.requireNonNull(event.getGuild()).getTextChannels()) {
                            if (channel.getName().toLowerCase(Locale.ROOT).startsWith(value)) {
                                choices.add(new Command.Choice(
                                        "#" + channel.getName(),
                                        channel.getAsMention()
                                ));
                            }
                            if (choices.size() == 25) break;
                        }
                        event.replyChoices(choices).queue();
                    }
                    case ROLE -> {
                        for (Role role : Objects.requireNonNull(event.getGuild()).getRoles()) {
                            if (role.getName().toLowerCase(Locale.ROOT).startsWith(value)) {
                                choices.add(new Command.Choice(
                                        "@" + role.getName(),
                                        role.getAsMention()
                                ));
                            }
                            if (choices.size() == 25) break;
                        }
                        event.replyChoices(choices).queue();
                    }
                    default -> event.replyChoices().queue();
                }
            }
        }
    }
}
