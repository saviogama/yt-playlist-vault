create table monitored_playlists (
    id uuid primary key,
    user_id uuid not null,
    youtube_playlist_id varchar(255) not null,
    title varchar(255) not null,
    description text,
    thumbnail_url varchar(255),
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,

    constraint fk_monitored_playlists_user
        foreign key (user_id)
        references users (id),

    constraint uk_monitored_playlists_user_youtube_playlist
        unique (user_id, youtube_playlist_id)
);