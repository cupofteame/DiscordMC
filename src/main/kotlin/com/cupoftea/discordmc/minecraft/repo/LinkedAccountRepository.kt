package com.cupoftea.discordmc.minecraft.repo

import com.cupoftea.discordmc.database.Mongo
import com.cupoftea.discordmc.minecraft.LinkedAccount
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.Document
import java.util.UUID

class LinkedAccountRepository(
    private val mongoDBManager: Mongo
) : LinkedAccount {
    private val linkedAccounts = mongoDBManager.getDatabase().getCollection("linked_accounts")

    override suspend fun linkAccount(minecraftUUID: UUID, minecraftUsername: String, discordId: String) {
        withContext(Dispatchers.IO) {
            val doc = Document()
                .append("minecraft_uuid", minecraftUUID.toString())
                .append("minecraft_username", minecraftUsername)
                .append("discord_id", discordId)
            linkedAccounts.insertOne(doc)
        }
    }

    override suspend fun getDiscordId(minecraftUUID: UUID): String? = withContext(Dispatchers.IO) {
        linkedAccounts.find(Filters.eq("minecraft_uuid", minecraftUUID.toString()))
            .first()
            ?.getString("discord_id")
    }

    override suspend fun getDiscordIdByUsername(minecraftUsername: String): String? = withContext(Dispatchers.IO) {
        linkedAccounts.find(Filters.eq("minecraft_username", minecraftUsername))
            .first()
            ?.getString("discord_id")
    }

    override suspend fun getMinecraftUsername(discordId: String): String? = withContext(Dispatchers.IO) {
        linkedAccounts.find(Filters.eq("discord_id", discordId))
            .first()
            ?.getString("minecraft_username")
    }

    override suspend fun getMinecraftUUID(discordId: String): UUID? = withContext(Dispatchers.IO) {
        linkedAccounts.find(Filters.eq("discord_id", discordId))
            .first()
            ?.getString("minecraft_uuid")
            ?.let { UUID.fromString(it) }
    }

    override suspend fun updateMinecraftUsername(minecraftUUID: UUID, newUsername: String) {
        withContext(Dispatchers.IO) {
            linkedAccounts.updateOne(
                Filters.eq("minecraft_uuid", minecraftUUID.toString()),
                Updates.set("minecraft_username", newUsername)
            )
        }
    }

    override suspend fun unlinkAccount(minecraftUUID: UUID): String? = withContext(Dispatchers.IO) {
        linkedAccounts.findOneAndDelete(Filters.eq("minecraft_uuid", minecraftUUID.toString()))
            ?.getString("discord_id")
    }
}