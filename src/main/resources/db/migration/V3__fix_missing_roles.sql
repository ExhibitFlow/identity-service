-- Flyway migration: Fix missing VIEWER and MANAGER roles
-- This handles cases where V2 migration was partially applied

-- Insert VIEWER role if missing
INSERT INTO roles (id, name, description) 
SELECT gen_random_uuid(), 'VIEWER', 'Can view resources (read-only access)'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'VIEWER'
);

-- Insert MANAGER role if missing
INSERT INTO roles (id, name, description) 
SELECT gen_random_uuid(), 'MANAGER', 'Can manage resources (create, read, update)'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'MANAGER'
);

-- Ensure content permissions exist
INSERT INTO permissions (id, name, description, resource, action) 
SELECT gen_random_uuid(), 'content:read', 'Read content', 'content', 'read'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'content:read');

INSERT INTO permissions (id, name, description, resource, action) 
SELECT gen_random_uuid(), 'content:write', 'Create and update content', 'content', 'write'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'content:write');

INSERT INTO permissions (id, name, description, resource, action) 
SELECT gen_random_uuid(), 'content:delete', 'Delete content', 'content', 'delete'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'content:delete');

-- Assign permissions to VIEWER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'VIEWER'
AND p.name IN ('user:read', 'role:read', 'permission:read', 'content:read')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- Assign permissions to MANAGER role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'MANAGER'
AND p.name IN ('user:read', 'user:write', 'role:read', 'content:read', 'content:write')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
