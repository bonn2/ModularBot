package com.bonn2.modules.nowplaying;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class NowPlaying extends Module {

    public NowPlaying() {
        name = "NowPlaying";
        version = "v1.0";
        priority = Priority.POST_JDA_LOW;
    }

    @Override
    public void registerSettings() {
        Settings.register(this, "game", Setting.Type.STRING, "",
                "The game you want to check for as it appears in the member list.");
        Settings.register(this, "role", Setting.Type.ROLE, Setting.Type.ROLE.unset,
                "The role to give when someone is playing a specific game.");
    }

    @Override
    public void load() {
        Bot.jda.addEventListener(new NowPlayingListener(this));
    }

    @Override
    public CommandData[] getCommands() {
        return new CommandData[0];
    }
}
