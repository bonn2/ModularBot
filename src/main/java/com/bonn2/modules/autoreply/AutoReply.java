package com.bonn2.modules.autoreply;

import com.google.gson.*;
import com.vectorio.Bot;
import com.vectorio.modules.core.commands.Commands;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class AutoReply extends Module {

    /**
     * Data holder for Auto replies
     */
    public static class Data {
        public String[] words;
        public String reply;
        public long lastSent = 0;

        public Data(@NotNull JsonObject jsonObject) {
            JsonArray jsonArray = jsonObject.get("words").getAsJsonArray();
            words = new String[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++)
                words[i] = jsonArray.get(i).getAsString().toLowerCase();
            reply = jsonObject.get("reply").getAsString();
        }

        public Data(String @NotNull [] words) {
            this.words = new String[words.length];
            for (int i = 0; i < words.length; i++)
                this.words[i] = words[i].toLowerCase();
            reply = "";
        }

        public @NotNull JsonObject toJsonObject() {
            JsonObject jsonObject = new JsonObject();
            JsonArray wordsJson = new JsonArray();
            for (String word : words)
                wordsJson.add(new JsonPrimitive(word));
            jsonObject.add("words", wordsJson);
            jsonObject.addProperty("reply", reply);
            return jsonObject;
        }
    }

    static File AUTO_REPLY_FILE;
    static List<Data> AUTO_REPLIES = new LinkedList<>();

    @Override
    public void load() {
        version = "v1.1";
        System.out.println("Loading auto reply module " + version + "...");

        System.out.println("- Loading Saved Data...");
        try {
            AUTO_REPLY_FILE = new File(Bot.localPath + "auto-replies.json");
            if (AUTO_REPLY_FILE.createNewFile()) {
                String defaultFile = "[]";
                new FileOutputStream(AUTO_REPLY_FILE).write(defaultFile.getBytes(StandardCharsets.UTF_8));
            } else {
                JsonArray jsonArray = new Gson().fromJson(new String(new FileInputStream(AUTO_REPLY_FILE).readAllBytes()), JsonArray.class);
                for (JsonElement jsonElement : jsonArray)
                    AUTO_REPLIES.add(new Data((JsonObject) jsonElement));
            }
        } catch (IOException e) {
            System.out.println("Failed to save default file!");
            e.printStackTrace();
        }

        System.out.println("- Creating commands...");
        Commands.addCommand(
                new CommandData(
                        "autoreply",
                        "Manage auto-replies"
                ).addSubcommands(
                        new SubcommandData(
                                "create",
                                "Create a new auto-reply."
                        ).addOption(
                                OptionType.STRING,
                                "words",
                                "The words to look for, (Will only reply if all are present)",
                                true
                        ),
                        new SubcommandData(
                                "list",
                                "View all active auto-replies."
                        ),
                        new SubcommandData(
                                "delete",
                                "Delete an auto-reply."
                        ).addOption(
                                OptionType.INTEGER,
                                "id",
                                "The id of the auto-reply to delete.",
                                true
                        )
                ));

        System.out.println("- Registering Listeners...");
        Bot.jda.addEventListener(new AutoReplyListener());
    }

    public static void save() {
        System.out.println("Saving auto replies to file...");
        try {
            JsonArray jsonArray = new JsonArray(AUTO_REPLIES.size());
            for (Data data : AUTO_REPLIES)
                jsonArray.add(data.toJsonObject());
            //noinspection ResultOfMethodCallIgnored
            AUTO_REPLY_FILE.createNewFile();

            new FileOutputStream(AUTO_REPLY_FILE).write(
                    new GsonBuilder()
                            .setPrettyPrinting()
                            .create()
                            .toJson(jsonArray)
                            .getBytes(StandardCharsets.UTF_8
                            )
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
