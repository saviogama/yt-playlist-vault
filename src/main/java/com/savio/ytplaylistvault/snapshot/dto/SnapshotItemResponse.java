package com.savio.ytplaylistvault.snapshot.dto;

import com.savio.ytplaylistvault.snapshot.SnapshotItem;
import java.time.Instant;
import java.util.UUID;

public record SnapshotItemResponse(
    UUID id,
    String youtubeVideoId,
    String title,
    String channelTitle,
    String thumbnailUrl,
    int position,
    Instant addedToPlaylistAt,
    Instant createdAt) {
  public static SnapshotItemResponse from(SnapshotItem item) {
    return new SnapshotItemResponse(
        item.getId(),
        item.getYoutubeVideoId(),
        item.getTitle(),
        item.getChannelTitle(),
        item.getThumbnailUrl(),
        item.getPosition(),
        item.getAddedToPlaylistAt(),
        item.getCreatedAt());
  }
}
