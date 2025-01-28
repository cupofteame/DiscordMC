package com.cupoftea.discordmc.listener

import com.cupoftea.discordmc.bridge.ChatBridgeService
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class MinecraftChatListener(
    private val chatBridgeService: ChatBridgeService
) : Listener {

    @EventHandler
    fun onPlayerChat(event: AsyncChatEvent) {
        val playerName = event.player.name
        val message = PlainTextComponentSerializer.plainText().serialize(event.message())

        chatBridgeService.sendMinecraftMessage(playerName, message)
    }
}