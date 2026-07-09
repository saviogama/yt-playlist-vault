create table users (
    id uuid primary key,
    google_subject varchar(255) not null unique,
    email varchar(255) not null,
    display_name varchar(255) not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null
);