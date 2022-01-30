package com.bonn2.modules.autoreply;

import com.bonn2.Bot;
import com.bonn2.modules.core.permissions.PermissionLevel;
import com.bonn2.modules.core.permissions.Permissions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.bonn2.Bot.logger;
import static com.bonn2.modules.autoreply.AutoReply.AUTO_REPLIES;
import static com.bonn2.modules.autoreply.AutoReply.save;

public class AutoReplyListener extends ListenerAdapter {

    Map<String, AutoReply.Data> listeningIDS = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("autoreply")) return;
        if (!Permissions.hasPermissionReply(event, PermissionLevel.ADMIN)) return;
        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case "create" -> {
                AutoReply.Data data = new AutoReply.Data(
                        Objects.requireNonNull(event.getOption("words")).getAsString().split(" ")
                );
                event.reply("What would you like me to say in response?").setEphemeral(true).complete();
                listeningIDS.put(Objects.requireNonNull(event.getMember()).getId(), data);
            }
            case "list" -> {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Auto replies");
                embedBuilder.setColor(Color.GREEN);
                StringBuilder description = new StringBuilder("```");
                for (int i = 0; i < AUTO_REPLIES.size(); i++) {
                    StringBuilder words = new StringBuilder();
                    for (String word : AUTO_REPLIES.get(i).words)
                        words.append(word).append(" ");
                    description.append("\n%s:\n words - %s\n reply - %s".formatted(i, words, AUTO_REPLIES.get(i).reply));
                }
                description.append("```");
                embedBuilder.setDescription(description.toString());
                event.replyEmbeds(embedBuilder.build()).queue();
            }
            case "delete" -> {
                int index = (int) Objects.requireNonNull(event.getOption("id")).getAsDouble();
                if (!(index <= AUTO_REPLIES.size() && index >= 0)) {
                    event.reply("That id does not exist.").setEphemeral(true).queue();
                    return;
                }
                logger.info("Deleting auto reply %s%n".formatted(index));
                AUTO_REPLIES.remove(index);
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Deleted auto reply %s".formatted(index));
                embedBuilder.setColor(Color.RED);
                event.replyEmbeds(embedBuilder.build()).queue();
                save();
            }
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMember() == null) return;
        if (listeningIDS.containsKey(event.getMember().getId())) {
            logger.info("Creating a new auto reply...");
            AutoReply.Data data = listeningIDS.get(event.getMember().getId());
            data.reply = event.getMessage().getContentRaw();
            listeningIDS.remove(event.getMember().getId());
            AUTO_REPLIES.add(data);
            // Log to log channel
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Created a new auto reply");
            embedBuilder.setColor(Color.GREEN);
            StringBuilder words = new StringBuilder();
            for (String word : data.words)
                words.append(word).append(" ");
            embedBuilder.addField("Words", words.toString(), false);
            embedBuilder.addField("Reply", data.reply, false);
            Bot.logChannel.sendMessageEmbeds(embedBuilder.build()).queue();
            event.getMessage().delete().queue();
            // Save auto replies to file
            save();
        } else {
            // Checking if I should send an auto reply
            if (event.getAuthor().isBot()) return;
            for (AutoReply.Data data : AUTO_REPLIES) {
                if (System.currentTimeMillis() - data.lastSent < 300000) continue;
                boolean shouldReply = true;
                for (String word : data.words)
                    if (!event.getMessage().getContentRaw().toLowerCase().contains(word)) shouldReply = false;
                if (shouldReply) {
                    event.getMessage().reply(data.reply).mentionRepliedUser(false).queue();
                    data.lastSent = System.currentTimeMillis();
                }
            }
        }
    }


}
