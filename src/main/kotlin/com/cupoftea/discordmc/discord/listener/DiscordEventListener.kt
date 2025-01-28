package com.cupoftea.discordmc.discord.listener

import com.cupoftea.discordmc.discord.DiscordMessage
import com.cupoftea.discordmc.discord.service.DiscordService
import kotlinx.coroutines.flow.MutableSharedFlow
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class DiscordEventListener(
    private val messageFlow: MutableSharedFlow<DiscordMessage>,
    private val channelId: String
) : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.channel.id != channelId) return

        val message = DiscordMessage(
            author = event.author.name,
            content = event.message.contentDisplay,
            isBot = event.author.isBot
        )

        messageFlow.tryEmit(message)
    }
}