INSERT INTO modules (id, code, name, description)
VALUES ('0196f000-0000-7000-8000-000000000001', 'identity', 'Identidade',
        'Gestão de usuários, roles e tenants'),
       ('0196f000-0000-7000-8000-000000000002', 'customers', 'Clientes',
        'Cadastro de clientes finais'),
       ('0196f000-0000-7000-8000-000000000003', 'products', 'Produtos',
        'Configuração de produtos e taxas'),
       ('0196f000-0000-7000-8000-000000000004', 'lending', 'Empréstimos',
        'Consignado novo e compra de dívida'),
       ('0196f000-0000-7000-8000-000000000005', 'card', 'Cartão', 'Cartão consignado'),
       ('0196f000-0000-7000-8000-000000000006', 'documents', 'Documentos', 'Anexos e arquivos');

INSERT INTO roles (id, code, module_id, description)
VALUES ('0196f000-0000-7000-8000-000000000101', 'platform.admin', NULL,
        'Administrador da plataforma'),

       ('0196f000-0000-7000-8000-000000000110', 'identity.admin',
        '0196f000-0000-7000-8000-000000000001', 'Administra usuários e roles do tenant'),

       ('0196f000-0000-7000-8000-000000000120', 'customers.admin',
        '0196f000-0000-7000-8000-000000000002', 'Cria, edita e remove clientes'),
       ('0196f000-0000-7000-8000-000000000121', 'customers.viewer',
        '0196f000-0000-7000-8000-000000000002', 'Visualiza clientes'),

       ('0196f000-0000-7000-8000-000000000130', 'products.admin',
        '0196f000-0000-7000-8000-000000000003', 'Configura produtos e taxas'),
       ('0196f000-0000-7000-8000-000000000131', 'products.viewer',
        '0196f000-0000-7000-8000-000000000003', 'Visualiza catálogo de produtos'),

       ('0196f000-0000-7000-8000-000000000140', 'lending.admin',
        '0196f000-0000-7000-8000-000000000004', 'Administra propostas de empréstimo'),
       ('0196f000-0000-7000-8000-000000000141', 'lending.operator',
        '0196f000-0000-7000-8000-000000000004', 'Cria e gerencia propostas'),
       ('0196f000-0000-7000-8000-000000000142', 'lending.viewer',
        '0196f000-0000-7000-8000-000000000004', 'Visualiza propostas'),

       ('0196f000-0000-7000-8000-000000000150', 'card.admin',
        '0196f000-0000-7000-8000-000000000005',
        'Administra propostas de cartão'),
       ('0196f000-0000-7000-8000-000000000151', 'card.operator',
        '0196f000-0000-7000-8000-000000000005', 'Cria e gerencia propostas de cartão'),
       ('0196f000-0000-7000-8000-000000000152', 'card.viewer',
        '0196f000-0000-7000-8000-000000000005',
        'Visualiza propostas de cartão'),

       ('0196f000-0000-7000-8000-000000000160', 'documents.admin',
        '0196f000-0000-7000-8000-000000000006', 'Gerencia documentos'),
       ('0196f000-0000-7000-8000-000000000161', 'documents.viewer',
        '0196f000-0000-7000-8000-000000000006', 'Visualiza documentos');