package com.cupoftea.discordmc.config;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private String mongoDbConnectionString;
    private String mongoDbDatabaseName;
    private String linkedAccountRoleName;
    private String linkedAccountRoleId;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
        plugin.reloadConfig();
        config = plugin.getConfig();
        return validateConfig();
    }

    private boolean validateConfig() {
        String token = config.getString("discord-bot-token");
        String channelId = config.getString("discord-channel-id");
        mongoDbConnectionString = config.getString("mongodb-connection-string");
        mongoDbDatabaseName = config.getString("mongodb-database-name");
        linkedAccountRoleName = config.getString("linked-account-role-name");
        linkedAccountRoleId = config.getString("linked-account-role-id", "");

        if (isInvalid(token, "Discord bot token")) return false;
        if (isInvalid(channelId, "Discord channel ID")) return false;
        if (isInvalid(mongoDbConnectionString, "MongoDB connection string")) return false;
        if (isInvalid(mongoDbDatabaseName, "MongoDB database name")) return false;

        return true;
    }

    private boolean isInvalid(String value, String configName) {
        if (value == null || value.isEmpty() || value.startsWith("YOUR_")) {
            plugin.getLogger().severe(configName + " is not set in the config.yml file.");
            return true;
        }
        return false;
    }

    public String getDiscordBotToken() {
        return config.getString("discord-bot-token");
    }

    public String getDiscordChannelId() {
        return config.getString("discord-channel-id");
    }

    public String getMongoDbConnectionString() {
        return mongoDbConnectionString;
    }

    public String getMongoDbDatabaseName() {
        return mongoDbDatabaseName;
    }

    public String getLinkedAccountRoleName() {
        return linkedAccountRoleName;
    }

    public String getLinkedAccountRoleId() {
        return linkedAccountRoleId;
    }
}