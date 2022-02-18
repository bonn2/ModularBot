package com.bonn2.modules.faq;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.bonn2.modules.faq.FAQ.QUESTIONS;

public class FAQTabComplete extends ListenerAdapter {

    public void onCommandAutoCompleteInteraction(@Nonnull CommandAutoCompleteInteractionEvent event) {
        List<Command.Choice> choices = new ArrayList<>();
        for (FAQ.Question question : QUESTIONS) {
            if (question.name.toLowerCase(Locale.ROOT).startsWith(event.getOption("name").getAsString().toLowerCase(Locale.ROOT))) {
                choices.add(new Command.Choice(
                        question.name,
                        question.name
                ));
            }
        }
        event.replyChoices(choices).queue();
    }
}
