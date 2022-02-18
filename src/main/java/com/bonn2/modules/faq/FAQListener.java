package com.bonn2.modules.faq;

import com.bonn2.Bot;
import com.bonn2.modules.core.permissions.Permissions;
import com.bonn2.modules.core.settings.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

import static com.bonn2.modules.faq.FAQ.QUESTIONS;

public class FAQListener extends ListenerAdapter {
    FAQ module;

    public FAQListener(FAQ module) {
        this.module = module;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("faq")) return;
        switch (Objects.requireNonNull(event.getSubcommandGroup())) {
            case "edit" -> {
                // Check permissions
                if (!Permissions.hasPermissionReply(event, Permissions.Level.MOD)) return;
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "new" -> {
                        String name = Objects.requireNonNull(event.getOption("name")).getAsString();
                        String title = Objects.requireNonNull(event.getOption("title")).getAsString();
                        String description = Objects.requireNonNull(event.getOption("description")).getAsString();
                        description = description.replaceAll("\\\\n", "\n");
                        for (FAQ.Question question : QUESTIONS) {
                            if (Objects.equals(question.name, name)) {
                                QUESTIONS.remove(question);
                                break;
                            }
                        }
                        QUESTIONS.add(new FAQ.Question(name, title, description));
                        FAQ.save();
                        Bot.updateCommands();
                        event.replyEmbeds(getListEmbed()).queue();
                    }
                    case "list" -> event.replyEmbeds(getListEmbed()).queue();
                    case "delete" -> {
                        String name = Objects.requireNonNull(event.getOption("name")).getAsString();
                        for (FAQ.Question question : QUESTIONS) {
                            if (Objects.equals(question.name, name)) {
                                QUESTIONS.remove(question);
                                FAQ.save();
                                Bot.updateCommands();
                                event.replyEmbeds(getListEmbed()).queue();
                                return;
                            }
                        }
                        event.reply("Could not find a FAQ entry named %s".formatted(name))
                                .setEphemeral(true)
                                .queue();
                    }
                }
            }
            case "read" -> {
                String entry = event.getSubcommandName();
                for (FAQ.Question question : QUESTIONS) {
                    if (Objects.equals(question.name, entry)) {
                        event.replyEmbeds(getFAQEmbed(question)).queue();
                        return;
                    }
                }
            }
        }
    }

    private static @NotNull MessageEmbed getListEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("FAQ Entries");
        embedBuilder.setColor(Color.BLUE);
        for (FAQ.Question question : QUESTIONS) {
            embedBuilder.addField(
                    question.name,
                    "**Title:** %s\n**Description:** %s".formatted(
                            question.title,
                            question.description
                    ),
                    true
            );
        }
        return embedBuilder.build();
    }

    public static MessageEmbed getFAQEmbed(FAQ.Question question) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(question.title);
        embedBuilder.setColor(Color.CYAN);
        embedBuilder.setDescription(question.description);
        return embedBuilder.build();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        int maxReplies = Settings.get(module, "max_replies").getAsInt();
        int replies = 0;
        for (FAQ.Question question : QUESTIONS) {
            if (replies >= maxReplies) return;
            if (event.getMessage().getContentRaw().contains("!" + question.name)) {
                event.getMessage().replyEmbeds(getFAQEmbed(question))
                        .mentionRepliedUser(false)
                        .queue();
                replies++;
            }
        }
    }
}
