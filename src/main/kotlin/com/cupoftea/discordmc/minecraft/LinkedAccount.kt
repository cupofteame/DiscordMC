package com.cupoftea.discordmc.minecraft

import java.util.UUID

interface LinkedAccount {
    suspend fun linkAccount(minecraftUUID: UUID, minecraftUsername: String, discordId: String)
    suspend fun getDiscordId(minecraftUUID: UUID): String?
    suspend fun getDiscordIdByUsername(minecraftUsername: String): String?
    suspend fun getMinecraftUsername(discordId: String): String?
    suspend fun getMinecraftUUID(discordId: String): UUID?
    suspend fun updateMinecraftUsername(minecraftUUID: UUID, newUsername: String)
    suspend fun unlinkAccount(minecraftUUID: UUID): String?
}