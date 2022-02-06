package com.bonn2.modules.displaymessagelink;

import com.bonn2.Bot;
import com.bonn2.modules.Module;

import static com.bonn2.Bot.logger;

public class DisplayMessageLink extends Module {

    public DisplayMessageLink() {
        version = "v1.0";
        priority = Priority.POST_JDA_LOW;
        name = "DisplayMessageLink";
    }

    @Override
    public void registerSettings() {

    }

    @Override
    public void load() {
        logger.info("Registering Listeners...");
        Bot.jda.addEventListener(new DisplayMessageLinkListener());
    }
}
