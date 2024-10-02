package com.cupoftea.discordmc.minecraft;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.cupoftea.discordmc.DiscordMCPlugin;

public class PlayerJoinListener implements Listener {
    private final DiscordMCPlugin plugin;

    public PlayerJoinListener(DiscordMCPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        String discordId = plugin.getMongoDBManager().getDiscordId(playerUUID);
        
        if (discordId != null) {
            plugin.getMongoDBManager().updateMinecraftUsername(playerUUID, player.getName());
            String message = plugin.getConfigManager().getMessage("minecraft.join-linked")
                .replace("{username}", player.getName());
            player.sendMessage(ChatColor.GREEN + message);
        }
    }
}