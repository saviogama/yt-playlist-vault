package com.savio.ytplaylistvault.auth;

import com.savio.ytplaylistvault.auth.dto.CsrfTokenResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/csrf")
public class CsrfController {

  @GetMapping
  public CsrfTokenResponse getCsrfToken(CsrfToken csrfToken) {
    return CsrfTokenResponse.from(csrfToken);
  }
}
