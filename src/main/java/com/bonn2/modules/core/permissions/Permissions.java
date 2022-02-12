package com.bonn2.modules.core.permissions;

import com.bonn2.modules.Module;
import com.bonn2.modules.core.config.Config;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class Permissions extends Module {

    public enum Level {
        OWNER(3), ADMIN(2), MOD(1), MEMBER(0);

        final int level;
        public static String ownerId;
        public static Permissions permissions;

        Level(int level) {
            this.level = level;
        }

        public static Level getPermissionLevel(@NotNull Member member) {
            List<Role> roles = member.getRoles();
            if (member.getId().equals(ownerId))
                return OWNER;
            for (Role role : roles)
                if (Objects.requireNonNull(Settings.get(permissions, "admin_roles")).getAsRoleIdList().contains(role.getId()))
                    return ADMIN;
            for (Role role : roles)
                if (Objects.requireNonNull(Settings.get(permissions, "mod_roles")).getAsRoleIdList().contains(role.getId()))
                    return MOD;
            return MEMBER;
        }
    }

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
        Level.ownerId = Config.get("owner_id").getAsString();
        Level.permissions = this;
    }

    public static boolean hasPermission(@NotNull Member member, @NotNull Level level) {
        Level permLevel = Level.getPermissionLevel(member);
        return permLevel.level >= level.level;
    }

    /**
     * Checks if a creator of a slash command even has a permission level, and informs them if they fail the check.
     * @param event The slash command event to check.
     * @param level The required permission level to pass the check.
     * @return True if the member has the provided permission level.
     */
    public static boolean hasPermissionReply(@NotNull SlashCommandInteraction event, @NotNull Level level) {
        if (hasPermission(Objects.requireNonNull(event.getMember()), level)) {
            return true;
        } else {
            event.reply("You do not have permission to do that!").setEphemeral(true).queue();
            return false;
        }
    }

    public static boolean hasPermissionReply(@NotNull SelectMenuInteractionEvent event, @NotNull Level level) {
        if (hasPermission(Objects.requireNonNull(event.getMember()), level)) {
            return true;
        } else {
            event.reply("You do not have permission to do that!").setEphemeral(true).queue();
            return false;
        }
    }
}
