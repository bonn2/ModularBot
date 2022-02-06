package com.bonn2.modules.core.permissions;

import com.bonn2.modules.core.settings.Settings;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public enum PermissionLevel {
    OWNER(3), ADMIN(2), MOD(1), MEMBER(0);

    final int level;
    public static String ownerId;
    public static Permissions permissions;

    PermissionLevel(int level) {
        this.level = level;
    }

    public static PermissionLevel getPermissionLevel(@NotNull Member member) {
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
