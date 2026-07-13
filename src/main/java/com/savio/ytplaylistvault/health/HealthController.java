package com.savio.ytplaylistvault.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health")
@RestController
public class HealthController {

  @SecurityRequirements
  @Operation(summary = "Check application health")
  @ApiResponse(responseCode = "200", description = "Application is healthy")
  @GetMapping("/health")
  public String health() {
    return "OK";
  }
}
