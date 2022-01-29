package com.bonn2;

import com.bonn2.modules.autoreply.AutoReply;
import com.bonn2.modules.core.commands.Commands;
import com.bonn2.modules.core.config.Config;
import com.bonn2.modules.core.permissions.Permissions;
import com.bonn2.modules.pubictimeout.PublicTimeout;
import com.bonn2.modules.uncaps.UnCaps;
import com.google.gson.JsonElement;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class Bot
{
    public static Logger logger;
    public static JDA jda = null;
    public static String localPath;
    public static Guild guild;
    public static TextChannel logChannel;
    public static List<String> modRoleIds = new ArrayList<>();

    // Handles starting the bot, and initializing static variables
    public static void main(String[] args) throws GeneralSecurityException, URISyntaxException, InterruptedException {

        long startTime = System.currentTimeMillis();

        logger = LoggerFactory.getLogger("Modular Bot");

        logger.info("Starting Bot...");

        File jarFile = new File(Bot.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        localPath = jarFile.getParent() + File.separator;

        // Load data from disk
        new Config().load();
        new Permissions().load();
        for (JsonElement element : Config.get("MODERATION_IDS").getAsJsonArray()) {
            modRoleIds.add(element.getAsString());
        }

        jda = JDABuilder.createDefault(Config.get("token").getAsString())
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_BANS
                )
                .build();

        // Wait until jda is initialized
        jda.awaitReady();

        logger.info("Logged in to: " + jda.getSelfUser().getAsTag());

        // Get Guild
        guild = jda.getGuildById(Config.get("guild").getAsString());

        // Get Channels
        logChannel = (TextChannel) jda.getGuildChannelById(Config.get("LOG_CHANNEL_ID").getAsString());

        // Load Modules
        new AutoReply().load();
        new UnCaps().load();
        new PublicTimeout().load();

        Commands.updateCommands();

        logger.info("Finished Loading! (" + ((float)(System.currentTimeMillis() - startTime)) / 1000 + " sec)");
    }
}
