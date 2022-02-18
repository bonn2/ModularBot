package com.bonn2.modules.moderation;

import com.bonn2.modules.core.chatlog.ChatLog;
import com.bonn2.modules.core.permissions.Permissions;
import com.bonn2.modules.core.settings.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Locale;
import java.util.Objects;

import static com.bonn2.modules.moderation.Moderation.BANNED_WORDS;

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

        if (event.getSubcommandGroup() == null) {
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
            return;
        }

        switch (event.getSubcommandGroup()) {
            case "banned_words" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "add" -> {
                        String word = event.getOption("word").getAsString();
                        if (BANNED_WORDS.contains(word)) {
                            event.reply("This word is already on the list!")
                                    .setEphemeral(true)
                                    .queue();
                            return;
                        }
                        BANNED_WORDS.add(word);
                        module.saveBannedWords();
                        event.reply("Added `%s` to the banned words list.".formatted(word))
                                .queue();
                    }
                    case "list" -> {
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setTitle("Banned words");
                        embedBuilder.setColor(Color.RED);
                        StringBuilder stringBuilder = new StringBuilder();
                        for (String word : BANNED_WORDS) {
                            stringBuilder.append(word);
                            stringBuilder.append("\n");
                        }
                        embedBuilder.setDescription(stringBuilder.toString());
                        event.replyEmbeds(embedBuilder.build()).queue();
                    }
                    case "remove" -> {
                        String word = event.getOption("word").getAsString();
                        if (BANNED_WORDS.remove(word)) {
                            module.saveBannedWords();
                            event.reply("Removed `%s` from the banned words list.".formatted(word))
                                    .queue();
                        }
                        else {
                            event.reply("`%s` was not in the banned words list.".formatted(word))
                                    .queue();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String message = " " + event.getMessage().getContentRaw().toLowerCase(Locale.ROOT) + " ";
        for (String word : BANNED_WORDS) {
            if (message.contains(" " + word.toLowerCase(Locale.ROOT) + " ")) {
                try {
                    event.getMessage().delete().complete();
                } catch (RuntimeException ignored) {
                    return;
                }
                break;
            }
        }
    }
}
