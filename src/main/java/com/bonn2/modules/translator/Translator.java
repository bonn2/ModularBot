package com.bonn2.modules.translator;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Translator extends Module {

    public Translator() {
        version = "v1.0";
        priority = Priority.POST_JDA_LOW;
        name = "Translator";
    }

    @Override
    public void registerSettings() {
        Settings.register(this, "deepl_key", Setting.Type.STRING, Setting.Type.STRING.unset,
                "An api key for the free version of DeepL. This is required for this module to work.");
    }

    @Override
    public void load() {
        Bot.jda.addEventListener(new TranslatorListener(this));
    }

    @Override
    public CommandData[] getCommands() {
        return new CommandData[] {
                Commands.message("Translate To English")
        };
    }
}
