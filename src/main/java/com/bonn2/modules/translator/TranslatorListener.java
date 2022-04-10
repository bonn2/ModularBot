package com.bonn2.modules.translator;

import com.bonn2.modules.core.settings.Settings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class TranslatorListener extends ListenerAdapter {

    static final String API_URL = "https://api-free.deepl.com/v2/translate?auth_key=!key&target_lang=!lang&text=!text";
    final Translator module;

    public TranslatorListener(Translator module) {
        this.module = module;
    }

    @Override
    public void onMessageContextInteraction(@Nonnull MessageContextInteractionEvent event) {
        long startTime = System.currentTimeMillis();
        try {
            URL url = new URL(API_URL
                    .replaceAll("!key", Settings.get(module, "deepl_key").getAsString())
                    .replaceAll("!lang", "EN")
                    .replaceAll("!text", URLEncoder.encode(event.getInteraction().getTarget().getContentDisplay(), StandardCharsets.UTF_8)));
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            if (conn.getResponseCode() != 200) {
                System.out.println(conn.getResponseMessage());
                return;
            }
            JsonObject response = new Gson().fromJson(new String(url.openStream().readAllBytes()), JsonObject.class);
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setAuthor(
                    event.getInteraction().getTarget().getAuthor().getName(),
                    null,
                    event.getInteraction().getTarget().getAuthor().getAvatarUrl()
            );
            embedBuilder.setDescription(
                    ":flag_%s: ".formatted(
                            response.get("translations").getAsJsonArray()
                                    .get(0).getAsJsonObject()
                                    .get("detected_source_language").getAsString().toLowerCase(Locale.ROOT)
                    )
                    + response.get("translations").getAsJsonArray()
                            .get(0).getAsJsonObject()
                            .get("text").getAsString()
                    + "\n\n[See Original](%s)".formatted(
                            event.getInteraction().getTarget().getJumpUrl()
                    )
            );
            embedBuilder.setFooter(
                    "Translation by DeepL | %s seconds".formatted(
                            ((float)(System.currentTimeMillis() - startTime)) / 1000
                    ),
                    "https://static.deepl.com/img/favicon/tile_144.png");
            event.replyEmbeds(embedBuilder.build()).queue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
