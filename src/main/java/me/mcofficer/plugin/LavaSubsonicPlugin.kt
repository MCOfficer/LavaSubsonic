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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class LavaSubsonicPlugin implements AudioPlayerManagerConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(LavaSubsonicPlugin.class);

    static String USER_AGENT = "subsonic-kotlin (github.com/zt64/subsonic-kotlin)";
    static String SOURCE_NAME = "subsonic-kotlin";

    private final Config config;

    public LavaSubsonicPlugin(Config config) {
        this.config = config;
    }


    @NotNull
    @Override
    public AudioPlayerManager configure(@NotNull AudioPlayerManager manager) {

        for (var server : config.getServers()) {
            var sourceManager = createSourceManager(server);
            if (sourceManager == null) continue;
            manager.registerSourceManager(sourceManager);
        }

        return manager;
    }

    private static @Nullable SubsonicAudioSourceManager createSourceManager(Config.SubsonicServer server) {
        SubsonicAuth auth;
        if (server.getApiKey() != null) {
            auth = new SubsonicAuth.Key(server.getApiKey());
        } else if (server.getUsername() == null || server.getPassword() == null) {
            LOG.error("Subsonic configuration failed, requires an API-Key or Username & Password");
            return null;
        } else {
            auth = SubsonicAuth.Token.Companion.invoke(server.getUsername(), server.getPassword());
        }

        var engine = new JavaHttpEngine(new JavaHttpConfig());
        Function1<HttpClientConfig<?>, Unit> clientConfiguration = (clientConfig) -> {
            // Place to configure the engine, f.e.
            // clientConfig.setFollowRedirects(true);
            return Unit.INSTANCE;
        };

        var client = SubsonicClient.Companion.invoke(server.getBaseUrl(), auth, SOURCE_NAME, USER_AGENT, engine,
                clientConfiguration);

        LOG.info("Subsonic server '{}' configured successfully", server.getName());
        return new SubsonicAudioSourceManager(server, client);
    }

}
