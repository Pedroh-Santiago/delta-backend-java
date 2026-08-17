CREATE TABLE customers.document_issuers (
    id    UUID PRIMARY KEY,
    name  VARCHAR(20) NOT NULL UNIQUE
);

INSERT INTO customers.document_issuers (id, name) VALUES
                                                      ('0197a000-0000-7000-8000-000000000001', 'SSP'),
                                                      ('0197a000-0000-7000-8000-000000000002', 'DETRAN'),
                                                      ('0197a000-0000-7000-8000-000000000003', 'PF'),
                                                      ('0197a000-0000-7000-8000-000000000004', 'MARINHA'),
                                                      ('0197a000-0000-7000-8000-000000000005', 'EXERCITO'),
                                                      ('0197a000-0000-7000-8000-000000000006', 'AERONAUTICA'),
                                                      ('0197a000-0000-7000-8000-000000000007', 'OUTROS');

ALTER TABLE customers.customer_documents
    ADD COLUMN issuer_id UUID,
    ADD CONSTRAINT fk_customer_documents_issuer FOREIGN KEY (issuer_id) REFERENCES customers.document_issuers(id);

UPDATE customers.customer_documents SET issuer_id = (SELECT id FROM customers.document_issuers WHERE document_issuers.name = customer_documents.issuer);

ALTER TABLE customers.customer_documents ALTER COLUMN issuer_id SET NOT NULL;
ALTER TABLE customers.customer_documents DROP CONSTRAINT customer_documents_issuer_check;
ALTER TABLE customers.customer_documents DROP COLUMN issuer;