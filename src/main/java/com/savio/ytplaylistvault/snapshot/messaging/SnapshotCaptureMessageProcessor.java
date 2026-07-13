package com.savio.ytplaylistvault.snapshot.messaging;

import com.savio.ytplaylistvault.playlist.MonitoredPlaylist;
import com.savio.ytplaylistvault.playlist.MonitoredPlaylistRepository;
import com.savio.ytplaylistvault.playlist.MonitoringStatus;
import com.savio.ytplaylistvault.snapshot.SnapshotCaptureService;
import com.savio.ytplaylistvault.snapshot.SnapshotService.SnapshotCaptureResult;
import com.savio.ytplaylistvault.user.User;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;

@Service
public class SnapshotCaptureMessageProcessor {
  private static final Logger log = LoggerFactory.getLogger(SnapshotCaptureMessageProcessor.class);
  private static final String GOOGLE_REGISTRATION_ID = "google";

  private final MonitoredPlaylistRepository monitoredPlaylistRepository;
  private final SnapshotCaptureService snapshotCaptureService;
  private final OAuth2AuthorizedClientManager authorizedClientManager;

  public SnapshotCaptureMessageProcessor(
      MonitoredPlaylistRepository monitoredPlaylistRepository,
      SnapshotCaptureService snapshotCaptureService,
      OAuth2AuthorizedClientManager authorizedClientManager) {
    this.monitoredPlaylistRepository = monitoredPlaylistRepository;
    this.snapshotCaptureService = snapshotCaptureService;
    this.authorizedClientManager = authorizedClientManager;
  }

  public void process(SnapshotCaptureRequested request) {
    MonitoredPlaylist playlist =
        monitoredPlaylistRepository.findById(request.monitoredPlaylistId()).orElse(null);

    if (playlist == null) {
      log.info(
          "Skipping snapshot capture because playlist {} no longer exists",
          request.monitoredPlaylistId());
      return;
    }

    if (playlist.getMonitoringStatus() != MonitoringStatus.ACTIVE) {
      log.info("Skipping snapshot capture because playlist {} is not active", playlist.getId());
      return;
    }

    User user = playlist.getUser();
    OAuth2AuthorizedClient authorizedClient = authorize(user);

    if (authorizedClient == null) {
      log.warn(
          "Skipping snapshot capture for playlist {} because OAuth authorization is unavailable",
          playlist.getId());
      return;
    }

    SnapshotCaptureResult captureResult =
        snapshotCaptureService.captureSnapshot(
            user, playlist.getId(), authorizedClient.getAccessToken().getTokenValue());

    log.info(
        "Asynchronous snapshot capture {} for playlist {}",
        captureResult.created() ? "created a snapshot" : "found no changes",
        playlist.getId());
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
