package me.mcofficer.plugin

import com.sedmelluq.discord.lavaplayer.tools.Units.DURATION_MS_UNKNOWN
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import dev.zt64.subsonic.api.model.Song
import dev.zt64.subsonic.client.SubsonicClient
import io.ktor.utils.io.asSource
import kotlinx.io.asInputStream
import kotlinx.io.buffered
import java.io.InputStream

// 1. Wraps Subsonic calls that use generics and suspends
// 2. Wraps calls that work better in kotlin, like accessing Components

suspend fun getSong(client: SubsonicClient, id: String): Song {
    return client.getSong(id)
}

suspend fun download(client: SubsonicClient, id: String): InputStream {
    return client.download(id).asSource().buffered().asInputStream()
    // TODO: get stream URL and use defer to HttpAudioSource instead?
}

fun extractSongInfo(song: Song): AudioTrackInfo {
    return AudioTrackInfo(
        song.title,
        song.displayArtist ?: song.artistName,
        song.duration?.inWholeMilliseconds ?: DURATION_MS_UNKNOWN,
        song.id, // TODO: use song: prefixes internally to distinguish from albums etc?
        false,
        null,
        coverArtUrl(song.musicBrainzId),
        song.isrc.firstOrNull()
    )
}

fun coverArtUrl(mbid: String?): String? {
    if (mbid != null) {
        return "https://coverartarchive.org/release/${mbid}/front"
    }
    return null
}