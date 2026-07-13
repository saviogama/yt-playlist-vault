package com.savio.ytplaylistvault.playlist;

import com.savio.ytplaylistvault.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "monitored_playlists")
public class MonitoredPlaylist {
  @Id private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private String providerPlaylistId;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "text")
  private String description;

  private String thumbnailUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MonitoringStatus monitoringStatus;

  private Instant lastCheckedAt;

  private Instant lastSnapshotAt;

  private Instant lastChangeDetectedAt;

  @Column(nullable = false)
  private int snapshotCount;

  @Column(nullable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  protected MonitoredPlaylist() {}

  public MonitoredPlaylist(
      User user, String providerPlaylistId, String title, String description, String thumbnailUrl) {
    this.id = UUID.randomUUID();
    this.user = user;
    this.providerPlaylistId = providerPlaylistId;
    this.title = title;
    this.description = description;
    this.thumbnailUrl = thumbnailUrl;
    this.monitoringStatus = MonitoringStatus.ACTIVE;
  }

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public String getProviderPlaylistId() {
    return providerPlaylistId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public MonitoringStatus getMonitoringStatus() {
    return monitoringStatus;
  }

  public Instant getLastCheckedAt() {
    return lastCheckedAt;
  }

  public Instant getLastSnapshotAt() {
    return lastSnapshotAt;
  }

  public Instant getLastChangeDetectedAt() {
    return lastChangeDetectedAt;
  }

  public int getSnapshotCount() {
    return snapshotCount;
  }

  public void recordCapture(
      Instant checkedAt, boolean createdSnapshot, boolean hadPreviousSnapshot) {
    this.lastCheckedAt = checkedAt;

    if (!createdSnapshot) {
      return;
    }

    this.lastSnapshotAt = checkedAt;
    this.snapshotCount++;

    if (hadPreviousSnapshot) {
      this.lastChangeDetectedAt = checkedAt;
    }
  }
}
