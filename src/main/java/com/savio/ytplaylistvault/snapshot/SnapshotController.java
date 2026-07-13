package com.savio.ytplaylistvault.snapshot;

import com.savio.ytplaylistvault.snapshot.SnapshotService.SnapshotCaptureResult;
import com.savio.ytplaylistvault.snapshot.SnapshotService.SnapshotWithItems;
import com.savio.ytplaylistvault.snapshot.dto.SnapshotDiffResponse;
import com.savio.ytplaylistvault.snapshot.dto.SnapshotResponse;
import com.savio.ytplaylistvault.user.User;
import com.savio.ytplaylistvault.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Snapshots")
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

  @Operation(
      summary = "Capture the current playlist state",
      parameters =
          @Parameter(
              name = "X-XSRF-TOKEN",
              in = ParameterIn.HEADER,
              required = true,
              description =
                  "CSRF token returned by GET /api/csrf. Required for state-changing requests."))
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "A new snapshot was created"),
    @ApiResponse(responseCode = "200", description = "Playlist content has not changed"),
    @ApiResponse(responseCode = "403", description = "YouTube authorization is unavailable"),
    @ApiResponse(responseCode = "404", description = "Playlist not found")
  })
  @PostMapping("/me/playlists/{playlistId}/snapshots")
  public ResponseEntity<SnapshotResponse> captureSnapshot(
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

    SnapshotCaptureResult captureResult =
        snapshotCaptureService.captureSnapshot(user, playlistId, accessToken);

    SnapshotWithItems snapshotWithItems = captureResult.snapshotWithItems();
    SnapshotResponse response =
        SnapshotResponse.from(snapshotWithItems.snapshot(), snapshotWithItems.items());

    HttpStatus status = captureResult.created() ? HttpStatus.CREATED : HttpStatus.OK;
    return ResponseEntity.status(status).body(response);
  }

  @Operation(summary = "List a playlist snapshot history")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Snapshot history returned"),
    @ApiResponse(responseCode = "404", description = "Playlist not found")
  })
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

  @Operation(summary = "Get the latest playlist change")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Latest snapshot diff returned"),
    @ApiResponse(responseCode = "404", description = "Playlist or snapshot not found")
  })
  @GetMapping("/me/playlists/{playlistId}/latest-diff")
  public SnapshotDiffResponse diffLatestSnapshotForUser(
      @PathVariable UUID playlistId, @AuthenticationPrincipal OAuth2User oauth2User) {
    User user = syncAuthenticatedUser(oauth2User);

    return snapshotService.diffLatestSnapshotForUser(user, playlistId);
  }

  @Operation(summary = "Get a snapshot and its items")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Snapshot returned"),
    @ApiResponse(responseCode = "404", description = "Snapshot not found")
  })
  @GetMapping("/me/snapshots/{snapshotId}")
  public SnapshotResponse getSnapshotForUser(
      @PathVariable UUID snapshotId, @AuthenticationPrincipal OAuth2User oauth2User) {
    User user = syncAuthenticatedUser(oauth2User);

    SnapshotWithItems snapshotWithItems = snapshotService.getSnapshotForUser(user, snapshotId);

    return SnapshotResponse.from(snapshotWithItems.snapshot(), snapshotWithItems.items());
  }

  @Operation(summary = "Compare a snapshot with its predecessor")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Snapshot diff returned"),
    @ApiResponse(responseCode = "404", description = "Snapshot not found")
  })
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
