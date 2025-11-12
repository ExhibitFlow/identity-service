-- Insert default roles
INSERT INTO roles (id, name, description) VALUES
    (gen_random_uuid(), 'USER', 'Default user role'),
    (gen_random_uuid(), 'ADMIN', 'Administrator role'),
    (gen_random_uuid(), 'MODERATOR', 'Moderator role');

-- Insert default permissions
INSERT INTO permissions (id, name, description, resource, action) VALUES
    (gen_random_uuid(), 'user:read', 'Read user information', 'user', 'read'),
    (gen_random_uuid(), 'user:write', 'Create and update user information', 'user', 'write'),
    (gen_random_uuid(), 'user:delete', 'Delete user', 'user', 'delete'),
    (gen_random_uuid(), 'role:read', 'Read role information', 'role', 'read'),
    (gen_random_uuid(), 'role:write', 'Create and update roles', 'role', 'write'),
    (gen_random_uuid(), 'role:delete', 'Delete roles', 'role', 'delete'),
    (gen_random_uuid(), 'permission:read', 'Read permission information', 'permission', 'read'),
    (gen_random_uuid(), 'permission:write', 'Create and update permissions', 'permission', 'write'),
    (gen_random_uuid(), 'permission:delete', 'Delete permissions', 'permission', 'delete');

-- Assign permissions to ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN';

-- Assign limited permissions to USER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'USER'
AND p.name IN ('user:read');

-- Assign moderate permissions to MODERATOR role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'MODERATOR'
AND p.name IN ('user:read', 'user:write');

-- Insert default admin user (password: admin123)
-- Password is BCrypt hash of "admin123"
INSERT INTO users (id, username, email, password, first_name, last_name, enabled, account_non_expired, account_non_locked, credentials_non_expired)
VALUES (
    gen_random_uuid(),
    'admin',
    'admin@exhibitflow.com',
    '$2a$10$7WNbrw0pYCdp9lSv8VhPqeGTuObJkVtggzUNmhlst7UQ4OYBwLEpm',
    'Admin',
    'User',
    TRUE,
    TRUE,
    TRUE,
    TRUE
);

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ADMIN';
