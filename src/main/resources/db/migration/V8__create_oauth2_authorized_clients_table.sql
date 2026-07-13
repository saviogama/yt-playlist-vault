create table oauth2_authorized_client (
    client_registration_id varchar(100) not null,
    principal_name varchar(200) not null,
    access_token_type varchar(100) not null,
    access_token_value bytea not null,
    access_token_issued_at timestamp not null,
    access_token_expires_at timestamp not null,
    access_token_scopes varchar(1000),
    refresh_token_value bytea,
    refresh_token_issued_at timestamp,
    created_at timestamp not null default current_timestamp,

    constraint pk_oauth2_authorized_client
        primary key (client_registration_id, principal_name)
);
