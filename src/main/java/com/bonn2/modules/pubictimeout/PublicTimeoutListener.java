package com.bonn2.modules.pubictimeout;

import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class PublicTimeoutListener extends ListenerAdapter {

    @Override
    public void onGuildMemberUpdate(@NotNull GuildMemberUpdateEvent event) {
        if (event.getMember().isTimedOut()) {
            if (!event.getMember().getRoles().contains(PublicTimeout.TIMEOUT_ROLE)) {
                // Remove unused schedulers
                if (PublicTimeout.SCHEDULED.containsKey(event.getMember())) {
                    PublicTimeout.SCHEDULED.get(event.getMember()).shutdownNow();
                    PublicTimeout.SCHEDULED.remove(event.getMember());
                }
                // Give role
                event.getGuild().addRoleToMember(event.getMember(), PublicTimeout.TIMEOUT_ROLE).queue();
                // Schedule role removal
                PublicTimeout.scheduleRoleRemoval(event.getMember());
            }
        } else {
            // Remove old schedulers
            if (PublicTimeout.SCHEDULED.containsKey(event.getMember())) {
                PublicTimeout.SCHEDULED.get(event.getMember()).shutdownNow();
                PublicTimeout.SCHEDULED.remove(event.getMember());
            }
            // Remove role
            if (event.getMember().getRoles().contains(PublicTimeout.TIMEOUT_ROLE))
                event.getGuild().removeRoleFromMember(event.getMember(), PublicTimeout.TIMEOUT_ROLE).queue();
        }
    }
}
