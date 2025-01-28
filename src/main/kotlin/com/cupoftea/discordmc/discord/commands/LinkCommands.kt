package com.cupoftea.discordmc.discord.commands

import com.cupoftea.discordmc.minecraft.service.AccountLinkService
import com.cupoftea.discordmc.config.ConfigurationService
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

class LinkCommands(
    private val accountLinkService: AccountLinkService,
    private val configurationService: ConfigurationService
) {
    val linkCommand = Commands.slash("link", "Generate a code to link your Discord account with Minecraft")
        .addOption(OptionType.STRING, "minecraft_username", "Your Minecraft username", true)

    fun handleLinkCommand(event: SlashCommandInteractionEvent) {
        if (!configurationService.getBoolean("allow-linking")) {
            event.reply("Account linking is currently disabled.").setEphemeral(true).queue()
            return
        }

        val minecraftUsername = event.getOption("minecraft_username")?.asString
            ?: return event.reply("Please provide your Minecraft username.").setEphemeral(true).queue()

        val code = accountLinkService.createPendingLink(event.user.id, minecraftUsername)
        
        event.reply(configurationService.getMessage(
            "discord.link-command-response",
            "username" to minecraftUsername,
            "code" to code
        ).toString()).setEphemeral(true).queue()
    }
} 