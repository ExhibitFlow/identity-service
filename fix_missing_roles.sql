-- Fix: Insert missing VIEWER role if it doesn't exist
INSERT INTO roles (id, name, description) 
SELECT gen_random_uuid(), 'VIEWER', 'Can view resources (read-only access)'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'VIEWER'
);

-- Fix: Insert missing MANAGER role if it doesn't exist
INSERT INTO roles (id, name, description) 
SELECT gen_random_uuid(), 'MANAGER', 'Can manage resources (create, read, update)'
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE name = 'MANAGER'
);

-- Assign read-only permissions to VIEWER role (if not already assigned)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'VIEWER'
AND p.name IN ('user:read', 'role:read', 'permission:read', 'content:read')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- Assign read and write permissions to MANAGER role (if not already assigned)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'MANAGER'
AND p.name IN ('user:read', 'user:write', 'role:read', 'content:read', 'content:write')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);

-- Verify roles exist
SELECT name, description FROM roles ORDER BY name;
