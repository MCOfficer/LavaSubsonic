package me.mcofficer.plugin

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack
import com.sedmelluq.discord.lavaplayer.tools.Units.DURATION_MS_UNKNOWN
import com.sedmelluq.discord.lavaplayer.track.*
import dev.zt64.subsonic.api.model.Song
import dev.zt64.subsonic.client.SubsonicClient
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.DataInput
import java.io.DataOutput

class SubsonicAudioSourceManager(var serverConfig: Config.SubsonicServer, var client: SubsonicClient) :
    HttpAudioSourceManager() {
    companion object {
        private val LOG: Logger? = LoggerFactory.getLogger(SubsonicAudioSourceManager::class.java)

        const val SEARCH_PREFIX: String = "subsearch:" // TODO - allow to configure
        const val SUBSONIC_PREFIX: String = "subsonic:" // TODO - allow to configure?
    }

    override fun getSourceName(): String? {
        return LavaSubsonicPlugin.SOURCE_NAME
    }

    override fun loadItem(manager: AudioPlayerManager?, reference: AudioReference): AudioItem? {
        val requestedId: String?
        if (reference.identifier.startsWith(SEARCH_PREFIX)) {
            return runBlocking {
                getSearchResult(manager, reference.identifier.substring(SEARCH_PREFIX.length))
            }
        } else if (!reference.identifier.startsWith(SUBSONIC_PREFIX)) {
            return null
        } else {
            requestedId = reference.identifier.substring(SUBSONIC_PREFIX.length)
        }

        val song: Song
        try {
            song = runBlocking {
                client.getSong(requestedId)
            }

        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }

        return createTrack(song, manager)
    }

    private fun createTrack(
        song: Song, manager: AudioPlayerManager?
    ): SubsonicAudioTrack {
        val trackInfo = AudioTrackInfo(
            song.title,
            song.displayArtist ?: song.artistName,
            song.duration?.inWholeMilliseconds ?: DURATION_MS_UNKNOWN,
            song.id, // TODO: use song: prefixes internally to distinguish from albums etc?
            false,
            null, // Would love to use the ID here, but some bots rely on uri being a valid URL
            runBlocking { fetchArtworkUrl(song) },
            song.isrc.firstOrNull()
        )

        // NOTE: The stream URL contains the password / API key, so it must not be part of the public track info
        var streamUrl = getStreamUrl(trackInfo)
        val httpReference = AudioReference(streamUrl, song.title)
        val httpTrack = super.loadItem(manager, httpReference) as HttpAudioTrack

        val track = SubsonicAudioTrack(trackInfo, this, httpTrack)
        return track
    }

    suspend fun fetchArtworkUrl(song: Song): String? {
        if (serverConfig.fetchArtworkUri && song.albumId != null) {
            val info = client.getAlbumInfo(song.albumId!!)
            return info.largeImageUrl
        }
        return null
    }

    suspend fun getSearchResult(manager: AudioPlayerManager?, identifier: String): AudioItem {
        val response = client.searchID3(identifier, 0, 0, 0, 0, 20, 0, null)
        val tracks = response.songs.map { song -> createTrack(song, manager) }.toList()
        return BasicAudioPlaylist("Search results for '${identifier}'", tracks, tracks.firstOrNull(), true)
    }


    override fun encodeTrack(track: AudioTrack, output: DataOutput?) {
        encodeTrackFactory((track as SubsonicAudioTrack).containerTrackFactory, output)
    }

    override fun decodeTrack(trackInfo: AudioTrackInfo?, input: DataInput?): AudioTrack? {
        if (trackInfo == null) return null

        val streamUrl = getStreamUrl(trackInfo)
        val internalTrackInfo = AudioTrackInfo(
            trackInfo.title, trackInfo.author, trackInfo.length, streamUrl, trackInfo.isStream, trackInfo.uri
        )
        val internalTrack = super.decodeTrack(internalTrackInfo, input) as? HttpAudioTrack? ?: return null

        return SubsonicAudioTrack(trackInfo, this, internalTrack)
    }

    private fun getStreamUrl(trackInfo: AudioTrackInfo): String {
        return client.getStreamUrl(
            trackInfo.identifier, serverConfig.maxBitRate, serverConfig.transcodeFormat, null, true
        )
    }

    override fun shutdown() {
        this.client.close()
        super.shutdown()
    }
}
