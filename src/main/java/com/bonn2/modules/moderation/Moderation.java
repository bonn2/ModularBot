package com.bonn2.modules.moderation;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

import static com.bonn2.Bot.logger;

public class Moderation extends Module {

    public Moderation() {
        version = "v1.0";
        priority = Priority.POST_JDA_LOW;
        name = "Moderation";
    }

    @Override
    public void registerSettings() {
        Settings.register(this, "mod_max_purge", Setting.Type.INT, String.valueOf(20),
                "The maximum number of messages that can be purged by moderators.");
    }

    @Override
    public void load() {
        logger.info("Registering listeners...");
        Bot.jda.addEventListener(new ModerationListener(this));
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
                )/*.addSubcommandGroups(
                        new SubcommandGroupData(
                                "banned_words",
                                "Ban words."
                        ).addSubcommands(
                                new SubcommandData(
                                        "add",
                                        "Ban a word."
                                ),
                                new SubcommandData(

                                )
                        )
                )*/
        };
    }
}
