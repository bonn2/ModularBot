package com.bonn2.modules.moderation;

import com.bonn2.modules.core.chatlog.ChatLog;
import com.bonn2.modules.core.permissions.Permissions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

public class ModerationListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        // Check permissions
        if (!event.getName().equals("mod")) return;
        if (!Permissions.hasPermissionReply(event, Permissions.Level.MOD)) return;

        // Do different stuff based on the different command do sub command thingy
        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "purge" -> {
                int amount = (int) Objects.requireNonNull(event.getOption("amount")).getAsLong();
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
