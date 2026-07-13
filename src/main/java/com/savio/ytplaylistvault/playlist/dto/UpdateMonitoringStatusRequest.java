package com.savio.ytplaylistvault.playlist.dto;

import com.savio.ytplaylistvault.playlist.MonitoringStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateMonitoringStatusRequest(@NotNull MonitoringStatus status) {}
