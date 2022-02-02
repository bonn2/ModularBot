package com.bonn2.modules.pubictimeout;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

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

    public static Role TIMEOUT_ROLE;
    public static Map<Member, ScheduledExecutorService> SCHEDULED = new HashMap<>();

    @Override
    public void load() {
        logger.info("- Getting Roles");
        TIMEOUT_ROLE = Bot.jda.getRoleById("936679469190635550");

        logger.info("- Registering Listeners...");
        Bot.jda.addEventListener(new PublicTimeoutListener());

        logger.info("- Checking Users for old Timeout Roles");
        checkUsers();
    }

    public void checkUsers() {
        Bot.guild.loadMembers().onSuccess((members -> {
            for (Member member : members) {
                if (member.isTimedOut()) {
                    // Give role
                    if (!member.getRoles().contains(TIMEOUT_ROLE))
                        Bot.guild.addRoleToMember(member, PublicTimeout.TIMEOUT_ROLE).queue();
                    // Schedule role removal
                    scheduleRoleRemoval(member);
                } else {
                    // Remove role
                    if (member.getRoles().contains(PublicTimeout.TIMEOUT_ROLE))
                        Bot.guild.removeRoleFromMember(member, PublicTimeout.TIMEOUT_ROLE).queue();
                }
            }
        }));
    }

    public static void scheduleRoleRemoval(Member member) {
        ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
        executor.schedule(
                () -> {
                    if (member.getRoles().contains(PublicTimeout.TIMEOUT_ROLE))
                        Bot.guild.removeRoleFromMember(member, PublicTimeout.TIMEOUT_ROLE).queue();
                    PublicTimeout.SCHEDULED.remove(member);
                },
                Objects.requireNonNull(member.getTimeOutEnd()).toEpochSecond() - System.currentTimeMillis() / 1000,
                TimeUnit.SECONDS
        );
        // Store executor so it can be canceled
        PublicTimeout.SCHEDULED.put(member, executor);
    }
}
