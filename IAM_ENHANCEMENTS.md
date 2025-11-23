# Identity Service - IAM Enhancements

## Overview
The Identity Service has been transformed into a comprehensive, general-purpose **Identity and Access Management (IAM)** service for the ExhibitFlow ecosystem. It now provides dynamic role and permission management capabilities that can be used by multiple microservices.

## 🎯 Key Features Implemented

### 1. **Dynamic Role Management** ✅
Create and manage roles without database migrations.

**Endpoints:**
```
POST   /api/v1/admin/roles                    # Create new role
GET    /api/v1/admin/roles                    # List all roles (paginated)
GET    /api/v1/admin/roles/{id}               # Get role by ID
GET    /api/v1/admin/roles/name/{name}        # Get role by name
PUT    /api/v1/admin/roles/{id}               # Update role
DELETE /api/v1/admin/roles/{id}               # Delete role
```

**Example Request:**
```json
POST /api/v1/admin/roles
{
  "name": "CONTENT_MODERATOR",
  "description": "Can moderate user content"
}
```

**Example Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "CONTENT_MODERATOR",
  "description": "Can moderate user content",
  "permissionCount": 0,
  "userCount": 0,
  "createdAt": "2025-11-23T10:00:00",
  "updatedAt": "2025-11-23T10:00:00"
}
```

### 2. **Dynamic Permission Management** ✅
Enable dynamic permission creation and assignment.

**Endpoints:**
```
POST   /api/v1/admin/permissions              # Create new permission
GET    /api/v1/admin/permissions              # List all permissions (paginated)
GET    /api/v1/admin/permissions/{id}         # Get permission by ID
GET    /api/v1/admin/permissions/name/{name}  # Get permission by name
PUT    /api/v1/admin/permissions/{id}         # Update permission
DELETE /api/v1/admin/permissions/{id}         # Delete permission
```

**Example Request:**
```json
POST /api/v1/admin/permissions
{
  "name": "content:moderate",
  "description": "Moderate user-generated content",
  "resource": "content",
  "action": "moderate"
}
```

**Example Response:**
```json
{
  "id": "660e8400-e29b-41d4-a716-446655440001",
  "name": "content:moderate",
  "description": "Moderate user-generated content",
  "resource": "content",
  "action": "moderate",
  "createdAt": "2025-11-23T10:00:00"
}
```

### 3. **Role-Permission Association** ✅
Assign and manage permissions for roles.

**Endpoints:**
```
POST   /api/v1/admin/roles/{roleId}/permissions           # Bulk assign permissions
DELETE /api/v1/admin/roles/{roleId}/permissions/{permissionId}  # Remove permission
GET    /api/v1/admin/roles/{roleId}/permissions           # Get role's permissions
```

**Example Request:**
```json
POST /api/v1/admin/roles/550e8400-e29b-41d4-a716-446655440000/permissions
{
  "permissionIds": [
    "660e8400-e29b-41d4-a716-446655440001",
    "660e8400-e29b-41d4-a716-446655440002"
  ]
}
```

### 4. **User-Role Management** ✅
Assign roles to users dynamically.

**Endpoints:**
```
POST   /api/v1/users/{userId}/roles              # Bulk assign roles to user
DELETE /api/v1/users/{userId}/roles/{roleId}     # Remove role from user
GET    /api/v1/users/{userId}/roles              # Get user's roles
```

**Example Request:**
```json
POST /api/v1/users/770e8400-e29b-41d4-a716-446655440003/roles
{
  "roleIds": [
    "550e8400-e29b-41d4-a716-446655440000",
    "550e8400-e29b-41d4-a716-446655440001"
  ]
}
```

### 5. **Enhanced JWT Tokens** ✅
JWT tokens now include comprehensive user information for easy consumption by microservices.

**Enhanced JWT Payload:**
```json
{
  "sub": "johndoe",
  "userId": "770e8400-e29b-41d4-a716-446655440003",
  "username": "johndoe",
  "email": "john@example.com",
  "roles": ["MANAGER", "VIEWER"],
  "authorities": ["ROLE_MANAGER", "ROLE_VIEWER", "content:read", "content:write"],
  "permissions": ["content:read", "content:write", "content:update"],
  "iss": "http://localhost:8080/api/v1",
  "iat": 1732356000,
  "exp": 1732442400
}
```

**Key Fields:**
- `userId`: Unique user identifier
- `username`: User's username
- `email`: User's email
- `roles`: Array of role names (without ROLE_ prefix)
- `authorities`: Spring Security format with ROLE_ prefix + permissions
- `permissions`: Array of permission names (resource:action format)

### 6. **Admin User Creation** ✅
Admins can create users with specific roles during creation.

**Endpoint:**
```
POST   /api/v1/admin/users                    # Admin creates user with roles
```

**Example Request:**
```json
POST /api/v1/admin/users
{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe",
  "roleIds": [
    "550e8400-e29b-41d4-a716-446655440000"
  ],
  "enabled": true
}
```

### 7. **Token Introspection** ✅
OAuth2-compliant token introspection for microservices.

**Endpoints:**
```
POST   /api/v1/oauth/introspect               # RFC 7662 token introspection
POST   /api/v1/oauth/validate                 # Simple validation endpoint
```

**Example Request:**
```
POST /api/v1/oauth/introspect?token={access_token}
```

**Example Response:**
```json
{
  "active": true,
  "username": "johndoe",
  "sub": "johndoe",
  "clientId": "identity-service",
  "exp": 1732442400,
  "iat": 1732356000,
  "roles": ["MANAGER"],
  "permissions": ["content:read", "content:write"]
}
```

### 8. **Generic Role Names** ✅
Default roles are now generic and widely applicable:

| Role | Description | Default Permissions |
|------|-------------|---------------------|
| **VIEWER** | Read-only access | user:read, role:read, permission:read, content:read |
| **MANAGER** | Can manage resources | user:read, user:write, role:read, content:read, content:write |
| **ADMIN** | Full administrative access | All permissions |

**Self-Registration:** Users who register via `/auth/register` automatically receive the **VIEWER** role.

### 9. **Configuration Properties** ✅
New configuration options in `application.yml`:

```yaml
jwt:
  # Token content customization
  include-roles: ${JWT_INCLUDE_ROLES:true}
  include-permissions: ${JWT_INCLUDE_PERMISSIONS:true}
  include-user-details: ${JWT_INCLUDE_USER_DETAILS:true}
  algorithm: ${JWT_ALGORITHM:HS512}

