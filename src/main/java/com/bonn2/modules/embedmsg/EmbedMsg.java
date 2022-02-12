package com.bonn2.modules.embedmsg;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;

import static com.bonn2.Bot.logger;

public class EmbedMsg extends Module {

    public EmbedMsg() {
        version = "v1.1";
        priority = Priority.POST_JDA_LOW;
        name = "EmbedMsg";
    }

    @Override
    public void registerSettings() {
        Settings.register(this, "max_links", Setting.Type.INT, "5");
    }

    @Override
    public void load() {
        logger.info("Registering Listeners...");
        Bot.jda.addEventListener(new EmbedMsgListener(this));
    }
}