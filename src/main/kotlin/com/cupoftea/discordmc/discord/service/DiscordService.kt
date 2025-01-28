package com.cupoftea.discordmc.discord.service

import com.cupoftea.discordmc.config.ConfigurationService
import com.cupoftea.discordmc.discord.listener.DiscordEventListener
import com.cupoftea.discordmc.discord.DiscordMessage
import com.cupoftea.discordmc.discord.commands.LinkCommands
import com.cupoftea.discordmc.minecraft.service.AccountLinkService
import com.cupoftea.discordmc.utils.MessageUtils.toDiscordMarkdown
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.requests.GatewayIntent
import net.kyori.adventure.text.Component
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.awt.Color

class DiscordService(private val configurationService: ConfigurationService, private val accountLinkService: AccountLinkService) {
    private lateinit var jda: JDA
    private lateinit var channel: TextChannel
    private val _discordMessages = MutableSharedFlow<DiscordMessage>()
    val discordMessages: SharedFlow<DiscordMessage> = _discordMessages

    private lateinit var linkCommands: LinkCommands

    fun initialize() {
        val token = configurationService.getDiscordToken()
        val channelId = configurationService.getChannelId()

        jda = JDABuilder.createDefault(token)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .build()
            .awaitReady()

        channel = jda.getTextChannelById(channelId)
            ?: throw IllegalStateException("Could not find channel with ID $channelId")

        jda.addEventListener(DiscordEventListener(_discordMessages, channel.id))

        // Initialize commands
        linkCommands = LinkCommands(accountLinkService, configurationService)
        
        // Register slash commands
        jda.updateCommands()
            .addCommands(linkCommands.linkCommand)
            .queue()

        // Add slash command listener
        jda.addEventListener(object : ListenerAdapter() {
            override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
                when (event.name) {
                    "link" -> linkCommands.handleLinkCommand(event)
                }
            }
        })
    }

    fun shutdown() {
        if (::jda.isInitialized) {
            jda.shutdown()
        }
    }

    fun sendMessage(message: Component) {
        if (::channel.isInitialized) {
            channel.sendMessage(message.toDiscordMarkdown()).queue()
        }
    }

    fun sendMessage(message: String) {
        if (::channel.isInitialized) {
            channel.sendMessage(message).queue()
        }
    }

    fun sendEmbed(embed: MessageEmbed) {
        if (::channel.isInitialized) {
            channel.sendMessageEmbeds(embed).queue()
        }
    }

    fun createPlayerJoinEmbed(playerName: String): MessageEmbed {
        return EmbedBuilder()
            .setColor(Color.GREEN)
            .setDescription("**$playerName** joined the game")
            .build()
    }

    fun createPlayerLeaveEmbed(playerName: String): MessageEmbed {
        return EmbedBuilder()
            .setColor(Color.RED)
            .setDescription("**$playerName** left the game")
            .build()
    }

    fun isInitialized(): Boolean = ::jda.isInitialized

    fun assignRole(userId: String, roleId: String) {
        if (::jda.isInitialized) {
            val guildId = configurationService.getString("discord.guild-id") ?: return
            jda.getGuildById(guildId)
                ?.retrieveMemberById(userId)?.queue { member ->
                    member.guild.getRoleById(roleId)?.let { role ->
                        member.guild.addRoleToMember(member, role).queue()
                    }
                }
        }
    }

    fun removeRole(userId: String, roleId: String) {
        if (::jda.isInitialized) {
            val guildId = configurationService.getString("discord.guild-id") ?: return
            jda.getGuildById(guildId)
                ?.retrieveMemberById(userId)?.queue { member ->
                    member.guild.getRoleById(roleId)?.let { role ->
                        member.guild.removeRoleFromMember(member, role).queue()
                    }
                }
        }
    }

    fun sendPrivateMessage(userId: String, message: String) {
        if (::jda.isInitialized) {
            jda.retrieveUserById(userId).queue { user ->
                user.openPrivateChannel().queue { channel ->
                    channel.sendMessage(message).queue()
                }
            }
        }
    }

    fun getUsernameById(userId: String): String? {
        return if (::jda.isInitialized) {
            jda.retrieveUserById(userId).complete()?.name
        } else {
            null
        }
    }
}