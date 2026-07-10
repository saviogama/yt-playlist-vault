package com.savio.ytplaylistvault.snapshot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "snapshot_items")
public class SnapshotItem {
  @Id private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "snapshot_id", nullable = false)
  private Snapshot snapshot;

  @Column(nullable = false)
  private String youtubeVideoId;

  @Column(nullable = false)
  private String title;

  private String channelTitle;

  private String thumbnailUrl;

  @Column(nullable = false)
  private int position;

  @Column(nullable = false)
  private Instant addedToPlaylistAt;

  @Column(nullable = false)
  private Instant createdAt;

  protected SnapshotItem() {}

  public SnapshotItem(
      Snapshot snapshot,
      String youtubeVideoId,
      String title,
      String channelTitle,
      String thumbnailUrl,
      int position,
      Instant addedToPlaylistAt) {
    this.id = UUID.randomUUID();
    this.snapshot = snapshot;
    this.youtubeVideoId = youtubeVideoId;
    this.title = title;
    this.channelTitle = channelTitle;
    this.thumbnailUrl = thumbnailUrl;
    this.position = position;
    this.addedToPlaylistAt = addedToPlaylistAt;
  }

  @PrePersist
  void onCreate() {
    this.createdAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public Snapshot getSnapshot() {
    return snapshot;
  }

  public String getYoutubeVideoId() {
    return youtubeVideoId;
  }

  public String getTitle() {
    return title;
  }

  public String getChannelTitle() {
    return channelTitle;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public int getPosition() {
    return position;
  }

  public Instant getAddedToPlaylistAt() {
    return addedToPlaylistAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
