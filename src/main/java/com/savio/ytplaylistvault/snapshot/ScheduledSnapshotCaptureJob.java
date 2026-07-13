package com.savio.ytplaylistvault.snapshot;

import com.savio.ytplaylistvault.playlist.MonitoredPlaylist;
import com.savio.ytplaylistvault.playlist.MonitoredPlaylistRepository;
import com.savio.ytplaylistvault.playlist.MonitoringStatus;
import com.savio.ytplaylistvault.snapshot.messaging.SnapshotCaptureMessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.snapshot-scheduler.enabled", havingValue = "true")
public class ScheduledSnapshotCaptureJob {
  private static final Logger log = LoggerFactory.getLogger(ScheduledSnapshotCaptureJob.class);

  private final MonitoredPlaylistRepository monitoredPlaylistRepository;
  private final SnapshotCaptureMessagePublisher snapshotCaptureMessagePublisher;

  public ScheduledSnapshotCaptureJob(
      MonitoredPlaylistRepository monitoredPlaylistRepository,
      SnapshotCaptureMessagePublisher snapshotCaptureMessagePublisher) {
    this.monitoredPlaylistRepository = monitoredPlaylistRepository;
    this.snapshotCaptureMessagePublisher = snapshotCaptureMessagePublisher;
  }

  @Scheduled(
      fixedDelayString = "${app.snapshot-scheduler.fixed-delay:PT72H}",
      initialDelayString = "${app.snapshot-scheduler.initial-delay:PT1M}")
  public void enqueueActivePlaylists() {
    monitoredPlaylistRepository
        .findByMonitoringStatus(MonitoringStatus.ACTIVE)
        .forEach(this::enqueueSnapshotCapture);
  }

  private void enqueueSnapshotCapture(MonitoredPlaylist playlist) {
    try {
      snapshotCaptureMessagePublisher.publish(playlist.getId());
      log.info("Scheduled snapshot capture request queued for playlist {}", playlist.getId());
    } catch (RuntimeException exception) {
      log.error(
          "Failed to queue scheduled snapshot capture for playlist {}",
          playlist.getId(),
          exception);
    }
  }
}
