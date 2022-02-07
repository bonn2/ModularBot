package com.bonn2.modules.roleselect;

import com.bonn2.Bot;
import com.bonn2.modules.Module;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.Setting;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import static com.bonn2.Bot.commands;
import static com.bonn2.Bot.logger;

public class RoleSelect extends Module {

    public static final int max_selections = 25;

    public RoleSelect() {
        version = "v1.0";
        priority = Priority.POST_JDA_LOW;
        name = "RoleSelect";
    }

    @Override
    public void registerSettings() {
        Settings.register(this, "allowed_roles", Setting.Type.ROLE_LIST, Setting.Type.ROLE_LIST.unset);
    }

    @Override
    public void load() {
        logger.info("Registering listeners...");
        Bot.jda.addEventListener(new RoleSelectListener(this));
        logger.info("Creating commands...");
        commands = commands.addCommands(
                Commands.slash(
                        "roleselect",
                        "Manage RoleSelect"
                ).addOption(
                        OptionType.STRING,
                        "title",
                        "The title of the embed.",
                        true
                ).addOption(
                        OptionType.STRING,
                        "description",
                        "The description of the embed.",
                        true
                ).addOption(
                        OptionType.INTEGER,
                        "min_selection",
                        "The minimum number of roles a user can select.",
                        true
                ).addOption(
                        OptionType.INTEGER,
                        "max_selection",
                        "The maximum number of roles a user can select.",
                        true
                ).addOption(
                        OptionType.STRING,
                        "placeholder",
                        "What the selection will show when nothing is selected.",
                        true
                ).addOption(
                        OptionType.ROLE,
                        "role_1",
                        "The first role.",
                        true
                ).addOption(
                        OptionType.ROLE,
                        "role_2",
                        "The second role.",
                        false
                ).addOption(
                        OptionType.ROLE,
                        "role_3",
                        "The third role.",
                        false
                ).addOption(
                        OptionType.ROLE,
                        "role_4",
                        "The fourth role.",
                        false
                ).addOption(
                        OptionType.ROLE,
                        "role_5",
                        "The fifth role.",
                        false
                ).addOption(
                        OptionType.ROLE,
                        "role_6",
                        "The sixth role.",
                        false
                ).addOption(
                        OptionType.ROLE,
                        "role_7",
                        "The seventh role.",
                        false
                ).addOption(
                        OptionType.ROLE,
                        "role_8",
                        "The eighth role.",
                        false
                ).addOption(
                        OptionType.ROLE,
                        "role_9",
                        "The ninth role.",
                        false
                ).addOption(
                        OptionType.ROLE,
                        "role_10",
                        "The tenth role.",
                        false
                ).addOption(
                        OptionType.ROLE,
                        "role_11",
                        "The eleventh role.",
                        false
                ).addOption(
                        OptionType.ROLE,
                        "role_12",
                        "The twelfth role.",
                        false
                ).addOption(
                        OptionType.ROLE,
                        "role_13",
                        "The thirteenth role.",
                        false
                ).addOption(
                        OptionType.ROLE,
                        "role_14",
                        "The fourteenth role.",
                        false
                ).addOption(
                        OptionType.ROLE,
                        "role_15",
                        "The fifteenth role",
                        false
                ).addOption(
                        OptionType.ROLE,
                        "role_16",
                        "The sixteenth role",
                        false
                ).addOption(
                        OptionType.ROLE,
                        "role_17",
                        "The seventeenth role.",
                        false
                ).addOption(
                        OptionType.ROLE,
                        "role_18",
                        "The eighteenth role.",
                        false
                ).addOption(
                        OptionType.ROLE,
                        "role_19",
                        "The nineteenth role.",
                        false
                ).addOption(
                        OptionType.ROLE,
                        "role_20",
                        "The twentieth role.",
                        false
                )
        );
    }
}
