CREATE TABLE tenants
(
    id         UUID PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    slug       VARCHAR(100) NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'active',
    created_at TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    updated_at TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    deleted_at TIMESTAMP,
    CONSTRAINT tenants_status_check
        CHECK (status IN ('active', 'suspended', 'inactive'))
);

CREATE UNIQUE INDEX uq_tenants_slug ON tenants (slug) WHERE deleted_at IS NULL;
CREATE INDEX idx_tenants_status ON tenants (status) WHERE deleted_at IS NULL;

CREATE TABLE tenant_ip_allowlist
(
    id          UUID PRIMARY KEY,
    tenant_id   UUID        NOT NULL,
    cidr        VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_at  TIMESTAMP   NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    deleted_at  TIMESTAMP,
    CONSTRAINT fk_tenant_ip_allowlist_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id)
);

CREATE INDEX idx_tenant_ip_allowlist_tenant ON tenant_ip_allowlist (tenant_id) WHERE deleted_at IS NULL;

CREATE TABLE modules
(
    id          UUID PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC')
);

CREATE TABLE tenant_modules
(
    id         UUID PRIMARY KEY,
    tenant_id  UUID      NOT NULL,
    module_id  UUID      NOT NULL,
    enabled    BOOLEAN   NOT NULL DEFAULT true,
    enabled_at TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    updated_at TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    deleted_at TIMESTAMP,
    CONSTRAINT fk_tenant_modules_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_tenant_modules_module
        FOREIGN KEY (module_id) REFERENCES modules (id)
);

