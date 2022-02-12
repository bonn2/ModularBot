package com.bonn2.modules.uncaps;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.bonn2.modules.core.permissions.Permissions;
import com.bonn2.modules.core.settings.Settings;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UnCapsListener extends ListenerAdapter {

    static final List<Character> UPPERCASE = Arrays.asList('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');
    static final List<Character> LOWERCASE = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z');

    final UnCaps module;

    public UnCapsListener(UnCaps module) {
        this.module = module;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMember() == null) return;
        if (Permissions.hasPermission(event.getMember(), Permissions.Level.MOD)) return;
        String content = event.getMessage().getContentRaw();

        // Decide if message should be converted to lower case
        if (shouldLower(content)) {
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

            messageBuilder.setContent(content.toLowerCase(Locale.ROOT).replace("@", "*"));

            // Send Message
            client.send(messageBuilder.build());

            // Delete original
            event.getMessage().delete().queue();

            // Delete temp webhook
            webhook.delete().queueAfter(10, TimeUnit.SECONDS);
        }

    }

    private boolean shouldLower(@NotNull String string) {
        String[] words = string.split("[\s_]");
        if (words.length < 3 && string.length() < 10)
            return false;
        float capitals = 0;
        float total = 0;
        for (char character : string.toCharArray()) {
            if (UPPERCASE.contains(character)) {
                capitals++;
                total++;
            }
            else if (LOWERCASE.contains(character))
                total++;
        }
        if (total > 0 && capitals > 1) return (capitals / total) >= Settings.get(module, "threshold").getAsFloat();
        return false;
    }
}
