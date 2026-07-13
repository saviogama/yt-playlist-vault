package com.savio.ytplaylistvault.snapshot;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.savio.ytplaylistvault.playlist.MonitoredPlaylist;
import com.savio.ytplaylistvault.playlist.MonitoredPlaylistRepository;
import com.savio.ytplaylistvault.playlist.MonitoringStatus;
import com.savio.ytplaylistvault.snapshot.messaging.SnapshotCaptureMessagePublisher;
import com.savio.ytplaylistvault.user.User;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ScheduledSnapshotCaptureJobTest {

  private final MonitoredPlaylistRepository monitoredPlaylistRepository =
      Mockito.mock(MonitoredPlaylistRepository.class);
  private final SnapshotCaptureMessagePublisher snapshotCaptureMessagePublisher =
      Mockito.mock(SnapshotCaptureMessagePublisher.class);
  private final ScheduledSnapshotCaptureJob scheduledSnapshotCaptureJob =
      new ScheduledSnapshotCaptureJob(monitoredPlaylistRepository, snapshotCaptureMessagePublisher);

  @Test
  void queuesEveryActivePlaylist() {
    User user = new User("google-123", "user@example.com", "Test User");
    MonitoredPlaylist firstPlaylist = playlist(user, "first");
    MonitoredPlaylist secondPlaylist = playlist(user, "second");

    when(monitoredPlaylistRepository.findByMonitoringStatus(MonitoringStatus.ACTIVE))
        .thenReturn(List.of(firstPlaylist, secondPlaylist));

    scheduledSnapshotCaptureJob.enqueueActivePlaylists();

    verify(snapshotCaptureMessagePublisher).publish(firstPlaylist.getId());
    verify(snapshotCaptureMessagePublisher).publish(secondPlaylist.getId());
  }

  @Test
  void continuesQueuingWhenOnePlaylistFails() {
    User user = new User("google-123", "user@example.com", "Test User");
    MonitoredPlaylist firstPlaylist = playlist(user, "first");
    MonitoredPlaylist secondPlaylist = playlist(user, "second");

    when(monitoredPlaylistRepository.findByMonitoringStatus(MonitoringStatus.ACTIVE))
        .thenReturn(List.of(firstPlaylist, secondPlaylist));
    doThrow(new RuntimeException("RabbitMQ unavailable"))
        .when(snapshotCaptureMessagePublisher)
        .publish(firstPlaylist.getId());

    scheduledSnapshotCaptureJob.enqueueActivePlaylists();

    verify(snapshotCaptureMessagePublisher).publish(firstPlaylist.getId());
    verify(snapshotCaptureMessagePublisher).publish(secondPlaylist.getId());
  }

  private MonitoredPlaylist playlist(User user, String suffix) {
    return new MonitoredPlaylist(
        user,
        "playlist-" + suffix,
        "Playlist " + suffix,
        "Playlist used by ScheduledSnapshotCaptureJobTest",
        "https://example.com/playlist.jpg");
  }
}
