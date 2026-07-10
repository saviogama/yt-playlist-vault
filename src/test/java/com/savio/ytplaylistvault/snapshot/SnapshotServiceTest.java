package com.savio.ytplaylistvault.snapshot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.savio.ytplaylistvault.error.ResourceNotFoundException;
import com.savio.ytplaylistvault.playlist.MonitoredPlaylist;
import com.savio.ytplaylistvault.playlist.MonitoredPlaylistRepository;
import com.savio.ytplaylistvault.snapshot.dto.CreateSnapshotItemRequest;
import com.savio.ytplaylistvault.snapshot.dto.CreateSnapshotRequest;
import com.savio.ytplaylistvault.snapshot.dto.SnapshotDiffResponse;
import com.savio.ytplaylistvault.user.User;
import com.savio.ytplaylistvault.user.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class SnapshotServiceTest {

  @Autowired private SnapshotService snapshotService;

  @Autowired private UserRepository userRepository;

  @Autowired private MonitoredPlaylistRepository monitoredPlaylistRepository;

  @Test
  void shouldReturnEmptyDiffWhenThereIsNoPreviousSnapshot() {
    MonitoredPlaylist playlist = createPlaylist("1");

    SnapshotService.SnapshotWithItems snapshotWithItems =
        snapshotService.createSnapshot(
            playlist.getId(),
            new CreateSnapshotRequest(
                List.of(
                    new CreateSnapshotItemRequest(
                        "track-1",
                        "Track 1",
                        "Artist 1",
                        "https://example.com/track-1.jpg",
                        0,
                        Instant.parse("2026-07-10T05:00:00Z")))));

    SnapshotDiffResponse diff =
        snapshotService.diffPreviousSnapshot(snapshotWithItems.snapshot().getId());

    assertThat(diff.snapshotId()).isEqualTo(snapshotWithItems.snapshot().getId());
    assertThat(diff.previousSnapshotId()).isNull();
    assertThat(diff.addedItems()).isEmpty();
    assertThat(diff.removedItems()).isEmpty();
    assertThat(diff.movedItems()).isEmpty();
  }

  @Test
  void shouldReturnAddedRemovedAndMovedItemsWhenComparingWithPreviousSnapshot() throws Exception {
    MonitoredPlaylist playlist = createPlaylist("2");

    SnapshotService.SnapshotWithItems previousSnapshot =
        snapshotService.createSnapshot(
            playlist.getId(),
            new CreateSnapshotRequest(
                List.of(
                    createItem("track-a", "Track A", "Artist A", 0),
                    createItem("track-b", "Track B", "Artist B", 1))));

    Thread.sleep(10);

    SnapshotService.SnapshotWithItems currentSnapshot =
        snapshotService.createSnapshot(
            playlist.getId(),
            new CreateSnapshotRequest(
                List.of(
                    createItem("track-b", "Track B", "Artist B", 0),
                    createItem("track-c", "Track C", "Artist C", 1))));

    SnapshotDiffResponse diff =
        snapshotService.diffPreviousSnapshot(currentSnapshot.snapshot().getId());

    assertThat(diff.snapshotId()).isEqualTo(currentSnapshot.snapshot().getId());
    assertThat(diff.previousSnapshotId()).isEqualTo(previousSnapshot.snapshot().getId());

    assertThat(diff.addedItems()).hasSize(1);
    assertThat(diff.addedItems().getFirst().providerItemId()).isEqualTo("track-c");

    assertThat(diff.removedItems()).hasSize(1);
    assertThat(diff.removedItems().getFirst().providerItemId()).isEqualTo("track-a");

    assertThat(diff.movedItems()).hasSize(1);
    assertThat(diff.movedItems().getFirst().providerItemId()).isEqualTo("track-b");
    assertThat(diff.movedItems().getFirst().previousPosition()).isEqualTo(1);
    assertThat(diff.movedItems().getFirst().currentPosition()).isEqualTo(0);
  }

  @Test
  void shouldThrowWhenSnapshotDoesNotExist() {
    UUID missingSnapshotId = UUID.randomUUID();

    assertThatThrownBy(() -> snapshotService.diffPreviousSnapshot(missingSnapshotId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Snapshot not found");
  }

  private MonitoredPlaylist createPlaylist(String suffix) {
    User user =
        userRepository.save(
            new User(
                "google-subject-test-" + suffix,
                "test-" + suffix + "@example.com",
                "Test User " + suffix));

    return monitoredPlaylistRepository.save(
        new MonitoredPlaylist(
            user,
            "playlist-test-" + suffix,
            "Playlist Test " + suffix,
            "Playlist used by SnapshotServiceTest",
            "https://example.com/playlist.jpg"));
  }

  private CreateSnapshotItemRequest createItem(
      String providerItemId, String title, String creatorName, int position) {
    return new CreateSnapshotItemRequest(
        providerItemId,
        title,
        creatorName,
        "https://example.com/" + providerItemId + ".jpg",
        position,
        Instant.parse("2026-07-10T05:00:00Z"));
  }
}
