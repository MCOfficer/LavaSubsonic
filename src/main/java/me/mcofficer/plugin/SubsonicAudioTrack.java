package me.mcofficer.plugin;

import com.sedmelluq.discord.lavaplayer.container.flac.FlacAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.io.NonSeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.*;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.Dispatchers;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.util.MimeType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;

public class SubsonicAudioTrack extends DelegatedAudioTrack {

    SubsonicAudioSourceManager sourceManager;
    MimeType mimeType;

    /**
     * @param trackInfo     Track info
     * @param sourceManager Source manager used to load this track
     */
    public SubsonicAudioTrack(AudioTrackInfo trackInfo, SubsonicAudioSourceManager sourceManager, MimeType mimeType) {
        super(trackInfo);
        this.sourceManager = sourceManager;
        this.mimeType = mimeType;
    }


    @Override
    public SubsonicAudioSourceManager getSourceManager() {
        return sourceManager;
    }

    @Override
    public void process(LocalAudioTrackExecutor localExecutor) throws Exception {
        try (InputStream inputStream = download()) {
            var track = TrackFactory.createAudioTrack(mimeType, trackInfo, inputStream);
            processDelegate(track, localExecutor);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream download() throws InterruptedException {
        return BuildersKt.runBlocking(Dispatchers.getIO(),
                (scope, continuation) -> KotlinWrapperKt.download(sourceManager.getClient(), trackInfo.identifier,
                        continuation));
    }

    public void encodeTrack(DataOutput output) throws IOException {
        output.writeUTF(mimeType.toString());
    }

    public static SubsonicAudioTrack decodeTrack(AudioTrackInfo trackInfo, SubsonicAudioSourceManager sourceManager,
                                                 DataInput input) throws IOException {
        var mime = MimeType.valueOf(input.readUTF());
        return new SubsonicAudioTrack(trackInfo, sourceManager, mime);
    }


    private static class TrackFactory {
        static MimeType MIME_FLAC = MimeType.valueOf("audio/flac");


        static InternalAudioTrack createAudioTrack(MimeType mime, AudioTrackInfo trackInfo,
                                                   InputStream inputStream) {
            if (mime.equals(MIME_FLAC)) {
                return new FlacAudioTrack(trackInfo, new NonSeekableInputStream(inputStream));
            } else {
                throw new InvalidMediaTypeException(mime.toString(), "Unsupported audio format");
            }
        }
    }
}
