package com.savio.ytplaylistvault.snapshot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.savio.ytplaylistvault.error.ResourceNotFoundException;
import com.savio.ytplaylistvault.playlist.MonitoredPlaylist;
import com.savio.ytplaylistvault.playlist.MonitoredPlaylistRepository;
import com.savio.ytplaylistvault.playlist.MonitoringStatus;
import com.savio.ytplaylistvault.snapshot.dto.CreateSnapshotItemRequest;
import com.savio.ytplaylistvault.snapshot.dto.CreateSnapshotRequest;
import com.savio.ytplaylistvault.snapshot.dto.SnapshotDiffResponse;
import com.savio.ytplaylistvault.user.User;
import com.savio.ytplaylistvault.user.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class SnapshotServiceTest {

  @Autowired private SnapshotService snapshotService;

  @Autowired private UserRepository userRepository;

  @Autowired private MonitoredPlaylistRepository monitoredPlaylistRepository;

  @Autowired private MutableClock clock;

  @Test
  void shouldReturnEmptyDiffWhenThereIsNoPreviousSnapshot() {
    clock.setInstant(Instant.parse("2026-07-10T10:00:00Z"));

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
  void shouldReturnAddedRemovedAndMovedItemsWhenComparingWithPreviousSnapshot() {
    MonitoredPlaylist playlist = createPlaylist("2");

    clock.setInstant(Instant.parse("2026-07-10T10:00:00Z"));

    SnapshotService.SnapshotWithItems previousSnapshot =
        snapshotService.createSnapshot(
            playlist.getId(),
            new CreateSnapshotRequest(
                List.of(
                    createItem("track-a", "Track A", "Artist A", 0),
                    createItem("track-b", "Track B", "Artist B", 1))));

    clock.setInstant(Instant.parse("2026-07-10T10:05:00Z"));

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

  @Test
  void shouldListSnapshotsForPlaylistOwnerAndRejectAnotherUser() {
    MonitoredPlaylist playlist = createPlaylist("owner-list");

    SnapshotService.SnapshotWithItems snapshotWithItems =
        snapshotService.createSnapshot(
            playlist.getId(),
            new CreateSnapshotRequest(List.of(createItem("track-1", "Track 1", "Artist 1", 0))));

    List<SnapshotService.SnapshotWithItems> snapshots =
        snapshotService.listSnapshotsForUser(playlist.getUser(), playlist.getId());

    assertThat(snapshots).hasSize(1);
    assertThat(snapshots.getFirst().snapshot().getId())
        .isEqualTo(snapshotWithItems.snapshot().getId());

    User anotherUser = createPlaylist("another-user-list").getUser();

    assertThatThrownBy(() -> snapshotService.listSnapshotsForUser(anotherUser, playlist.getId()))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Playlist not found");
  }

  @Test
  void shouldReturnSnapshotAndDiffForOwnerAndRejectAnotherUser() {
    MonitoredPlaylist playlist = createPlaylist("owner-snapshot");

    SnapshotService.SnapshotWithItems snapshotWithItems =
        snapshotService.createSnapshot(
            playlist.getId(),
            new CreateSnapshotRequest(List.of(createItem("track-1", "Track 1", "Artist 1", 0))));

    SnapshotService.SnapshotWithItems snapshot =
        snapshotService.getSnapshotForUser(
            playlist.getUser(), snapshotWithItems.snapshot().getId());
    SnapshotDiffResponse diff =
        snapshotService.diffPreviousSnapshotForUser(
            playlist.getUser(), snapshotWithItems.snapshot().getId());

    assertThat(snapshot.snapshot().getId()).isEqualTo(snapshotWithItems.snapshot().getId());
    assertThat(snapshot.items()).hasSize(1);
    assertThat(diff.snapshotId()).isEqualTo(snapshotWithItems.snapshot().getId());
    assertThat(diff.previousSnapshotId()).isNull();

    User anotherUser = createPlaylist("another-user-snapshot").getUser();

    assertThatThrownBy(
            () ->
                snapshotService.getSnapshotForUser(
                    anotherUser, snapshotWithItems.snapshot().getId()))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Snapshot not found");
    assertThatThrownBy(
            () ->
                snapshotService.diffPreviousSnapshotForUser(
                    anotherUser, snapshotWithItems.snapshot().getId()))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Snapshot not found");
  }

  @Test
  void shouldReturnDiffForLatestSnapshotOfPlaylistOwner() {
    MonitoredPlaylist playlist = createPlaylist("latest-diff");

    clock.setInstant(Instant.parse("2026-07-10T10:00:00Z"));
    SnapshotService.SnapshotWithItems previousSnapshot =
        snapshotService.createSnapshot(
            playlist.getId(),
            new CreateSnapshotRequest(
                List.of(createItem("playlist-item-1", "Track 1", "Artist 1", 0))));

    clock.setInstant(Instant.parse("2026-07-10T10:05:00Z"));
    SnapshotService.SnapshotWithItems latestSnapshot =
        snapshotService.createSnapshot(
            playlist.getId(),
            new CreateSnapshotRequest(
                List.of(createItem("playlist-item-2", "Track 2", "Artist 2", 0))));

    SnapshotDiffResponse diff =
        snapshotService.diffLatestSnapshotForUser(playlist.getUser(), playlist.getId());

    assertThat(diff.snapshotId()).isEqualTo(latestSnapshot.snapshot().getId());
    assertThat(diff.previousSnapshotId()).isEqualTo(previousSnapshot.snapshot().getId());
    assertThat(diff.addedItems())
        .singleElement()
        .extracting(item -> item.providerItemId())
        .isEqualTo("playlist-item-2");
    assertThat(diff.removedItems())
        .singleElement()
        .extracting(item -> item.providerItemId())
        .isEqualTo("playlist-item-1");
    assertThat(diff.movedItems()).isEmpty();
  }

  @Test
  void shouldThrowWhenPlaylistHasNoSnapshots() {
    MonitoredPlaylist playlist = createPlaylist("no-snapshots");

    assertThatThrownBy(
            () -> snapshotService.diffLatestSnapshotForUser(playlist.getUser(), playlist.getId()))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("No snapshots found for this playlist");
  }

  @Test
  void shouldReuseLatestSnapshotWhenPlaylistContentIsUnchanged() {
    MonitoredPlaylist playlist = createPlaylist("unchanged");
    CreateSnapshotRequest request =
        new CreateSnapshotRequest(
            List.of(
                createItem("playlist-item-1", "Track 1", "Artist 1", 0),
                createItem("playlist-item-2", "Track 2", "Artist 2", 1)));

    clock.setInstant(Instant.parse("2026-07-10T10:00:00Z"));
    SnapshotService.SnapshotCaptureResult initialCapture =
        snapshotService.createSnapshotIfChanged(playlist, request);

    clock.setInstant(Instant.parse("2026-07-10T10:05:00Z"));
    SnapshotService.SnapshotCaptureResult captureResult =
        snapshotService.createSnapshotIfChanged(playlist, request);

    assertThat(captureResult.created()).isFalse();
    assertThat(captureResult.snapshotWithItems().snapshot().getId())
        .isEqualTo(initialCapture.snapshotWithItems().snapshot().getId());
    assertThat(snapshotService.listSnapshotsForUser(playlist.getUser(), playlist.getId()))
        .hasSize(1);
    assertThat(playlist.getMonitoringStatus()).isEqualTo(MonitoringStatus.ACTIVE);
    assertThat(playlist.getSnapshotCount()).isEqualTo(1);
    assertThat(playlist.getLastCheckedAt()).isEqualTo(Instant.parse("2026-07-10T10:05:00Z"));
    assertThat(playlist.getLastSnapshotAt()).isEqualTo(Instant.parse("2026-07-10T10:00:00Z"));
    assertThat(playlist.getLastChangeDetectedAt()).isNull();
  }

  @Test
  void shouldCreateNewSnapshotWhenPlaylistOrderChanges() {
    MonitoredPlaylist playlist = createPlaylist("changed-order");
    CreateSnapshotRequest initialRequest =
        new CreateSnapshotRequest(
            List.of(
                createItem("playlist-item-1", "Track 1", "Artist 1", 0),
                createItem("playlist-item-2", "Track 2", "Artist 2", 1)));
    clock.setInstant(Instant.parse("2026-07-10T10:00:00Z"));
    SnapshotService.SnapshotCaptureResult initialCapture =
        snapshotService.createSnapshotIfChanged(playlist, initialRequest);
    CreateSnapshotRequest reorderedRequest =
        new CreateSnapshotRequest(
            List.of(
                createItem("playlist-item-2", "Track 2", "Artist 2", 0),
                createItem("playlist-item-1", "Track 1", "Artist 1", 1)));

    clock.setInstant(Instant.parse("2026-07-10T10:05:00Z"));
    SnapshotService.SnapshotCaptureResult captureResult =
        snapshotService.createSnapshotIfChanged(playlist, reorderedRequest);

    assertThat(captureResult.created()).isTrue();
    assertThat(captureResult.snapshotWithItems().snapshot().getId())
        .isNotEqualTo(initialCapture.snapshotWithItems().snapshot().getId());
    assertThat(snapshotService.listSnapshotsForUser(playlist.getUser(), playlist.getId()))
        .hasSize(2);
    assertThat(playlist.getSnapshotCount()).isEqualTo(2);
    assertThat(playlist.getLastCheckedAt()).isEqualTo(Instant.parse("2026-07-10T10:05:00Z"));
    assertThat(playlist.getLastSnapshotAt()).isEqualTo(Instant.parse("2026-07-10T10:05:00Z"));
    assertThat(playlist.getLastChangeDetectedAt()).isEqualTo(Instant.parse("2026-07-10T10:05:00Z"));
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

  @TestConfiguration
  static class TestClockConfig {

    @Bean
    @Primary
    MutableClock mutableClock() {
      return new MutableClock(Instant.parse("2026-07-10T00:00:00Z"), ZoneId.of("UTC"));
    }
  }

  static class MutableClock extends Clock {
    private Instant instant;
    private final ZoneId zone;

    MutableClock(Instant instant, ZoneId zone) {
      this.instant = instant;
      this.zone = zone;
    }

    void setInstant(Instant instant) {
      this.instant = instant;
    }

    @Override
    public ZoneId getZone() {
      return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return new MutableClock(instant, zone);
    }

    @Override
    public Instant instant() {
      return instant;
    }
  }
}
