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

create unique index uix_namespace_admin__namespace_id_user_sub on namespace_admin (namespace_id, user_sub);
--rollback drop index if exists uix_namespace_admin__namespace_id_user_sub;

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

create table application_manager(
    ---- tech fields
    id bigserial primary key not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    ---- entity fields
    application_id bigint not null references application (id) on delete cascade,
    assigner_sub uuid not null,
    user_sub uuid not null
);
--rollback drop table if exists application_manager;

create unique index uix_application_manager__application_id_user_sub on application_manager (application_id, user_sub);
--rollback drop index if exists uix_application_manager__application_id_user_sub;

create table configuration(
    ---- tech fields
    id bigserial primary key not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    ---- entity fields
    application_id bigint not null references application (id) on delete cascade,
    creator_sub uuid not null,
    name varchar(64) not null,
    commit_hash varchar(64),
    ---- settings
    schema_source_type varchar(16) not null
);
--rollback drop table if exists configuration;

create unique index uix_configuration__application_id_name on configuration (application_id, name);
--rollback drop index if exists uix_configuration__application_id_name;

create table configuration_commit(
    ---- tech fields
    id bigserial primary key not null,
    created_at timestamptz not null default now(),
    ---- entity fields
    configuration_id bigint not null references configuration (id) on delete cascade,
    source_type varchar(16) not null,
    source_identity varchar(64) not null,
    commit_hash varchar(64) not null,
    json_values jsonb,
    json_schema jsonb
);
--rollback drop table if exists configuration_commit;

create unique index ix_configuration_commit__configuration_id_commit_hash on configuration_commit (configuration_id, commit_hash);
--rollback drop index if exists ix_configuration_commit__configuration_id_commit_hash;
