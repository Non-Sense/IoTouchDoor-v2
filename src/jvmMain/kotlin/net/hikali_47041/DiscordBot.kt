package net.hikali_47041

import net.dv8tion.jda.api.JDABuilder
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DiscordBot(
    discordBotToken: String,
    private val channelId: String
) {
    private val jda = JDABuilder.createDefault(discordBotToken).build()

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS")
    }

    fun start() {
        jda.awaitReady()
    }

    fun sendNotify() {
        jda.getTextChannelById(channelId)?.apply {
            val time = LocalDateTime.now(ZoneId.of("Asia/Tokyo"))
            val formattedTime = formatter.format(time)
            sendMessage("Ping at $formattedTime").queue()
        }
    }
}