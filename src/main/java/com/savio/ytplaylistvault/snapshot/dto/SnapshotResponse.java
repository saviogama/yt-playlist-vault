package com.savio.ytplaylistvault.snapshot.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.savio.ytplaylistvault.snapshot.Snapshot;
import com.savio.ytplaylistvault.snapshot.SnapshotItem;

public record SnapshotResponse(
        UUID id,
        UUID monitoredPlaylistId,
        Instant capturedAt,
        int itemCount,
        Instant createdAt,
        List<SnapshotItemResponse> items
) {
    public static SnapshotResponse from(Snapshot snapshot, List<SnapshotItem> items) {
        return new SnapshotResponse(
                snapshot.getId(),
                snapshot.getMonitoredPlaylist().getId(),
                snapshot.getCapturedAt(),
                snapshot.getItemCount(),
                snapshot.getCreatedAt(),
                items.stream()
                        .map(SnapshotItemResponse::from)
                        .toList()
        );
    }
}
