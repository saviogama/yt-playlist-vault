package com.savio.ytplaylistvault.error;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.savio.ytplaylistvault.user.UserController;
import com.savio.ytplaylistvault.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerErrorHandlingTest {
  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserService userService;

  @Test
  void returnsBadRequestWithFieldErrorsWhenRequestIsInvalid() throws Exception {
    mockMvc
        .perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "googleSubject": "",
                      "email": "invalid-email",
                      "displayName": ""
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Invalid request"))
        .andExpect(jsonPath("$.path").value("/api/users"))
        .andExpect(jsonPath("$.fields").isArray())
        .andExpect(jsonPath("$.fields[?(@.field == 'googleSubject')]").exists())
        .andExpect(jsonPath("$.fields[?(@.field == 'email')]").exists())
        .andExpect(jsonPath("$.fields[?(@.field == 'displayName')]").exists());
  }

  @Test
  void returnsConflictWhenResourceAlreadyExists() throws Exception {
    when(userService.createUser(any()))
        .thenThrow(new DuplicateResourceException("User already exists for this Google subject"));

    mockMvc
        .perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "googleSubject": "google-123",
                      "email": "user@example.com",
                      "displayName": "User"
                    }
                    """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.error").value("Conflict"))
        .andExpect(jsonPath("$.message").value("User already exists for this Google subject"))
        .andExpect(jsonPath("$.path").value("/api/users"))
        .andExpect(jsonPath("$.fields").isEmpty());
  }

  @Test
  void returnsInternalServerErrorWithoutLeakingUnexpectedExceptionMessage() throws Exception {
    when(userService.createUser(any())).thenThrow(new RuntimeException("Database password leaked"));

    mockMvc
        .perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "googleSubject": "google-123",
                      "email": "user@example.com",
                      "displayName": "User"
                    }
                    """))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.error").value("Internal Server Error"))
        .andExpect(jsonPath("$.message").value("Unexpected internal server error"))
        .andExpect(jsonPath("$.path").value("/api/users"))
        .andExpect(jsonPath("$.fields").isEmpty());
  }
}