security:
  # Default role for new registrations
  default-role: ${SECURITY_DEFAULT_ROLE:VIEWER}
```

## 🔒 Security Features

### 1. **Admin-Only Endpoints**
All `/api/v1/admin/**` endpoints require `ROLE_ADMIN` authorization.

### 2. **Protected Operations**
- ❌ **Cannot delete ADMIN role**
- ❌ **Cannot modify ADMIN role description**
- ❌ **Users cannot remove their own ADMIN role**
- ❌ **Cannot delete roles with existing users** (must reassign first)
- ❌ **Cannot delete permissions assigned to roles** (must remove from roles first)

### 3. **Validation Rules**
- **Role names**: Uppercase letters and underscores only (e.g., `CONTENT_MODERATOR`)
- **Permission names**: Must follow `resource:action` pattern (e.g., `content:moderate`)
- **Resource/Action**: Lowercase letters, numbers, underscores, hyphens only

## 📁 New Files Created

### DTOs
- `CreateRoleRequest.java`
- `UpdateRoleRequest.java`
- `RoleResponse.java`
- `CreatePermissionRequest.java`
- `UpdatePermissionRequest.java`
- `PermissionResponse.java`
- `AssignPermissionsRequest.java`
- `AssignRolesRequest.java`
- `AdminUserCreationRequest.java`
- `TokenIntrospectionResponse.java`

### Services
- `RoleService.java` - Complete CRUD for roles
- `PermissionService.java` - Complete CRUD for permissions
- `TokenIntrospectionService.java` - OAuth token introspection

### Controllers
- `RoleController.java` - Role management endpoints
- `PermissionController.java` - Permission management endpoints
- `AdminUserController.java` - Admin user creation
- `OAuthController.java` - Token introspection endpoints

### Updated Files
- `UserService.java` - Added user-role management methods
- `JwtUtil.java` - Enhanced with comprehensive JWT claims
- `SecurityConfig.java` - Added `/admin/**` authorization rules
- `AuthService.java` - Updated to assign VIEWER role by default
- `UserController.java` - Added user-role management endpoints
- `V2__insert_default_data.sql` - Updated with generic role names

## 🚀 Usage Examples

### Example 1: Create a Custom Role and Assign Permissions
```bash
# 1. Create a role
curl -X POST http://localhost:8080/api/v1/admin/roles \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "CONTENT_MODERATOR",
    "description": "Can moderate user content"
  }'

# 2. Create permissions
curl -X POST http://localhost:8080/api/v1/admin/permissions \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "content:moderate",
    "description": "Moderate content",
    "resource": "content",
    "action": "moderate"
  }'

# 3. Assign permission to role
curl -X POST http://localhost:8080/api/v1/admin/roles/{roleId}/permissions \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "permissionIds": ["{permissionId}"]
  }'
```

### Example 2: Create User with Specific Roles
```bash
curl -X POST http://localhost:8080/api/v1/admin/users \
  -H "Authorization: Bearer {admin_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "moderator1",
    "email": "moderator1@example.com",
    "password": "SecurePass123",
    "firstName": "John",
    "lastName": "Moderator",
    "roleIds": ["{contentModeratorRoleId}"],
    "enabled": true
  }'
```

### Example 3: Introspect a Token
```bash
curl -X POST "http://localhost:8080/api/v1/oauth/introspect?token={access_token}"
```

## 🔄 Migration Guide

### For Existing Users
After deployment, existing users with the old `USER` role will need to be migrated to `VIEWER` role. You can:

1. **Manual Update (Recommended for small datasets):**
   ```sql
   UPDATE roles SET name = 'VIEWER' WHERE name = 'USER';
   ```

2. **Or run a custom migration script** to update user-role associations.

### For Microservices
Microservices consuming JWT tokens should:

1. **Extract `roles` array** for role-based authorization
2. **Extract `permissions` array** for fine-grained permission checks
3. **Use `authorities` array** for Spring Security compatibility
4. **Optionally call `/oauth/introspect`** for token validation

## ✅ Benefits

✅ **Generic & Reusable** - Works with any microservice  
✅ **Flexible** - New roles/permissions without code changes  
✅ **Standard** - Follows OAuth2 and Spring Security conventions  
✅ **Scalable** - Supports multiple microservices with different permission models  
✅ **Backward Compatible** - Existing OAuth2 flows continue to work  

## 📚 API Documentation

Once the service is running, access the Swagger UI for complete API documentation:
```
http://localhost:8080/api/v1/swagger-ui.html
```

## 🧪 Testing

### Default Admin Credentials
```
Username: admin
Password: admin123
Email: admin@exhibitflow.com
Roles: ADMIN
```

### Test Workflow
1. Login as admin to get access token
2. Create custom roles and permissions
3. Assign permissions to roles
4. Create users with specific roles
5. Test JWT tokens contain expected claims
6. Test token introspection endpoints

## 🔐 Security Considerations

1. **Change default admin password** in production
2. **Use strong JWT secret** (at least 256 bits)
3. **Enable HTTPS** for all endpoints
4. **Rotate JWT secrets** periodically
5. **Monitor admin actions** for security auditing
6. **Implement rate limiting** on token endpoints

## 📊 Database Schema

The existing schema already supports all features through:
- `users` table
- `roles` table
- `permissions` table
- `user_roles` junction table
- `role_permissions` junction table

No schema changes were required! 🎉

## 🎓 Next Steps

Consider implementing:
1. **Audit logging** for RBAC changes
2. **Role hierarchies** (e.g., ADMIN inherits MANAGER)
3. **Permission groups** for easier management
4. **Soft delete** for roles/permissions
5. **Role/permission search and filtering**
6. **Bulk user role assignments**
7. **API rate limiting** per role
8. **Token revocation** blacklist

---

**Version:** 2.0.0  
**Last Updated:** November 23, 2025  
**Maintained By:** ExhibitFlow Team
