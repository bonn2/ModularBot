package com.bonn2.modules.nowplaying;

import com.bonn2.Bot;
import com.bonn2.modules.core.settings.Settings;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivitiesEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class NowPlayingListener extends ListenerAdapter {

    final NowPlaying module;

    public NowPlayingListener(NowPlaying module) {
        this.module = module;
    }

    @Override
    public void onUserUpdateActivities(@Nonnull UserUpdateActivitiesEvent event) {
        Member member = event.getMember();
        Role role = Settings.get(module, "role").getAsRole();
        if (role == null) return;
        for (Activity activity : member.getActivities()) {
            if (activity.getType().equals(Activity.ActivityType.PLAYING)
                    && activity.getName().equalsIgnoreCase(Settings.get(module, "game").getAsString())) {
                event.getGuild().addRoleToMember(member, role).queue();
                return;
            }
        }
        if (member.getRoles().contains(role))
            event.getGuild().removeRoleFromMember(member, role).queue();
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        Member member = event.getMember();
        Role role = Settings.get(module, "role").getAsRole();
        if (role == null) return;
        for (Activity activity : member.getActivities()) {
            if (activity.getType().equals(Activity.ActivityType.PLAYING)
                    && activity.getName().equalsIgnoreCase(Settings.get(module, "game").getAsString())) {
                event.getGuild().addRoleToMember(member, role).queue();
                return;
            }
        }
    }
}
