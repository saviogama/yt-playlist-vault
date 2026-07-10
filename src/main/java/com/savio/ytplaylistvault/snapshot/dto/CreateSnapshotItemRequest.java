package com.savio.ytplaylistvault.snapshot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;

public record CreateSnapshotItemRequest(
    @NotBlank String youtubeVideoId,
    @NotBlank String title,
    String channelTitle,
    String thumbnailUrl,
    @PositiveOrZero int position,
    @NotNull Instant addedToPlaylistAt) {}
