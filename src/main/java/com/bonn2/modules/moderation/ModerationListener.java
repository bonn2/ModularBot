package com.bonn2.modules.moderation;

import com.bonn2.modules.core.chatlog.ChatLog;
import com.bonn2.modules.core.permissions.Permissions;
import com.bonn2.modules.core.settings.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;
import java.util.Set;

public class ModerationListener extends ListenerAdapter {

    Moderation module;

    public ModerationListener(Moderation module) {
        this.module = module;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        // Check permissions
        if (!event.getName().equals("mod")) return;
        if (!Permissions.hasPermissionReply(event, Permissions.Level.MOD)) return;

        // Do different stuff based on the different command do sub command thingy
        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "purge" -> {
                int amount = (int) Objects.requireNonNull(event.getOption("amount")).getAsLong();
                if (Permissions.Level.getPermissionLevel(event.getMember()).equals(Permissions.Level.MOD)) {
                    int max = Settings.get(module, "mod_max_purge").getAsInt();
                    if (amount > max) amount = max;
                }
                TextChannel channel = event.getTextChannel();
                event.reply("Purging %s messages in <#%s>".formatted(amount, channel.getId()))
                        .setEphemeral(true)
                        .queue();
                channel.getIterableHistory()
                        .takeAsync(amount)
                        .thenAccept(channel::purgeMessages);
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Purged messages!");
                embedBuilder.setColor(Color.RED);
                embedBuilder.setDescription("<@%s> (%s) purged %s messages in <#%s>".formatted(
                        event.getUser().getId(),
                        event.getUser().getAsTag(),
                        amount,
                        channel.getId()
                ));
                ChatLog.getLogChannel().sendMessageEmbeds(embedBuilder.build()).queue();
            }
        }
    }
}
