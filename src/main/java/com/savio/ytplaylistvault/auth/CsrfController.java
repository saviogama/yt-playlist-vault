package com.savio.ytplaylistvault.auth;

import com.savio.ytplaylistvault.auth.dto.CsrfTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth")
@RestController
@RequestMapping("/api/csrf")
public class CsrfController {

  @Operation(summary = "Get the CSRF token for state-changing requests")
  @ApiResponse(responseCode = "200", description = "CSRF token returned")
  @GetMapping
  public CsrfTokenResponse getCsrfToken(CsrfToken csrfToken) {
    return CsrfTokenResponse.from(csrfToken);
  }
}
