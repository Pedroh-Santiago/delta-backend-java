ALTER TABLE roles
    ADD COLUMN active     BOOLEAN   NOT NULL DEFAULT TRUE,
    ADD COLUMN deleted_at TIMESTAMP;

ALTER TABLE roles
DROP CONSTRAINT IF EXISTS roles_code_key;

CREATE UNIQUE INDEX uq_roles_code ON roles (code) WHERE deleted_at IS NULL;
