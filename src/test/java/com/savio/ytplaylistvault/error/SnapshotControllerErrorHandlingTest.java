package com.savio.ytplaylistvault.error;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.savio.ytplaylistvault.snapshot.SnapshotController;
import com.savio.ytplaylistvault.snapshot.SnapshotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SnapshotController.class)
class SnapshotControllerErrorHandlingTest {
  @Autowired private MockMvc mockMvc;

  @MockitoBean private SnapshotService snapshotService;

  @Test
  void returnsNotFoundWhenResourceDoesNotExist() throws Exception {
    when(snapshotService.getSnapshot(any()))
        .thenThrow(new ResourceNotFoundException("Snapshot not found"));

    mockMvc
        .perform(get("/api/snapshots/11111111-1111-1111-1111-111111111111"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("Snapshot not found"))
        .andExpect(jsonPath("$.path").value("/api/snapshots/11111111-1111-1111-1111-111111111111"))
        .andExpect(jsonPath("$.fields").isEmpty());
  }
}
