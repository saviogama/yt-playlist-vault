package com.savio.ytplaylistvault.snapshot;

import com.savio.ytplaylistvault.playlist.MonitoredPlaylist;
import com.savio.ytplaylistvault.playlist.MonitoredPlaylistRepository;
import com.savio.ytplaylistvault.playlist.MonitoringStatus;
import com.savio.ytplaylistvault.user.User;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.snapshot-scheduler.enabled", havingValue = "true")
public class ScheduledSnapshotCaptureJob {
  private static final Logger log = LoggerFactory.getLogger(ScheduledSnapshotCaptureJob.class);
  private static final String GOOGLE_REGISTRATION_ID = "google";

  private final MonitoredPlaylistRepository monitoredPlaylistRepository;
  private final SnapshotCaptureService snapshotCaptureService;
  private final OAuth2AuthorizedClientManager authorizedClientManager;

  public ScheduledSnapshotCaptureJob(
      MonitoredPlaylistRepository monitoredPlaylistRepository,
      SnapshotCaptureService snapshotCaptureService,
      OAuth2AuthorizedClientManager authorizedClientManager) {
    this.monitoredPlaylistRepository = monitoredPlaylistRepository;
    this.snapshotCaptureService = snapshotCaptureService;
    this.authorizedClientManager = authorizedClientManager;
  }

  @Scheduled(
      fixedDelayString = "${app.snapshot-scheduler.fixed-delay:PT72H}",
      initialDelayString = "${app.snapshot-scheduler.initial-delay:PT1M}")
  public void captureActivePlaylists() {
    Map<User, List<MonitoredPlaylist>> playlistsByUser =
        monitoredPlaylistRepository.findByMonitoringStatus(MonitoringStatus.ACTIVE).stream()
            .collect(java.util.stream.Collectors.groupingBy(MonitoredPlaylist::getUser));

    playlistsByUser.forEach(this::capturePlaylistsForUser);
  }

  private void capturePlaylistsForUser(User user, List<MonitoredPlaylist> playlists) {
    OAuth2AuthorizedClient authorizedClient = authorize(user);

    if (authorizedClient == null) {
      log.warn("Skipping scheduled snapshot capture because OAuth authorization is unavailable");
      return;
    }

    String accessToken = authorizedClient.getAccessToken().getTokenValue();

    for (MonitoredPlaylist playlist : playlists) {
      try {
        SnapshotService.SnapshotCaptureResult captureResult =
            snapshotCaptureService.captureSnapshot(user, playlist.getId(), accessToken);
        log.info(
            "Scheduled snapshot capture {} for playlist {}",
            captureResult.created() ? "created a snapshot" : "found no changes",
            playlist.getId());
      } catch (RuntimeException exception) {
        log.error("Scheduled snapshot capture failed for playlist {}", playlist.getId(), exception);
      }
    }
  }

  private OAuth2AuthorizedClient authorize(User user) {
    Authentication principal =
        UsernamePasswordAuthenticationToken.authenticated(
            user.getGoogleSubject(), "N/A", List.of());
    OAuth2AuthorizeRequest authorizeRequest =
        OAuth2AuthorizeRequest.withClientRegistrationId(GOOGLE_REGISTRATION_ID)
            .principal(principal)
            .build();

    return authorizedClientManager.authorize(authorizeRequest);
  }
}
