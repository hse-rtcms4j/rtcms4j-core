--liquibase formatted sql

--changeset Enzhine:1.0.0:1
create extension if not exists "uuid-ossp";

--changeset Enzhine:1.0.0:2
create table namespace(
    ---- tech fields
    id bigserial primary key not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    ---- entity fields
    creator_sub uuid not null,
    name varchar(64) not null,
    description text not null
)
--rollback drop table if exists namespace;

--changeset Enzhine:1.0.0:3
create index ix_namespace__id on namespace (id);
--rollback drop index if exists ix_namespace__id;

--changeset Enzhine:1.0.0:4
create unique index uix_namespace__name on namespace (name);
--rollback drop index if exists uix_namespace__name;

--changeset Enzhine:1.0.0:5
create table namespace_admin(
    ---- tech fields
    id bigserial primary key not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    ---- entity fields
    namespace_id bigint not null references namespace (id) on delete cascade,
    assigner_sub uuid not null,
    user_sub uuid not null
)
--rollback drop table if exists namespace_admin;

--changeset Enzhine:1.0.0:6
create unique index uix_namespace_admin__user_sub_namespace_id on namespace_admin (namespace_id, user_sub);
--rollback drop index if exists uix_namespace_admin__user_sub_namespace_id;

--create table deletion_audit(
--    ---- tech fields
--    id bigserial primary key not null,
--    created_at timestamptz not null default now(),
--    ---- audit fields
--    table_affected varchar(64) not null,
--    initiator_sub uuid not null
--)
