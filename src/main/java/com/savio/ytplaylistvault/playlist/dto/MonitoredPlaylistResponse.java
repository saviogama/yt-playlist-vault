package com.savio.ytplaylistvault.playlist.dto;

import com.savio.ytplaylistvault.playlist.MonitoredPlaylist;
import java.time.Instant;
import java.util.UUID;

public record MonitoredPlaylistResponse(
    UUID id,
    String providerPlaylistId,
    String title,
    String description,
    String thumbnailUrl,
    Instant createdAt,
    Instant updatedAt) {
  public static MonitoredPlaylistResponse from(MonitoredPlaylist playlist) {
    return new MonitoredPlaylistResponse(
        playlist.getId(),
        playlist.getProviderPlaylistId(),
        playlist.getTitle(),
        playlist.getDescription(),
        playlist.getThumbnailUrl(),
        playlist.getCreatedAt(),
        playlist.getUpdatedAt());
  }
}
