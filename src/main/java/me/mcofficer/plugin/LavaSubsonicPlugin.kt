package me.mcofficer.plugin

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import dev.arbjerg.lavalink.api.AudioPlayerManagerConfiguration
import dev.zt64.subsonic.client.SubsonicAuth
import dev.zt64.subsonic.client.SubsonicClient
import io.ktor.client.*
import io.ktor.client.engine.java.*
import me.mcofficer.plugin.Config.SubsonicServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class LavaSubsonicPlugin(private val config: Config) : AudioPlayerManagerConfiguration {

    companion object {
        var SOURCE_NAME: String = "subsonic-kotlin"
        var USER_AGENT: String = "subsonic-kotlin (github.com/zt64/subsonic-kotlin)"
        private val LOG: Logger = LoggerFactory.getLogger(LavaSubsonicPlugin::class.java)
    }

    override fun configure(manager: AudioPlayerManager): AudioPlayerManager {
        for (server in config.servers) {
            val sourceManager: SubsonicAudioSourceManager = createSourceManager(server) ?: continue
            manager.registerSourceManager(sourceManager)
        }

        return manager
    }

    private fun createSourceManager(server: SubsonicServer): SubsonicAudioSourceManager? {
        val auth = if (server.apiKey != null) {
            SubsonicAuth.Key(server.apiKey!!)
        } else if (server.username == null || server.password == null) {
            LOG.error("Subsonic configuration failed, requires an API-Key or Username & Password")
            return null
        } else {
            SubsonicAuth.Token(server.username!!, server.password!!)
        }

        val engine = JavaHttpEngine(JavaHttpConfig())
        val clientConfiguration = { _: HttpClientConfig<*>? -> }


        val client = SubsonicClient(
            server.baseUrl!!, auth, SOURCE_NAME, USER_AGENT, engine, clientConfiguration
        )

        LOG.info("Subsonic server '{}' configured successfully", server.name)
        return SubsonicAudioSourceManager(server, client)
    }

}
