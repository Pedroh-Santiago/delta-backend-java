CREATE TABLE idempotency_keys
(
    key UUID PRIMARY KEY ,
    response    TEXT NOT NULL ,
    created_at   TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC')
);