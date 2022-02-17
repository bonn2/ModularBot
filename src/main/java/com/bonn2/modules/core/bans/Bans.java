package com.bonn2.modules.core.bans;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static com.bonn2.Bot.logger;

public class Bans extends Module {

    static File BAN_FILE = new File(Bot.localPath + "bans.json");
    public static JsonArray BANS = new JsonArray();

    public Bans() {
        name = "Bans";
        version = "v1.0";
        priority = Priority.POST_JDA_HIGH;
    }

    @Override
    public void registerSettings() {

    }

    @Override
    public void load() {
        logger.info("Loading bans from file...");
        try {
            if (BAN_FILE.createNewFile()) {
                new FileOutputStream(BAN_FILE).write("[]".getBytes(StandardCharsets.UTF_8));
                BANS = new JsonArray();
            } else {
                BANS = new Gson().fromJson(new String(new FileInputStream(BAN_FILE).readAllBytes()), JsonArray.class);
            }
        } catch (IOException e) {
            logger.error("Failed to write bans.json!");
            e.printStackTrace();
        }
        logger.info("Registering listeners...");
        Bot.jda.addEventListener(new BansListener());
    }

    @Override
    public CommandData[] getCommands() {
        return new CommandData[0];
    }

    /**
     * Attempts to ban the user, then adds them to the persistent ban list
     * @param userId The user ID of the user to ban
     */
    public static void ban(String userId) {
        User user = Bot.jda.getUserById(userId);
        if (user != null)
            Bot.guild.ban(user, 0).queue();
        if (!BANS.contains(new JsonPrimitive(userId))) {
            BANS.add(new JsonPrimitive(userId));
            save();
        }
    }

    public static boolean isBanned(String userId) {
        return BANS.contains(new JsonPrimitive(userId));
    }

    /**
     * Save the bans to file
     */
    public static void save() {
        try (Writer writer = new FileWriter(BAN_FILE)) {
            Gson gson = new GsonBuilder().create();
            gson.toJson(BANS, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
