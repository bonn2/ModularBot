package net.bonn2.modules.basic;

import net.bonn2.Bot;
import net.bonn2.modules.Module;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class BasicTabComplete extends ListenerAdapter {

    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        // Only reply to /modules
        if (!event.getName().equals("modules")) return;

        List<Command.Choice> choices = new ArrayList<>();
        for (Module module : Bot.modules)
            if (module.getName().toLowerCase(Locale.ROOT).startsWith(Objects.requireNonNull(event.getOption("module")).getAsString().toLowerCase(Locale.ROOT)))
                choices.add(new Command.Choice(module.getName(), module.getName()));
        event.replyChoices(choices).queue();
    }
}
