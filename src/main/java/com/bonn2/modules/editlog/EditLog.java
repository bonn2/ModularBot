package com.bonn2.modules.editlog;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class EditLog extends Module {

    public EditLog() {
        name = "EditLog";
        version = "v1.0";
        priority = Priority.POST_JDA_LOW;
    }

    @Override
    public void registerSettings() {
        Settings.register(this, "log_channel", Setting.Type.TEXT_CHANNEL, Setting.Type.TEXT_CHANNEL.unset,
                "The channel to post the message edit log to.");
    }

    @Override
    public void load() {
        Bot.jda.addEventListener(new EditLogListener(this));
    }

    @Override
    public CommandData[] getCommands() {
        return new CommandData[0];
    }
}
