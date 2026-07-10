package com.savio.ytplaylistvault.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.savio.ytplaylistvault.auth.dto.AuthenticatedUserResponse;
import com.savio.ytplaylistvault.user.User;
import com.savio.ytplaylistvault.user.UserService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

class AuthControllerTest {
  private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final String YOUTUBE_READONLY_SCOPE =
      "https://www.googleapis.com/auth/youtube.readonly";

  private final OAuth2AuthorizedClientService authorizedClientService =
      org.mockito.Mockito.mock(OAuth2AuthorizedClientService.class);
  private final UserService userService = org.mockito.Mockito.mock(UserService.class);
  private final AuthController authController =
      new AuthController(authorizedClientService, userService);

  @Test
  void returnsAuthenticatedUserWithYoutubeAccessGrantedWhenScopeWasGranted() {
    OAuth2User oauth2User = oauth2User();
    OAuth2AuthenticationToken authentication = authentication(oauth2User);
    OAuth2AuthorizedClient authorizedClient =
        authorizedClientWithScopes(Set.of("openid", "email", "profile", YOUTUBE_READONLY_SCOPE));
    when(authorizedClientService.loadAuthorizedClient("google", "google-123"))
        .thenReturn(authorizedClient);
    when(userService.syncAuthenticatedUser("google-123", "user@example.com", "User Example"))
        .thenReturn(user());

    AuthenticatedUserResponse response =
        authController.getAuthenticatedUser(oauth2User, authentication);

    assertThat(response.userId()).isEqualTo(USER_ID);
    assertThat(response.googleSubject()).isEqualTo("google-123");
    assertThat(response.email()).isEqualTo("user@example.com");
    assertThat(response.displayName()).isEqualTo("User Example");
    assertThat(response.grantedScopes()).contains(YOUTUBE_READONLY_SCOPE);
    assertThat(response.youtubeAccessGranted()).isTrue();
  }

  @Test
  void returnsAuthenticatedUserWithoutYoutubeAccessGrantedWhenScopeWasNotGranted() {
    OAuth2User oauth2User = oauth2User();
    OAuth2AuthenticationToken authentication = authentication(oauth2User);
    OAuth2AuthorizedClient authorizedClient =
        authorizedClientWithScopes(Set.of("openid", "email", "profile"));
    when(authorizedClientService.loadAuthorizedClient("google", "google-123"))
        .thenReturn(authorizedClient);
    when(userService.syncAuthenticatedUser("google-123", "user@example.com", "User Example"))
        .thenReturn(user());

    AuthenticatedUserResponse response =
        authController.getAuthenticatedUser(oauth2User, authentication);

    assertThat(response.grantedScopes()).doesNotContain(YOUTUBE_READONLY_SCOPE);
    assertThat(response.youtubeAccessGranted()).isFalse();
  }

  @Test
  void returnsAuthenticatedUserWithoutYoutubeAccessGrantedWhenAuthorizedClientIsMissing() {
    OAuth2User oauth2User = oauth2User();
    OAuth2AuthenticationToken authentication = authentication(oauth2User);
    when(authorizedClientService.loadAuthorizedClient("google", "google-123")).thenReturn(null);
    when(userService.syncAuthenticatedUser("google-123", "user@example.com", "User Example"))
        .thenReturn(user());

    AuthenticatedUserResponse response =
        authController.getAuthenticatedUser(oauth2User, authentication);

    assertThat(response.grantedScopes()).isEmpty();
    assertThat(response.youtubeAccessGranted()).isFalse();
  }

  private OAuth2User oauth2User() {
    return new DefaultOAuth2User(
        List.of(new SimpleGrantedAuthority("ROLE_USER")),
        Map.of(
            "sub", "google-123",
            "email", "user@example.com",
            "name", "User Example"),
        "sub");
  }

  private OAuth2AuthenticationToken authentication(OAuth2User oauth2User) {
    return new OAuth2AuthenticationToken(oauth2User, oauth2User.getAuthorities(), "google");
  }

  private OAuth2AuthorizedClient authorizedClientWithScopes(Set<String> scopes) {
    OAuth2AccessToken accessToken =
        new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            "access-token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            scopes);

    return new OAuth2AuthorizedClient(clientRegistration(), "google-123", accessToken);
  }

  private ClientRegistration clientRegistration() {
    return ClientRegistration.withRegistrationId("google")
        .clientId("test-client-id")
        .clientSecret("test-client-secret")
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
        .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
        .tokenUri("https://oauth2.googleapis.com/token")
        .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
        .userNameAttributeName("sub")
        .clientName("Google")
        .build();
  }

  private User user() {
    User user = new User("google-123", "user@example.com", "User Example");

    try {
      java.lang.reflect.Field idField = User.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(user, USER_ID);
    } catch (NoSuchFieldException | IllegalAccessException exception) {
      throw new RuntimeException(exception);
    }

    return user;
  }
}
