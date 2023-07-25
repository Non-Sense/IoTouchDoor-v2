package net.hikali_47041

import net.dv8tion.jda.api.JDABuilder

class DiscordBot(
    discordBotToken: String,
    private val channelId: String
) {
    private val jda = JDABuilder.createDefault(discordBotToken).build()

    fun start() {
        jda.awaitReady()
    }

    fun sendNotify() {
        jda.getTextChannelById(channelId)?.apply {
            sendMessage("Ping").queue()
        }
    }
}