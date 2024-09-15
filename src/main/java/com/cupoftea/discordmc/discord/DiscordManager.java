package com.cupoftea.discordmc.discord;

import java.awt.Color;
import java.time.Instant;

import org.bukkit.entity.Player;

import com.cupoftea.discordmc.DiscordMCPlugin;
import com.cupoftea.discordmc.commands.DiscordMCCommands;
import com.cupoftea.discordmc.config.ConfigManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordManager {
    private final DiscordMCPlugin plugin;
    private final ConfigManager configManager;
    private JDA jda;
    private String discordChannelId;
    private DiscordListener discordListener;

    public DiscordManager(DiscordMCPlugin plugin, ConfigManager configManager, DiscordMCCommands discordMCCommands) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.discordListener = new DiscordListener(plugin, this, discordMCCommands);
    }

    public boolean initialize() {
        String token = configManager.getDiscordBotToken();
        discordChannelId = configManager.getDiscordChannelId();

        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(discordListener)
                    .setActivity(Activity.listening("Accounts"))
                    .build();
            jda.awaitReady();
            discordListener.registerCommands(jda);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize Discord bot: " + e.getMessage());
            return false;
        }
    }

    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
        }
    }

    public void sendMessageToDiscord(Player player, String message) {
        TextChannel channel = jda.getTextChannelById(discordChannelId);
        if (channel != null) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.GREEN);
            embed.setAuthor(player.getName(), null, getPlayerAvatarUrl(player.getName()));
            embed.setDescription(message);
            embed.setFooter("Minecraft Chat");
            embed.setTimestamp(Instant.now());

            channel.sendMessageEmbeds(embed.build()).queue();
        } else {
            plugin.getLogger().warning("Failed to send message to Discord: Channel not found");
        }
    }

    private String getPlayerAvatarUrl(String playerName) {
        return "https://minotar.net/avatar/" + playerName + "/64.png";
    }

    public String getDiscordChannelId() {
        return discordChannelId;
    }

    public boolean isConnected() {
        return jda != null && jda.getStatus() == JDA.Status.CONNECTED;
    }

    public JDA getJda() {
        return jda;
    }

    public DiscordListener getDiscordListener() {
        return discordListener;
    }

    public String getDiscordUsername(String discordId) {
        try {
            User user = jda.retrieveUserById(discordId).complete();
            return user != null ? user.getName() : null;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to retrieve Discord username for ID " + discordId + ": " + e.getMessage());
            return null;
        }
    }
}