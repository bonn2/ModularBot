package com.bonn2.modules.antispam;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.chatlog.ChatLog;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AntiSpam extends Module {

    public AntiSpam() {
        version = "v1.0";
        priority = Priority.POST_JDA_HIGH;
        name = "AntiSpam";
    }

    @Override
    public void registerSettings() {
        Settings.register(this, "messages_delete_threshold", Setting.Type.INT, "3",
                "The number of repeat messages before they are deleted.");
        Settings.register(this, "messages_timeout_threshold", Setting.Type.INT, "4",
                "The number of repeat messages before the user is timed out.");
        Settings.register(this, "channels_kick_threshold", Setting.Type.INT, "4",
                "The number of channels repeat messages can be sent in before the user is kicked.");
    }

    @Override
    public void load() {
        Bot.logger.info("Registering Listeners...");
        Bot.jda.addEventListener(new AntiSpamListener(this));
    }

    @Override
    public CommandData[] getCommands() {
        return new CommandData[0];
    }

    public static class MessageLog {

        public final List<Message> messages;
        public Message deletedLog = null;
        private int deleted = 0;

        public MessageLog(List<Message> messages) {
            this.messages = messages;
        }

        /**
         * Returns if a message is similar to other logged messages.
         * @param message The message to check
         * @return True if the message is similar
         */
        public boolean isSimilar(@NotNull Message message) {
            // This is extremely basic atm, but I broke this out into a separate function, so I can make
            if (message.getContentRaw().equals("")) return false;
            return messages.get(0).getContentRaw().equals(message.getContentRaw());
        }

        /**
         * Delete all messages in log. Is safe to call multiple times
         */
        public void deleteMessages() {
            for (int i = 0; i < messages.size(); i++) {
                if (i < deleted) continue;
                messages.get(i).delete().queue();
                deleted++;
            }
        }

        public List<TextChannel> getUniqueChannels() {
            List<TextChannel> channels = new ArrayList<>();
            for (Message message : messages)
                if (!channels.contains(message.getTextChannel()))
                    channels.add(message.getTextChannel());
            return channels;
        }

        public void logDeleted(String reason) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Deleted Messages");
            embedBuilder.setColor(Color.YELLOW);
            embedBuilder.addField(
                    "User",
                    "%s (%s)".formatted(
                            Objects.requireNonNull(messages.get(0).getMember()).getAsMention(),
                            Objects.requireNonNull(messages.get(0).getMember()).getUser().getAsTag()
                    ),
                    false
            );
            embedBuilder.addField(
                    "Reason",
                    reason,
                    false
            );
            embedBuilder.addField(
                    "Message",
                    "```\n%s```".formatted(messages.get(0).getContentRaw()),
                    false
            );
            StringBuilder channels = new StringBuilder();
            for (Message message : messages)
                channels.append("<#%s> on <t:%s:f>\n".formatted(
                        message.getTextChannel().getId(),
                        message.getTimeCreated().toEpochSecond()));
            embedBuilder.addField(
                    "Channels",
                    channels.toString(),
                    false
            );

            if (deletedLog == null)
                deletedLog = ChatLog.getLogChannel().sendMessageEmbeds(embedBuilder.build()).complete();
            else
                deletedLog = deletedLog.editMessageEmbeds(embedBuilder.build()).complete();
        }

        public void logTimeout(String reason) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Timed out a user!");
            embedBuilder.setColor(Color.ORANGE);
            embedBuilder.addField(
                    "User",
                    "%s (%s)".formatted(
                            Objects.requireNonNull(messages.get(0).getMember()).getAsMention(),
                            Objects.requireNonNull(messages.get(0).getMember()).getUser().getAsTag()
                    ),
                    false
            );
            embedBuilder.addField(
                    "Reason",
                    reason,
                    false
            );
            embedBuilder.addField(
                    "Message",
                    "```\n%s```".formatted(messages.get(0).getContentRaw()),
                    false
            );
            StringBuilder channels = new StringBuilder();
            for (Message message : messages)
                channels.append("<#%s> on <t:%s:f>\n".formatted(
                        message.getTextChannel().getId(),
                        message.getTimeCreated().toEpochSecond()));
            embedBuilder.addField(
                    "Channels",
                    channels.toString(),
                    false
            );
            ChatLog.getLogChannel().sendMessageEmbeds(embedBuilder.build()).queue();
        }

        public void logKick(String reason) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Kicked a user!");
            embedBuilder.setColor(Color.RED);
            embedBuilder.addField(
                    "User",
                    "%s (%s)".formatted(
                            messages.get(0).getAuthor().getAsMention(),
                            messages.get(0).getAuthor().getAsTag()
                    ),
                    false
            );
            embedBuilder.addField(
                    "Reason",
                    reason,
                    false
            );
            embedBuilder.addField(
                    "Message",
                    "```\n%s```".formatted(messages.get(0).getContentRaw()),
                    false
            );
            StringBuilder channels = new StringBuilder();
            for (Message message : messages)
                channels.append("<#%s> on <t:%s:f>\n".formatted(
                        message.getTextChannel().getId(),
                        message.getTimeCreated().toEpochSecond()));
            embedBuilder.addField(
                    "Channels",
                    channels.toString(),
                    false
            );
            SelectMenu selectionMenu = SelectMenu.create("ban")
                    .setPlaceholder("Should this have been a ban?")
                    .setRequiredRange(0, 1)
                    .addOption("Yes", messages.get(0).getAuthor().getId())
                    .addOption("No", "no")
                    .build();
            MessageBuilder messageBuilder = new MessageBuilder();
            messageBuilder.setEmbeds(embedBuilder.build());
            messageBuilder.setActionRows(ActionRow.of(selectionMenu));
            ChatLog.getLogChannel().sendMessage(messageBuilder.build()).queue();
        }
    }
}
