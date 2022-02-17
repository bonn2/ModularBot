package com.bonn2.modules.moderation;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import static com.bonn2.Bot.commands;
import static com.bonn2.Bot.logger;

public class Moderation extends Module {

    public Moderation() {
        version = "v1.0";
        priority = Priority.POST_JDA_LOW;
        name = "Moderation";
    }

    @Override
    public void registerSettings() {

    }

    @Override
    public void load() {
        logger.info("Registering listeners...");
        Bot.jda.addEventListener(new ModerationListener());
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
                )
        };
    }
}
