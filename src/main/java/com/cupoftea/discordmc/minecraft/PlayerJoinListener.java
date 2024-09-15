package com.cupoftea.discordmc.minecraft;

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
        plugin.getMongoDBManager().updateMinecraftUsername(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }
}