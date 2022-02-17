package com.bonn2.modules.antispam;

import com.bonn2.Bot;
import com.bonn2.modules.core.bans.Bans;
import com.bonn2.modules.core.permissions.Permissions;
import com.bonn2.modules.core.settings.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AntiSpamListener extends ListenerAdapter {

    final AntiSpam module;
    Map<Member, AntiSpam.MessageLog> messageLogs = new HashMap<>();

    public AntiSpamListener(AntiSpam module) {
        this.module = module;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.TEXT)) {
            Member member = event.getMember();
            Message message = event.getMessage();
            // Don't filter webhooks
            if (member == null) return;
            // Don't filter bots
            if (member.getUser().isBot()) return;
            // Only filter users who are not moderator or above
            if (Permissions.hasPermission(member, Permissions.Level.MOD)) return;
            // Update existing log
            if (messageLogs.containsKey(member)) {
                AntiSpam.MessageLog messageLog = messageLogs.get(member);
                if (messageLog.isSimilar(message))
                    messageLog.messages.add(message);
                else
                    messageLog = new AntiSpam.MessageLog(new LinkedList<>(List.of(message)));
                // Save updated log
                messageLogs.put(member, messageLog);
            }
            // Create new log
            else {
                messageLogs.put(
                        member,
                        new AntiSpam.MessageLog(new LinkedList<>(List.of(message)))
                );
            }
            // Get updated message log
            AntiSpam.MessageLog messageLog = messageLogs.get(member);
            int deleteThreshold = Settings.get(module, "messages_delete_threshold").getAsInt();
            int timeoutThreshold = Settings.get(module, "messages_timeout_threshold").getAsInt();
            int kickThreshold = Settings.get(module, "channels_kick_threshold").getAsInt();
            // Only kick or timeout, never both
            if (kickThreshold > 0 && messageLog.getUniqueChannels().size() > kickThreshold) {
                // Kick the user
                messageLog.logKick("Sent identical messages across > %s unique channels.".formatted(
                        kickThreshold
                ));
                member.kick("Sent identical messages across > %s unique channels.".formatted(
                        kickThreshold
                )).queue();
            }
            else if (timeoutThreshold > 0 && messageLog.messages.size() > timeoutThreshold) {
                // Timeout member and DM them a warning
                member.timeoutFor(1, TimeUnit.MINUTES).queue();
                messageLog.logTimeout("Sent > %s identical messages in a row.".formatted(timeoutThreshold));
                User user = member.getUser();
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Warning!");
                embedBuilder.setColor(Color.RED);
                embedBuilder.setDescription("Spam is not allowed in %s!\nContinued spam will result in a kick!"
                        .formatted(
                                Bot.guild.getName()
                        ));
                embedBuilder.setTimestamp(Instant.now());
                user.openPrivateChannel().complete().sendMessageEmbeds(embedBuilder.build()).queue();
            }
            // Delete regardless of kick or timeout
            if (deleteThreshold > 0 && messageLog.messages.size() > deleteThreshold) {
                // Delete messages
                messageLog.deleteMessages();
                messageLog.logDeleted("Sent > %s identical messages in a row".formatted(deleteThreshold));
            }
        }
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        if (!Objects.equals(event.getComponent().getId(), "ban")) return;

        if (!Permissions.hasPermissionReply(event, Permissions.Level.MOD)) return;

        // Remove reply if no option is selected
        if (event.getValues().size() == 0) {
            event.reply("No option selected").setEphemeral(true).queue();
            return;
        }
        String idToBan = event.getValues().get(0);
        // Remove reply if "No" is selected, or user is already banned\
        if (Objects.equals(idToBan, "no")) {
            event.reply("Did not ban the user.").setEphemeral(true).queue();
            return;
        }
        if (Bans.isBanned(idToBan)) {
            event.reply("This user is already banned!").setEphemeral(true).queue();
            return;
        }
        User userToBan = Bot.jda.getUserById(idToBan);
        Bans.ban(idToBan);
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Banned a user!");
        embedBuilder.setColor(Color.RED);
        if (userToBan == null)
            embedBuilder.addField(
                    "User",
                    idToBan,
                    false
            );
        else
            embedBuilder.addField(
                    "User",
                    "<@%s> (%s)".formatted(
                            userToBan.getId(),
                            userToBan.getAsTag()
                    ),
                    false
            );
        embedBuilder.addField(
                "Reason",
                "Banned by <@%s> (%s)".formatted(
                        event.getUser().getId(),
                        event.getUser().getAsTag()
                ),
                false
        );
        embedBuilder.setTimestamp(Instant.now());
        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
