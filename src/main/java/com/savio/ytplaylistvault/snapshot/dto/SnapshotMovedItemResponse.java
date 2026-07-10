package com.savio.ytplaylistvault.snapshot.dto;

public record SnapshotMovedItemResponse(
    String providerItemId,
    String title,
    String creatorName,
    String thumbnailUrl,
    int previousPosition,
    int currentPosition) {}
