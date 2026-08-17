CREATE TABLE pix_payments
(
    id                         UUID PRIMARY KEY,
    account_id                 BIGINT NOT NULL,
    recipient_institution_code INTEGER NOT NULL,
    recipient_branch_code      VARCHAR(15) NOT NULL,
    recipient_account_number   VARCHAR(50) NOT NULL,
    recipient_account_type     VARCHAR(15) NOT NULL,
    recipient_name             VARCHAR(200) NOT NULL,
    operation_amount           BIGINT NOT NULL,
    created_at                 TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC')
);

CREATE INDEX idx_pix_payments_account_id ON pix_payments (account_id);