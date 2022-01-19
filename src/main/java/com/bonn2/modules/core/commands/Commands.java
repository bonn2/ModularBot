package com.bonn2.modules.core.commands;

import com.vectorio.Bot;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.LinkedList;
import java.util.List;

public class Commands extends Module {

    private static final List<CommandData> commands = new LinkedList<>();

    @Override
    public void load() {
        System.out.println("Loading commands module...");
        version = "v1.0";
    }

    /**
     * Add a new command to the list of commands
     * @param commandData The command to add
     */
    public static void addCommand(CommandData commandData) {
        commands.add(commandData);
    }

    /**
     * Tell bot to send the commands to Discord
     */
    public static void updateCommands() {
        System.out.println("Updating guild commands...");
        Bot.guild.updateCommands().addCommands(commands).complete();
    }
}
