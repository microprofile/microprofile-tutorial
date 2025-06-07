# Order Service - MicroProfile E-Commerce Store

## Overview

The Order Service is a microservice in the MicroProfile E-Commerce Store application. It provides order management functionality with secure JWT-based authentication and role-based authorization.

## Features

* 🔐 **JWT Authentication & Authorization**: Role-based security using MicroProfile JWT
* 📋 **Order Management**: Create, read, update, and delete operations for orders
* 📖 **OpenAPI Documentation**: Comprehensive API documentation with Swagger UI
* 🏗️ **MicroProfile Compliance**: Built with Jakarta EE and MicroProfile standards

## Technology Stack

* **Runtime**: Open Liberty
* **Framework**: MicroProfile, Jakarta EE
* **Security**: MicroProfile JWT
* **API**: Jakarta RESTful Web Services, MicroProfile OpenAPI
* **Build**: Maven

## API Endpoints

### Role-Based Secured Endpoints

#### GET /api/orders/{id}
Returns order information for the specified ID.

**Security**: Requires valid JWT Bearer token with `user` role

**Response Example**:
```
Order for user: user1@example.com, ID: 12345
```

**HTTP Status Codes**:
* `200` - Order information returned successfully
* `401` - Unauthorized - JWT token is missing or invalid
* `403` - Forbidden - User lacks required permissions

#### DELETE /api/orders/{id}
Deletes an order with the specified ID.

**Security**: Requires valid JWT Bearer token with `admin` role

**Response Example**:
```
Order deleted by admin: admin@example.com, ID: 12345
```

**HTTP Status Codes**:
* `200` - Order deleted successfully
* `401` - Unauthorized - JWT token is missing or invalid
* `403` - Forbidden - User lacks required permissions
* `404` - Not Found - Order does not exist

## Authentication & Authorization

### JWT Token Requirements

The service expects JWT tokens with the following claims:

| Claim      | Required | Description                                              |
|------------|----------|----------------------------------------------------------|
| `iss`      | Yes      | Issuer - must match `mp.jwt.verify.issuer` configuration |
| `sub`      | Yes      | Subject - unique user identifier                         |
| `groups`   | Yes      | Array containing user roles ("user" or "admin")          |
| `upn`      | Yes      | User Principal Name - used as username                   |
| `exp`      | Yes      | Expiration timestamp                                     |
| `iat`      | Yes      | Issued at timestamp                                      |

### Example JWT Payload

```json
{
  "iss": "mp-ecomm-store",
  "jti": "42",
  "sub": "user1",
  "upn": "user1@example.com",
  "groups": ["user"],
  "exp": 1748951611,
  "iat": 1748950611
}
```

For admin access:
```json
{
  "iss": "mp-ecomm-store",
  "jti": "43",
  "sub": "admin1",
  "upn": "admin@example.com",
  "groups": ["admin", "user"],
  "exp": 1748951611,
  "iat": 1748950611
}
```

## Configuration

### MicroProfile Configuration

The service uses the following MicroProfile configuration properties:

```properties
# Enable OpenAPI scanning
mp.openapi.scan=true

# JWT verification settings
mp.jwt.verify.publickey.location=/META-INF/publicKey.pem
mp.jwt.verify.issuer=mp-ecomm-store

# OpenAPI UI configuration
mp.openapi.ui.enable=true
```

### Security Configuration

The service requires:

1. **Public Key**: RSA public key in PEM format located at `/META-INF/publicKey.pem`
2. **Issuer Validation**: JWT tokens must have matching `iss` claim
3. **Role-Based Access**: Endpoints require specific roles in JWT `groups` claim:
   - `/api/orders/{id}` (GET) - requires "user" role
   - `/api/orders/{id}` (DELETE) - requires "admin" role

## Development Setup

### Prerequisites

* Java 17 or higher
* Maven 3.6+
* Open Liberty runtime
* Docker (optional, for containerized deployment)

### Building the Service

```bash
# Build the project
mvn clean package

# Run with Liberty dev mode
mvn liberty:dev
```

### Running with Docker

The Order Service can be run in a Docker container:

```bash
# Make the script executable (if needed)
chmod +x run-docker.sh

# Build and run using Docker
./run-docker.sh
```

This script will:
1. Build the application with Maven
2. Create a Docker image with Open Liberty
3. Run the container with ports mapped to host

Or manually with Docker commands:

```bash
# Build the application
mvn clean package

# Build the Docker image
docker build -t mp-ecomm-store/order:latest .

# Run the container
docker run -d --name order-service -p 8050:8050 -p 8051:8051 mp-ecomm-store/order:latest
```

### Testing with JWT Tokens

The Order Service uses JWT-based authentication with role-based authorization. To test the endpoints, you'll need valid JWT tokens with the appropriate roles.

#### Generate JWT Tokens with jwtenizr

The project includes a `jwtenizr` tool in the `/tools` directory:

```bash
# Navigate to tools directory
cd tools/

# Generate token for user role (default)
java -jar jwtenizr.jar

# Generate token and test endpoint
java -Dverbose -jar jwtenizr.jar http://localhost:8050/order/api/orders/12345
```

#### Testing Different Roles

For testing admin-only endpoints, you'll need to modify the JWT token payload to include the "admin" role:

1. Edit the `jwt-token.json` file in the tools directory
2. Add "admin" to the groups array: `"groups": ["admin", "user"]`
3. Generate a new token: `java -jar jwtenizr.jar`
4. Test the admin endpoint:
   ```bash
   curl -X DELETE -H "Authorization: Bearer $(cat token.jwt)" \
        http://localhost:8050/order/api/orders/12345
   ```

## API Documentation

The OpenAPI documentation is available at:

* **OpenAPI Spec**: `http://localhost:8050/order/openapi`
* **Swagger UI**: `http://localhost:8050/order/openapi/ui`

## Troubleshooting

### Common JWT Issues

* **403 Forbidden**: Verify the JWT token contains the required role in the `groups` claim
* **401 Unauthorized**: Check that the token is valid and hasn't expired
* **Token Validation Errors**: Ensure the issuer (`iss`) matches the configuration

### Testing with curl

```bash
# Test user role endpoint
curl -H "Authorization: Bearer $(cat tools/token.jwt)" \
     http://localhost:8050/order/api/orders/12345

# Test admin role endpoint
curl -X DELETE -H "Authorization: Bearer $(cat tools/token.jwt)" \
     http://localhost:8050/order/api/orders/12345
```
