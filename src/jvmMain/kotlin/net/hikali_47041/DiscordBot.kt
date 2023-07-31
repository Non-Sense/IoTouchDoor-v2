package net.hikali_47041

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.audio.AudioSendHandler
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private class AudioPlayerSendHandler(
    private val audioPlayer: AudioPlayer
): AudioSendHandler {

    private var lastFrame: AudioFrame? = null

    override fun canProvide(): Boolean {
        lastFrame = audioPlayer.provide()
        return lastFrame != null
    }

    override fun provide20MsAudio(): ByteBuffer? {
        return ByteBuffer.wrap(lastFrame?.data)
    }

    override fun isOpus(): Boolean {
        return true
    }

}

class DiscordBot(
    discordBotToken: String,
    private val channelId: String,
    private val voiceChannelId: String,
    private val audioPath: String
) {

    private val logger = LoggerFactory.getLogger("DiscordBot")

    private val jda = JDABuilder.createDefault(discordBotToken).build()
    private val playerManager = DefaultAudioPlayerManager()
    private val player = playerManager.createPlayer()
    private val audioPlayerSendHandler = AudioPlayerSendHandler(player)
    private var audioTrack: AudioTrack? = null

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS")
    }

    fun start() {
        AudioSourceManagers.registerLocalSource(playerManager)
        jda.awaitReady()
        playerManager.loadItem(audioPath, object: AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack?) {
                audioTrack = track
                connectToVoiceChannel()
            }

            override fun playlistLoaded(playlist: AudioPlaylist?) {
            }

            override fun noMatches() {
                logger.error("audio file not found")
            }

            override fun loadFailed(exception: FriendlyException?) {
                logger.error("failed to load audio file")
            }
        })
    }

    private fun connectToVoiceChannel() {
        logger.debug("trying to connect to voice channel")
        val voiceChannel = jda.getVoiceChannelById(voiceChannelId) ?: return
        val audioManager = voiceChannel.guild.audioManager
        audioManager.sendingHandler = audioPlayerSendHandler
        audioManager.openAudioConnection(voiceChannel)
        logger.debug("voice channel connected")
    }

    fun sendNotify() {
        logger.debug("send notify")
        audioTrack?.let {
            player.playTrack(it.makeClone())
        }

        jda.getTextChannelById(channelId)?.apply {
            val time = LocalDateTime.now(ZoneId.of("Asia/Tokyo"))
            val formattedTime = formatter.format(time)
            sendMessage("Ping at $formattedTime").queue()
        }
    }
}