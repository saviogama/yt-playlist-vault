package com.savio.ytplaylistvault.snapshot.dto;

public record SnapshotDiffItemResponse(
    String providerItemId, String title, String creatorName, String thumbnailUrl, int position) {}
