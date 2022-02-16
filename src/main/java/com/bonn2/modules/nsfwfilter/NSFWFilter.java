package com.bonn2.modules.nsfwfilter;

import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;

import static com.bonn2.Bot.jda;

public class NSFWFilter extends Module {

    public NSFWFilter() {
        name = "NSFWFilter";
        version = "v1.0";
        priority = Priority.POST_JDA_LOW;
    }

    @Override
    public void registerSettings() {
        Settings.register(this, "api_key", Setting.Type.STRING, Setting.Type.STRING.unset);
        Settings.register(this, "delete_adult", Setting.Type.BOOLEAN, "false");
        Settings.register(this, "adult_delete_threshold", Setting.Type.FLOAT, "-1");
        Settings.register(this, "teen_delete_threshold", Setting.Type.FLOAT, "-1");
    }

    @Override
    public void load() {
        jda.addEventListener(new NSFWFilterListener(this));
    }
}
