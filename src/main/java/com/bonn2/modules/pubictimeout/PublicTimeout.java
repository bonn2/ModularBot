package com.bonn2.modules.pubictimeout;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.bonn2.Bot.logger;

public class PublicTimeout extends Module {

    public PublicTimeout() {
        version = "v1.1";
        priority = Priority.POST_JDA_LOW;
        name = "PublicTimeout";
    }
    public static Map<Member, ScheduledExecutorService> SCHEDULED = new HashMap<>();

    @Override
    public void registerSettings() {
        Settings.register(this, "timeout_role", Setting.Type.ROLE, "0");
    }

    @Override
    public void load() {
        logger.info("Registering Listeners...");
        Bot.jda.addEventListener(new PublicTimeoutListener(this));
        logger.info("Checking Users for old Timeout Roles");
        checkUsers();
    }

    public void checkUsers() {
        Role timeoutRole = getTimeoutRole();
        Bot.guild.loadMembers().onSuccess((members -> {
            for (Member member : members) {
                if (member.isTimedOut()) {
                    // Give role
                    if (!member.getRoles().contains(timeoutRole))
                        Bot.guild.addRoleToMember(member, timeoutRole).queue();
                    // Schedule role removal
                    scheduleRoleRemoval(member);
                } else {
                    // Remove role
                    if (member.getRoles().contains(timeoutRole))
                        Bot.guild.removeRoleFromMember(member, timeoutRole).queue();
                }
            }
        }));
    }

    public void scheduleRoleRemoval(@NotNull Member member) {
        Role timeoutRole = getTimeoutRole();
        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
        executor.schedule(
                () -> {
                    if (member.getRoles().contains(timeoutRole))
                        Bot.guild.removeRoleFromMember(member, timeoutRole).queue();
                    PublicTimeout.SCHEDULED.remove(member);
                },
                Objects.requireNonNull(member.getTimeOutEnd()).toEpochSecond() - System.currentTimeMillis() / 1000,
                TimeUnit.SECONDS
        );
        // Store executor so it can be canceled
        PublicTimeout.SCHEDULED.put(member, executor);
    }

    public Role getTimeoutRole() {
        return Objects.requireNonNull(Settings.get(this, "timeout_role")).getAsRole();
    }
}
