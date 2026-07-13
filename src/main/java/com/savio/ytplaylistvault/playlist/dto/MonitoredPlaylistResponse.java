package com.savio.ytplaylistvault.playlist.dto;

import com.savio.ytplaylistvault.playlist.MonitoredPlaylist;
import com.savio.ytplaylistvault.playlist.MonitoringStatus;
import java.time.Instant;
import java.util.UUID;

public record MonitoredPlaylistResponse(
    UUID id,
    String providerPlaylistId,
    String title,
    String description,
    String thumbnailUrl,
    MonitoringStatus monitoringStatus,
    Instant lastCheckedAt,
    Instant lastSnapshotAt,
    Instant lastChangeDetectedAt,
    int snapshotCount,
    Instant createdAt,
    Instant updatedAt) {
  public static MonitoredPlaylistResponse from(MonitoredPlaylist playlist) {
    return new MonitoredPlaylistResponse(
        playlist.getId(),
        playlist.getProviderPlaylistId(),
        playlist.getTitle(),
        playlist.getDescription(),
        playlist.getThumbnailUrl(),
        playlist.getMonitoringStatus(),
        playlist.getLastCheckedAt(),
        playlist.getLastSnapshotAt(),
        playlist.getLastChangeDetectedAt(),
        playlist.getSnapshotCount(),
        playlist.getCreatedAt(),
        playlist.getUpdatedAt());
  }
}
