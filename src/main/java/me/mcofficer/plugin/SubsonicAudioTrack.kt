package me.mcofficer.plugin;

import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;

public class SubsonicAudioTrack extends DelegatedAudioTrack {

    SubsonicAudioSourceManager sourceManager;
    HttpAudioTrack internalTrack;

    /**
     * @param trackInfo     Track info
     * @param sourceManager Source manager used to load this track
     */
    public SubsonicAudioTrack(AudioTrackInfo trackInfo, SubsonicAudioSourceManager sourceManager,
                              HttpAudioTrack internalTrack) {
        super(trackInfo);
        this.internalTrack = internalTrack;
        this.sourceManager = sourceManager;
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return sourceManager;
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        processDelegate(internalTrack, executor);
    }

    public MediaContainerDescriptor getContainerTrackFactory() {
        return internalTrack.getContainerTrackFactory();
    }
}
