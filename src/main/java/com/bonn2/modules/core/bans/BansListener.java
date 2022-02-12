package com.bonn2.modules.core.bans;

import com.google.gson.JsonElement;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.awt.*;

import static com.bonn2.modules.core.bans.Bans.BANS;

public class BansListener extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        for (JsonElement id : BANS) {
            if (event.getUser().getId().equals(id.getAsString())) {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Banned a user!");
                embedBuilder.setColor(Color.RED);
                embedBuilder.addField(
                        "User",
                        "<@%s> (%s)".formatted(
                                event.getUser().getId(),
                                event.getUser().getAsTag()
                        ),
                        false
                );
                embedBuilder.addField(
                        "Reason",
                        "Banned by <@%s> (%s)".formatted(
                                event.getUser().getId(),
                                event.getUser().getAsTag()
                        ),
                        false
                );
                event.getMember().ban(0).queue();
            }
        }
    }
}
