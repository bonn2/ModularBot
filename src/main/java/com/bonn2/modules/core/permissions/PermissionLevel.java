package com.bonn2.modules.core.permissions;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.List;

public enum PermissionLevel {
    ADMIN(5), MOD(3), MEMBER(0);

    final int level;
    public static List<String> adminIDS;
    public static List<String> modIDS;

    PermissionLevel(int level) {
        this.level = level;
    }

    public static PermissionLevel getPermissionLevel(Member member) {
        List<Role> roles = member.getRoles();
        for (Role role : roles)
            if (adminIDS.contains(role.getId()))
                return ADMIN;
        for (Role role : roles)
            if (modIDS.contains(role.getId()))
                return MOD;
        return MEMBER;
    }
}
