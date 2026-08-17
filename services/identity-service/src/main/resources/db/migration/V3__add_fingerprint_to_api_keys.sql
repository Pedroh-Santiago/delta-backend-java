ALTER TABLE identity.api_keys
    ADD COLUMN fingerprint VARCHAR(64),
    ADD COLUMN updated_at  TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC');

CREATE UNIQUE INDEX uq_api_keys_fingerprint
    ON identity.api_keys (fingerprint)
    WHERE deleted_at IS NULL AND fingerprint IS NOT NULL;

CREATE INDEX idx_api_keys_api_client_id
    ON identity.api_keys (api_client_id)
    WHERE deleted_at IS NULL;

COMMENT ON COLUMN identity.api_keys.fingerprint IS 'HMAC-SHA256 da api_key em hex. Usado pra cache Redis e invalidação na revogação.';