CREATE DATABASE penguin;

USE penguin;

CREATE TABLE penguin.user
(
    id            bigint auto_increment not null,
    nickname      varchar(255)          not null,
    email         varchar(255)          not null,
    last_login_at timestamp             null,
    created_at    timestamp             not null,
    updated_at    timestamp             not null,
    primary key (id)
);

INSERT INTO penguin.user (nickname, email, last_login_at, created_at, updated_at)
values ('devjk',
        'dfjung4254@gmail.com',
        now(),
        now(),
        now());