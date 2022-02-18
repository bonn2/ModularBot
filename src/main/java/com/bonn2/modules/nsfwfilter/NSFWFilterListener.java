package com.bonn2.modules.nsfwfilter;

import com.bonn2.modules.Module;
import com.bonn2.modules.core.chatlog.ChatLog;
import com.bonn2.modules.core.settings.Settings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.AttachmentOption;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class NSFWFilterListener extends ListenerAdapter {

    private static final String API_URL = "https://api.moderatecontent.com/moderate/?key=%s&url=%s";
    private final Module module;

    public NSFWFilterListener(Module module) {
        this.module = module;
    }

    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.getMember() == null) return;
        for (Role role : Settings.get(module, "immune_roles").getAsRoleList())
            if (event.getMember().getRoles().contains(role)) return;
        String key = Settings.get(module, "api_key").getAsString();
        if (key.equals("")) return;
        for (Message.Attachment attachment : event.getMessage().getAttachments()) {
            if (attachment.isImage()) {
                try {
                    URL url = new URL(API_URL.formatted(key, attachment.getUrl()));
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.connect();
                    if (conn.getResponseCode() != 200) continue;
                    JsonObject response = new Gson().fromJson(new String(url.openStream().readAllBytes()), JsonObject.class);
                    if (response.get("rating_index") == null) continue;
                    float adultDeleteThreshold = Settings.get(module, "adult_delete_threshold").getAsFloat();
                    // Delete if detected as overall adult
                    boolean deleteAdult = Settings.get(module, "delete_adult").getAsBoolean();
                    if (deleteAdult && Objects.equals(response.get("rating_letter").getAsString(), "a")) {
                        MessageBuilder messageBuilder = new MessageBuilder();
                        messageBuilder.setContent(":x: Deleted an image that was detected as adult!");
                        messageBuilder.setEmbeds(getResultsEmbed(
                                response.get("predictions").getAsJsonObject(),
                                event.getMessage().getTimeCreated().toInstant().toEpochMilli())
                        );
                        ChatLog.getLogChannel().sendMessage(messageBuilder.build())
                                .addFile(
                                        new URL(attachment.getUrl()).openStream(),
                                        "image." + attachment.getFileExtension(),
                                        AttachmentOption.SPOILER
                                )
                                .queue();
                        event.getMessage().delete().queue();
                        String message = Settings.get(module, "message").getAsString();
                        if (!Objects.equals(message, "")) {
                            event.getTextChannel().sendMessageEmbeds(new EmbedBuilder()
                                    .setTitle(message.replaceAll("@user", event.getAuthor().getAsMention()))
                                    .setColor(Color.RED)
                                    .build())
                                    .mention(event.getAuthor())
                                    .queue();
                        }
                        return;
                    }
                    // Delete if over adult threshold
                    if (adultDeleteThreshold >= 0 && response.get("predictions").getAsJsonObject().get("adult").getAsFloat() >= adultDeleteThreshold) {
                        MessageBuilder messageBuilder = new MessageBuilder();
                        messageBuilder.setContent(":x: Deleted an image that is over the adult threshold!");
                        messageBuilder.setEmbeds(getResultsEmbed(
                                response.get("predictions").getAsJsonObject(),
                                event.getMessage().getTimeCreated().toInstant().toEpochMilli())
                        );
                        ChatLog.getLogChannel().sendMessage(messageBuilder.build())
                                .addFile(
                                        new URL(attachment.getUrl()).openStream(),
                                        "image." + attachment.getFileExtension(),
                                        AttachmentOption.SPOILER
                                )
                                .queue();
                        event.getMessage().delete().queue();
                        String message = Settings.get(module, "message").getAsString();
                        if (!Objects.equals(message, "")) {
                            event.getTextChannel().sendMessageEmbeds(new EmbedBuilder()
                                            .setTitle(message.replaceAll("@user", event.getAuthor().getAsMention()))
                                            .setColor(Color.RED)
                                            .build())
                                    .mention(event.getAuthor())
                                    .queue();
                        }
                        return;
                    }
                    float teenDeleteThreshold = Settings.get(module, "teen_delete_threshold").getAsFloat();
                    // Delete if over teen threshold
                    if (teenDeleteThreshold >= 0 && response.get("predictions").getAsJsonObject().get("teen").getAsFloat() >= teenDeleteThreshold) {
                        MessageBuilder messageBuilder = new MessageBuilder();
                        messageBuilder.setContent(":x: Deleted an image that is over the teen threshold!");
                        messageBuilder.setEmbeds(getResultsEmbed(
                                response.get("predictions").getAsJsonObject(),
                                event.getMessage().getTimeCreated().toInstant().toEpochMilli())
                        );
                        ChatLog.getLogChannel().sendMessage(messageBuilder.build())
                                .addFile(
                                        new URL(attachment.getUrl()).openStream(),
                                        "image." + attachment.getFileExtension(),
                                        AttachmentOption.SPOILER
                                )
                                .queue();
                        event.getMessage().delete().queue();
                        String message = Settings.get(module, "message").getAsString();
                        if (!Objects.equals(message, "")) {
                            event.getTextChannel().sendMessageEmbeds(new EmbedBuilder()
                                            .setTitle(message.replaceAll("@user", event.getAuthor().getAsMention()))
                                            .setColor(Color.RED)
                                            .build())
                                    .mention(event.getAuthor())
                                    .queue();
                        }
                        return;
                    }
                    float teenAndAdultThreshold = Settings.get(module, "teen_and_adult_delete_threshold").getAsFloat();
                    // Delete if over teen threshold
                    if (teenAndAdultThreshold >= 0 &&
                            response.get("predictions").getAsJsonObject().get("teen").getAsFloat() + response.get("predictions").getAsJsonObject().get("adult").getAsFloat() >= teenAndAdultThreshold) {
                        MessageBuilder messageBuilder = new MessageBuilder();
                        messageBuilder.setContent(":x: Deleted an image that is over the teen + adult threshold!");
                        messageBuilder.setEmbeds(getResultsEmbed(
                                response.get("predictions").getAsJsonObject(),
                                event.getMessage().getTimeCreated().toInstant().toEpochMilli())
                        );
                        ChatLog.getLogChannel().sendMessage(messageBuilder.build())
                                .addFile(
                                        new URL(attachment.getUrl()).openStream(),
                                        "image." + attachment.getFileExtension(),
                                        AttachmentOption.SPOILER
                                )
                                .queue();
                        event.getMessage().delete().queue();
                        String message = Settings.get(module, "message").getAsString();
                        if (!Objects.equals(message, "")) {
                            event.getTextChannel().sendMessageEmbeds(new EmbedBuilder()
                                            .setTitle(message.replaceAll("@user", event.getAuthor().getAsMention()))
                                            .setColor(Color.RED)
                                            .build())
                                    .mention(event.getAuthor())
                                    .queue();
                        }
                        return;
                    }
                    // Log if detected as non-everyone
                    if (response.get("rating_index").getAsInt() > 1) {
                        // Image was not auto rated as E
                        MessageBuilder messageBuilder = new MessageBuilder();
                        messageBuilder.setContent(":x: Detected an image that seems to be NSFW!");
                        messageBuilder.setEmbeds(getResultsEmbed(
                                response.get("predictions").getAsJsonObject(),
                                event.getMessage().getTimeCreated().toInstant().toEpochMilli())
                        );
                        ChatLog.getLogChannel().sendMessage(messageBuilder.build())
                                .addFile(
                                        new URL(attachment.getUrl()).openStream(),
                                        "image." + attachment.getFileExtension(),
                                        AttachmentOption.SPOILER
                                )
                                .queue();
                    }
                    conn.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static @NotNull MessageEmbed getResultsEmbed(JsonObject predictionsObject, long messageTime) {
        StringBuilder predictions = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            float biggest = 0;
            String biggestPrediction = "";
            for (String prediction : predictionsObject.keySet()) {
                if (predictionsObject.get(prediction).getAsFloat() > biggest) {
                    biggestPrediction = prediction;
                    biggest = predictionsObject.get(prediction).getAsFloat();
                }
            }
            predictions.append("%s : %.2f%%\n".formatted(biggestPrediction, biggest));
            predictionsObject.remove(biggestPrediction);
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Detection Results");
        embedBuilder.setDescription(
                predictions.toString()
        );
        embedBuilder.setFooter("Time elapsed: %ssec".formatted(
                (float) (System.currentTimeMillis() - messageTime) / 1000
        ));
        embedBuilder.setColor(Color.RED);
        return embedBuilder.build();
    }
}
