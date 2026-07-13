package com.savio.ytplaylistvault.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  OpenAPI ytPlaylistVaultOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("YT Playlist Vault API")
                .version("v1")
                .description("API for monitoring YouTube and YouTube Music playlist changes."))
        .components(
            new Components()
                .addSecuritySchemes(
                    "sessionAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("JSESSIONID")
                        .description("Session cookie established after OAuth2 login.")))
        .addSecurityItem(new SecurityRequirement().addList("sessionAuth"))
        .addTagsItem(new Tag().name("Health").description("Health check endpoint."))
        .addTagsItem(new Tag().name("Users").description("Create user endpoint."))
        .addTagsItem(new Tag().name("Auth").description("Authenticated user and CSRF endpoints."))
        .addTagsItem(new Tag().name("Playlists").description("Monitored playlist management."))
        .addTagsItem(
            new Tag().name("Snapshots").description("Playlist history and change detection."))
        .addTagsItem(new Tag().name("YouTube").description("YouTube provider integration."));
  }
}
