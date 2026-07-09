package com.savio.ytplaylistvault.playlist.dto;

import java.time.Instant;
import java.util.UUID;

import com.savio.ytplaylistvault.playlist.MonitoredPlaylist;

public record MonitoredPlaylistResponse(
        UUID id,
        UUID userId,
        String youtubePlaylistId,
        String title,
        String description,
        String thumbnailUrl,
        Instant createdAt,
        Instant updatedAt
) {
    public static MonitoredPlaylistResponse from(MonitoredPlaylist playlist) {
        return new MonitoredPlaylistResponse(
                playlist.getId(),
                playlist.getUser().getId(),
                playlist.getYoutubePlaylistId(),
                playlist.getTitle(),
                playlist.getDescription(),
                playlist.getThumbnailUrl(),
                playlist.getCreatedAt(),
                playlist.getUpdatedAt()
        );
    }
}
