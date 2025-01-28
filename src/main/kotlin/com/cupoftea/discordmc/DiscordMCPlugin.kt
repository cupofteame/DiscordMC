package com.cupoftea.discordmc

import cloud.commandframework.annotations.AnnotationParser
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.kotlin.coroutines.annotations.installCoroutineSupport
import cloud.commandframework.paper.PaperCommandManager
import cloud.commandframework.meta.SimpleCommandMeta
import com.cupoftea.discordmc.database.Mongo
import com.cupoftea.discordmc.minecraft.repo.LinkedAccountRepository
import com.cupoftea.discordmc.minecraft.service.AccountLinkService
import com.cupoftea.discordmc.config.ConfigurationService
import com.cupoftea.discordmc.bridge.ChatBridgeService
import com.cupoftea.discordmc.commands.DiscordMCCommands
import com.cupoftea.discordmc.discord.service.DiscordService
import com.cupoftea.discordmc.listener.MinecraftChatListener
import com.cupoftea.discordmc.listener.PlayerConnectionListener
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.util.function.Function

class DiscordMCPlugin : JavaPlugin() {
    private lateinit var configurationService: ConfigurationService
    private lateinit var mongoDBManager: Mongo
    private lateinit var linkedAccountRepository: LinkedAccountRepository
    private lateinit var accountLinkService: AccountLinkService
    private lateinit var commandManager: PaperCommandManager<CommandSender>
    private lateinit var discordService: DiscordService
    private lateinit var chatBridgeService: ChatBridgeService
    
    override fun onEnable() {
        // Initialize configuration
        configurationService = ConfigurationService(this)
        configurationService.initialize()
        
        // Initialize MongoDB and repositories
        mongoDBManager = Mongo(
            configurationService.getMongoDbConnectionString(),
            configurationService.getMongoDbDatabaseName()
        )
        linkedAccountRepository = LinkedAccountRepository(mongoDBManager)
        
        // Initialize services
        accountLinkService = AccountLinkService(linkedAccountRepository)
        
        // Initialize Discord service
        discordService = DiscordService(configurationService, accountLinkService)
        discordService.initialize()
        
        // Initialize chat bridge
        chatBridgeService = ChatBridgeService(server, configurationService, discordService)
        
        // Setup command manager
        setupCommands()
        
        // Register listeners
        registerListeners()
        
        logger.info("DiscordMC plugin has been enabled!")
    }
    
    private fun setupCommands() {
        val executionCoordinator = AsynchronousCommandExecutionCoordinator.builder<CommandSender>()
            .withAsynchronousParsing()
            .withExecutor { cmd -> server.scheduler.runTask(this, cmd) }
            .build()
            
        commandManager = PaperCommandManager(
            this,
            executionCoordinator,
            Function.identity(),
            Function.identity()
        )
        
        // Create annotation parser
        val annotationParser = AnnotationParser(
            commandManager,
            CommandSender::class.java,
            { SimpleCommandMeta.empty() }
        )
        
        // Install coroutine support with default dispatcher
        annotationParser.installCoroutineSupport()
        
        // Create command class instance
        val discordMCCommands = DiscordMCCommands(this)

        // Register command classes
        annotationParser.parse(discordMCCommands)
    }
    
    private fun registerListeners() {
        server.pluginManager.registerEvents(
            MinecraftChatListener(chatBridgeService),
            this
        )
        server.pluginManager.registerEvents(
            PlayerConnectionListener(chatBridgeService),
            this
        )
    }
    
    override fun onDisable() {
        // Cleanup resources
        discordService.shutdown()
        mongoDBManager.close()
        logger.info("DiscordMC plugin has been disabled!")
    }
    
    // Expose services for other parts of the plugin
    companion object {
        private lateinit var instance: DiscordMCPlugin
        
        fun getInstance(): DiscordMCPlugin = instance
        
        val configurationService: ConfigurationService
            get() = instance.configurationService
        
        val accountLinkService: AccountLinkService
            get() = instance.accountLinkService
            
        val discordService: DiscordService
            get() = instance.discordService
            
        val chatBridgeService: ChatBridgeService
            get() = instance.chatBridgeService
    }
    
    init {
        instance = this
    }
} 