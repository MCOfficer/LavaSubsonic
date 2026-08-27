package me.mcofficer.plugin

import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor

class SubsonicAudioTrack
/**
 * @param trackInfo     Track info
 * @param sourceManager Source manager used to load this track
 */(
    trackInfo: AudioTrackInfo?, var sourceManager: SubsonicAudioSourceManager?, var internalTrack: HttpAudioTrack
) : DelegatedAudioTrack(trackInfo) {

    val containerTrackFactory: MediaContainerDescriptor?
        get() = internalTrack.containerTrackFactory

    override fun getSourceManager(): AudioSourceManager? {
        return sourceManager
    }

    @Throws(Exception::class)
    override fun process(executor: LocalAudioTrackExecutor?) {
        processDelegate(internalTrack, executor)
    }
}
