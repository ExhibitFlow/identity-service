# Identity Service

A secure microservice for authentication and authorization using JWT and OAuth2, built with Spring Boot 3.5.7 and Java 17.

## Overview

This service manages user identities, tokens, and permissions with Spring Security, JPA, and a dedicated PostgreSQL database (identitydb). It provides comprehensive authentication and authorization capabilities for microservices architectures.

## Features

- **JWT Authentication**: Secure token-based authentication with access and refresh tokens
- **OAuth2 Authorization Server**: Full OAuth2 implementation with authorization code flow
- **User Management**: Complete user registration, login, and profile management
- **Role-Based Access Control (RBAC)**: Fine-grained permissions system with roles and permissions
- **PostgreSQL Database**: Dedicated identitydb with versioned migrations
- **Flyway Migrations**: Automatic database schema versioning and management
- **Spring Kafka Integration**: Asynchronous event publishing for user and auth events
- **SpringDoc OpenAPI**: Interactive API documentation with Swagger UI
- **Spring Security**: Enterprise-grade security configuration
- **Docker Support**: Containerized deployment with Docker Compose
- **Health Checks**: Spring Actuator endpoints for monitoring

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.5.7
- **Spring Security**: OAuth2 Authorization Server & Resource Server
- **Spring Data JPA**: Database access layer
- **PostgreSQL**: 16+ (identitydb)
- **Flyway**: Database migration tool
- **Spring Kafka**: Async messaging
- **JJWT**: JWT token generation and validation
- **SpringDoc OpenAPI**: API documentation
- **Lombok**: Boilerplate code reduction
- **Maven**: Build and dependency management
- **Docker**: Containerization

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose (for containerized deployment)
- PostgreSQL 16+ (if running locally without Docker)
- Apache Kafka (if running locally without Docker)

## Getting Started

### Running with Docker Compose (Recommended)

1. Clone the repository:
   ```bash
   git clone https://github.com/ExhibitFlow/identity-service.git
   cd identity-service
   ```

2. Start all services:
   ```bash
   docker-compose up -d
   ```

3. The service will be available at:
   - API: http://localhost:8080/api/v1
   - Swagger UI: http://localhost:8080/api/v1/swagger-ui.html
   - Actuator: http://localhost:8080/api/v1/actuator

### Running Locally

1. Start PostgreSQL and create the database:
   ```sql
   CREATE DATABASE identitydb;
   CREATE USER identity_user WITH PASSWORD 'identity_pass';
   GRANT ALL PRIVILEGES ON DATABASE identitydb TO identity_user;
   ```

2. Start Kafka (or use Docker):
   ```bash
   docker run -d --name kafka -p 9092:9092 \
     -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
     confluentinc/cp-kafka:7.5.0
   ```

3. Build and run the application:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## Configuration

Key configuration properties in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/identitydb
    username: identity_user
    password: identity_pass
  
  kafka:
    bootstrap-servers: localhost:9092
    topics:
      user-events: user-events
      auth-events: auth-events

jwt:
  secret: your-256-bit-secret-key
  expiration: 86400000  # 24 hours
  refresh-expiration: 604800000  # 7 days

oauth2:
  issuer-uri: http://localhost:8080/api/v1
  client:
    client-id: identity-service-client
    client-secret: secret
```

## API Endpoints

### Authentication Endpoints

#### Register User
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "john.doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "john.doe",
  "password": "securePassword123"
}
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

#### Refresh Token
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Logout
```http
POST /api/v1/auth/logout
Authorization: Bearer {accessToken}
```

### User Management Endpoints

#### Get Current User
```http
GET /api/v1/users/me
Authorization: Bearer {accessToken}
```

#### Get User by ID (Admin only)
```http
GET /api/v1/users/{id}
Authorization: Bearer {accessToken}
```

#### Get All Users (Admin only)
```http
GET /api/v1/users?page=0&size=20
Authorization: Bearer {accessToken}
```

#### Update User Status (Admin only)
```http
PATCH /api/v1/users/{id}/status?enabled=true
Authorization: Bearer {accessToken}
```

#### Delete User (Admin only)
```http
DELETE /api/v1/users/{id}
Authorization: Bearer {accessToken}
```

## OAuth2 Endpoints

- Authorization Endpoint: `/oauth2/authorize`
- Token Endpoint: `/oauth2/token`
- JWK Set Endpoint: `/.well-known/jwks.json`
- OpenID Configuration: `/.well-known/openid-configuration`

## Database Schema

### Tables

- **users**: User accounts with credentials and profile information
- **roles**: Role definitions (USER, ADMIN, MODERATOR)
- **permissions**: Granular permissions (user:read, user:write, etc.)
- **user_roles**: Many-to-many relationship between users and roles
- **role_permissions**: Many-to-many relationship between roles and permissions
- **refresh_tokens**: Refresh token storage and management

### Default Data

- **Admin User**:
  - Username: `admin`
  - Password: `admin123`
  - Email: `admin@exhibitflow.com`
  
- **Roles**: USER, ADMIN, MODERATOR
- **Permissions**: Various permissions for user, role, and permission management

## Kafka Events

The service publishes events to Kafka topics:

### User Events Topic
- `USER_REGISTERED`: Published when a new user registers

### Auth Events Topic
- `USER_LOGIN`: Published on successful login
- `USER_LOGOUT`: Published on logout

## Security

- **Password Encryption**: BCrypt with strength 10
- **JWT Tokens**: HMAC-SHA256 signed tokens
- **OAuth2**: RS256 signed tokens for OAuth2 flows
- **HTTPS**: Recommended for production deployment
- **CORS**: Configure allowed origins in production

## Monitoring

Access health and metrics endpoints:

- Health: http://localhost:8080/api/v1/actuator/health
- Info: http://localhost:8080/api/v1/actuator/info
- Metrics: http://localhost:8080/api/v1/actuator/metrics

## API Documentation

Interactive API documentation is available at:
- Swagger UI: http://localhost:8080/api/v1/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/v1/api-docs

## Testing

Run tests with Maven:
```bash
mvn test
```

## Building for Production

Build the application:
```bash
mvn clean package -DskipTests
```

Build Docker image:
```bash
docker build -t identity-service:latest .
```

## Microservices Integration

This service is designed to work behind an API Gateway in a microservices architecture:

1. Deploy the service in your microservices environment
2. Configure the API Gateway to route authentication requests
3. Use JWT tokens for inter-service communication
4. Subscribe to Kafka events in other microservices

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_USERNAME` | PostgreSQL username | identity_user |
| `DB_PASSWORD` | PostgreSQL password | identity_pass |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses | localhost:9092 |
| `JWT_SECRET` | Secret key for JWT signing | (change in production) |
| `JWT_EXPIRATION` | Access token expiration (ms) | 86400000 |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiration (ms) | 604800000 |
| `SERVER_PORT` | Application port | 8080 |
| `OAUTH2_ISSUER_URI` | OAuth2 issuer URI | http://localhost:8080/api/v1 |

## Best Practices

1. **Change Default Credentials**: Update admin password and JWT secret in production
2. **Use HTTPS**: Enable SSL/TLS for production deployments
3. **Configure CORS**: Restrict allowed origins
4. **Monitor Logs**: Set up centralized logging
5. **Database Backups**: Regular backups of identitydb
6. **Token Rotation**: Implement token rotation policies
7. **Rate Limiting**: Add rate limiting at API Gateway level
