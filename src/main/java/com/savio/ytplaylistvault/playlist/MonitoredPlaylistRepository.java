package com.savio.ytplaylistvault.playlist;

import com.savio.ytplaylistvault.user.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonitoredPlaylistRepository extends JpaRepository<MonitoredPlaylist, UUID> {
  List<MonitoredPlaylist> findByUser(User user);

  List<MonitoredPlaylist> findByMonitoringStatus(MonitoringStatus monitoringStatus);

  Optional<MonitoredPlaylist> findByUserAndProviderPlaylistId(User user, String providerPlaylistId);

  Optional<MonitoredPlaylist> findByIdAndUser(UUID id, User user);

  boolean existsByUserAndProviderPlaylistId(User user, String providerPlaylistId);
}
