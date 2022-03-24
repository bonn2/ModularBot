package com.bonn2.modules.votechannel;

import com.bonn2.modules.core.settings.Settings;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class VoteChannelListener extends ListenerAdapter {

    final VoteChannel module;

    public VoteChannelListener(VoteChannel module) {
        this.module = module;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (Settings.get(module, "channels").getAsTextChannelList().contains(event.getTextChannel())) {
            event.getMessage().addReaction("\uD83D\uDC4D").queue();
            event.getMessage().addReaction("\uD83D\uDC4E").queue();
        }
    }
}
