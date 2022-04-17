package com.bonn2.modules.editlog;

import com.bonn2.modules.core.settings.Settings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.concurrent.TimeUnit;

public class EditLogListener extends ListenerAdapter {

    EditLog module;

    Cache<String, Message> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();

    public EditLogListener(EditLog module) {
        this.module = module;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        cache.put(event.getMessageId(), event.getMessage());
    }

    @Override
    public void onMessageUpdate(@Nonnull MessageUpdateEvent event) {
        Message cachedMessage = cache.getIfPresent(event.getMessageId());
        TextChannel logChannel = Settings.get(module, "log_channel").getAsTextChannel();
        if (cachedMessage != null && logChannel != null) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Message Edited");
            embedBuilder.setColor(Color.YELLOW);
            embedBuilder.setDescription(
                    "%s edited a message in %s".formatted(
                            event.getAuthor().getAsMention(),
                            event.getMessage().getChannel().getAsMention()
                    )
            );
            boolean wasEdited = false;
            if (!cachedMessage.getContentRaw().equals(event.getMessage().getContentRaw())) {
                // Text was edited
                wasEdited = true;
                embedBuilder.appendDescription("\n\nOriginal: `%s`\nNew: `%s`".formatted(
                        cachedMessage.getContentRaw(),
                        event.getMessage().getContentRaw()
                ));
            }
            if (cachedMessage.getAttachments().size() > event.getMessage().getAttachments().size()) {
                // Attachments were removed
                wasEdited = true;
                embedBuilder.appendDescription("\n\n**Removed Attachments**");
                for (int i = 1; i <= cachedMessage.getAttachments().size(); i++)
                    if (!event.getMessage().getAttachments().contains(cachedMessage.getAttachments().get(i - 1)))
                        embedBuilder.appendDescription("\n**%s:** [%s](%s)".formatted(
                                i,
                                cachedMessage.getAttachments().get(i - 1).getFileName(),
                                cachedMessage.getAttachments().get(i - 1).getUrl()
                        ));
            }
            embedBuilder.appendDescription("\n\n[See Message](%s)".formatted(
                    event.getMessage().getJumpUrl()
            ));

            if (wasEdited) logChannel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
        cache.put(event.getMessageId(), event.getMessage());
    }

    @Override
    public void onMessageDelete(@Nonnull MessageDeleteEvent event) {
        Message cachedMessage = cache.getIfPresent(event.getMessageId());
        TextChannel logChannel = Settings.get(module, "log_channel").getAsTextChannel();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Message Deleted");
        embedBuilder.setColor(Color.RED);
        boolean hasContent = false;
        if (cachedMessage != null) {
            embedBuilder.setDescription("%s's message was deleted from %s.".formatted(
                    cachedMessage.getAuthor().getAsMention(),
                    cachedMessage.getChannel().getAsMention()
            ));
            if (!cachedMessage.getContentRaw().equals("")) {
                hasContent = true;
                embedBuilder.appendDescription("\n\n`%s`".formatted(
                        cachedMessage.getContentRaw()
                ));
            }
            if (cachedMessage.getAttachments().size() > 0) {
                hasContent = true;
                embedBuilder.appendDescription("\n\n**Attachments:**");
                for (int i = 1; i <= cachedMessage.getAttachments().size(); i++)
                    embedBuilder.appendDescription("\n**%s:** [%s](%s)".formatted(
                            i,
                            cachedMessage.getAttachments().get(i - 1).getFileName(),
                            cachedMessage.getAttachments().get(i - 1).getUrl()
                    ));
            }
            if (hasContent)
                logChannel.sendMessageEmbeds(embedBuilder.build()).queue();
        }
        cache.invalidate(event.getMessageId());
    }
}
