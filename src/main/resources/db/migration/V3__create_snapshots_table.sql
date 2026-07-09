create table snapshots (
    id uuid primary key,
    monitored_playlist_id uuid not null,
    captured_at timestamp(6) with time zone not null,
    item_count integer not null,
    created_at timestamp(6) with time zone not null,

    constraint fk_snapshots_monitored_playlist
        foreign key (monitored_playlist_id)
        references monitored_playlists (id),

    constraint ck_snapshots_item_count_non_negative
        check (item_count >= 0)
);