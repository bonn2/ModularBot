package com.bonn2.modules.updatingip;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.autoreply.AutoReply;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;
import com.google.gson.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import static com.bonn2.Bot.logger;

public class UpdatingIP extends Module {

    static File MESSAGE_FILE;
    static List<UpdatingIP.Data> MESSAGES = new LinkedList<>();
    
    public record Data(
        String name,
        String domain,
        int port,
        String password
    ) {
        public static @NotNull Data fromJson(@NotNull JsonObject jsonObject) {
            String name = jsonObject.has("name") ? jsonObject.get("name").getAsString() : "";
            String domain = jsonObject.has("domain") ? jsonObject.get("domain").getAsString() : "";
            int port = jsonObject.has("port") ? jsonObject.get("port").getAsInt() : 0;
            String password = jsonObject.has("password") ? jsonObject.get("password").getAsString(): "";
            return new Data(name, domain, port, password);
        }

        public @NotNull JsonObject toJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("name", new JsonPrimitive(name));
            jsonObject.add("domain", new JsonPrimitive(domain));
            jsonObject.add("port", new JsonPrimitive(port));
            jsonObject.add("password", new JsonPrimitive(password));
            return jsonObject;
        }
    }

    public UpdatingIP() {
        version = "v1.0";
        priority = Priority.POST_JDA_LOW;
        name = "UpdatingIP";
    }

    @Override
    public void registerSettings() {}

    @Override
    public void load() {
        logger.info("Loading Saved Data...");
        try {
            MESSAGE_FILE = new File(Bot.localPath + "updating-ip.json");
            if (MESSAGE_FILE.createNewFile()) {
                String defaultFile = "[]";
                new FileOutputStream(MESSAGE_FILE).write(defaultFile.getBytes(StandardCharsets.UTF_8));
            } else {
                JsonArray jsonArray = new Gson().fromJson(new String(new FileInputStream(MESSAGE_FILE).readAllBytes()), JsonArray.class);
                for (JsonElement jsonElement : jsonArray)
                    MESSAGES.add(UpdatingIP.Data.fromJson((JsonObject) jsonElement));
            }
        } catch (IOException e) {
            logger.error("Failed to save default file!");
            e.printStackTrace();
        }

        logger.info("Registering Listeners...");
        Bot.jda.addEventListener(new UpdatingIPListener());
/*        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
        Runnable task1 = () -> {
            try {
                InetAddress ipaddress = InetAddress.getByName("vrising.envirocraft.net");
                String message =
                        """
                        The current v rising ip is: **%s:27031
                        The password is: macrodeeznuts
                        """;
                message = message.formatted(ipaddress.getHostAddress());
                TextChannel channel = Settings.get(this, "channel").getAsTextChannel();
                if (channel != null) {
                    Message updatingMessage = channel.getHistory().retrievePast(1).complete().get(0);
                    if (updatingMessage != null) {
                        Bot.logger.info(updatingMessage.getAuthor().getId());
                        Bot.logger.info(Bot.jda.getSelfUser().getId());
                        if (updatingMessage.getAuthor().getId().equals(Bot.jda.getSelfUser().getId())) {
                            Bot.logger.info(ipaddress.getHostAddress());
                            Bot.logger.info(ip);
                            if (!ipaddress.getHostAddress().equals(ip)) {
                                Bot.logger.info("Editing");
                                updatingMessage.editMessage(message).queue();
                                ip = ipaddress.getHostAddress();
                            }
                        } else {
                            Bot.logger.info("Different author");
                            channel.sendMessage(message.formatted(ipaddress.getHostAddress())).queue();
                            ip = ipaddress.getHostAddress();
                        }
                    } else {
                        Bot.logger.info("No message");
                        channel.sendMessage(message.formatted(ipaddress.getHostAddress())).queue();
                        ip = ipaddress.getHostAddress();
                    }
                }
            } catch (UnknownHostException ignored) {}
        };
        scheduler.scheduleAtFixedRate(task1, 1, 1, TimeUnit.MINUTES);*/
    }

    @Override
    public CommandData[] getCommands() {
        return new CommandData[] {
                Commands.slash(
                        "updatingip",
                        "Create a message with server details that auto updates."
                ).addOption(
                        OptionType.STRING,
                        "name",
                        "The name of the server",
                        true
                ).addOption(
                        OptionType.STRING,
                        "domain",
                        "The domain name of the server.",
                        true
                ).addOption(
                        OptionType.INTEGER,
                        "port",
                        "The port of the server",
                        false
                ).addOption(
                        OptionType.STRING,
                        "password",
                        "The password for the server",
                        false
                ).setDefaultEnabled(false)
        };
    }
}
