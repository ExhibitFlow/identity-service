# Identity Service - API Quick Reference

## Authentication Endpoints (Public)

### Register New User
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe"
}
```
*Note: Automatically assigns VIEWER role*

### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "SecurePass123"
}
```

### Refresh Token
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "your-refresh-token"
}
```

### Logout
```http
POST /api/v1/auth/logout
Authorization: Bearer {token}
```

---

## 👥 User Management Endpoints

### Get Current User
```http
GET /api/v1/users/me
Authorization: Bearer {token}
```

### Get User by ID (Admin)
```http
GET /api/v1/users/{userId}
Authorization: Bearer {admin_token}
```

### Get All Users (Admin, Paginated)
```http
GET /api/v1/users?page=0&size=20
Authorization: Bearer {admin_token}
```

### Delete User (Admin)
```http
DELETE /api/v1/users/{userId}
Authorization: Bearer {admin_token}
```

### Update User Status (Admin)
```http
PATCH /api/v1/users/{userId}/status?enabled=true
Authorization: Bearer {admin_token}
```

### Get User's Roles (Admin)
```http
GET /api/v1/users/{userId}/roles
Authorization: Bearer {admin_token}
```

### Assign Roles to User (Admin)
```http
POST /api/v1/users/{userId}/roles
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "roleIds": ["uuid1", "uuid2"]
}
```

### Remove Role from User (Admin)
```http
DELETE /api/v1/users/{userId}/roles/{roleId}
Authorization: Bearer {admin_token}
```

---

## Role Management Endpoints (Admin Only)

### Create Role
```http
POST /api/v1/admin/roles
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "name": "CONTENT_MODERATOR",
  "description": "Can moderate user content"
}
```

### Get All Roles (Paginated)
```http
GET /api/v1/admin/roles?page=0&size=20&sort=name,asc
Authorization: Bearer {admin_token}
```

### Get Role by ID
```http
GET /api/v1/admin/roles/{roleId}
Authorization: Bearer {admin_token}
```

### Get Role by Name
```http
GET /api/v1/admin/roles/name/{roleName}
Authorization: Bearer {admin_token}
```

### Update Role
```http
PUT /api/v1/admin/roles/{roleId}
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "description": "Updated description"
}
```

### Delete Role
```http
DELETE /api/v1/admin/roles/{roleId}
Authorization: Bearer {admin_token}
```

### Get Role's Permissions
```http
GET /api/v1/admin/roles/{roleId}/permissions
Authorization: Bearer {admin_token}
```

### Assign Permissions to Role
```http
POST /api/v1/admin/roles/{roleId}/permissions
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "permissionIds": ["uuid1", "uuid2", "uuid3"]
}
```

### Remove Permission from Role
```http
DELETE /api/v1/admin/roles/{roleId}/permissions/{permissionId}
Authorization: Bearer {admin_token}
```

---

## Permission Management Endpoints (Admin Only)

### Create Permission
```http
POST /api/v1/admin/permissions
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "name": "content:moderate",
  "description": "Moderate user-generated content",
  "resource": "content",
  "action": "moderate"
}
```

### Get All Permissions (Paginated)
```http
GET /api/v1/admin/permissions?page=0&size=20&sort=name,asc
Authorization: Bearer {admin_token}
```

### Get Permission by ID
```http
GET /api/v1/admin/permissions/{permissionId}
Authorization: Bearer {admin_token}
```

### Get Permission by Name
```http
GET /api/v1/admin/permissions/name/{permissionName}
Authorization: Bearer {admin_token}
```

### Update Permission
```http
PUT /api/v1/admin/permissions/{permissionId}
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "description": "Updated description"
}
```

### Delete Permission
```http
DELETE /api/v1/admin/permissions/{permissionId}
Authorization: Bearer {admin_token}
```

---

## Admin User Creation Endpoint

### Create User with Specific Roles (Admin)
```http
POST /api/v1/admin/users
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe",
  "roleIds": ["uuid1", "uuid2"],
  "enabled": true
}
```

---

## 🔍 OAuth Token Endpoints (Public)

### Token Introspection (RFC 7662)
```http
POST /api/v1/oauth/introspect?token={access_token}
```

**Response:**
```json
{
  "active": true,
  "username": "johndoe",
  "sub": "johndoe",
  "clientId": "identity-service",
  "exp": 1732442400,
  "iat": 1732356000,
  "roles": ["MANAGER", "VIEWER"],
  "permissions": ["content:read", "content:write"]
}
```

### Simple Token Validation
```http
POST /api/v1/oauth/validate?token={access_token}
```

**Response:**
```json
{
  "valid": true
}
```

---

## Default Roles & Permissions

### Roles

| Role | Description | Auto-Assigned |
|------|-------------|---------------|
| VIEWER | Read-only access | ✅ On registration |
| MANAGER | Can manage resources | ❌ Admin assigns |
| ADMIN | Full administrative access | ❌ Seeded by default |

### Permission Naming Convention
```
resource:action
```

Examples:
- `user:read` - Read user information
- `user:write` - Create/update users
- `user:delete` - Delete users
- `content:read` - Read content
- `content:write` - Create/update content
- `content:moderate` - Moderate content

---

## Authorization Requirements

| Endpoint Pattern | Required Authority |
|-----------------|-------------------|
| `/api/v1/auth/**` | Public (no auth) |
| `/api/v1/oauth/**` | Public (no auth) |
| `/api/v1/users/me` | Authenticated user |
| `/api/v1/users/{id}` | `ROLE_ADMIN` |
| `/api/v1/admin/**` | `ROLE_ADMIN` |

---

## JWT Token Claims

After login, your JWT token will include:

```json
{
  "sub": "johndoe",
  "userId": "770e8400-e29b-41d4-a716-446655440003",
  "username": "johndoe",
  "email": "john@example.com",
  "roles": ["MANAGER", "VIEWER"],
  "authorities": ["ROLE_MANAGER", "ROLE_VIEWER", "content:read", "content:write"],
  "permissions": ["content:read", "content:write", "user:read"],
  "iss": "http://localhost:8080/api/v1",
  "iat": 1732356000,
  "exp": 1732442400
}
```

---

## Testing with cURL

### 1. Login as Admin
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

Save the `accessToken` from the response.

### 2. Create a Custom Role
```bash
curl -X POST http://localhost:8080/api/v1/admin/roles \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "CONTENT_MODERATOR",
    "description": "Can moderate user content"
  }'
```

### 3. Create a Permission
```bash
curl -X POST http://localhost:8080/api/v1/admin/permissions \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "content:moderate",
    "description": "Moderate user-generated content",
    "resource": "content",
    "action": "moderate"
  }'
```

### 4. Assign Permission to Role
```bash
curl -X POST http://localhost:8080/api/v1/admin/roles/ROLE_ID/permissions \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "permissionIds": ["PERMISSION_ID"]
  }'
```

### 5. Create User with Role
```bash
curl -X POST http://localhost:8080/api/v1/admin/users \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "moderator1",
    "email": "moderator1@example.com",
    "password": "SecurePass123",
    "firstName": "John",
    "lastName": "Moderator",
    "roleIds": ["ROLE_ID"],
    "enabled": true
  }'
```

---

## Response Formats

### Success Response (200 OK)
```json
{
  "id": "uuid",
  "name": "CONTENT_MODERATOR",
  "description": "Can moderate user content",
  ...
}
```

### Created Response (201 Created)
```json
{
  "id": "uuid",
  "username": "newuser",
  "email": "newuser@example.com",
  ...
}
```

### No Content (204 No Content)
*Empty response body - operation successful*

### Error Response (4xx, 5xx)
```json
{
  "timestamp": "2025-11-23T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Role name must contain only uppercase letters and underscores",
  "path": "/api/v1/admin/roles"
}
```

---

## Swagger UI

Interactive API documentation available at:
```
http://localhost:8080/api/v1/swagger-ui.html
```

---

**Version:** 2.0.0  
**Port:** 8080  
**Context Path:** /api/v1
