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

        var apiKey: String? = null

        var transcodeFormat: String? = null

        var maxBitRate: Int = 0

        var fetchArtworkUri: Boolean = true

        var matchPrefixes: SubsonicPrefixes = SubsonicPrefixes()
        var matchPatterns: SubsonicPatterns? = null
    }

    class SubsonicPrefixes {
        var song: String = "subsong:"
        var album: String = "subalbum:"
        var artist: String = "subartist:"
        var playlist: String = "subplaylist:"
        var search: String = "subsearch:"
    }

    class SubsonicPatterns {
        var song: Regex? = null
        var album: Regex? = null
        var artist: Regex? = null
        var playlist: Regex? = null
        var search: Regex? = null
    }
}