package com.cupoftea.discordmc.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import org.bson.UuidRepresentation
import java.util.concurrent.TimeUnit

class Mongo(
    connectionString: String,
    private val databaseName: String
) {
    private var mongoClient: MongoClient
    private var database: MongoDatabase

    init {
        val settings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(connectionString))
            .uuidRepresentation(UuidRepresentation.STANDARD)
            .applyToConnectionPoolSettings { builder ->
                builder.maxConnectionIdleTime(60, TimeUnit.SECONDS)
                builder.maxSize(50)
            }
            .applyToSocketSettings { builder ->
                builder.connectTimeout(5, TimeUnit.SECONDS)
                builder.readTimeout(5, TimeUnit.SECONDS)
            }
            .build()

        mongoClient = MongoClients.create(settings)
        database = mongoClient.getDatabase(databaseName)
    }

    fun getDatabase(): MongoDatabase {
        return database
    }

    fun close() {
        try {
            mongoClient.close()
        } catch (e: Exception) {
            // Log error or handle exception
            e.printStackTrace()
        }
    }

    fun isConnected(): Boolean {
        return try {
            database.runCommand(org.bson.Document("ping", 1))
            true
        } catch (e: Exception) {
            false
        }
    }

    fun reconnect() {
        try {
            close()
            mongoClient = MongoClients.create(MongoClientSettings.builder()
                .applyConnectionString(ConnectionString("mongodb://localhost:27017"))
                .build())
            database = mongoClient.getDatabase(databaseName)
        } catch (e: Exception) {
            // Log error or handle exception
            e.printStackTrace()
        }
    }
} 