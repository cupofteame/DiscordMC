package com.cupoftea.discordmc.discord;

import java.util.UUID;

import org.bukkit.Bukkit;

import com.cupoftea.discordmc.DiscordMCPlugin;
import com.cupoftea.discordmc.commands.DiscordMCCommands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.RoleAction;

public class DiscordListener extends ListenerAdapter {
    private final DiscordMCPlugin plugin;
    private final DiscordManager discordManager;
    private final DiscordMCCommands discordMCCommands;

    public DiscordListener(DiscordMCPlugin plugin, DiscordManager discordManager, DiscordMCCommands discordMCCommands) {
        this.plugin = plugin;
        this.discordManager = discordManager;
        this.discordMCCommands = discordMCCommands;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.getChannel().getId().equals(discordManager.getDiscordChannelId())) return;

        String author = event.getAuthor().getName();
        String content = event.getMessage().getContentDisplay();

        if (!content.trim().isEmpty()) {
            String formattedMessage = String.format("[Discord] <%s> %s", author, content);

            Bukkit.getScheduler().runTask(plugin, () -> {
                Bukkit.broadcastMessage(formattedMessage);
            });

            plugin.getLogger().info("Received Discord message: " + formattedMessage);
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("link")) {
            String minecraftUsername = event.getOption("minecraft_username").getAsString();
            String discordId = event.getUser().getId();
            
            UUID existingUUID = plugin.getMongoDBManager().getMinecraftUUID(discordId);
            if (existingUUID != null) {
                String existingUsername = plugin.getMongoDBManager().getMinecraftUsername(discordId);
                String message = plugin.getConfigManager().getMessage("discord.link-already-linked")
                    .replace("{username}", existingUsername);
                event.reply(message).setEphemeral(true).queue();
                return;
            }
            
            String existingDiscordId = plugin.getMongoDBManager().getDiscordIdByUsername(minecraftUsername);
            if (existingDiscordId != null) {
                String message = plugin.getConfigManager().getMessage("discord.link-minecraft-already-linked")
                    .replace("{username}", minecraftUsername);
                event.reply(message).setEphemeral(true).queue();
                return;
            }
            
            String linkCode = discordMCCommands.generateLinkCode(discordId, minecraftUsername);
            String message = plugin.getConfigManager().getMessage("discord.link-command-response")
                .replace("{username}", minecraftUsername)
                .replace("{code}", linkCode);
            event.reply(message).setEphemeral(true).queue();
        }
    }

    public void registerCommands(JDA jda) {
        jda.updateCommands().addCommands(
            Commands.slash("link", "Link your Discord account to Minecraft")
                .addOption(OptionType.STRING, "minecraft_username", "Your Minecraft username", true)
        ).queue();
    }

    public void assignLinkedRole(String discordId) {
        String roleId = plugin.getConfigManager().getLinkedAccountRoleId();
        JDA jda = discordManager.getJda();
        Guild guild = jda.getGuilds().stream().findFirst().orElse(null);
        if (guild == null) {
            plugin.getLogger().warning("Failed to assign role: Bot is not in any guild");
            return;
        }

        Role role;
        if (roleId.isEmpty()) {
            role = getOrCreateLinkedRole(guild);
        } else {
            role = guild.getRoleById(roleId);
            if (role == null) {
                plugin.getLogger().warning("Failed to assign role: Role with ID '" + roleId + "' not found");
                return;
            }
        }

        guild.addRoleToMember(UserSnowflake.fromId(discordId), role).queue(
            success -> plugin.getLogger().info("Successfully assigned role to user " + discordId),
            error -> plugin.getLogger().warning("Failed to assign role to user " + discordId + ": " + error.getMessage())
        );
    }

    public void removeLinkedRole(String discordId) {
        String roleId = plugin.getConfigManager().getLinkedAccountRoleId();
        JDA jda = discordManager.getJda();
        Guild guild = jda.getGuilds().stream().findFirst().orElse(null);
        if (guild == null) {
            plugin.getLogger().warning("Failed to remove role: Bot is not in any guild");
            return;
        }

        Role role;
        if (roleId.isEmpty()) {
            role = guild.getRolesByName("Account Linked", true).stream().findFirst().orElse(null);
        } else {
            role = guild.getRoleById(roleId);
        }

        if (role == null) {
            plugin.getLogger().warning("Failed to remove role: Role not found");
            return;
        }

        guild.removeRoleFromMember(UserSnowflake.fromId(discordId), role).queue(
            success -> plugin.getLogger().info("Removed linked role from user " + discordId),
            error -> plugin.getLogger().warning("Failed to remove role from user " + discordId + ": " + error.getMessage())
        );
    }

    private Role getOrCreateLinkedRole(Guild guild) {
        Role existingRole = guild.getRolesByName("Account Linked", true).stream().findFirst().orElse(null);
        if (existingRole != null) {
            return existingRole;
        }

        RoleAction roleAction = guild.createRole()
            .setName("Account Linked")
            .setColor(java.awt.Color.GREEN)
            .setHoisted(false)
            .setMentionable(false);

        try {
            Role newRole = roleAction.complete();
            plugin.getLogger().info("Created new 'Account Linked' role with ID: " + newRole.getId());
            return newRole;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create 'Account Linked' role: " + e.getMessage());
            return null;
        }
    }
}