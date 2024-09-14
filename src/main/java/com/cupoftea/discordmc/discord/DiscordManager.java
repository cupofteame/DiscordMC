package com.cupoftea.discordmc.discord;

import com.cupoftea.discordmc.config.ConfigManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.EmbedBuilder;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.Color;
import java.time.Instant;

public class DiscordManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private JDA jda;
    private String discordChannelId;

    public DiscordManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public boolean initialize() {
        String token = configManager.getDiscordBotToken();
        discordChannelId = configManager.getDiscordChannelId();

        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new DiscordListener(plugin, this))
                    .build();
            jda.awaitReady();
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
}