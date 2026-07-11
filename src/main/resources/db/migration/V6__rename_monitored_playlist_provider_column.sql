alter table monitored_playlists
    rename column youtube_playlist_id to provider_playlist_id;

alter table monitored_playlists
    rename constraint uk_monitored_playlists_user_youtube_playlist to uk_monitored_playlists_user_provider_playlist;
