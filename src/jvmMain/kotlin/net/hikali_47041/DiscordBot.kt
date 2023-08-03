package net.hikali_47041

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.audio.SpeakingMode
import org.slf4j.LoggerFactory
import java.io.File
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

private class AudioFileHandler(
    audioPath: String,
    private val playerManager: AudioPlayerManager
) {
    private val logger = LoggerFactory.getLogger("AudioFileHandler")

    private val tracks = mutableSetOf<AudioTrack>()

    init {
        val file = File(audioPath)
        if(file.isFile)
            loadTrack(audioPath)
        if(file.isDirectory)
            file.walk().forEach {
                if(it.isFile)
                    loadTrack(it.absolutePath)
            }
    }

    fun pickAudio(): AudioTrack? {
        return tracks.randomOrNull()
    }

    private fun loadTrack(path: String) {
        playerManager.loadItem(path, object: AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack?) {
                track?.let {
                    logger.debug("loaded: ${it.info.uri}")
                    tracks += it
                }
            }

            override fun playlistLoaded(playlist: AudioPlaylist?) {
                playlist?.tracks?.let {
                    tracks.addAll(it)
                }
            }

            override fun noMatches() {
                logger.error("audio file not found")
            }

            override fun loadFailed(exception: FriendlyException?) {
                logger.error("failed to load audio file")
            }
        })
    }

}

class DiscordBot(
    discordBotToken: String,
    private val channelId: String,
    private val voiceChannelId: String,
    private val audioPath: String,
    private val entryAudioPath: String
) {

    private val logger = LoggerFactory.getLogger("DiscordBot")

    private val jda = JDABuilder.createDefault(discordBotToken).build()
    private val playerManager = DefaultAudioPlayerManager()
    private val player = playerManager.createPlayer()
    private val audioPlayerSendHandler = AudioPlayerSendHandler(player)
    private var audioFileHandler: AudioFileHandler? = null
    private var entryAudioFileHandler: AudioFileHandler? = null

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS")
    }

    fun start() {
        AudioSourceManagers.registerLocalSource(playerManager)
        jda.awaitReady()
        audioFileHandler = AudioFileHandler(audioPath, playerManager)
        entryAudioFileHandler = AudioFileHandler(entryAudioPath, playerManager)
        CoroutineScope(Dispatchers.Default).launch {
            delay(3000)
            connectToVoiceChannel()
        }
    }

    private fun connectToVoiceChannel() {
        logger.debug("trying to connect to voice channel")
        val voiceChannel = jda.getVoiceChannelById(voiceChannelId) ?: return
        val audioManager = voiceChannel.guild.audioManager
        audioManager.setSpeakingMode(SpeakingMode.SOUNDSHARE)
        audioManager.sendingHandler = audioPlayerSendHandler
        audioManager.openAudioConnection(voiceChannel)
        logger.debug("voice channel connected")
    }

    fun sendEntrySound() {
        logger.debug("entry")
        entryAudioFileHandler?.pickAudio()?.let {
            player.stopTrack()
            player.playTrack(it.makeClone())
        }
    }

    fun sendNotify() {
        logger.debug("send notify")
        audioFileHandler?.pickAudio()?.let {
            player.stopTrack()
            player.playTrack(it.makeClone())
        }

        jda.getTextChannelById(channelId)?.apply {
            val time = LocalDateTime.now(ZoneId.of("Asia/Tokyo"))
            val formattedTime = formatter.format(time)
            sendMessage("Ping at $formattedTime").queue()
        }
    }
}