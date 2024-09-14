# DiscordMC

DiscordMC is a Minecraft plugin that bridges chat between your Minecraft server and a Discord channel.

## Features

- Sends Minecraft chat messages to a specified Discord channel
- Relays Discord messages to the Minecraft server
- Simple configuration

## Requirements

- Minecraft Server: Paper 1.13.2 or higher
- Java 8 or higher

## Dependencies

This plugin uses the following libraries:
- JDA (Java Discord API) 5.0.0-beta.13
- ACF (Annotation Command Framework) 0.5.1-SNAPSHOT

These dependencies are automatically managed by Gradle and don't require manual installation.

## Setup

1. Place the plugin JAR in your server's `plugins` folder
2. Start the server to generate the config file
3. Edit `plugins/DiscordMC/config.yml` with your Discord bot token and channel ID
4. Restart the server or reload the plugin

## Commands

- `/discordmc` - Show help message
- `/discordmc reload` - Reload the plugin configuration
- `/discordmc status` - Check Discord connection status

## Permissions

- `discordmc.reload` - Allows use of the reload command
- `discordmc.status` - Allows use of the status command

## Support

For issues or feature requests, please use the GitHub issue tracker.