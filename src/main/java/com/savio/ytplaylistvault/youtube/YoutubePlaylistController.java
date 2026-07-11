package com.savio.ytplaylistvault.youtube;

import com.savio.ytplaylistvault.youtube.dto.YoutubePlaylistResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

    return youtubePlaylistService.listPlaylists(accessToken);
  }
}
