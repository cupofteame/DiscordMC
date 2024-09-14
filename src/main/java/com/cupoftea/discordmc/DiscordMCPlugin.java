package com.cupoftea.discordmc;

import org.bukkit.plugin.java.JavaPlugin;

import com.cupoftea.discordmc.commands.DiscordMCCommands;
import com.cupoftea.discordmc.config.ConfigManager;
import com.cupoftea.discordmc.discord.DiscordManager;
import com.cupoftea.discordmc.minecraft.MinecraftChatListener;

import co.aikar.commands.PaperCommandManager;

public class DiscordMCPlugin extends JavaPlugin {

	private ConfigManager configManager;
	private DiscordManager discordManager;
	private PaperCommandManager commandManager;

	@Override
	public void onEnable() {
		configManager = new ConfigManager(this);
		if (!configManager.loadConfig()) {
			getLogger().severe("Failed to load configuration. Disabling plugin.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		discordManager = new DiscordManager(this, configManager);
		if (!discordManager.initialize()) {
			getLogger().severe("Failed to initialize Discord connection. Disabling plugin.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		this.commandManager = new PaperCommandManager(this);
		commandManager.registerCommand(new DiscordMCCommands(this));

		getServer().getPluginManager().registerEvents(new MinecraftChatListener(this, discordManager), this);
		getLogger().info("DiscordMC plugin has been enabled!");
	}

	@Override
	public void onDisable() {
		if (discordManager != null) {
			discordManager.shutdown();
		}
		getLogger().info("DiscordMC plugin has been disabled!");
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

		discordManager = new DiscordManager(this, configManager);
		if (!discordManager.initialize()) {
			getLogger().severe("Failed to reinitialize Discord connection.");
			return false;
		}

		getServer().getPluginManager().registerEvents(new MinecraftChatListener(this, discordManager), this);

		getLogger().info("DiscordMC plugin has been reloaded successfully!");
		return true;
	}
}