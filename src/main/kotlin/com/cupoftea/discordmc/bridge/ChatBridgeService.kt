package com.cupoftea.discordmc.bridge

import com.cupoftea.discordmc.config.ConfigurationService
import com.cupoftea.discordmc.discord.service.DiscordService
import com.cupoftea.discordmc.utils.MessageUtils.toComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.bukkit.Server

class ChatBridgeService(
    private val server: Server,
    private val configurationService: ConfigurationService,
    private val discordService: DiscordService
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        discordService.discordMessages
            .onEach { message ->
                if (!message.isBot) {
                    val formattedMessage = configurationService.getMessage(
                        "chat.discord-to-minecraft",
                        "author" to message.author,
                        "message" to message.content
                    )
                    server.broadcast(formattedMessage)
                }
            }
            .launchIn(scope)
    }

    fun sendMinecraftMessage(playerName: String, message: String) {
        discordService.sendMessage(
            "<blue>$playerName</blue><gray>:</gray> <white>$message</white>".toComponent()
        )
    }

    fun broadcastJoin(playerName: String) {
        discordService.sendEmbed(discordService.createPlayerJoinEmbed(playerName))
    }

    fun broadcastLeave(playerName: String) {
        discordService.sendEmbed(discordService.createPlayerLeaveEmbed(playerName))
    }
} 