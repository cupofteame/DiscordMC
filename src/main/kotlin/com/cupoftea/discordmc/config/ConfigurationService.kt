package com.cupoftea.discordmc.config

import com.cupoftea.discordmc.utils.MessageUtils.toComponent
import net.kyori.adventure.text.Component
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File

class ConfigurationService(private val plugin: Plugin) {
    lateinit var config: FileConfiguration
        private set
    private lateinit var messages: FileConfiguration

    fun initialize() {
        plugin.saveDefaultConfig()
        config = plugin.config

        val messagesFile = File(plugin.dataFolder, "messages.yml")
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false)
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile)
    }

    fun reload() {
        plugin.reloadConfig()
        config = plugin.config

        val messagesFile = File(plugin.dataFolder, "messages.yml")
        messages = YamlConfiguration.loadConfiguration(messagesFile)
    }

    fun getDiscordToken(): String {
        return config.getString("discord.token")
            ?: throw IllegalStateException("Discord token not found in config.yml")
    }

    fun getChannelId(): String {
        return config.getString("discord.channel-id")
            ?: throw IllegalStateException("Channel ID not found in config.yml")
    }

    fun getMessage(path: String, vararg placeholders: Pair<String, String>): Component {
        var message = messages.getString(path) ?: return Component.empty()
        
        // Replace placeholders
        placeholders.forEach { (key, value) ->
            message = message.replace("{$key}", value)
        }
        
        return message.toComponent()
    }

    fun getString(path: String): String? {
        return config.getString(path)
    }

    fun getBoolean(path: String): Boolean {
        return config.getBoolean(path)
    }

    fun getInt(path: String): Int {
        return config.getInt(path)
    }

    fun getLong(path: String): Long {
        return config.getLong(path)
    }

    fun getDouble(path: String): Double {
        return config.getDouble(path)
    }

    fun getStringList(path: String): List<String> {
        return config.getStringList(path)
    }

    fun set(path: String, value: Any?) {
        config.set(path, value)
        plugin.saveConfig()
    }

    fun getMongoDbConnectionString(): String {
        return config.getString("mongodb.connection-string")
            ?: throw IllegalStateException("MongoDB connection string not found in config.yml")
    }

    fun getMongoDbDatabaseName(): String {
        return config.getString("mongodb.database")
            ?: throw IllegalStateException("MongoDB database name not found in config.yml")
    }
}