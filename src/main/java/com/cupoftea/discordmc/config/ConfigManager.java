package com.cupoftea.discordmc.config;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private String mongoDbConnectionString;
    private String mongoDbDatabaseName;
    private String linkedAccountRoleName;
    private String linkedAccountRoleId;
    private String syncInfoChannelId;
    private YamlConfiguration messages;
    private FileTime lastMessagesModified;

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
        syncInfoChannelId = config.getString("sync-info-channel-id");
        
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        lastMessagesModified = getLastModifiedTime(messagesFile);

        return validateConfig();
    }

    public boolean hasMessagesChanged() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        FileTime currentModified = getLastModifiedTime(messagesFile);
        if (currentModified != null && !currentModified.equals(lastMessagesModified)) {
            lastMessagesModified = currentModified;
            messages = YamlConfiguration.loadConfiguration(messagesFile);
            return true;
        }
        return false;
    }

    private FileTime getLastModifiedTime(File file) {
        try {
            return Files.getLastModifiedTime(file.toPath());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to get last modified time for " + file.getName());
            return null;
        }
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

    public String getSyncInfoChannelId() {
        return syncInfoChannelId;
    }

    public String getMessage(String path) {
        return messages.getString(path);
    }

    public List<String> getMessageList(String path) {
        return messages.getStringList(path);
    }
}