package com.bonn2.modules.showtimeout;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ShowTimeoutListener extends ListenerAdapter {

    private final ShowTimeout showTimeout;

    public ShowTimeoutListener(ShowTimeout showTimeout) {
        this.showTimeout = showTimeout;
    }

    @Override
    public void onGuildMemberUpdate(@NotNull GuildMemberUpdateEvent event) {
        Role timeoutRole = showTimeout.getTimeoutRole();
        if (event.getMember().isTimedOut()) {
            if (!event.getMember().getRoles().contains(timeoutRole)) {
                // Remove unused schedulers
                if (ShowTimeout.SCHEDULED.containsKey(event.getMember())) {
                    ShowTimeout.SCHEDULED.get(event.getMember()).shutdownNow();
                    ShowTimeout.SCHEDULED.remove(event.getMember());
                }
                // Give role
                event.getGuild().addRoleToMember(event.getMember(), timeoutRole).queue();
                // Schedule role removal
                showTimeout.scheduleRoleRemoval(event.getMember());
            }
        } else {
            // Remove old schedulers
            if (ShowTimeout.SCHEDULED.containsKey(event.getMember())) {
                ShowTimeout.SCHEDULED.get(event.getMember()).shutdownNow();
                ShowTimeout.SCHEDULED.remove(event.getMember());
            }
            // Remove role
            if (event.getMember().getRoles().contains(timeoutRole))
                event.getGuild().removeRoleFromMember(event.getMember(), timeoutRole).queue();
        }
    }
}
