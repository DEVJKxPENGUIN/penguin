CREATE DATABASE penguin;

USE penguin;

CREATE TABLE penguin.user
(
    id            bigint       not null,
    nickname      varchar(255) not null,
    email         varchar(255) not null,
    last_login_at timestamp    not null,
    created_at    timestamp    not null,
    updated_at    timestamp    not null,
    primary key (id)
);