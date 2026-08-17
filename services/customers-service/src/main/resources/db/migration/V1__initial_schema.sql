CREATE TABLE customers (
                           id              UUID PRIMARY KEY,
                           tenant_id       UUID         NOT NULL,
                           cpf             VARCHAR(11)  NOT NULL,
                           full_name       VARCHAR(255) NOT NULL,
                           birth_date      DATE         NOT NULL,
                           gender          VARCHAR(20)  NOT NULL,
                           nationality     VARCHAR(50)  NOT NULL DEFAULT 'brasileira',
                           mother_name     VARCHAR(255) NOT NULL,
                           marital_status  VARCHAR(20)  NOT NULL,
                           email           VARCHAR(255),
                           phone_number    VARCHAR(20)  NOT NULL,
                           address_cep           VARCHAR(8)   NOT NULL,
                           address_street        VARCHAR(255) NOT NULL,
                           address_number        VARCHAR(20),
                           address_complement    VARCHAR(255),
                           address_neighborhood  VARCHAR(255),
                           address_city          VARCHAR(255) NOT NULL,
                           address_state         CHAR(2)      NOT NULL,
                           address_country       VARCHAR(50)  NOT NULL DEFAULT 'BR',
                           status          VARCHAR(20)  NOT NULL DEFAULT 'active',
                           created_at      TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
                           updated_at      TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
                           deleted_at      TIMESTAMP,
                           created_by      UUID         NOT NULL,
                           updated_by      UUID         NOT NULL,
                           CONSTRAINT customers_gender_check
                               CHECK (gender IN ('male', 'female', 'other')),
                           CONSTRAINT customers_marital_status_check
                               CHECK (marital_status IN ('single', 'married', 'divorced', 'widowed', 'stable_union')),
                           CONSTRAINT customers_status_check
                               CHECK (status IN ('active', 'inactive'))
);

CREATE UNIQUE INDEX uq_customers_cpf_per_tenant
    ON customers (tenant_id, cpf)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_customers_tenant_id
    ON customers (tenant_id)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_customers_full_name_lower
    ON customers (LOWER(full_name))
    WHERE deleted_at IS NULL;

CREATE TABLE customer_bank_accounts (
                                        id              UUID PRIMARY KEY,
                                        customer_id     UUID         NOT NULL,
                                        bank_code       VARCHAR(10)  NOT NULL,
                                        agency          VARCHAR(10)  NOT NULL,
                                        account_number  VARCHAR(20)  NOT NULL,
                                        account_digit   VARCHAR(2),
                                        account_type    VARCHAR(20)  NOT NULL,
                                        purpose         VARCHAR(20)  NOT NULL,
                                        is_primary      BOOLEAN      NOT NULL DEFAULT false,
                                        created_at      TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
                                        updated_at      TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
                                        deleted_at      TIMESTAMP,
                                        CONSTRAINT fk_customer_bank_accounts_customer
                                            FOREIGN KEY (customer_id) REFERENCES customers (id),
                                        CONSTRAINT customer_bank_accounts_account_type_check
                                            CHECK (account_type IN ('checking', 'savings')),
                                        CONSTRAINT customer_bank_accounts_purpose_check
                                            CHECK (purpose IN ('disbursement', 'payoff'))
);

CREATE INDEX idx_customer_bank_accounts_customer_id
    ON customer_bank_accounts (customer_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_customer_bank_accounts_primary_per_purpose
    ON customer_bank_accounts (customer_id, purpose)
    WHERE deleted_at IS NULL AND is_primary = true;

CREATE TABLE customer_documents (
                                    id              UUID PRIMARY KEY,
                                    customer_id     UUID         NOT NULL,
                                    document_type   VARCHAR(10)  NOT NULL,
                                    document_number VARCHAR(50)  NOT NULL,
                                    issuer          VARCHAR(20)  NOT NULL,
                                    issuer_state    CHAR(2)      NOT NULL,
                                    issued_at       DATE         NOT NULL,
                                    expires_at      DATE,
                                    created_at      TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
                                    updated_at      TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
                                    deleted_at      TIMESTAMP,
                                    CONSTRAINT fk_customer_documents_customer
                                        FOREIGN KEY (customer_id) REFERENCES customers (id),
                                    CONSTRAINT customer_documents_type_check
                                        CHECK (document_type IN ('rg', 'cnh')),
                                    CONSTRAINT customer_documents_issuer_check
                                        CHECK (issuer IN ('SSP', 'DETRAN', 'PF', 'MARINHA', 'EXERCITO', 'AERONAUTICA', 'OUTROS'))
);

CREATE INDEX idx_customer_documents_customer_id
    ON customer_documents (customer_id)
    WHERE deleted_at IS NULL;

CREATE TABLE customer_audit (
                                id           UUID PRIMARY KEY,
                                customer_id  UUID         NOT NULL,
                                tenant_id    UUID         NOT NULL,
                                action       VARCHAR(50)  NOT NULL,
                                old_value    VARCHAR(500),
                                new_value    VARCHAR(500),
                                changed_by   UUID         NOT NULL,
                                changed_at   TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
                                CONSTRAINT customer_audit_action_check
                                    CHECK (action IN (
                                                      'cpf_changed',
                                                      'full_name_changed',
                                                      'bank_account_added',
                                                      'bank_account_removed',
                                                      'status_changed',
                                                      'customer_deleted'
                                        ))
);

CREATE INDEX idx_customer_audit_customer_id
    ON customer_audit (customer_id, changed_at DESC);

CREATE INDEX idx_customer_audit_tenant_id
    ON customer_audit (tenant_id, changed_at DESC);
