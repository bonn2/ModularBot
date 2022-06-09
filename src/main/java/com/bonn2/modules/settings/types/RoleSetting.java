package com.bonn2.modules.settings.types;

import com.bonn2.Bot;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

public class RoleSetting extends Setting {

    public final String id;

    public RoleSetting(@NotNull String id) {
        this.id = id;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive("%s:%s".formatted(Type.ROLE.toString(), id));
    }

    @Override
    public String getDisplayString() {
        return "<@&%s>".formatted(id);
    }

    @Override
    public Role getAsRole(Guild guild) {
        return guild.getRoleById(id);
    }
}
