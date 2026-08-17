CREATE TABLE internal_transferences
(
    id  UUID PRIMARY KEY ,
    account_number BIGINT NOT NULL ,
    amount  INTEGER NOT NULL ,
    description VARCHAR(70),
    requested_at  TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    requested_by_id UUID NOT NULL
);

CREATE INDEX idx_internal_transferences_requested_by ON internal_transferences (requested_by_id);