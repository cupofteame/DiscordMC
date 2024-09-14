package com.cupoftea.discordmc.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean loadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        return validateConfig();
    }

    private boolean validateConfig() {
        String token = config.getString("discord-bot-token");
        String channelId = config.getString("discord-channel-id");

        if (token == null || token.isEmpty() || token.equals("YOUR_DISCORD_BOT_TOKEN")) {
            plugin.getLogger().severe("Discord bot token is not set in the config.yml file.");
            return false;
        }

        if (channelId == null || channelId.isEmpty() || channelId.equals("YOUR_DISCORD_CHANNEL_ID")) {
            plugin.getLogger().severe("Discord channel ID is not set in the config.yml file.");
            return false;
        }

        return true;
    }

    public String getDiscordBotToken() {
        return config.getString("discord-bot-token");
    }

    public String getDiscordChannelId() {
        return config.getString("discord-channel-id");
    }
}