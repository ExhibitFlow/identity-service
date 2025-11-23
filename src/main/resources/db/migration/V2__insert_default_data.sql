-- Insert default roles with generic names
INSERT INTO roles (id, name, description) VALUES
    (gen_random_uuid(), 'VIEWER', 'Can view resources (read-only access)'),
    (gen_random_uuid(), 'MANAGER', 'Can manage resources (create, read, update)'),
    (gen_random_uuid(), 'ADMIN', 'Full administrative access');

-- Insert default permissions with generic resource:action pattern
INSERT INTO permissions (id, name, description, resource, action) VALUES
    (gen_random_uuid(), 'user:read', 'Read user information', 'user', 'read'),
    (gen_random_uuid(), 'user:write', 'Create and update user information', 'user', 'write'),
    (gen_random_uuid(), 'user:delete', 'Delete user', 'user', 'delete'),
    (gen_random_uuid(), 'role:read', 'Read role information', 'role', 'read'),
    (gen_random_uuid(), 'role:write', 'Create and update roles', 'role', 'write'),
    (gen_random_uuid(), 'role:delete', 'Delete roles', 'role', 'delete'),
    (gen_random_uuid(), 'permission:read', 'Read permission information', 'permission', 'read'),
    (gen_random_uuid(), 'permission:write', 'Create and update permissions', 'permission', 'write'),
    (gen_random_uuid(), 'permission:delete', 'Delete permissions', 'permission', 'delete'),
    (gen_random_uuid(), 'content:read', 'Read content', 'content', 'read'),
    (gen_random_uuid(), 'content:write', 'Create and update content', 'content', 'write'),
    (gen_random_uuid(), 'content:delete', 'Delete content', 'content', 'delete');

-- Assign all permissions to ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN';

-- Assign read-only permissions to VIEWER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'VIEWER'
AND p.name IN ('user:read', 'role:read', 'permission:read', 'content:read');

-- Assign read and write permissions to MANAGER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'MANAGER'
AND p.name IN ('user:read', 'user:write', 'role:read', 'content:read', 'content:write');

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
