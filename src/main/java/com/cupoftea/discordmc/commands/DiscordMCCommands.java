package com.cupoftea.discordmc.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

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
}