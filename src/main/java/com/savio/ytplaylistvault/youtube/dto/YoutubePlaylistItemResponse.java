package com.savio.ytplaylistvault.youtube.dto;

import java.time.Instant;

public record YoutubePlaylistItemResponse(
    String providerItemId,
    String title,
    String creatorName,
    String thumbnailUrl,
    int position,
    Instant addedToPlaylistAt) {}
