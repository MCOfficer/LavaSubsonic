package me.mcofficer.plugin

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack
import com.sedmelluq.discord.lavaplayer.tools.Units.DURATION_MS_UNKNOWN
import com.sedmelluq.discord.lavaplayer.track.*
import dev.zt64.subsonic.api.model.AlbumInfo
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
        private val LOG: Logger = LoggerFactory.getLogger(SubsonicAudioSourceManager::class.java)
    }

    override fun getSourceName(): String? {
        return LavaSubsonicPlugin.SOURCE_NAME
    }

    fun matchIdentifier(identifier: String, prefix: String, pattern: Regex?): String? {
        if (pattern != null) {
            val match = pattern.matchEntire(identifier)
            return match?.groups?.get(1)?.value
        } else if (identifier.startsWith(prefix)) {
            return identifier.substring(prefix.length)
        }
        return null
    }

    override fun loadItem(manager: AudioPlayerManager?, reference: AudioReference): AudioItem? {
        val identifier = reference.identifier
        val prefixes = serverConfig.matchPrefixes
        val patterns = serverConfig.matchPatterns

        val audioItem = runBlocking {
            matchIdentifier(identifier, prefixes.song, patterns?.song)?.let {
                return@runBlocking loadSong(it, manager)
            }
            matchIdentifier(identifier, prefixes.album, patterns?.album)?.let {
                return@runBlocking loadAlbum(it, manager)
            }
            matchIdentifier(identifier, prefixes.artist, patterns?.artist)?.let {
                return@runBlocking loadArtist(it, manager)
            }
            matchIdentifier(identifier, prefixes.playlist, patterns?.playlist)?.let {
                return@runBlocking loadPlaylist(it, manager)
            }
            matchIdentifier(identifier, prefixes.search, patterns?.search)?.let {
                return@runBlocking loadSearchResults(it, manager)
            }
        }

        return audioItem
    }

    suspend fun loadSong(identifier: String, manager: AudioPlayerManager?): AudioItem {
        return createTrack(client.getSong(identifier), manager)
    }

    suspend fun loadAlbum(identifier: String, manager: AudioPlayerManager?): AudioItem {
        val album = client.getAlbum(identifier)
        val albumInfo = client.getAlbumInfo(identifier)
        val tracks = album.songs.map { song -> createTrack(song, manager, albumInfo) }.toList()
        return BasicAudioPlaylist(album.name, tracks, tracks.firstOrNull(), false)
    }

    suspend fun loadArtist(identifier: String, manager: AudioPlayerManager?): AudioItem? {
        TODO()
    }

    suspend fun loadPlaylist(identifier: String, manager: AudioPlayerManager?): AudioItem? {
        TODO()
    }

    suspend fun loadSearchResults(identifier: String, manager: AudioPlayerManager?): AudioItem {
        val response = client.searchID3(identifier, 0, 0, 0, 0, 20, 0, null)
        val tracks = response.songs.map { song -> createTrack(song, manager) }.toList()
        return BasicAudioPlaylist("Search results for '${identifier}'", tracks, tracks.firstOrNull(), true)
    }


    private fun createTrack(
        song: Song, manager: AudioPlayerManager?, albumInfo: AlbumInfo? = null
    ): SubsonicAudioTrack {
        val trackInfo = AudioTrackInfo(
            song.title,
            song.displayArtist ?: song.artistName,
            song.duration?.inWholeMilliseconds ?: DURATION_MS_UNKNOWN,
            song.id, // TODO: use song: prefixes internally to distinguish from albums etc?
            false,
            null, // Would love to use the ID here, but some bots rely on uri being a valid URL
            runBlocking { fetchArtworkUrl(song, albumInfo) },
            song.isrc.firstOrNull()
        )

        // NOTE: The stream URL contains the password / API key, so it must not be part of the public track info
        var streamUrl = getStreamUrl(trackInfo)
        val httpReference = AudioReference(streamUrl, song.title)
        val httpTrack = super.loadItem(manager, httpReference) as HttpAudioTrack

        val track = SubsonicAudioTrack(trackInfo, this, httpTrack)
        return track
    }

    suspend fun fetchArtworkUrl(song: Song, albumInfo: AlbumInfo?): String? {
        var albumInfo = albumInfo

        if (albumInfo != null && serverConfig.fetchArtworkUri)
            albumInfo = song.albumId?.let { client.getAlbumInfo(it) }

        return albumInfo?.largeImageUrl
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
