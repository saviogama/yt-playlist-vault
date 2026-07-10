package com.savio.ytplaylistvault.snapshot.dto;

import java.util.List;
import java.util.UUID;

public record SnapshotDiffResponse(
    UUID snapshotId,
    UUID previousSnapshotId,
    List<SnapshotDiffItemResponse> addedItems,
    List<SnapshotDiffItemResponse> removedItems,
    List<SnapshotMovedItemResponse> movedItems) {}
