package com.cupoftea.discordmc.minecraft.service

import com.cupoftea.discordmc.minecraft.LinkedAccount
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class AccountLinkService(
    private val linkedAccountRepository: LinkedAccount
) {
    private val pendingLinks = ConcurrentHashMap<String, PendingLink>()

    suspend fun linkAccount(minecraftUUID: UUID, minecraftUsername: String, discordId: String): LinkResult {
        // Check if the Discord ID is already linked
        linkedAccountRepository.getMinecraftUsername(discordId)?.let {
            return LinkResult.AlreadyLinkedDiscord(it)
        }

        // Check if the Minecraft UUID is already linked
        linkedAccountRepository.getDiscordId(minecraftUUID)?.let {
            return LinkResult.AlreadyLinkedMinecraft
        }

        // Link the accounts
        linkedAccountRepository.linkAccount(minecraftUUID, minecraftUsername, discordId)
        return LinkResult.Success
    }

    suspend fun unlinkAccount(minecraftUUID: UUID): UnlinkResult {
        val discordId = linkedAccountRepository.unlinkAccount(minecraftUUID)
        return if (discordId != null) {
            UnlinkResult.Success(discordId)
        } else {
            UnlinkResult.NotLinked
        }
    }

    fun createPendingLink(discordId: String, minecraftUsername: String): String {
        val code = generateLinkCode()
        pendingLinks[code] = PendingLink(discordId, minecraftUsername)
        return code
    }

    fun getPendingLink(code: String): PendingLink? = pendingLinks.remove(code)

    private fun generateLinkCode(): String = UUID.randomUUID().toString().substring(0, 6)

    suspend fun getDiscordId(minecraftUUID: UUID): String? {
        return linkedAccountRepository.getDiscordId(minecraftUUID)
    }

    data class PendingLink(
        val discordId: String,
        val minecraftUsername: String
    )

    sealed class LinkResult {
        object Success : LinkResult()
        data class AlreadyLinkedDiscord(val existingUsername: String) : LinkResult()
        object AlreadyLinkedMinecraft : LinkResult()
    }

    sealed class UnlinkResult {
        data class Success(val discordId: String) : UnlinkResult()
        object NotLinked : UnlinkResult()
    }
}