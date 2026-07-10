package com.savio.ytplaylistvault.snapshot;

import com.savio.ytplaylistvault.playlist.MonitoredPlaylist;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SnapshotRepository extends JpaRepository<Snapshot, UUID> {
  List<Snapshot> findByMonitoredPlaylistOrderByCapturedAtDesc(MonitoredPlaylist monitoredPlaylist);

  Optional<Snapshot> findFirstByMonitoredPlaylistOrderByCapturedAtDesc(
      MonitoredPlaylist monitoredPlaylist);
}
