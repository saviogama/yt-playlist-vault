package com.savio.ytplaylistvault.playlist;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.savio.ytplaylistvault.user.User;

public interface MonitoredPlaylistRepository extends JpaRepository<MonitoredPlaylist, UUID> {
    List<MonitoredPlaylist> findByUser(User user);

    Optional<MonitoredPlaylist> findByUserAndYoutubePlaylistId(User user, String youtubePlaylistId);

    boolean existsByUserAndYoutubePlaylistId(User user, String youtubePlaylistId);
}
