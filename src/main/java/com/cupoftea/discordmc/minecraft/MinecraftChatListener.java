package com.cupoftea.discordmc.minecraft;

import com.cupoftea.discordmc.discord.DiscordManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MinecraftChatListener implements Listener {
    private final JavaPlugin plugin;
    private final DiscordManager discordManager;

    public MinecraftChatListener(JavaPlugin plugin, DiscordManager discordManager) {
        this.plugin = plugin;
        this.discordManager = discordManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        discordManager.sendMessageToDiscord(event.getPlayer(), event.getMessage());
    }
}