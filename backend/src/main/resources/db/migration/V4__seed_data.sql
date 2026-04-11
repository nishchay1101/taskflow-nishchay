INSERT INTO users (id, name, email, password) VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'Test User',
    'test@example.com',
    '$2a$12$wjrybROkWv.OQuL8crNooO3iGjOfCBOvjb2pDi5FArMAe0GARjRzq'
);

INSERT INTO projects (id, name, description, owner_id) VALUES (
    'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'Sample Project',
    'A seed project for testing',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
);

INSERT INTO tasks (title, status, priority, project_id, creator_id) VALUES
    ('Set up project structure', 'done',        'high',   'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
    ('Implement auth',          'in_progress',  'high',   'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'),
    ('Write tests',             'todo',         'medium', 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11');