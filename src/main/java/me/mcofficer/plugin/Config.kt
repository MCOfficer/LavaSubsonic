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

        /**
         * Not required if username & password is set
         */
        var apiKey: String? = null

        /**
         * passed to the stream endpoint, f.e. "mp3", "opus". See https://opensubsonic.netlify.app/docs/endpoints/stream/
         * default is null, which streams the audio file unmodified. Transcoding may still be required, but is done by lavaplayer.
         */
        var transcodeFormat: String? = null

        /**
         * passed to the stream endpoint, see https://opensubsonic.netlify.app/docs/endpoints/stream/
         * default is 0, which means no restriction.
         */
        var maxBitRate: Int = 0

        /**
         *  Make an extra API call for each track to fetch the artwork uri. Disable if you don't need it.
         */
        var fetchArtworkUri: Boolean = true

        /**
         * Configure prefixes to match identifiers as belonging to this Subsonic server. Default prefixes are:
         *   song: "subsong:"
         *   album: "subalbum:"
         *   artist: "subartist:"
         *   playlist: "subplaylist:"
         *   search: "subsearch:"
         * Anything after the prefix is interpreted as ID or search query.
         */
        var matchPrefixes: SubsonicPrefixes = SubsonicPrefixes()

        /**
         * Same as "matchPrefixes", but uses regular expressions instead.
         * These can be useful if you want to match URLs of your Subsonic instance.
         * If you define this, prefixes are ignored (meaning, you must define all patterns!).
         * Each pattern must have one capture group containing the ID or search query.
         * The patterns should only match identifiers belonging to your server.
         *
         * For example, the following pattern would match artist URLs of a Navidrome instance:
         *     https://YOUR_NAVIDROME_URL.ORG/app/#/artist/([\w]+)/show
         * Note that you don't have to escape the forward slashes.
         */
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