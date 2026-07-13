package com.savio.ytplaylistvault.youtube;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.savio.ytplaylistvault.youtube.dto.YoutubePlaylistResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class YoutubePlaylistServiceCacheTest {

  @Test
  void cachesPlaylistDiscoveryByGoogleSubject() {
    RestClient.Builder restClientBuilder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();

    server
        .expect(
            requestTo(
                "https://www.googleapis.com/youtube/v3/playlists?part=snippet&mine=true&maxResults=25"))
        .andExpect(header("Authorization", "Bearer first-access-token"))
        .andRespond(
            withSuccess(
                """
                {
                  "items": [
                    {
                      "id": "playlist-123",
                      "snippet": {
                        "title": "Playlist",
                        "description": "Description",
                        "thumbnails": {"default": {"url": "https://example.com/playlist.jpg"}}
                      }
                    }
                  ]
                }
                """,
                MediaType.APPLICATION_JSON));

    try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
      context.register(CacheTestConfig.class);
      context.registerBean(RestClient.Builder.class, () -> restClientBuilder);
      context.refresh();

      YoutubePlaylistService service = context.getBean(YoutubePlaylistService.class);

      List<YoutubePlaylistResponse> firstResponse =
          service.listPlaylists("google-123", "first-access-token");
      List<YoutubePlaylistResponse> cachedResponse =
          service.listPlaylists("google-123", "refreshed-access-token");

      org.assertj.core.api.Assertions.assertThat(cachedResponse).isEqualTo(firstResponse);
    }

    server.verify();
  }

  @Configuration
  @EnableCaching
  static class CacheTestConfig {

    @Bean
    CacheManager cacheManager() {
      return new ConcurrentMapCacheManager();
    }

    @Bean
    YoutubePlaylistService youtubePlaylistService(RestClient.Builder restClientBuilder) {
      return new YoutubePlaylistService(restClientBuilder);
    }
  }
}
