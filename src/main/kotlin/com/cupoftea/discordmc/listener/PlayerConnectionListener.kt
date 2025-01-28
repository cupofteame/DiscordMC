package com.cupoftea.discordmc.listener

import com.cupoftea.discordmc.bridge.ChatBridgeService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerConnectionListener(
    private val chatBridgeService: ChatBridgeService
) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val playerName = event.player.name
        chatBridgeService.broadcastJoin(playerName)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val playerName = event.player.name
        chatBridgeService.broadcastLeave(playerName)
    }
}