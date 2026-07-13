package com.savio.ytplaylistvault.youtube;

import com.savio.ytplaylistvault.youtube.dto.YoutubePlaylistResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "YouTube")
@RestController
@RequestMapping("/api/youtube/playlists")
public class YoutubePlaylistController {
  private final OAuth2AuthorizedClientService authorizedClientService;
  private final YoutubePlaylistService youtubePlaylistService;

  public YoutubePlaylistController(
      OAuth2AuthorizedClientService authorizedClientService,
      YoutubePlaylistService youtubePlaylistService) {
    this.authorizedClientService = authorizedClientService;
    this.youtubePlaylistService = youtubePlaylistService;
  }

  @Operation(summary = "List playlists available from the authenticated YouTube account")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "YouTube playlists returned"),
    @ApiResponse(responseCode = "403", description = "YouTube authorization is unavailable")
  })
  @GetMapping
  public List<YoutubePlaylistResponse> listPlaylists(OAuth2AuthenticationToken authentication) {
    OAuth2AuthorizedClient authorizedClient =
        authorizedClientService.loadAuthorizedClient(
            authentication.getAuthorizedClientRegistrationId(), authentication.getName());

    if (authorizedClient == null) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "YouTube access was not granted or is no longer available");
    }

    String accessToken = authorizedClient.getAccessToken().getTokenValue();

    return youtubePlaylistService.listPlaylists(authentication.getName(), accessToken);
  }
}
