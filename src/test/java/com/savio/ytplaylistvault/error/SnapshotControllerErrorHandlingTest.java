package com.savio.ytplaylistvault.error;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.savio.ytplaylistvault.snapshot.SnapshotCaptureService;
import com.savio.ytplaylistvault.snapshot.SnapshotController;
import com.savio.ytplaylistvault.snapshot.SnapshotService;
import com.savio.ytplaylistvault.user.User;
import com.savio.ytplaylistvault.user.UserService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@WebMvcTest(
    value = SnapshotController.class,
    excludeAutoConfiguration = {
      OAuth2ClientAutoConfiguration.class,
      OAuth2ClientWebSecurityAutoConfiguration.class
    })
@Import(SnapshotControllerErrorHandlingTest.SecurityTestConfig.class)
class SnapshotControllerErrorHandlingTest {
  @Autowired private MockMvc mockMvc;

  @MockitoBean private SnapshotService snapshotService;

  @MockitoBean private SnapshotCaptureService snapshotCaptureService;

  @MockitoBean private UserService userService;

  @MockitoBean private OAuth2AuthorizedClientService authorizedClientService;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void returnsNotFoundWhenResourceDoesNotExist() throws Exception {
    OAuth2User oauth2User = oauth2User();
    SecurityContextHolder.getContext()
        .setAuthentication(
            new OAuth2AuthenticationToken(oauth2User, oauth2User.getAuthorities(), "google"));

    when(userService.syncAuthenticatedUser("google-123", "user@example.com", "User Example"))
        .thenReturn(new User("google-123", "user@example.com", "User Example"));
    when(snapshotService.getSnapshotForUser(any(), any()))
        .thenThrow(new ResourceNotFoundException("Snapshot not found"));

    mockMvc
        .perform(get("/api/me/snapshots/11111111-1111-1111-1111-111111111111"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Snapshot not found"))
        .andExpect(
            jsonPath("$.path").value("/api/me/snapshots/11111111-1111-1111-1111-111111111111"))
        .andExpect(jsonPath("$.fields").isEmpty());
  }

  private OAuth2User oauth2User() {
    return new DefaultOAuth2User(
        List.of(new SimpleGrantedAuthority("ROLE_USER")),
        Map.of("sub", "google-123", "email", "user@example.com", "name", "User Example"),
        "sub");
  }

  @TestConfiguration
  static class SecurityTestConfig implements WebMvcConfigurer {
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
      resolvers.add(new AuthenticationPrincipalArgumentResolver());
    }
  }
}
