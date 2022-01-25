package com.bonn2.modules.uncaps;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class UnCapsListener extends ListenerAdapter {

    static final List<Character> UPPERCASE = Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
    static final List<Character> LOWERCASE = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z');

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String content = event.getMessage().getContentRaw();

        // Decide if message should be converted to lower case
        if (shouldLower(content.toCharArray())) {
            // Create temp webhook in channel
            Webhook webhook = event.getTextChannel().createWebhook("UnCapsTemp " + UUID.randomUUID()).complete();

            // Create Webhook client
            WebhookClientBuilder builder = new WebhookClientBuilder(webhook.getUrl());
            builder.setThreadFactory((job) -> {
                Thread thread = new Thread(job);
                thread.setName("Webhook-Thread");
                thread.setDaemon(true);
                return thread;
            });
            WebhookClient client = builder.build();

            // Create Webhook message
            WebhookMessageBuilder messageBuilder = new WebhookMessageBuilder();
            if (event.getMember() == null) {
                messageBuilder.setAvatarUrl(event.getAuthor().getEffectiveAvatarUrl());
                messageBuilder.setUsername(event.getAuthor().getName());
            }
            else {
                messageBuilder.setAvatarUrl(event.getMember().getEffectiveAvatarUrl());
                messageBuilder.setUsername(event.getMember().getEffectiveName());
            }

            messageBuilder.setContent(content.toLowerCase(Locale.ROOT));

            // Send Message
            client.send(messageBuilder.build());

            // Delete original
            event.getMessage().delete().queue();

            // Delete temp webhook
            webhook.delete().queueAfter(10, TimeUnit.SECONDS);
        }

    }

    private boolean shouldLower(char[] characters) {
        int combo = 0;
        for (char character : characters) {
            // TODO: 1/25/2022 Make combo configurable with Settings
            if (combo >= 5) return true;
            if (UPPERCASE.contains(character)) combo++;
            else if (LOWERCASE.contains(character)) combo = 0;
        }
        return false;
    }
}
