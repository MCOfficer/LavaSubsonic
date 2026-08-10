package me.mcofficer.plugin;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import dev.arbjerg.lavalink.api.AudioPlayerManagerConfiguration;
import dev.zt64.subsonic.client.SubsonicAuth;
import dev.zt64.subsonic.client.SubsonicClient;
import io.ktor.client.HttpClientConfig;
import io.ktor.client.engine.java.JavaHttpConfig;
import io.ktor.client.engine.java.JavaHttpEngine;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class LavaSubsonicPlugin implements AudioPlayerManagerConfiguration, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(LavaSubsonicPlugin.class);

    static String USER_AGENT = "subsonic-kotlin (github.com/zt64/subsonic-kotlin)";
    static String SOURCE_NAME = "subsonic-kotlin";

    private SubsonicClient client;

    public LavaSubsonicPlugin(Config config) {
        SubsonicAuth auth = null;

        if (config.apiKey != null) {
            auth = new SubsonicAuth.Key(config.apiKey);
        } else if (config.getUsername() == null || config.getPassword() == null) {
            LOG.error("Subsonic configuration failed, requires an API-Key or Username & Password");
            return;
        } else {
            auth = new SubsonicAuth.Token(config.getUsername(), "TODOsalt123", config.getPassword());
        }

        JavaHttpEngine engine = new JavaHttpEngine(new JavaHttpConfig());
        Function1<HttpClientConfig<?>, Unit> clientConfiguration = (clientConfig) -> {
            // Place to configure the engine, f.e.
            // clientConfig.setFollowRedirects(true);
            return Unit.INSTANCE;
        };

        client = SubsonicClient.Companion.invoke(config.getBaseUrl(), auth, SOURCE_NAME, USER_AGENT, engine,
                clientConfiguration);

        LOG.info("Subsonic plugin configured successfully");
    }


    @NotNull
    @Override
    public AudioPlayerManager configure(@NotNull AudioPlayerManager audioPlayerManager) {
        audioPlayerManager.registerSourceManager(new SubsonicAudioSourceManager());
        return audioPlayerManager;
    }

    @Override
    public void close() throws Exception {
        this.client.close();
    }
}
