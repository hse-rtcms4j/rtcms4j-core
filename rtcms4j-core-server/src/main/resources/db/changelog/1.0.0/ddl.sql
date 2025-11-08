--liquibase formatted sql

--changeset Enzhine:1.0.0:1
create extension if not exists "uuid-ossp";

create table namespace(
    ---- tech fields
    id bigserial primary key not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    ---- entity fields
    creator_sub uuid not null,
    name varchar(64) not null,
    description text not null
);
--rollback drop table if exists namespace;

create unique index uix_namespace__name on namespace (name);
--rollback drop index if exists uix_namespace__name;

create table namespace_admin(
    ---- tech fields
    id bigserial primary key not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    ---- entity fields
    namespace_id bigint not null references namespace (id) on delete cascade,
    assigner_sub uuid not null,
    user_sub uuid not null
);
--rollback drop table if exists namespace_admin;

create unique index uix_namespace_admin__user_sub_namespace_id on namespace_admin (namespace_id, user_sub);
--rollback drop index if exists uix_namespace_admin__user_sub_namespace_id;

create table application(
    ---- tech fields
    id bigserial primary key not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    ---- entity fields
    namespace_id bigint not null references namespace (id) on delete cascade,
    creator_sub uuid not null,
    name varchar(64) not null,
    description text not null,
    access_token varchar(64) not null
);
--rollback drop table if exists application;

create unique index uix_application__namespace_id_name on application (namespace_id, name);
--rollback drop index if exists uix_application__namespace_id_name;
