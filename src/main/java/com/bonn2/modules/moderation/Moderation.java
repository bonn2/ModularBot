package com.bonn2.modules.moderation;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;
import com.bonn2.modules.faq.FAQ;
import com.google.gson.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import static com.bonn2.Bot.logger;

public class Moderation extends Module {

    public Moderation() {
        version = "v1.0";
        priority = Priority.POST_JDA_LOW;
        name = "Moderation";
    }

    static File BANNED_WORDS_FILE;
    static List<String> BANNED_WORDS = new LinkedList<>();

    @Override
    public void registerSettings() {
        Settings.register(this, "mod_max_purge", Setting.Type.INT, String.valueOf(20),
                "The maximum number of messages that can be purged by moderators.");
    }

    @Override
    public void load() {
        logger.info("Loading Saved Data...");
        try {
            BANNED_WORDS_FILE = new File(Bot.localPath + "banned-words.json");
            if (BANNED_WORDS_FILE.createNewFile()) {
                new FileOutputStream(BANNED_WORDS_FILE).write("[]".getBytes(StandardCharsets.UTF_8));
            } else {
                JsonArray jsonArray = new Gson().fromJson(new String(new FileInputStream(BANNED_WORDS_FILE).readAllBytes()), JsonArray.class);
                for (JsonElement jsonElement : jsonArray)
                    BANNED_WORDS.add(jsonElement.getAsString());
            }
        } catch (IOException e) {
            logger.error("Failed to save default file!");
            e.printStackTrace();
        }
        logger.info("Registering listeners...");
        Bot.jda.addEventListener(new ModerationListener(this));
    }

    @Override
    public CommandData[] getCommands() {
        return new CommandData[] {
                Commands.slash(
                        "mod",
                        "Moderation commands"
                ).addSubcommands(
                        new SubcommandData(
                                "purge",
                                "Purge messages from a channel"
                        ).addOption(
                                OptionType.INTEGER,
                                "amount",
                                "The number of messages to purge.",
                                true
                        )
                ).addSubcommandGroups(
                        new SubcommandGroupData(
                                "banned_words",
                                "Ban words."
                        ).addSubcommands(
                                new SubcommandData(
                                        "add",
                                        "Ban a word."
                                ).addOption(
                                        OptionType.STRING,
                                        "word",
                                        "The word to ban.",
                                        true
                                ),
                                new SubcommandData(
                                        "list",
                                        "List all banned words."
                                ),
                                new SubcommandData(
                                        "remove",
                                        "Unban a word."
                                ).addOption(
                                        OptionType.STRING,
                                        "word",
                                        "The word to unban.",
                                        true
                                )
                        )
                )
        };
    }

    public void saveBannedWords() {
        logger.info("Saving banned words to file...");
        try {
            JsonArray jsonArray = new JsonArray(BANNED_WORDS.size());
            for (String word : BANNED_WORDS)
                jsonArray.add(word);
            //noinspection ResultOfMethodCallIgnored
            BANNED_WORDS_FILE.createNewFile();

            new FileOutputStream(BANNED_WORDS_FILE).write(
                    new GsonBuilder()
                            .setPrettyPrinting()
                            .create()
                            .toJson(jsonArray)
                            .getBytes(StandardCharsets.UTF_8)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
