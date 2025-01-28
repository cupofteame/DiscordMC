package com.cupoftea.discordmc.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

object MessageUtils {
    private val miniMessage = MiniMessage.miniMessage()
    private val legacySerializer = LegacyComponentSerializer.builder()
        .character('§')
        .hexColors()
        .build()

    fun String.toComponent(): Component {
        return if (this.contains('§')) {
            // Handle legacy color codes
            legacySerializer.deserialize(this)
        } else {
            // Use MiniMessage format
            miniMessage.deserialize(this)
        }
    }

    fun Component.toDiscordMarkdown(): String {
        return miniMessage.serialize(this)
            .replace("<color:#", "")
            .replace(">", "")
            .replace("</color>", "")
    }
} 