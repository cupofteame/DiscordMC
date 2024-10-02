package com.cupoftea.discordmc;

import org.bukkit.plugin.java.JavaPlugin;

import com.cupoftea.discordmc.commands.DiscordMCCommands;
import com.cupoftea.discordmc.config.ConfigManager;
import com.cupoftea.discordmc.database.MongoDBManager;
import com.cupoftea.discordmc.discord.DiscordManager;
import com.cupoftea.discordmc.minecraft.MinecraftChatListener;
import com.cupoftea.discordmc.minecraft.PlayerJoinListener;

import co.aikar.commands.PaperCommandManager;

public class DiscordMCPlugin extends JavaPlugin {

	private ConfigManager configManager;
	private DiscordManager discordManager;
	private PaperCommandManager commandManager;
	private MongoDBManager mongoDBManager;
	private DiscordMCCommands discordMCCommands;

	@Override
	public void onEnable() {
		configManager = new ConfigManager(this);
		if (!configManager.loadConfig()) {
			getLogger().severe("Failed to load configuration. Disabling plugin.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		this.commandManager = new PaperCommandManager(this);
		this.discordMCCommands = new DiscordMCCommands(this);
		commandManager.registerCommand(discordMCCommands);

		discordManager = new DiscordManager(this, configManager, discordMCCommands);
		if (discordManager.initialize()) {
			getLogger().info(configManager.getMessage("plugin.discord-connected"));
			discordManager.sendSyncInfoEmbed();
		} else {
			getLogger().severe(configManager.getMessage("plugin.discord-connection-failed"));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		getServer().getPluginManager().registerEvents(new MinecraftChatListener(this, discordManager), this);
		getLogger().info("DiscordMC plugin has been enabled!");

		mongoDBManager = new MongoDBManager(
			configManager.getMongoDbConnectionString(),
			configManager.getMongoDbDatabaseName()
		);

		getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
	}

	@Override
	public void onDisable() {
		if (discordManager != null) {
			discordManager.shutdown();
		}
		getLogger().info(configManager.getMessage("plugin.disabled"));
		if (mongoDBManager != null) {
			mongoDBManager.close();
		}
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public DiscordManager getDiscordManager() {
		return discordManager;
	}

	public boolean reload() {
		if (!configManager.loadConfig()) {
			getLogger().severe("Failed to reload configuration.");
			return false;
		}

		if (discordManager != null) {
			discordManager.shutdown();
		}

		discordManager = new DiscordManager(this, configManager, discordMCCommands);
		if (!discordManager.initialize()) {
			getLogger().severe("Failed to reinitialize Discord connection.");
			return false;
		}

		getServer().getPluginManager().registerEvents(new MinecraftChatListener(this, discordManager), this);

		getLogger().info("DiscordMC plugin has been reloaded successfully!");
		return true;
	}

	public MongoDBManager getMongoDBManager() {
		return mongoDBManager;
	}

	public PaperCommandManager getCommandManager() {
		return commandManager;
	}

	public DiscordMCCommands getDiscordMCCommands() {
		return discordMCCommands;
	}
}