package com.bonn2.modules.core.basic;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import static com.bonn2.Bot.commands;
import static com.bonn2.Bot.logger;

public class Basic extends Module {

    /*
    This module will contain basic commands for controlling the bot, ex:
    /ping
    /modules list
    /modules docs <module>
    */

    public Basic() {
        name = "Basic";
        version = "v1.0";
        priority = Priority.POST_JDA_LOW;
    }

    @Override
    public void registerSettings() {

    }

    @Override
    public void load() {
        logger.info("Registering Listeners...");
        Bot.jda.addEventListener(new BasicListener());
        logger.info("Creating commands...");
        commands = commands.addCommands(
                Commands.slash(
                        "ping",
                        "Get the ping of the bot."
                )
        );
    }
}
