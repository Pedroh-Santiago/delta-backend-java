INSERT INTO roles (id, module_id, code, description)
VALUES
    ('0196f000-0000-7000-8000-000000000132', '0196f000-0000-7000-8000-000000000003', 'products.lending.read',   'Pode consultar produtos lending'),
    ('0196f000-0000-7000-8000-000000000133', '0196f000-0000-7000-8000-000000000003', 'products.lending.create', 'Pode criar produtos lending'),
    ('0196f000-0000-7000-8000-000000000134', '0196f000-0000-7000-8000-000000000003', 'products.lending.update', 'Pode atualizar produtos lending'),
    ('0196f000-0000-7000-8000-000000000135', '0196f000-0000-7000-8000-000000000003', 'products.lending.delete', 'Pode remover logicamente produtos lending'),
    ('0196f000-0000-7000-8000-000000000136', '0196f000-0000-7000-8000-000000000003', 'products.lending.admin',  'Pode administrar produtos lending')
    ON CONFLICT DO NOTHING;