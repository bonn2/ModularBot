package net.bonn2.modules.basic;

import net.bonn2.Bot;
import net.bonn2.modules.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

public class BasicCommands extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "ping" -> {
                long time = System.currentTimeMillis();
                event.reply("Pong!").setEphemeral(true) // Initial reply
                        .flatMap(v ->
                                // Edit reply with amount of time previous message took
                                event.getHook().editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time)
                        ).queue(); // Queue both
            }
            case "modules" -> {
                switch (Objects.requireNonNull(event.getSubcommandName())) {
                    case "list" -> {
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setTitle("Modules");
                        embedBuilder.setColor(Color.CYAN);
                        for (Module module : Bot.modules) {
                            embedBuilder.addField(
                                    module.getName(),
                                    "Version: %s".formatted(module.getVersion()),
                                    true
                            );
                        }
                        event.replyEmbeds(embedBuilder.build()).queue();
                    }
                    case "docs" -> {
                        // TODO: 2/12/2022 Get the docs for the stuff and things, yeah
                        event.reply("This will be a thing in the future").setEphemeral(true).queue();
                    }
                }
            }
        }
    }
}
