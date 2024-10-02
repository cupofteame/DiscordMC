package com.cupoftea.discordmc.minecraft;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.cupoftea.discordmc.DiscordMCPlugin;
import com.cupoftea.discordmc.discord.DiscordManager;

public class MinecraftChatListener implements Listener {
    private final DiscordMCPlugin plugin;
    private final DiscordManager discordManager;

    public MinecraftChatListener(DiscordMCPlugin plugin, DiscordManager discordManager) {
        this.plugin = plugin;
        this.discordManager = discordManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        discordManager.sendMessageToDiscord(player, message);
    }
}