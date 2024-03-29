package net.bonn2.modules.config;

import net.bonn2.Bot;
import net.bonn2.modules.Module;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Config extends Module {

    static JsonObject CONFIG;

    @Override
    public String getName() {
        return "Config";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public void registerLoggingChannels() {}

    @Override
    public void registerSettings() {}

    /**
     * Load the config from file into ram.
     * Write a new config to disk if no config exists.
     */
    @Override
    public void load() {
        Bot.logger.info("Loading config...");
        try {
            File configFile = new File(Bot.localPath + "config.json");
            if (configFile.createNewFile()) {
                String defaultConfig = new String(Objects.requireNonNull(Bot.class.getClassLoader().getResourceAsStream("config.json")).readAllBytes());
                new FileOutputStream(configFile).write(defaultConfig.getBytes(StandardCharsets.UTF_8));
                CONFIG = new Gson().fromJson(defaultConfig, JsonObject.class);
            } else {
                CONFIG = new Gson().fromJson(new String(new FileInputStream(configFile).readAllBytes()), JsonObject.class);
            }
        } catch (IOException e) {
            System.out.println("Failed to save default config!");
            e.printStackTrace();
        }
    }

    @Override
    public CommandData[] getCommands() {
        return new CommandData[0];
    }

    /**
     * Get an item from the config
     * @param key The string key of the config item
     * @return A JsonElement containing the config item
     */
    public static @NotNull JsonElement get(@NotNull String key) {
        return CONFIG.get(key.toUpperCase());
    }

    /**
     * Get a list item from the config
     * @param key The string key of the config item
     * @return A List of Strings containing the list items
     */
    public static @NotNull List<String> getList(@NotNull String key) {
        List<String> output = new LinkedList<>();
        for (JsonElement element : Config.get(key).getAsJsonArray()) {
            output.add(element.getAsString());
        }
        return output;
    }
}
