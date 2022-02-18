package com.bonn2.modules.nsfwfilter;

import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.Set;

import static com.bonn2.Bot.jda;

public class NSFWFilter extends Module {

    public NSFWFilter() {
        name = "NSFWFilter";
        version = "v1.0";
        priority = Priority.POST_JDA_LOW;
    }

    @Override
    public void registerSettings() {
        Settings.register(this, "api_key", Setting.Type.STRING, Setting.Type.STRING.unset,
                "The api key to use. From https://moderatecontent.com/");
        Settings.register(this, "delete_adult", Setting.Type.BOOLEAN, "false",
                "Whether or not to delete messages that are detected as adult.");
        Settings.register(this, "adult_delete_threshold", Setting.Type.FLOAT, "-1",
                "The percentage confidence to delete adult images. (-1 to disable)");
        Settings.register(this, "teen_delete_threshold", Setting.Type.FLOAT, "-1",
                "The percentage confidence to delete teen images. (-1 to disable)");
        Settings.register(this, "teen_and_adult_delete_threshold", Setting.Type.FLOAT, "-1",
                "The percentage confidence of `teen + adult` to delete images. (-1 to disable)");
        Settings.register(this, "immune_roles", Setting.Type.ROLE_LIST, Setting.Type.ROLE_LIST.unset,
                "The roles that should not be scanned.");
        Settings.register(this, "message", Setting.Type.STRING, "",
                "The message to send the user as a warning, use @user to mention the user.");
    }

    @Override
    public void load() {
        jda.addEventListener(new NSFWFilterListener(this));
    }

    @Override
    public CommandData[] getCommands() {
        return new CommandData[0];
    }
}
