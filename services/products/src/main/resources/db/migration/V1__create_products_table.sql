CREATE TABLE products (
                          id uuid PRIMARY KEY,
                          tenant_id uuid NOT NULL,
                          type varchar(40) NOT NULL,
                          agreement_name varchar(160) NOT NULL,
                          display_name varchar(200) NOT NULL,
                          min_monthly_rate numeric(10,6) NOT NULL,
                          max_monthly_rate numeric(10,6) NOT NULL,
                          min_months integer NOT NULL,
                          max_months integer NOT NULL,
                          min_amount numeric(19,2) NOT NULL,
                          max_amount numeric(19,2) NOT NULL,
                          commission_rate numeric(10,6) NULL,
                          active boolean NOT NULL DEFAULT true,
                          created_at timestamptz NOT NULL,
                          updated_at timestamptz NOT NULL,
                          created_by uuid NULL,
                          updated_by uuid NULL,

                          CONSTRAINT chk_products_min_monthly_rate_non_negative CHECK (min_monthly_rate >= 0),
                          CONSTRAINT chk_products_max_monthly_rate_gte_min CHECK (max_monthly_rate >= min_monthly_rate),
                          CONSTRAINT chk_products_min_months_positive CHECK (min_months >= 1),
                          CONSTRAINT chk_products_max_months_gte_min CHECK (max_months >= min_months),
                          CONSTRAINT chk_products_min_amount_non_negative CHECK (min_amount >= 0),
                          CONSTRAINT chk_products_max_amount_gte_min CHECK (max_amount >= min_amount),
                          CONSTRAINT chk_products_commission_rate_non_negative CHECK (commission_rate IS NULL OR commission_rate >= 0)
);

CREATE INDEX idx_products_tenant_type_active
    ON products (tenant_id, type, active);

CREATE INDEX idx_products_tenant_agreement
    ON products (tenant_id, agreement_name);

CREATE UNIQUE INDEX uk_products_active_tenant_type_agreement
    ON products (tenant_id, type, lower(agreement_name))
    WHERE active = true;