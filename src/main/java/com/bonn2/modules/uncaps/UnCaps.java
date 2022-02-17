package com.bonn2.modules.uncaps;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import static com.bonn2.Bot.logger;

public class UnCaps extends Module {

    public UnCaps() {
        version = "v1.0";
        priority = Priority.POST_JDA_LOW;
        name = "UnCaps";
    }

    @Override
    public void registerSettings() {

    }

    @Override
    public void load() {
        logger.info("Registering settings...");
        Settings.register(this, "threshold", Setting.Type.FLOAT, String.valueOf(0.5),
                "The percentage of capital letters before dropping messages to lower case.");
        Settings.register(this, "minimum_words", Setting.Type.INT, String.valueOf(3),
                "The minimum number of space-separated words required to lower. (-1 to disable)");
        Settings.register(this, "minimum_characters", Setting.Type.INT, String.valueOf(10),
                "The minimum number of characters in a message to lower. (-1 to disable)");
        Settings.register(this, "immune_roles", Setting.Type.ROLE_LIST, Setting.Type.ROLE_LIST.unset,
                "Roles that should not be lowered.");

        logger.info("Registering Listeners...");
        Bot.jda.addEventListener(new UnCapsListener(this));
    }

    @Override
    public CommandData[] getCommands() {
        return new CommandData[0];
    }
}
