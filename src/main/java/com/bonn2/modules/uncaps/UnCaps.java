package com.bonn2.modules.uncaps;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;

import static com.bonn2.Bot.logger;

public class UnCaps extends Module {

    public UnCaps() {
        version = "v1.0";
        priority = Priority.POST_JDA_LOW;
        name = "UnCaps";
    }

    @Override
    public void load() {
        logger.info("- Registering settings...");
        Settings.register(this, "threshold", Setting.Type.FLOAT, String.valueOf(0.5));

        logger.info("- Registering Listeners...");
        Bot.jda.addEventListener(new UnCapsListener());
    }
}
