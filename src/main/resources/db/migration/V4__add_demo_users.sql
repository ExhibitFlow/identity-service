-- Add demo users with fixed UUIDs for consistent seeding
-- Passwords are 'password123' (BCrypt hash)

INSERT INTO users (id, username, email, password, first_name, last_name, enabled, account_non_expired, account_non_locked, credentials_non_expired)
VALUES 
    (
        '11111111-1111-1111-1111-111111111111',
        'exhibitor_a',
        'exhibitor.a@exhibitflow.com',
        '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
        'Alice',
        'Exhibitor',
        TRUE, TRUE, TRUE, TRUE
    ),
    (
        '22222222-2222-2222-2222-222222222222',
        'exhibitor_b',
        'exhibitor.b@exhibitflow.com',
        '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG',
        'Bob',
        'Exhibitor',
        TRUE, TRUE, TRUE, TRUE
    );

-- Assign MANAGER role to Exhibitor A
INSERT INTO user_roles (user_id, role_id)
SELECT '11111111-1111-1111-1111-111111111111', r.id
FROM roles r
WHERE r.name = 'MANAGER';

-- Assign VIEWER role to Exhibitor B
INSERT INTO user_roles (user_id, role_id)
SELECT '22222222-2222-2222-2222-222222222222', r.id
FROM roles r
WHERE r.name = 'VIEWER';
