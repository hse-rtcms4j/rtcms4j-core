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

create index ix_namespace_admin__user_sub on namespace_admin (user_sub);
--rollback drop index if exists ix_namespace_admin__user_sub;

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
    creation_by_service boolean not null
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

create index ix_application_manager__user_sub on application_manager (user_sub);
--rollback drop index if exists ix_application_manager__user_sub;

create table configuration(
    ---- tech fields
    id bigserial primary key not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    ---- entity fields
    application_id bigint not null references application (id) on delete cascade,
    creator_sub uuid not null,
    name varchar(64) not null,
    schema_source_type varchar(16) not null,
    actual_commit_id bigint,
    actual_commit_version varchar
);
--rollback drop table if exists configuration;

create unique index uix_configuration__application_id_name on configuration (application_id, name);
--rollback drop index if exists uix_configuration__application_id_name;

create table config_schema(
    ---- tech fields
    id bigserial primary key not null,
    created_at timestamptz not null default now(),
    ---- entity fields
    configuration_id bigint not null references configuration (id) on delete cascade,
    source_type varchar(16) not null,
    source_identity varchar(64) not null,
    json_schema jsonb not null
);
--rollback drop table if exists config_schema;

create unique index ix_config_schema__configuration_id_json_schema on config_schema (configuration_id, json_schema);
--rollback drop index if exists ix_config_schema__configuration_id_json_schema;

create table config_commit(
    ---- tech fields
    id bigserial primary key not null,
    created_at timestamptz not null default now(),
    ---- entity fields
    config_schema_id bigint not null references config_schema (id) on delete cascade,
    configuration_id bigint not null,
    source_type varchar(16) not null,
    source_identity varchar(64) not null,
    json_values jsonb not null
);
--rollback drop table if exists config_commit;

create unique index ix_config_commit__config_schema_id_json_values on config_commit (config_schema_id, (json_values->>'version'));
--rollback drop index if exists ix_config_commit__config_schema_id_json_values;
