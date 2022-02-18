package com.bonn2.modules.faq;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;
import com.google.gson.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import static com.bonn2.Bot.logger;

public class FAQ extends Module {

    public FAQ() {
        name = "FAQ";
        version = "v1.0";
        priority = Priority.POST_JDA_LOW;
    }

    public static class Question {
        public String name;
        public String title;
        public String description;

        public Question(@NotNull JsonObject jsonObject) {
            name = jsonObject.get("name").getAsString();
            title = jsonObject.get("title").getAsString();
            description = jsonObject.get("description").getAsString();
        }

        public Question(String name, String title, String description) {
            this.name = name;
            this.title = title;
            this.description = description;
        }

        public @NotNull JsonObject toJsonObject() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", name);
            jsonObject.addProperty("title", title);
            jsonObject.addProperty("description", description);
            return jsonObject;
        }
    }

    static File FAQ_FILE;
    static List<Question> QUESTIONS = new LinkedList<>();

    @Override
    public void registerSettings() {
        Settings.register(this, "max_replies", Setting.Type.INT, String.valueOf(3),
                "The maximum number of FAQ responses per message");
    }

    @Override
    public void load() {
        logger.info("Loading Saved Data...");
        try {
            FAQ_FILE = new File(Bot.localPath + "faq.json");
            if (FAQ_FILE.createNewFile()) {
                new FileOutputStream(FAQ_FILE).write("[]".getBytes(StandardCharsets.UTF_8));
            } else {
                JsonArray jsonArray = new Gson().fromJson(new String(new FileInputStream(FAQ_FILE).readAllBytes()), JsonArray.class);
                for (JsonElement jsonElement : jsonArray)
                    QUESTIONS.add(new Question((JsonObject) jsonElement));
            }
        } catch (IOException e) {
            logger.error("Failed to save default file!");
            e.printStackTrace();
        }
        logger.info("Registering Listener...");
        Bot.jda.addEventListener(new FAQListener(this));
        Bot.jda.addEventListener(new FAQTabComplete());
    }

    @Override
    public CommandData[] getCommands() {
        SubcommandGroupData entries = new SubcommandGroupData(
                "read",
                "Read the FAQ."
        );
        for (Question question : QUESTIONS) {
            entries.addSubcommands(
                    new SubcommandData(
                            question.name,
                            "Run to get the answer."
                    )
            );
        }
        return new CommandData[] {
                Commands.slash(
                        "faq",
                        "The server faq."
                ).addSubcommandGroups(
                        entries,
                        new SubcommandGroupData(
                                "edit",
                                "Edit the faq."
                        ).addSubcommands(
                                new SubcommandData(
                                        "new",
                                        "Create a new FAQ entry."
                                ).addOption(
                                        OptionType.STRING,
                                        "name",
                                        "The name of the faq entry. Also used as !name",
                                        true
                                ).addOption(
                                        OptionType.STRING,
                                        "title",
                                        "The title of the FAQ embed.",
                                        true
                                ).addOption(
                                        OptionType.STRING,
                                        "description",
                                        "The description of the FAQ embed. (You can use \\n for a new line)",
                                        true
                                ),
                                new SubcommandData(
                                        "list",
                                        "List all FAQ entries."
                                ),
                                new SubcommandData(
                                        "delete",
                                        "Delete a FAQ entry."
                                ).addOption(
                                        OptionType.STRING,
                                        "name",
                                        "The name of the FAQ to delete.",
                                        true,
                                        true
                                )
                        )
                )
        };
    }

    /**
     * Save all questions to file
     */
    public static void save() {
        logger.info("Saving FAQs to file...");
        try {
            JsonArray jsonArray = new JsonArray(QUESTIONS.size());
            for (Question question : QUESTIONS)
                jsonArray.add(question.toJsonObject());
            //noinspection ResultOfMethodCallIgnored
            FAQ_FILE.createNewFile();

            new FileOutputStream(FAQ_FILE).write(
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
