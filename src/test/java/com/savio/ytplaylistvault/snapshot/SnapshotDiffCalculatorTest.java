package com.savio.ytplaylistvault.snapshot;

import static org.assertj.core.api.Assertions.assertThat;

import com.savio.ytplaylistvault.playlist.MonitoredPlaylist;
import com.savio.ytplaylistvault.snapshot.dto.SnapshotDiffResponse;
import com.savio.ytplaylistvault.user.User;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class SnapshotDiffCalculatorTest {
  private final SnapshotDiffCalculator snapshotDiffCalculator = new SnapshotDiffCalculator();

  @Test
  void shouldCalculateAddedRemovedAndMovedItems() {
    MonitoredPlaylist playlist =
        new MonitoredPlaylist(
            new User("google-subject", "user@example.com", "User Example"),
            "playlist-id",
            "Playlist",
            "Description",
            null);
    Snapshot previousSnapshot = new Snapshot(playlist, Instant.parse("2026-07-10T10:00:00Z"), 2);
    Snapshot currentSnapshot = new Snapshot(playlist, Instant.parse("2026-07-10T10:05:00Z"), 2);

    SnapshotDiffResponse diff =
        snapshotDiffCalculator.calculate(
            currentSnapshot,
            previousSnapshot,
            List.of(
                createItem(currentSnapshot, "track-b", "Track B", 0),
                createItem(currentSnapshot, "track-c", "Track C", 1)),
            List.of(
                createItem(previousSnapshot, "track-a", "Track A", 0),
                createItem(previousSnapshot, "track-b", "Track B", 1)));

    assertThat(diff.snapshotId()).isEqualTo(currentSnapshot.getId());
    assertThat(diff.previousSnapshotId()).isEqualTo(previousSnapshot.getId());
    assertThat(diff.addedItems())
        .singleElement()
        .extracting(item -> item.providerItemId())
        .isEqualTo("track-c");
    assertThat(diff.removedItems())
        .singleElement()
        .extracting(item -> item.providerItemId())
        .isEqualTo("track-a");
    assertThat(diff.movedItems())
        .singleElement()
        .satisfies(
            item -> {
              assertThat(item.providerItemId()).isEqualTo("track-b");
              assertThat(item.previousPosition()).isEqualTo(1);
              assertThat(item.currentPosition()).isEqualTo(0);
            });
  }

  @Test
  void shouldTreatDistinctPlaylistItemsAsSeparateEntriesWhenTheirMediaMetadataMatches() {
    MonitoredPlaylist playlist =
        new MonitoredPlaylist(
            new User("google-subject", "user@example.com", "User Example"),
            "playlist-id",
            "Playlist",
            "Description",
            null);
    Snapshot previousSnapshot = new Snapshot(playlist, Instant.parse("2026-07-10T10:00:00Z"), 2);
    Snapshot currentSnapshot = new Snapshot(playlist, Instant.parse("2026-07-10T10:05:00Z"), 2);

    SnapshotDiffResponse diff =
        snapshotDiffCalculator.calculate(
            currentSnapshot,
            previousSnapshot,
            List.of(
                createItem(currentSnapshot, "playlist-item-b", "Same Track", 0),
                createItem(currentSnapshot, "playlist-item-a", "Same Track", 1)),
            List.of(
                createItem(previousSnapshot, "playlist-item-a", "Same Track", 0),
                createItem(previousSnapshot, "playlist-item-b", "Same Track", 1)));

    assertThat(diff.addedItems()).isEmpty();
    assertThat(diff.removedItems()).isEmpty();
    assertThat(diff.movedItems()).hasSize(2);
  }

  private SnapshotItem createItem(
      Snapshot snapshot, String providerItemId, String title, int position) {
    return new SnapshotItem(
        snapshot,
        providerItemId,
        title,
        "Artist",
        "https://example.com/" + providerItemId + ".jpg",
        position,
        Instant.parse("2026-07-10T05:00:00Z"));
  }
}
