package com.bonn2.modules.updatingip;

import com.bonn2.modules.core.permissions.Permissions;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class UpdatingIPListener extends ListenerAdapter {

    public void onSlashCommandInteraction(@Nonnull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("updatingip")) return;
        if (!Permissions.hasPermissionReply(event, Permissions.Level.MOD)) return;
    }
}
