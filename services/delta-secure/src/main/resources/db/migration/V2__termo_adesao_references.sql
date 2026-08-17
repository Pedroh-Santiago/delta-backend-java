CREATE TABLE termo_adesao_references (
   id UUID PRIMARY KEY,
   ticket VARCHAR(64) NOT NULL UNIQUE,
   hero_seguros_id INTEGER NOT NULL,
   convenio VARCHAR(32) NOT NULL,
   external_id VARCHAR(128),
   created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
