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

CREATE TABLE penguin.oidc_project
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
    comment 'penguin oidc project';

DROP TABLE IF EXISTS penguin.user_oidc_provision;
CREATE TABLE penguin.user_oidc_provision
(
    id         bigint auto_increment primary key comment 'id',
    user_id    bigint       not null comment 'user id',
    project_id bigint       not null comment 'oidc project id',
    code       varchar(255) comment '제공동의 시 발급코드',
    status     varchar(100) not null comment '제공 상태',
    created_at datetime     not null comment '생성 일시',
    updated_at datetime     not null comment '수정 일시',
    unique key uk_user_project (user_id, project_id)
)
    comment 'user가 oidc project에 대해 제공한 정보';


INSERT INTO penguin.user (provider, provider_id, nickname, email, role, id_token, last_login_at,
                          created_at, updated_at)
VALUES ('google', '100828347037604660700', 'devjk', 'dfjung4254@gmail.com', 'SUPER', '',
        '2025-09-21 08:15:22', '2025-09-21 08:15:22', '2025-09-21 08:15:22');

INSERT INTO penguin.oidc_project (client_id, client_secret, project_name, owner_id,
                                  redirect_uris, scopes, created_at, updated_at)
VALUES ( 'penguin-ppGO3dKSGu-1758442565993',
        'd2d55c5dc5b6d334a8f608d38fc3b28c828fd6cf1b560b6b205cf6a28eb7e319', 'devume', 1,
        'http://localhost:3000/api/auth/callback', 'openid', '2025-09-21 08:16:06',
        '2025-09-21 08:16:06');

#### DEVUME

CREATE DATABASE devume;

USE devume;

CREATE TABLE devume.user
(
    id            bigint auto_increment primary key,
    provider      varchar(100) default 'penguin' not null,
    provider_id   varchar(100)                  not null,
    nickname      varchar(255)                  not null,
    email         varchar(255)                  null,
    role          varchar(100) default 'NORMAL' not null,
    id_token      text                          null,
    last_login_at timestamp                     null,
    created_at    timestamp                     not null,
    updated_at    timestamp                     not null
);