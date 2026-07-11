package com.savio.ytplaylistvault.snapshot;

import com.savio.ytplaylistvault.snapshot.SnapshotService.SnapshotWithItems;
import com.savio.ytplaylistvault.snapshot.dto.CreateSnapshotRequest;
import com.savio.ytplaylistvault.snapshot.dto.SnapshotDiffResponse;
import com.savio.ytplaylistvault.snapshot.dto.SnapshotResponse;
import com.savio.ytplaylistvault.user.User;
import com.savio.ytplaylistvault.user.UserService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestBody;
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

  @PostMapping("/playlists/{playlistId}/snapshots")
  @ResponseStatus(HttpStatus.CREATED)
  public SnapshotResponse createSnapshot(
      @PathVariable UUID playlistId, @Valid @RequestBody CreateSnapshotRequest request) {
    SnapshotWithItems snapshotWithItems = snapshotService.createSnapshot(playlistId, request);

    return SnapshotResponse.from(snapshotWithItems.snapshot(), snapshotWithItems.items());
  }

  @GetMapping("/playlists/{playlistId}/snapshots")
  public List<SnapshotResponse> listSnapshots(@PathVariable UUID playlistId) {
    return snapshotService.listSnapshots(playlistId).stream()
        .map(
            snapshotWithItems ->
                SnapshotResponse.from(snapshotWithItems.snapshot(), snapshotWithItems.items()))
        .toList();
  }

  @GetMapping("/snapshots/{snapshotId}")
  public SnapshotResponse getSnapshot(@PathVariable UUID snapshotId) {
    SnapshotWithItems snapshotWithItems = snapshotService.getSnapshot(snapshotId);

    return SnapshotResponse.from(snapshotWithItems.snapshot(), snapshotWithItems.items());
  }

  @GetMapping("/snapshots/{snapshotId}/diff-previous")
  public SnapshotDiffResponse diffPreviousSnapshot(@PathVariable UUID snapshotId) {
    return snapshotService.diffPreviousSnapshot(snapshotId);
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

  private User syncAuthenticatedUser(OAuth2User oauth2User) {
    return userService.syncAuthenticatedUser(
        oauth2User.getAttribute("sub"),
        oauth2User.getAttribute("email"),
        oauth2User.getAttribute("name"));
  }
}
