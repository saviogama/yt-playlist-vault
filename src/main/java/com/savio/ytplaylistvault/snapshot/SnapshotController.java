package com.savio.ytplaylistvault.snapshot;

import com.savio.ytplaylistvault.snapshot.SnapshotService.SnapshotWithItems;
import com.savio.ytplaylistvault.snapshot.dto.SnapshotDiffResponse;
import com.savio.ytplaylistvault.snapshot.dto.SnapshotResponse;
import com.savio.ytplaylistvault.user.User;
import com.savio.ytplaylistvault.user.UserService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class SnapshotController {
  private final SnapshotService snapshotService;
  private final SnapshotCaptureService snapshotCaptureService;
  private final UserService userService;
  private final OAuth2AuthorizedClientService authorizedClientService;

  public SnapshotController(
      SnapshotService snapshotService,
      SnapshotCaptureService snapshotCaptureService,
      UserService userService,
      OAuth2AuthorizedClientService authorizedClientService) {
    this.snapshotService = snapshotService;
    this.snapshotCaptureService = snapshotCaptureService;
    this.userService = userService;
    this.authorizedClientService = authorizedClientService;
  }

  @PostMapping("/me/playlists/{playlistId}/snapshots")
  @ResponseStatus(HttpStatus.CREATED)
  public SnapshotResponse captureSnapshot(
      @PathVariable UUID playlistId,
      @AuthenticationPrincipal OAuth2User oauth2User,
      OAuth2AuthenticationToken authentication) {
    User user = syncAuthenticatedUser(oauth2User);

    OAuth2AuthorizedClient authorizedClient =
        authorizedClientService.loadAuthorizedClient(
            authentication.getAuthorizedClientRegistrationId(), authentication.getName());

    if (authorizedClient == null) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "YouTube access was not granted or is no longer available");
    }

    String accessToken = authorizedClient.getAccessToken().getTokenValue();

    SnapshotWithItems snapshotWithItems =
        snapshotCaptureService.captureSnapshot(user, playlistId, accessToken);

    return SnapshotResponse.from(snapshotWithItems.snapshot(), snapshotWithItems.items());
  }

  @GetMapping("/me/playlists/{playlistId}/snapshots")
  public List<SnapshotResponse> listSnapshotsForUser(
      @PathVariable UUID playlistId, @AuthenticationPrincipal OAuth2User oauth2User) {
    User user = syncAuthenticatedUser(oauth2User);

    return snapshotService.listSnapshotsForUser(user, playlistId).stream()
        .map(
            snapshotWithItems ->
                SnapshotResponse.from(snapshotWithItems.snapshot(), snapshotWithItems.items()))
        .toList();
  }

  @GetMapping("/me/snapshots/{snapshotId}")
  public SnapshotResponse getSnapshotForUser(
      @PathVariable UUID snapshotId, @AuthenticationPrincipal OAuth2User oauth2User) {
    User user = syncAuthenticatedUser(oauth2User);

    SnapshotWithItems snapshotWithItems = snapshotService.getSnapshotForUser(user, snapshotId);

    return SnapshotResponse.from(snapshotWithItems.snapshot(), snapshotWithItems.items());
  }

  @GetMapping("/me/snapshots/{snapshotId}/diff-previous")
  public SnapshotDiffResponse diffPreviousSnapshotForUser(
      @PathVariable UUID snapshotId, @AuthenticationPrincipal OAuth2User oauth2User) {
    User user = syncAuthenticatedUser(oauth2User);

    return snapshotService.diffPreviousSnapshotForUser(user, snapshotId);
  }

  private User syncAuthenticatedUser(OAuth2User oauth2User) {
    return userService.syncAuthenticatedUser(
        oauth2User.getAttribute("sub"),
        oauth2User.getAttribute("email"),
        oauth2User.getAttribute("name"));
  }
}
