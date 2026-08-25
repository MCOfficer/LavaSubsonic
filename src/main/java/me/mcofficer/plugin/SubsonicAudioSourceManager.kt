package me.mcofficer.plugin;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import dev.zt64.subsonic.api.model.Song;
import dev.zt64.subsonic.client.SubsonicClient;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.Dispatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MimeType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SubsonicAudioSourceManager implements AudioSourceManager {

    private static final Logger LOG = LoggerFactory.getLogger(SubsonicAudioSourceManager.class);

    public static final String SEARCH_PREFIX = "subsearch:"; // TODO - allow to configure? support multiple servers?
    public static final String SUBSONIC_PREFIX = "subsonic:"; // TODO - allow to configure? support multiple servers?

    SubsonicClient client;

    public SubsonicAudioSourceManager(SubsonicClient client) {
        this.client = client;
    }

    public SubsonicClient getClient() {
        return client;
    }

    @Override
    public String getSourceName() {
        return LavaSubsonicPlugin.SOURCE_NAME;
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        String requestedId;
        if (reference.identifier.startsWith(SEARCH_PREFIX)) {
            return getSearchResult(reference.identifier.substring(SEARCH_PREFIX.length()));
        } else if (!reference.identifier.startsWith("subsonic:")) {
            return null;
        } else {
            requestedId = reference.identifier.substring(SEARCH_PREFIX.length() - 1);
        }

        Song song;
        try {
            song = BuildersKt.runBlocking(Dispatchers.getIO(),
                    (scope, continuation) -> KotlinWrapperKt.getSong(client, requestedId, continuation));

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        var info = KotlinWrapperKt.extractSongInfo(song);
        var mimeType = MimeType.valueOf(song.getMimeType());
        var track = new SubsonicAudioTrack(info, this, mimeType);

        return track;
    }

    public AudioItem getSearchResult(String identifier) {
        return null;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        ((SubsonicAudioTrack) track).encodeTrack(output);
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return SubsonicAudioTrack.decodeTrack(trackInfo, this, input);
    }


    @Override
    public void shutdown() {
        this.client.close();
    }
}
