package com.savio.ytplaylistvault.snapshot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;

public record CreateSnapshotItemRequest(
    @NotBlank String providerItemId,
    @NotBlank String title,
    String creatorName,
    String thumbnailUrl,
    @PositiveOrZero int position,
    @NotNull Instant addedToPlaylistAt) {}