CREATE UNIQUE INDEX uq_tenant_modules ON tenant_modules (tenant_id, module_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_tenant_modules_tenant ON tenant_modules (tenant_id) WHERE deleted_at IS NULL;

CREATE TABLE users
(
    id                   UUID PRIMARY KEY,
    tenant_id            UUID         NOT NULL,
    email                VARCHAR(255) NOT NULL,
    password_hash        VARCHAR(255) NOT NULL,
    status               VARCHAR(20)  NOT NULL DEFAULT 'active',
    must_change_password BOOLEAN      NOT NULL DEFAULT false,
    password_changed_at  TIMESTAMP,
    last_login_at        TIMESTAMP,
    failed_attempts      INTEGER      NOT NULL DEFAULT 0,
    locked_until         TIMESTAMP,
    created_at           TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    updated_at           TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    deleted_at           TIMESTAMP,
    CONSTRAINT fk_users_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT users_status_check
        CHECK (status IN ('active', 'suspended', 'locked'))
);

CREATE UNIQUE INDEX uq_users_email ON users (email) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_tenant ON users (tenant_id) WHERE deleted_at IS NULL;

CREATE TABLE roles
(
    id          UUID PRIMARY KEY,
    code        VARCHAR(100) NOT NULL UNIQUE,
    module_id   UUID,
    description TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    CONSTRAINT fk_roles_module
        FOREIGN KEY (module_id) REFERENCES modules (id)
);

CREATE INDEX idx_roles_module ON roles (module_id);

CREATE TABLE user_roles
(
    id         UUID PRIMARY KEY,
    user_id    UUID      NOT NULL,
    role_id    UUID      NOT NULL,
    granted_at TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    granted_by UUID,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_user_roles_granted_by
        FOREIGN KEY (granted_by) REFERENCES users (id)
);

CREATE UNIQUE INDEX uq_user_roles ON user_roles (user_id, role_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_user_roles_user ON user_roles (user_id) WHERE deleted_at IS NULL;

CREATE TABLE api_clients
(
    id          UUID PRIMARY KEY,
    tenant_id   UUID         NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'active',
    created_at  TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    updated_at  TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    deleted_at  TIMESTAMP,
    CONSTRAINT fk_api_clients_tenant
        FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT api_clients_status_check
        CHECK (status IN ('active', 'suspended'))
);

CREATE INDEX idx_api_clients_tenant ON api_clients (tenant_id) WHERE deleted_at IS NULL;

CREATE TABLE api_keys
(
    id            UUID PRIMARY KEY,
    api_client_id UUID         NOT NULL,
    key_hash      VARCHAR(255) NOT NULL,
    key_prefix    VARCHAR(16)  NOT NULL,
    name          VARCHAR(255) NOT NULL,
    expires_at    TIMESTAMP,
    last_used_at  TIMESTAMP,
    revoked_at    TIMESTAMP,
    created_at    TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    deleted_at    TIMESTAMP,
    CONSTRAINT fk_api_keys_client
        FOREIGN KEY (api_client_id) REFERENCES api_clients (id)
);

CREATE UNIQUE INDEX uq_api_keys_hash ON api_keys (key_hash) WHERE deleted_at IS NULL;
CREATE INDEX idx_api_keys_client ON api_keys (api_client_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_api_keys_prefix ON api_keys (key_prefix) WHERE deleted_at IS NULL;

CREATE TABLE api_client_roles
(
    id            UUID PRIMARY KEY,
    api_client_id UUID      NOT NULL,
    role_id       UUID      NOT NULL,
    granted_at    TIMESTAMP NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    deleted_at    TIMESTAMP,
    CONSTRAINT fk_api_client_roles_client
        FOREIGN KEY (api_client_id) REFERENCES api_clients (id),
    CONSTRAINT fk_api_client_roles_role
        FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE UNIQUE INDEX uq_api_client_roles ON api_client_roles (api_client_id, role_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_api_client_roles_client ON api_client_roles (api_client_id) WHERE deleted_at IS NULL;

CREATE TABLE api_client_ip_allowlist
(
    id            UUID PRIMARY KEY,
    api_client_id UUID        NOT NULL,
    cidr          VARCHAR(50) NOT NULL,
    description   VARCHAR(255),
    created_at    TIMESTAMP   NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    deleted_at    TIMESTAMP,
    CONSTRAINT fk_api_client_ip_allowlist_client
        FOREIGN KEY (api_client_id) REFERENCES api_clients (id)
);

CREATE INDEX idx_api_client_ip_allowlist_client ON api_client_ip_allowlist (api_client_id) WHERE deleted_at IS NULL;

CREATE TABLE refresh_tokens
(
    id           UUID PRIMARY KEY,
    user_id      UUID         NOT NULL,
    token_hash   VARCHAR(255) NOT NULL,
    expires_at   TIMESTAMP    NOT NULL,
    revoked_at   TIMESTAMP,
    created_at   TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    last_used_at TIMESTAMP,
    user_agent   VARCHAR(500),
    ip_address   VARCHAR(50),
    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE UNIQUE INDEX uq_refresh_tokens_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens (expires_at);

CREATE TABLE signing_keys
(
    id           UUID PRIMARY KEY,
    kid          VARCHAR(100) NOT NULL UNIQUE,
    algorithm    VARCHAR(20)  NOT NULL DEFAULT 'RS256',
    public_key   TEXT         NOT NULL,
    private_key  TEXT         NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'active',
    created_at   TIMESTAMP    NOT NULL DEFAULT (NOW() AT TIME ZONE 'UTC'),
    activated_at TIMESTAMP,
    retired_at   TIMESTAMP,
    CONSTRAINT signing_keys_status_check
        CHECK (status IN ('active', 'retired', 'revoked'))
);

CREATE INDEX idx_signing_keys_status ON signing_keys (status);

CREATE TABLE issued_tokens_audit
(
    id             UUID PRIMARY KEY,
    jti            UUID        NOT NULL,
    principal_type VARCHAR(20) NOT NULL,
    principal_id   UUID        NOT NULL,
    tenant_id      UUID        NOT NULL,
    issued_at      TIMESTAMP   NOT NULL,
    expires_at     TIMESTAMP   NOT NULL,
    ip_address     VARCHAR(50),
    user_agent     VARCHAR(500),
    CONSTRAINT issued_tokens_audit_principal_type_check
        CHECK (principal_type IN ('user', 'api_client'))
);

CREATE INDEX idx_issued_tokens_audit_jti ON issued_tokens_audit (jti);
CREATE INDEX idx_issued_tokens_audit_principal ON issued_tokens_audit (principal_type, principal_id);
CREATE INDEX idx_issued_tokens_audit_tenant ON issued_tokens_audit (tenant_id);
CREATE INDEX idx_issued_tokens_audit_issued_at ON issued_tokens_audit (issued_at);