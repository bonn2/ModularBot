package com.bonn2.modules.uncaps;

import com.bonn2.Bot;
import com.bonn2.modules.Module;

import static com.bonn2.Bot.logger;

public class UnCaps extends Module {
    @Override
    public void load() {
        version = "v1.0";
        logger.info("Loading UnCaps module " + version + "...");
        logger.info("- Registering Listeners...");
        Bot.jda.addEventListener(new UnCapsListener());
    }
}
