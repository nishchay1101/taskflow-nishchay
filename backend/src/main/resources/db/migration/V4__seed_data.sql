-- =============================================================
-- Seed Data — V4
-- 3 users, 3 projects, 6 tasks with varied statuses/priorities
-- All passwords = password123 (BCrypt cost 12)
-- =============================================================

-- Users
INSERT INTO users (id, name, email, password, created_at) VALUES
(
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'Arjun Sharma',
    'arjun@example.com',
    '$2b$12$edyJWHpdWDQFNrn39LVQMOxCl82LFc7zrmIq7nQWqVPHX4XtiNjkG',
    NOW()
),
(
    'b1ffcd00-1d2c-4fa9-ac7e-7cc0ce491b22',
    'Priya Mehta',
    'priya@example.com',
    '$2b$12$edyJWHpdWDQFNrn39LVQMOxCl82LFc7zrmIq7nQWqVPHX4XtiNjkG',
    NOW()
),
(
    'c2aade11-2e3d-4ba0-bd8f-8dd1df502c33',
    'Rohan Verma',
    'rohan@example.com',
    '$2b$12$edyJWHpdWDQFNrn39LVQMOxCl82LFc7zrmIq7nQWqVPHX4XtiNjkG',
    NOW()
);

-- Projects
-- Project 1: owned by Arjun
-- Project 2: owned by Priya
-- Project 3: owned by Arjun
INSERT INTO projects (id, name, description, owner_id, created_at) VALUES
(
    'd3bbef22-3f4e-4ba1-ae9a-9ee2ef613d44',
    'Zomato Payments Revamp',
    'Revamping the payment gateway integration for faster checkout',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    NOW()
),
(
    'e4ccfa33-4a5f-4bc2-bf0b-0ff3fa724e55',
    'Rider App Redesign',
    'Redesigning the rider-facing mobile app for better UX',
    'b1ffcd00-1d2c-4fa9-ac7e-7cc0ce491b22',
    NOW()
),
(
    'f5ddab44-5b6a-4cd3-ca1c-1aa4ab835f66',
    'Internal Analytics Dashboard',
    'Build an internal dashboard for ops team to track delivery metrics',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    NOW()
);

-- Tasks
-- Project 1 (Zomato Payments Revamp) — owned by Arjun
--   Task 1: assigned to Priya, IN_PROGRESS, HIGH
--   Task 2: assigned to Rohan, TODO, MEDIUM
--   Task 3: unassigned, DONE, LOW

-- Project 2 (Rider App Redesign) — owned by Priya
--   Task 4: assigned to Arjun, TODO, HIGH
--   Task 5: unassigned, IN_PROGRESS, MEDIUM

-- Project 3 (Internal Analytics Dashboard) — owned by Arjun
--   Task 6: assigned to Rohan, DONE, HIGH

INSERT INTO tasks (id, title, description, status, priority, due_date, project_id, assignee_id, creator_id, created_at, updated_at) VALUES
(
    'a1bb1111-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    'Integrate Razorpay webhook handler',
    'Set up webhook endpoint to handle payment success and failure events from Razorpay',
    'IN_PROGRESS',
    'HIGH',
    '2025-06-30',
    'd3bbef22-3f4e-4ba1-ae9a-9ee2ef613d44',
    'b1ffcd00-1d2c-4fa9-ac7e-7cc0ce491b22',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    NOW(),
    NOW()
),
(
    'b2cc2222-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    'Write unit tests for payment service',
    'Cover edge cases: partial refunds, duplicate transactions, timeout retries',
    'TODO',
    'MEDIUM',
    '2025-07-15',
    'd3bbef22-3f4e-4ba1-ae9a-9ee2ef613d44',
    'c2aade11-2e3d-4ba0-bd8f-8dd1df502c33',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    NOW(),
    NOW()
),
(
    'c3dd3333-cccc-cccc-cccc-cccccccccccc',
    'Deprecate old payment gateway endpoints',
    'Remove legacy UPI endpoints after migration is complete',
    'DONE',
    'LOW',
    '2025-05-01',
    'd3bbef22-3f4e-4ba1-ae9a-9ee2ef613d44',
    NULL,
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    NOW(),
    NOW()
),
(
    'd4ee4444-dddd-dddd-dddd-dddddddddddd',
    'Redesign rider earnings screen',
    'New layout showing daily, weekly, monthly breakdown with charts',
    'TODO',
    'HIGH',
    '2025-08-01',
    'e4ccfa33-4a5f-4bc2-bf0b-0ff3fa724e55',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'b1ffcd00-1d2c-4fa9-ac7e-7cc0ce491b22',
    NOW(),
    NOW()
),
(
    'e5ff5555-eeee-eeee-eeee-eeeeeeeeeeee',
    'Fix map rendering lag on low-end devices',
    'Profile and optimise map tile loading for devices with less than 3GB RAM',
    'IN_PROGRESS',
    'MEDIUM',
    '2025-07-20',
    'e4ccfa33-4a5f-4bc2-bf0b-0ff3fa724e55',
    NULL,
    'b1ffcd00-1d2c-4fa9-ac7e-7cc0ce491b22',
    NOW(),
    NOW()
),
(
    'f6aa6666-ffff-ffff-ffff-ffffffffffff',
    'Build delivery heatmap query',
    'Write optimised PostgreSQL query to aggregate delivery counts by geo-grid for heatmap visualisation',
    'DONE',
    'HIGH',
    '2025-05-15',
    'f5ddab44-5b6a-4cd3-ca1c-1aa4ab835f66',
    'c2aade11-2e3d-4ba0-bd8f-8dd1df502c33',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    NOW(),
    NOW()
);