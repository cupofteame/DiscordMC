package com.cupoftea.discordmc.discord;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class DiscordListener extends ListenerAdapter {
    private final JavaPlugin plugin;
    private final DiscordManager discordManager;

    public DiscordListener(JavaPlugin plugin, DiscordManager discordManager) {
        this.plugin = plugin;
        this.discordManager = discordManager;
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
}