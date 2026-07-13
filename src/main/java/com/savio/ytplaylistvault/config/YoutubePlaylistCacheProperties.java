package com.savio.ytplaylistvault.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cache.youtube-playlists")
public record YoutubePlaylistCacheProperties(Duration ttl) {}
