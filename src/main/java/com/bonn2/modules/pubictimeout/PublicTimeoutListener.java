package com.bonn2.modules.pubictimeout;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class PublicTimeoutListener extends ListenerAdapter {

    private final PublicTimeout publicTimeout;

    public PublicTimeoutListener(PublicTimeout publicTimeout) {
        this.publicTimeout = publicTimeout;
    }

    @Override
    public void onGuildMemberUpdate(@NotNull GuildMemberUpdateEvent event) {
        Role timeoutRole = publicTimeout.getTimeoutRole();
        if (event.getMember().isTimedOut()) {
            if (!event.getMember().getRoles().contains(timeoutRole)) {
                // Remove unused schedulers
                if (PublicTimeout.SCHEDULED.containsKey(event.getMember())) {
                    PublicTimeout.SCHEDULED.get(event.getMember()).shutdownNow();
                    PublicTimeout.SCHEDULED.remove(event.getMember());
                }
                // Give role
                event.getGuild().addRoleToMember(event.getMember(), timeoutRole).queue();
                // Schedule role removal
                publicTimeout.scheduleRoleRemoval(event.getMember());
            }
        } else {
            // Remove old schedulers
            if (PublicTimeout.SCHEDULED.containsKey(event.getMember())) {
                PublicTimeout.SCHEDULED.get(event.getMember()).shutdownNow();
                PublicTimeout.SCHEDULED.remove(event.getMember());
            }
            // Remove role
            if (event.getMember().getRoles().contains(timeoutRole))
                event.getGuild().removeRoleFromMember(event.getMember(), timeoutRole).queue();
        }
    }
}
