package com.bonn2.modules.displaymessagelink;

import com.bonn2.Bot;
import com.bonn2.modules.core.settings.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.BaseGuildMessageChannel;
import net.dv8tion.jda.api.entities.Channel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisplayMessageLinkListener extends ListenerAdapter {

    static Pattern linkPattern = Pattern.compile("https://discord\\.com/channels/[0-9]+/[0-9]+/[0-9]+");
    final DisplayMessageLink module;

    public DisplayMessageLinkListener(DisplayMessageLink module) {
        this.module = module;
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        Matcher matcher = linkPattern.matcher(event.getMessage().getContentRaw());
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.CYAN);
        while (matcher.find()) {
            // Array Mapping
            // 4: Guild ID
            // 5: Channel ID
            // 6: Message ID
            String[] splitLink = matcher.group().split("/");
            Guild guild = Bot.jda.getGuildById(splitLink[4]);
            if (guild == null) continue;
            Channel channel = guild.getGuildChannelById(splitLink[5]);
            if (channel == null) continue;
            if (channel instanceof BaseGuildMessageChannel messageChannel) {
                Message message = messageChannel.retrieveMessageById(splitLink[6]).complete();
                if (message == null) continue;
                if (message.getContentRaw().equals("")) continue;
                embedBuilder.addField(
                        "@%s on <t:%s:d>".formatted(
                                message.getAuthor().getAsTag(),
                                message.getTimeCreated().toEpochSecond()
                        ),
                        "%s\n\n[See Original](%s)".formatted(
                                message.getContentRaw(),
                                matcher.group()
                        ),
                        true
                );
            }
        }
        int count = embedBuilder.getFields().size();
        embedBuilder.setTitle(count > 1 ? "Original messages" : "Original message");
        if (embedBuilder.getFields().size() > 0
                && embedBuilder.getFields().size() <= Objects.requireNonNull(Settings.get(module, "max_links")).getAsInt())
            event.getMessage().replyEmbeds(embedBuilder.build()).mentionRepliedUser(false).queue();
    }
}
