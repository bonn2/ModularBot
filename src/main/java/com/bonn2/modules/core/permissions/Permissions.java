package com.bonn2.modules.core.permissions;

import com.bonn2.modules.Module;
import com.bonn2.modules.core.config.Config;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.bonn2.Bot.logger;

public class Permissions extends Module {

    @Override
    public void load() {
        logger.info("Loading permissions module...");
        version = "v1.0";
        PermissionLevel.adminIDS = Config.getList("ADMIN_IDS");
        PermissionLevel.modIDS = Config.getList("MODERATION_IDS");
    }

    public static boolean hasPermission(@NotNull Member member, @NotNull PermissionLevel level) {
        PermissionLevel permLevel = PermissionLevel.getPermissionLevel(member);
        return permLevel.level >= level.level;
    }

    /**
     * Checks if a creator of a slash command even has a permission level, and informs them if they fail the check.
     * @param event The slash command event to check.
     * @param level The required permission level to pass the check.
     * @return True if the member has the provided permission level.
     */
    public static boolean hasPermissionReply(@NotNull SlashCommandEvent event, @NotNull PermissionLevel level) {
        if (hasPermission(Objects.requireNonNull(event.getMember()), level)) {
            return true;
        } else {
            event.reply("You do not have permission to do that!").setEphemeral(true).queue();
            return false;
        }
    }
}
