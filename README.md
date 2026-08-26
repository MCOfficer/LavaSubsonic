# LavaSubsonic

A LavaLink plugin to connect to Subsonic-compatible music servers.

## Installation

Add the following to your `application.yaml`:

```yml
lavalink:
  plugins:
    # Replace VERSION with the latest version or short commit hash
    - dependency: com.github.MCOfficer.LavaSubsonic:lava-subsonic:VERSION
      repository: https://jitpack.io
```

## Configuration

```yaml
plugins:
  subsonic:
    servers:

      # A Subsonic server. You can define multiple servers and configure them separately.
      - name: MyServer

        # The server base URL, not including `/rest`
        baseUrl: https://my-subsonic.domain.net

        # For authentication, you probably want to create a dedicated user.
        # This also allows you to restrict access to music folders. 
        # Either apiKey OR username&password are required to log in. If present, apiKey takes precedence.
        username: my-lavalink-user
        password: a-strong-password
        apiKey: null # default

        # passed to the stream endpoint, f.e. "mp3", "opus". See https://opensubsonic.netlify.app/docs/endpoints/stream/
        transcodeFormat: null # default, means unmodified streaming. Make sure all your audio formats are supported by lavalink!

        # passed to the stream endpoint, see https://opensubsonic.netlify.app/docs/endpoints/stream/
        maxBitrate: 0 # default, no restriction

        # Make an extra API call for each track to fetch the artwork uri. Disable if you don't use the artwork URI anyway
        fetchArtworkUri: true # default

        # Prefixes that match identifiers belonging to this Subsonic server.
        # Anything after the prefix is considered a Subsonic ID (or query, in the case of `search`).
        # These are usually URLs (like `https.youtube.com/watch?v=` ), though the defaults are simple prefixes.
        matchPrefixes:
          song: "subsong:" # default
          album: "subalbum:" # default
          artist: "subartist:" # default
          playlist: "subplaylist:" # default
          search: "subsearch:" # default

        # Same structure as `matchPrefixes`, but uses Regular Expressions.
        # These are useful if you have to match IDs in more complex URLs.
        # Patterns MUST have one capture group, which has to capture the subsonic ID.
        #
        # For example, this is part of a configuration for a Navidrome server:
        # matchPatterns:
        #   artist: "^https://my-navidrome.domain.net/app/#/artist/([\\w]+)/show$"
        #
        # Defining matchPatterns will override matchPrefixes of the same name, so in this example,
        # the default prefix for artists ("subartists:") would not be used.
        # 
        # Make sure your patterns only match queries belonging to this server!
        matchPatterns: null # default
```
