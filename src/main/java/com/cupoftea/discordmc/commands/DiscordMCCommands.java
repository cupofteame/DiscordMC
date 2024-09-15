package com.cupoftea.discordmc.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.cupoftea.discordmc.DiscordMCPlugin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;

@CommandAlias("discordmc|dmc")
public class DiscordMCCommands extends BaseCommand {

    private final DiscordMCPlugin plugin;
    private final Map<String, PendingLink> pendingLinks = new HashMap<>();

    public DiscordMCCommands(DiscordMCPlugin plugin) {
        this.plugin = plugin;
    }

    @Default
    @Description("Shows DiscordMC commands")
    public void onDefault(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== DiscordMC Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/discordmc" + ChatColor.WHITE + " - Show this help message");
        sender.sendMessage(ChatColor.YELLOW + "/discordmc reload" + ChatColor.WHITE + " - Reload the plugin configuration");
        sender.sendMessage(ChatColor.YELLOW + "/discordmc status" + ChatColor.WHITE + " - Check the Discord connection status");
        sender.sendMessage(ChatColor.YELLOW + "/link <code>" + ChatColor.WHITE + " - Link your Minecraft account to Discord");
        sender.sendMessage(ChatColor.YELLOW + "/discord" + ChatColor.WHITE + " - Show your linked Discord account");
    }

    @Subcommand("reload")
    @Description("Reloads the DiscordMC configuration")
    @CommandPermission("discordmc.reload")
    public void onReload(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Reloading DiscordMC configuration...");
        if (plugin.reload()) {
            sender.sendMessage(ChatColor.GREEN + "DiscordMC configuration reloaded successfully!");
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to reload DiscordMC configuration. Check console for details.");
        }
    }

    @Subcommand("status")
    @Description("Checks the status of the Discord connection")
    @CommandPermission("discordmc.status")
    public void onStatus(CommandSender sender) {
        boolean isConnected = plugin.getDiscordManager().isConnected();
        sender.sendMessage(ChatColor.YELLOW + "Discord connection status: " + 
                           (isConnected ? ChatColor.GREEN + "Connected" : ChatColor.RED + "Disconnected"));
    }

    @CommandAlias("link")
    @Description("Link your Minecraft account to Discord")
    public void onLink(Player player, String code) {
        PendingLink pendingLink = pendingLinks.remove(code);
        if (pendingLink != null) {
            if (!player.getName().equalsIgnoreCase(pendingLink.minecraftUsername)) {
                player.sendMessage(ChatColor.RED + "This link code is for a different Minecraft username.");
                return;
            }

            String discordId = pendingLink.discordId;
            String existingLink = plugin.getMongoDBManager().getDiscordId(player.getUniqueId());
            if (existingLink != null) {
                player.sendMessage(ChatColor.RED + "Your Minecraft account is already linked to a Discord account.");
                return;
            }
            
            UUID existingMinecraftUUID = plugin.getMongoDBManager().getMinecraftUUID(discordId);
            if (existingMinecraftUUID != null) {
                player.sendMessage(ChatColor.RED + "This Discord account is already linked to a different Minecraft account.");
                return;
            }
            
            plugin.getMongoDBManager().linkAccount(player.getUniqueId(), player.getName(), discordId);
            player.sendMessage(ChatColor.GREEN + "Your Minecraft account has been successfully linked to Discord!");
            plugin.getDiscordManager().getDiscordListener().assignLinkedRole(discordId);
        } else {
            player.sendMessage(ChatColor.RED + "Invalid or expired link code. Please generate a new one on Discord.");
        }
    }

    @CommandAlias("discord")
    @Description("Show your linked Discord account")
    public void onDiscord(Player player) {
        String discordId = plugin.getMongoDBManager().getDiscordId(player.getUniqueId());
        if (discordId != null) {
            String discordUsername = plugin.getDiscordManager().getDiscordUsername(discordId);
            if (discordUsername != null) {
                player.sendMessage(ChatColor.GREEN + "Your Minecraft account is linked to Discord user: " + discordUsername);
            } else {
                player.sendMessage(ChatColor.RED + "Failed to retrieve Discord username. Please try again later.");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Your Minecraft account is not linked to a Discord account.");
        }
    }

    public String generateLinkCode(String discordId, String minecraftUsername) {
        String code = UUID.randomUUID().toString().substring(0, 6);
        pendingLinks.put(code, new PendingLink(discordId, minecraftUsername));
        return code;
    }

    private static class PendingLink {
        final String discordId;
        final String minecraftUsername;

        PendingLink(String discordId, String minecraftUsername) {
            this.discordId = discordId;
            this.minecraftUsername = minecraftUsername;
        }
    }
}