package com.savio.ytplaylistvault.auth;

import com.savio.ytplaylistvault.auth.dto.AuthenticatedUserResponse;
import com.savio.ytplaylistvault.user.User;
import com.savio.ytplaylistvault.user.UserService;
import java.util.List;
import java.util.Set;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class AuthController {
  private static final String YOUTUBE_READONLY_SCOPE =
      "https://www.googleapis.com/auth/youtube.readonly";

  private final OAuth2AuthorizedClientService authorizedClientService;
  private final UserService userService;

  public AuthController(
      OAuth2AuthorizedClientService authorizedClientService, UserService userService) {
    this.authorizedClientService = authorizedClientService;
    this.userService = userService;
  }

  @GetMapping
  public AuthenticatedUserResponse getAuthenticatedUser(
      @AuthenticationPrincipal OAuth2User oauth2User, OAuth2AuthenticationToken authentication) {
    OAuth2AuthorizedClient authorizedClient =
        authorizedClientService.loadAuthorizedClient(
            authentication.getAuthorizedClientRegistrationId(), authentication.getName());
    User user =
        userService.syncAuthenticatedUser(
            oauth2User.getAttribute("sub"),
            oauth2User.getAttribute("email"),
            oauth2User.getAttribute("name"));

    Set<String> scopes =
        authorizedClient == null ? Set.of() : authorizedClient.getAccessToken().getScopes();
    List<String> grantedScopes = scopes.stream().sorted().toList();

    return new AuthenticatedUserResponse(
        user.getId(),
        oauth2User.getAttribute("sub"),
        oauth2User.getAttribute("email"),
        oauth2User.getAttribute("name"),
        grantedScopes,
        grantedScopes.contains(YOUTUBE_READONLY_SCOPE));
  }
}
