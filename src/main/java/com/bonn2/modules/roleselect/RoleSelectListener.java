package com.bonn2.modules.roleselect;

import com.bonn2.Bot;
import com.bonn2.modules.core.permissions.Permissions;
import com.bonn2.modules.core.settings.Settings;
import com.bonn2.modules.core.settings.types.RoleListSetting;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RoleSelectListener extends ListenerAdapter {

    final RoleSelect module;

    public RoleSelectListener(RoleSelect module) {
        this.module = module;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        // Check permissions
        if (!event.getName().equals("roleselect")) return;
        if (!Permissions.hasPermissionReply(event, Permissions.Level.ADMIN)) return;

        // Validate min/max
        if (event.getOption("min_selection").getAsLong() < 0) {
            event.reply("Your minimum cannot be below 0!").setEphemeral(true).queue();
            return;
        }
        if (event.getOption("max_selection").getAsLong() < event.getOption("min_selection").getAsLong()) {
            event.reply("You maximum cannot be less than your minimum!").setEphemeral(true).queue();
            return;
        }

        // Create embed
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(event.getOption("title").getAsString());
        embedBuilder.setDescription(event.getOption("description").getAsString().replaceAll("\\\\n", "\n"));
        embedBuilder.setColor(Color.CYAN);

        // Create selection menu
        SelectMenu.Builder selectionBuilder = SelectMenu.create("roleselect")
                .setMinValues((int) event.getOption("min_selection").getAsLong())
                .setMaxValues(
                        event.getOption("max_selection").getAsLong() > 25 ?
                                25 : (int) event.getOption("max_selection").getAsLong()
                )
                .setPlaceholder(event.getOption("placeholder").getAsString());

        // List of added roles (needed for security)
        List<Role> roles = new ArrayList<>();
        // Add roles
        for (int i = 1; i <= RoleSelect.max_selections; i++) {
            OptionMapping option = event.getOption("role_" + i);
            if (option == null) continue;
            Role role = option.getAsRole();
            roles.add(role);
            selectionBuilder.addOption(role.getName(), "role_select_" + role.getId());
        }

        // Add added roles to Settings (to prevent spoofing)
        List<Role> savedRoles = Settings.get(module, "allowed_roles").getAsRoleList();
        for (Role role : roles)
            if (!savedRoles.contains(role))
                savedRoles.add(role);
        Settings.set(module, "allowed_roles", new RoleListSetting(savedRoles));

        // Build final message
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.setEmbeds(embedBuilder.build());
        messageBuilder.setActionRows(ActionRow.of(selectionBuilder.build()));

        // Send message
        event.getTextChannel().sendMessage(messageBuilder.build()).queue();

        // Confirm
        event.reply("Posted!").setEphemeral(true).queue();
    }

    @Override
    public void onSelectMenuInteraction(@Nonnull SelectMenuInteractionEvent event) {
        if (!event.getComponentId().equals("roleselect")) return;
        event.deferReply(true).queue();
        Member member = event.getMember();
        assert member != null;
        List<Role> addedRoles = new ArrayList<>();
        // Add selected roles
        for (String value : event.getValues()) {
            // Only add allowed roles
            String id = value.split("_")[2];
            if (!Settings.get(module, "allowed_roles").getAsRoleIdList().contains(id)) continue;
            // Get and add role
            Role role = Bot.guild.getRoleById(id);
            if (role == null) continue;
            addedRoles.add(role);
            Bot.guild.addRoleToMember(member, role).queue();
        }
        // Remove deselected roles
        for (SelectOption option : event.getComponent().getOptions()) {
            // Don't remove selected roles
            if (event.getValues().contains(option.getValue())) continue;
            // Only remove allowed roles
            String id = option.getValue().split("_")[2];
            if (!Settings.get(module, "allowed_roles").getAsRoleIdList().contains(id)) continue;
            // Get and remove role
            Role role = Bot.guild.getRoleById(id);
            if (role == null) continue;
            Bot.guild.removeRoleFromMember(member, role).queue();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Roles successfully updated!\n" +
                             "Your new roles are: ");
        for (Role role : addedRoles)
            stringBuilder.append("<@&%s> ".formatted(role.getId()));

        event.getHook().editOriginal(stringBuilder.toString()).queue();
    }

    @Override
    public void onGuildMemberJoin(@Nonnull GuildMemberJoinEvent event) {
        List<Role> roles = Settings.get(module, "default_roles").getAsRoleList();
        for (Role role : roles) {
            event.getGuild().addRoleToMember(event.getMember(), role).queue();
        }
    }
}
