alter table monitored_playlists
    add column monitoring_status varchar(20) not null default 'ACTIVE',
    add column last_checked_at timestamp(6) with time zone,
    add column last_snapshot_at timestamp(6) with time zone,
    add column last_change_detected_at timestamp(6) with time zone,
    add column snapshot_count integer not null default 0;

alter table monitored_playlists
    add constraint ck_monitored_playlists_monitoring_status
        check (monitoring_status in ('ACTIVE', 'PAUSED')),
    add constraint ck_monitored_playlists_snapshot_count_non_negative
        check (snapshot_count >= 0);

update monitored_playlists monitored_playlist
set snapshot_count = snapshot_stats.snapshot_count,
    last_checked_at = snapshot_stats.last_snapshot_at,
    last_snapshot_at = snapshot_stats.last_snapshot_at
from (
    select monitored_playlist_id,
           count(*) as snapshot_count,
           max(captured_at) as last_snapshot_at
    from snapshots
    group by monitored_playlist_id
) snapshot_stats
where monitored_playlist.id = snapshot_stats.monitored_playlist_id;
