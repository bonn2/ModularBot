package net.bonn2.modules.basic;

import net.bonn2.Bot;
import net.bonn2.modules.Module;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Basic extends Module {

    @Override
    public String getName() {
        return "Basic";
    }

    @Override
    public String getVersion() {
        return "v1.0";
    }

    @Override
    public void registerLoggingChannels() {}

    @Override
    public void registerSettings() {}

    @Override
    public void load() {
        Bot.logger.info("Registering Listeners...");
        Bot.jda.addEventListener(new BasicCommands());
        Bot.jda.addEventListener(new BasicTabComplete());
    }

    @Override
    public CommandData[] getCommands() {
        return new CommandData[] {
                Commands.slash(
                        "ping",
                        "Get the ping of the bot."
                ),
                Commands.slash(
                        "modules",
                        "Get information about modules."
                ).addSubcommands(
                        new SubcommandData(
                                "list",
                                "List all of the modules, and their versions."
                        )
                ).addSubcommands(
                        new SubcommandData(
                                "docs",
                                "Get detailed information about a module."
                        ).addOption(
                                OptionType.STRING,
                                "module",
                                "The module to get the information of.",
                                true,
                                true
                        )
                )
        };
    }
}
