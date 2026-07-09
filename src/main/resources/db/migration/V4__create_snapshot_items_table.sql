create table snapshot_items (
    id uuid primary key,
    snapshot_id uuid not null,
    youtube_video_id varchar(255) not null,
    title varchar(255) not null,
    channel_title varchar(255),
    thumbnail_url varchar(255),
    position integer not null,
    added_to_playlist_at timestamp(6) with time zone not null,
    created_at timestamp(6) with time zone not null,

    constraint fk_snapshot_items_snapshot
        foreign key (snapshot_id)
        references snapshots (id),

    constraint uk_snapshot_items_snapshot_video
        unique (snapshot_id, youtube_video_id),

    constraint ck_snapshot_items_position_non_negative
        check (position >= 0)
);