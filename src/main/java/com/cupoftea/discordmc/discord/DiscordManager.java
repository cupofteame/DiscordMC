package com.cupoftea.discordmc.discord;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;

import com.cupoftea.discordmc.DiscordMCPlugin;
import com.cupoftea.discordmc.commands.DiscordMCCommands;
import com.cupoftea.discordmc.config.ConfigManager;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordManager {
    private final DiscordMCPlugin plugin;
    private final ConfigManager configManager;
    private JDA jda;
    private String discordChannelId;
    private DiscordListener discordListener;
    private String lastSentSyncInfoContent;

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
            updateSyncInfo();
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize Discord bot: " + e.getMessage());
            return false;
        }
    }

    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
            try {
                jda.awaitShutdown(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                plugin.getLogger().warning("Interrupted while shutting down Discord connection: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
            jda = null;
        }
    }

    public void sendMessageToDiscord(Player player, String message) {
        if (jda == null || jda.getStatus() != JDA.Status.CONNECTED) {
            plugin.getLogger().warning("Cannot send message to Discord: Bot is not connected.");
            return;
        }

        TextChannel channel = jda.getTextChannelById(discordChannelId);
        if (channel != null) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.GREEN);
            embed.setAuthor(player.getName(), null, getPlayerAvatarUrl(player.getName()));
            embed.setDescription(message);
            embed.setFooter("Minecraft Chat");
            embed.setTimestamp(Instant.now());

            channel.sendMessageEmbeds(embed.build()).queue(
                success -> {},
                error -> plugin.getLogger().warning("Failed to send message to Discord: " + error.getMessage())
            );
        } else {
            plugin.getLogger().warning("Failed to send message to Discord: Channel not found");
        }
    }

    public void sendEmbedToDiscord(MessageEmbed embed) {
        TextChannel channel = jda.getTextChannelById(discordChannelId);
        if (channel != null) {
            channel.sendMessageEmbeds(embed).queue();
        } else {
            plugin.getLogger().warning("Failed to send embed to Discord: Channel not found");
        }
    }

    public void sendDirectMessage(String discordId, String message) {
        try {
            User user = jda.retrieveUserById(discordId).complete();
            if (user != null) {
                user.openPrivateChannel().queue((channel) ->
                    channel.sendMessage(message).queue(
                        success -> plugin.getLogger().info("Sent DM to user " + discordId),
                        error -> plugin.getLogger().warning("Failed to send DM to user " + discordId + ": " + error.getMessage())
                    )
                );
            } else {
                plugin.getLogger().warning("Failed to find Discord user with ID: " + discordId);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error sending direct message to user " + discordId + ": " + e.getMessage());
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

    public void sendSyncInfoEmbed() {
        String syncInfoChannelId = configManager.getSyncInfoChannelId();
        if (syncInfoChannelId == null || syncInfoChannelId.isEmpty()) {
            plugin.getLogger().warning("Sync info channel ID is not set in the config.");
            return;
        }

        TextChannel channel = jda.getTextChannelById(syncInfoChannelId);
        if (channel == null) {
            plugin.getLogger().warning("Could not find the sync info channel with ID: " + syncInfoChannelId);
            return;
        }

        EmbedBuilder embed = createSyncInfoEmbed();
        String newContent = embed.build().getDescription();

        channel.getHistory().retrievePast(100).queue(messages -> {
            Message existingMessage = findExistingBotMessage(messages);
            if (existingMessage != null) {
                if (!newContent.equals(lastSentSyncInfoContent)) {
                    existingMessage.editMessageEmbeds(embed.build()).queue(
                        success -> {
                            plugin.getLogger().info("Sync info embed updated successfully.");
                            lastSentSyncInfoContent = newContent;
                        },
                        error -> plugin.getLogger().warning("Failed to update sync info embed: " + error.getMessage())
                    );
                }
            } else {
                channel.sendMessageEmbeds(embed.build()).queue(
                    success -> {
                        plugin.getLogger().info("Sync info embed sent successfully.");
                        lastSentSyncInfoContent = newContent;
                    },
                    error -> plugin.getLogger().warning("Failed to send sync info embed: " + error.getMessage())
                );
            }
        });
    }

    private EmbedBuilder createSyncInfoEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(configManager.getMessage("sync-info.title"));
        embed.setColor(Color.decode(configManager.getMessage("sync-info.color")));
        embed.setDescription(configManager.getMessage("sync-info.description"));

        List<String> steps = configManager.getMessageList("sync-info.steps");
        for (int i = 0; i < steps.size(); i++) {
            embed.addField("Step " + (i + 1), steps.get(i), false);
        }

        embed.setFooter(configManager.getMessage("sync-info.footer"));
        return embed;
    }

    private Message findExistingBotMessage(List<Message> messages) {
        return messages.stream()
                .filter(message -> message.getAuthor().equals(jda.getSelfUser()))
                .findFirst()
                .orElse(null);
    }

    public void updateSyncInfo() {
        sendSyncInfoEmbed();
    }
}