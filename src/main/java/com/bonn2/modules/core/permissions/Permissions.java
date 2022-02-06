package com.bonn2.modules.core.permissions;

import com.bonn2.modules.Module;
import com.bonn2.modules.core.config.Config;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Permissions extends Module {

    public Permissions() {
        version = "v1.1";
        priority = Priority.POST_JDA_HIGH;
        name = "Permissions";
    }

    @Override
    public void registerSettings() {
        Settings.register(this, "admin_roles", Setting.Type.ROLE_LIST, Setting.Type.ROLE_LIST.unset);
        Settings.register(this, "mod_roles", Setting.Type.ROLE_LIST, Setting.Type.ROLE_LIST.unset);
    }

    @Override
    public void load() {
        PermissionLevel.ownerId = Config.get("owner_id").getAsString();
        PermissionLevel.permissions = this;
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
    public static boolean hasPermissionReply(@NotNull SlashCommandInteraction event, @NotNull PermissionLevel level) {
        if (hasPermission(Objects.requireNonNull(event.getMember()), level)) {
            return true;
        } else {
            event.reply("You do not have permission to do that!").setEphemeral(true).queue();
            return false;
        }
    }
}
