--liquibase formatted sql

--changeset Enzhine:1.0.0:1
create extension if not exists "uuid-ossp";

--changeset Enzhine:1.0.0:2
create table namespace(
    ---- tech fields
    id bigserial primary key not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted boolean not null default false,
    ---- entity fields
    creator_sub uuid not null,
    name varchar(64) not null,
    description text not null
)
--rollback drop table if exists namespace;

--changeset Enzhine:1.0.0:3
create index namespace_id_ix on namespace (id) where deleted = false;
--rollback drop index if exists namespace_id_ix;

--changeset Enzhine:1.0.0:4
create unique index namespace_name_uix on namespace (name) where deleted = false;
--rollback drop index if exists namespace_name_uix;
