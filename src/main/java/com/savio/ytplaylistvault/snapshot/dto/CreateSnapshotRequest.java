package com.savio.ytplaylistvault.snapshot.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record CreateSnapshotRequest(
        @NotEmpty
        List<@Valid CreateSnapshotItemRequest> items
) {
    
}
