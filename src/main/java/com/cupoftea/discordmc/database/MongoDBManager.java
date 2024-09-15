package com.cupoftea.discordmc.database;

import java.util.UUID;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MongoDBManager {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> linkedAccounts;

    public MongoDBManager(String connectionString, String databaseName) {
        mongoClient = MongoClients.create(connectionString);
        database = mongoClient.getDatabase(databaseName);
        linkedAccounts = database.getCollection("linked_accounts");
    }

    public void linkAccount(UUID minecraftUUID, String minecraftUsername, String discordId) {
        Document doc = new Document("minecraft_uuid", minecraftUUID.toString())
                .append("minecraft_username", minecraftUsername)
                .append("discord_id", discordId);
        linkedAccounts.insertOne(doc);
    }

    public String getDiscordId(UUID minecraftUUID) {
        Document doc = linkedAccounts.find(new Document("minecraft_uuid", minecraftUUID.toString())).first();
        return doc != null ? doc.getString("discord_id") : null;
    }

    public String getDiscordIdByUsername(String minecraftUsername) {
        Document doc = linkedAccounts.find(new Document("minecraft_username", minecraftUsername)).first();
        return doc != null ? doc.getString("discord_id") : null;
    }

    public String getMinecraftUsername(String discordId) {
        Document doc = linkedAccounts.find(new Document("discord_id", discordId)).first();
        return doc != null ? doc.getString("minecraft_username") : null;
    }

    public UUID getMinecraftUUID(String discordId) {
        Document doc = linkedAccounts.find(new Document("discord_id", discordId)).first();
        return doc != null ? UUID.fromString(doc.getString("minecraft_uuid")) : null;
    }

    public void updateMinecraftUsername(UUID minecraftUUID, String newUsername) {
        linkedAccounts.updateOne(
            new Document("minecraft_uuid", minecraftUUID.toString()),
            new Document("$set", new Document("minecraft_username", newUsername))
        );
    }

    public void unlinkAccount(UUID minecraftUUID) {
        linkedAccounts.deleteOne(new Document("minecraft_uuid", minecraftUUID.toString()));
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}