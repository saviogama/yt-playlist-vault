package com.savio.ytplaylistvault.user;

import com.savio.ytplaylistvault.user.dto.CreateUserRequest;
import com.savio.ytplaylistvault.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users")
@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @Operation(
      summary = "Create a user",
      parameters =
          @Parameter(
              name = "X-XSRF-TOKEN",
              in = ParameterIn.HEADER,
              required = true,
              description =
                  "CSRF token returned by GET /api/csrf. Required for state-changing requests."))
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "User created"),
    @ApiResponse(responseCode = "409", description = "User already exists for this Google subject")
  })
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
    User user = userService.createUser(request);
    return UserResponse.from(user);
  }
}
