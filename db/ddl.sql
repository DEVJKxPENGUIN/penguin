CREATE DATABASE penguin;

USE penguin;

CREATE TABLE penguin.user
(
    id            bigint auto_increment primary key,
    provider      varchar(100) default 'google' not null,
    provider_id   varchar(100)                  not null,
    nickname      varchar(255)                  not null,
    email         varchar(255)                  null,
    role          varchar(100) default 'NORMAL' not null,
    id_token      text                          null,
    last_login_at timestamp                     null,
    created_at    timestamp                     not null,
    updated_at    timestamp                     not null
);

CREATE TABLE penguin.oidc_user
(
    id            bigint auto_increment primary key comment 'id',
    client_id     varchar(255) not null unique comment '발급한 client_id',
    client_secret varchar(255) not null comment '발급한 client_secret',
    project_name  varchar(255) not null comment '유저 프로젝트 이름',
    owner_id      bigint       not null comment '생성한 유저의 user_id',
    redirect_uris text         null comment 'redirect uri , 쉼표로 구분',
    scopes        varchar(255) not null comment '요청 가능 scope (openid, profile, email)',
    created_at    datetime     not null comment '생성 일시',
    updated_at    datetime     not null comment '수정 일시'
)
    comment 'penguin oidc user';
