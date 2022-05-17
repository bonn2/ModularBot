package com.bonn2.modules.nowplaying;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class NowPlaying extends Module {

    public NowPlaying() {
        name = "NowPlaying";
        version = "v1.1";
        priority = Priority.DO_NOT_LOAD;
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
        checkUsers();
    }

    @Override
    public CommandData[] getCommands() {
        return new CommandData[0];
    }

    public void checkUsers() {
        Role role = Settings.get(this, "role").getAsRole();
        Bot.guild.loadMembers().onSuccess((members -> {
            for (Member member : members) {
                boolean isPlaying = false;
                for (Activity activity : member.getActivities()) {
                    if (activity.getType().equals(Activity.ActivityType.PLAYING)
                            && activity.getName().equalsIgnoreCase(Settings.get(this, "game").getAsString())) {
                        Bot.guild.addRoleToMember(member, role).queue();
                        isPlaying = true;
                    }
                }
                if (!isPlaying)
                    if (member.getRoles().contains(role))
                        Bot.guild.removeRoleFromMember(member, role).queue();
            }
        }));
    }
}
