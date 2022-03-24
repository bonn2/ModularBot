package com.bonn2.modules.votechannel;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class VoteChannel extends Module {

    public VoteChannel() {
        version = "v1.0";
        priority = Priority.POST_JDA_HIGH;
        name = "VoteChannel";
    }

    @Override
    public void registerSettings() {
        Settings.register(this, "channels", Setting.Type.TEXT_CHANNEL_LIST, Setting.Type.TEXT_CHANNEL_LIST.unset,
                "The channels to add voting reactions to.");
    }

    @Override
    public void load() {
        Bot.jda.addEventListener(new VoteChannelListener(this));
    }

    @Override
    public CommandData[] getCommands() {
        return new CommandData[0];
    }
}
