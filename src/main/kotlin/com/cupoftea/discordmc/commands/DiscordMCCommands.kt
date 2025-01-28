package com.cupoftea.discordmc.commands

import cloud.commandframework.annotations.CommandMethod
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.Argument
import com.cupoftea.discordmc.DiscordMCPlugin
import com.cupoftea.discordmc.minecraft.service.AccountLinkService
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import com.cupoftea.discordmc.utils.MessageUtils.toComponent

class DiscordMCCommands(plugin: DiscordMCPlugin) {
    private val configurationService = DiscordMCPlugin.configurationService
    private val accountLinkService = DiscordMCPlugin.accountLinkService
    private val discordService = DiscordMCPlugin.discordService

    @CommandMethod("discordmc|dmc")
    @CommandDescription("Shows DiscordMC commands")
    fun showHelp(sender: CommandSender) {
        sender.sendMessage("""
            <gold>=== DiscordMC Commands ===</gold>
            <yellow>/discordmc</yellow> <white>- Show this help message</white>
            <yellow>/discordmc reload</yellow> <white>- Reload the plugin configuration</white>
            <yellow>/discordmc status</yellow> <white>- Check the Discord connection status</white>
            <yellow>/discordmc togglelinking</yellow> <white>- Toggle the ability to link accounts</white>
            <yellow>/link <code></yellow> <white>- Link your Minecraft account to Discord</white>
            <yellow>/discord</yellow> <white>- Show your linked Discord account</white>
            <yellow>/unlink</yellow> <white>- Unlink your Minecraft account from Discord</white>
        """.trimIndent().toComponent())
    }

    @CommandMethod("discordmc reload")
    @CommandPermission("discordmc.reload")
    @CommandDescription("Reloads the DiscordMC configuration")
    fun reloadConfig(sender: CommandSender) {
        sender.sendMessage("<yellow>Reloading DiscordMC configuration...</yellow>".toComponent())
        configurationService.reload()
        sender.sendMessage("<green>DiscordMC configuration reloaded successfully!</green>".toComponent())
    }

    @CommandMethod("discordmc status")
    @CommandPermission("discordmc.status")
    @CommandDescription("Checks the status of the Discord connection")
    fun checkStatus(sender: CommandSender) {
        val isConnected = discordService.isInitialized()
        sender.sendMessage("§eDiscord connection status: ${if (isConnected) "§aConnected" else "§cDisconnected"}")
    }

    @CommandMethod("discordmc togglelinking")
    @CommandPermission("discordmc.togglelinking")
    @CommandDescription("Toggle the ability to link accounts")
    fun toggleLinking(sender: CommandSender) {
        val currentState = configurationService.getBoolean("allow-linking")
        configurationService.set("allow-linking", !currentState)
        val state = if (!currentState) "enabled" else "disabled"
        sender.sendMessage("§aAccount linking has been $state.")
    }

    @CommandMethod("link <code>")
    @CommandDescription("Link your Minecraft account to Discord")
    suspend fun linkAccount(player: Player, @Argument("code") code: String) {
        if (!configurationService.getBoolean("allow-linking")) {
            player.sendMessage("<red>Account linking is currently disabled.</red>".toComponent())
            return
        }

        val pendingLink = accountLinkService.getPendingLink(code) ?: run {
            player.sendMessage("§c${configurationService.getMessage("minecraft.link-invalid-code")}")
            return
        }

        if (player.name != pendingLink.minecraftUsername) {
            player.sendMessage("§cThis link code is for a different Minecraft username.")
            return
        }

        when (val result = accountLinkService.linkAccount(
            player.uniqueId,
            player.name,
            pendingLink.discordId
        )) {
            is AccountLinkService.LinkResult.Success -> {
                player.sendMessage("§a${configurationService.getMessage("minecraft.link-success")}")
                configurationService.getString("discord.linked-role-id")?.let { roleId ->
                    discordService.assignRole(pendingLink.discordId, roleId)
                }
                discordService.sendPrivateMessage(
                    pendingLink.discordId,
                    configurationService.getMessage("discord.dm-link-success", "username" to player.name).toString()
                )
            }
            is AccountLinkService.LinkResult.AlreadyLinkedDiscord -> {
                player.sendMessage("§c${configurationService.getMessage("minecraft.link-discord-already-linked")}")
            }
            is AccountLinkService.LinkResult.AlreadyLinkedMinecraft -> {
                player.sendMessage("§c${configurationService.getMessage("minecraft.link-already-linked")}")
            }
        }
    }

    @CommandMethod("discord")
    @CommandDescription("Show your linked Discord account")
    suspend fun showLinkedAccount(player: Player) {
        val discordId = accountLinkService.getDiscordId(player.uniqueId)
        if (discordId != null) {
            val discordUsername = discordService.getUsernameById(discordId)
            if (discordUsername != null) {
                player.sendMessage("§aYour Minecraft account is linked to Discord user: $discordUsername")
            } else {
                player.sendMessage("§cFailed to retrieve Discord username. Please try again later.")
            }
        } else {
            player.sendMessage("§cYour Minecraft account is not linked to a Discord account.")
        }
    }

    @CommandMethod("unlink")
    @CommandDescription("Unlink your Minecraft account from Discord")
    suspend fun unlinkAccount(player: Player) {
        when (val result = accountLinkService.unlinkAccount(player.uniqueId)) {
            is AccountLinkService.UnlinkResult.Success -> {
                player.sendMessage("§a${configurationService.getMessage("minecraft.unlink-success")}")
                configurationService.getString("discord.linked-role-id")?.let { roleId ->
                    discordService.removeRole(result.discordId, roleId)
                }
                discordService.sendPrivateMessage(
                    result.discordId,
                    configurationService.getMessage("discord.dm-unlink-success", "username" to player.name).toString()
                )
            }
            is AccountLinkService.UnlinkResult.NotLinked -> {
                player.sendMessage("§c${configurationService.getMessage("minecraft.unlink-not-linked")}")
            }
        }
    }
} 