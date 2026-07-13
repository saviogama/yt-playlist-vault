package com.savio.ytplaylistvault.youtube;

import com.savio.ytplaylistvault.error.YoutubeIntegrationException;
import com.savio.ytplaylistvault.youtube.dto.YoutubePlaylistItemResponse;
import com.savio.ytplaylistvault.youtube.dto.YoutubePlaylistResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;

@Service
public class YoutubePlaylistService {
  private final RestClient restClient;

  public YoutubePlaylistService(RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder.baseUrl("https://www.googleapis.com/youtube/v3").build();
  }

  public List<YoutubePlaylistResponse> listPlaylists(String accessToken) {
    JsonNode response =
        getYoutubeResponse(
            () ->
                restClient
                    .get()
                    .uri(
                        uriBuilder ->
                            uriBuilder
                                .path("/playlists")
                                .queryParam("part", "snippet")
                                .queryParam("mine", true)
                                .queryParam("maxResults", 25)
                                .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(JsonNode.class));

    List<YoutubePlaylistResponse> playlists = new ArrayList<>();

    for (JsonNode item : response.path("items")) {
      String id = item.path("id").asString();
      JsonNode snippet = item.path("snippet");

      String title = snippet.path("title").asString();
      String description = snippet.path("description").asString();
      String thumbnailUrl = snippet.path("thumbnails").path("default").path("url").asString(null);

      playlists.add(new YoutubePlaylistResponse(id, title, description, thumbnailUrl));
    }

    return playlists;
  }

  public List<YoutubePlaylistItemResponse> listPlaylistItems(
      String accessToken, String providerPlaylistId) {
    List<YoutubePlaylistItemResponse> playlistItems = new ArrayList<>();
    String pageToken = null;

    do {
      String currentPageToken = pageToken;

      JsonNode response =
          getYoutubeResponse(
              () ->
                  restClient
                      .get()
                      .uri(
                          uriBuilder -> {
                            var builder =
                                uriBuilder
                                    .path("/playlistItems")
                                    .queryParam("part", "snippet")
                                    .queryParam("playlistId", providerPlaylistId)
                                    .queryParam("maxResults", 50);

                            if (currentPageToken != null) {
                              builder.queryParam("pageToken", currentPageToken);
                            }

                            return builder.build();
                          })
                      .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                      .retrieve()
                      .body(JsonNode.class));

      for (JsonNode item : response.path("items")) {
        JsonNode snippet = item.path("snippet");

        String providerItemId = item.path("id").asString();
        String title = snippet.path("title").asString();
        String creatorName = snippet.path("videoOwnerChannelTitle").asString(null);
        String thumbnailUrl = snippet.path("thumbnails").path("default").path("url").asString(null);
        int position = snippet.path("position").asInt();
        Instant addedToPlaylistAt = Instant.parse(snippet.path("publishedAt").asString());

        playlistItems.add(
            new YoutubePlaylistItemResponse(
                providerItemId, title, creatorName, thumbnailUrl, position, addedToPlaylistAt));
      }

      pageToken = response.path("nextPageToken").asString(null);
    } while (pageToken != null);

    return playlistItems;
  }

  private JsonNode getYoutubeResponse(Supplier<JsonNode> request) {
    try {
      JsonNode response = request.get();

      if (response == null) {
        throw YoutubeIntegrationException.unavailable();
      }

      return response;
    } catch (RestClientResponseException exception) {
      throw YoutubeIntegrationException.fromStatus(exception.getStatusCode());
    } catch (RestClientException exception) {
      throw YoutubeIntegrationException.unavailable();
    }
  }
}
