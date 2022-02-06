package com.bonn2.modules.core.settings.types;

import com.bonn2.Bot;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RoleListSetting extends Setting {

    List<String> ids;

    public RoleListSetting(@NotNull String string) {
        String[] splitString = string.split(",");
        ids = new ArrayList<>();
        for (String subStr : splitString)
            ids.add(subStr.trim());
    }

    public RoleListSetting(@NotNull List<Role> roles) {
        ids = new ArrayList<>();
        for (Role role : roles)
            ids.add(role.getId());
    }

    @Override
    public JsonElement toJson() {
        // Output: "ROLE_LIST:24151132,41231241,421421352,125352435,51351421,125134123"
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("%s:".formatted(Type.ROLE_LIST));
        for (String id : ids)
            stringBuilder.append("%s,".formatted(id));
        if (stringBuilder.toString().endsWith(","))
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return new JsonPrimitive(stringBuilder.toString());
    }

    @Override
    public String getDisplayString() {
        StringBuilder builder = new StringBuilder();
        for (String id : ids)
            builder.append("<@&%s> ".formatted(id));
        return builder.toString().trim();
    }

    @Override
    public List<Role> getAsRoleList() {
        List<Role> roles = new LinkedList<>();
        for (String id : ids)
            if (!id.equals("0"))
                roles.add(Bot.guild.getRoleById(id));
        return roles;
    }

    @Override
    public List<String> getAsRoleIdList() {
        return ids;
    }
}
