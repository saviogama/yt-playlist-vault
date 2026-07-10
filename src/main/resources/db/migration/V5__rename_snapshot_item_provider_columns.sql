alter table snapshot_items
    rename column youtube_video_id to provider_item_id;

alter table snapshot_items
    rename column channel_title to creator_name;

alter table snapshot_items
    rename constraint uk_snapshot_items_snapshot_video to uk_snapshot_items_snapshot_provider_item;
