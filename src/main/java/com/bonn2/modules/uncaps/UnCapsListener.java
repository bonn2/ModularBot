package com.bonn2.modules.uncaps;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.bonn2.modules.core.config.Config;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class UnCapsListener extends ListenerAdapter {

    Map<String, Webhook> webhooks = new HashMap<>();

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
            messageBuilder.setUsername(event.getAuthor().getName());
            messageBuilder.setAvatarUrl(event.getAuthor().getEffectiveAvatarUrl());
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
            if (combo >= 10) return true;
            if (isUpperCase(character)) combo++;
            else if (isLowerCase(character)) combo = 0;
        }
        return false;
    }

    private boolean isUpperCase(char character) {
        // List of all uppercase chars
        List<Character> uppercase = Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
        return uppercase.contains(character);
    }

    private boolean isLowerCase(char character) {
        // List of all lowercase chars
        List<Character> lowercase = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z');
        return lowercase.contains(character);
    }
}
