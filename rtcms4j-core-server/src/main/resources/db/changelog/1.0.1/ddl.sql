--liquibase formatted sql

--changeset Enzhine:1.0.1:1

create table outbox_task_kc_client(
    id bigserial primary key not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    attempt int not null,
    skip boolean not null,
    --content
    action varchar(16) not null,
    namespace_id bigint not null,
    application_id bigint not null
);
--rollback drop table if exists outbox_task_kc_client;

create index ix_outbox_task_kc_client__created_at__skipfalse on outbox_task_kc_client (created_at) where skip = false;
--rollback drop index if exists ix_outbox_task_kc_client__created_at__skipfalse;
