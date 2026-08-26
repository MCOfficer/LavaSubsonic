package me.mcofficer.plugin

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "plugins.subsonic")
class Config {

    var servers: List<SubsonicServer> = emptyList()


    class SubsonicServer {

        var name: String? = null

        var baseUrl: String? = null

        var username: String? = null
        var password: String? = null

        /// Not required if username & password is set
        var apiKey: String? = null

        /// passed to the stream endpoint, f.e. "mp3", "opus". See https://opensubsonic.netlify.app/docs/endpoints/stream/
        /// default is null, which streams the audio file unmodified. Transcoding may still be required, but is done by lavaplayer.
        var transcodeFormat: String? = null

        /// passed the stream endpoint, see https://opensubsonic.netlify.app/docs/endpoints/stream/
        /// default is 0, which means no restriction.
        var maxBitRate: Int = 0

        /// Make an extra API call for each track to fetch the artwork uri. Disable if you don't need it.
        var fetchArtworkUri: Boolean = true
    }
}
