package com.bonn2.modules.displaymessagelink;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;

import static com.bonn2.Bot.logger;

public class DisplayMessageLink extends Module {

    public DisplayMessageLink() {
        version = "v1.0";
        priority = Priority.POST_JDA_LOW;
        name = "DisplayMessageLink";
    }

    @Override
    public void registerSettings() {
        Settings.register(this, "max_links", Setting.Type.INT, "5");
    }

    @Override
    public void load() {
        logger.info("Registering Listeners...");
        Bot.jda.addEventListener(new DisplayMessageLinkListener(this));
    }
}
