package com.savio.ytplaylistvault.snapshot.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateSnapshotItemRequest(
        @NotBlank String youtubeVideoId,
        @NotBlank String title,
        String channelTitle,
        String thumbnailUrl,
        @PositiveOrZero int position,
        @NotNull Instant addedToPlaylistAt
) {
    
}
