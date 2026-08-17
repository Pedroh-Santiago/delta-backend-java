CREATE TABLE areas (
                       id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name        VARCHAR(100) NOT NULL UNIQUE,
                       active      BOOLEAN NOT NULL DEFAULT true,
                       created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
