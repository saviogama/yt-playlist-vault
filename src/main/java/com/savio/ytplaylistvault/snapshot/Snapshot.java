package com.savio.ytplaylistvault.snapshot;

import java.time.Instant;
import java.util.UUID;

import com.savio.ytplaylistvault.playlist.MonitoredPlaylist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "snapshots")
public class Snapshot {
    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "monitored_playlist_id", nullable = false)
    private MonitoredPlaylist monitoredPlaylist;

    @Column(nullable = false)
    private Instant capturedAt;

    @Column(nullable = false)
    private int itemCount;

    @Column(nullable = false)
    private Instant createdAt;

    protected Snapshot() {
    }

    public Snapshot(MonitoredPlaylist monitoredPlaylist, Instant capturedAt, int itemCount) {
        this.id = UUID.randomUUID();
        this.monitoredPlaylist = monitoredPlaylist;
        this.capturedAt = capturedAt;
        this.itemCount = itemCount;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public MonitoredPlaylist getMonitoredPlaylist() {
        return monitoredPlaylist;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    public int getItemCount() {
        return itemCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
