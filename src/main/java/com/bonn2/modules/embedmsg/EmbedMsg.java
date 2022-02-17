package com.bonn2.modules.embedmsg;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import static com.bonn2.Bot.logger;

public class EmbedMsg extends Module {

    public EmbedMsg() {
        version = "v1.1";
        priority = Priority.POST_JDA_LOW;
        name = "EmbedMsg";
    }

    @Override
    public void registerSettings() {
        Settings.register(this, "max_links", Setting.Type.INT, "5",
                "The maximum number of message links to reply to in one message.");
    }

    @Override
    public void load() {
        logger.info("Registering Listeners...");
        Bot.jda.addEventListener(new EmbedMsgListener(this));
    }

    @Override
    public CommandData[] getCommands() {
        return new CommandData[0];
    }
}
