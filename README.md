# CardTransactionManagement
# CardTransactionManagement
# Card Transaction Management Application

A production-ready Spring Boot application for managing purchase transactions and converting them to different currencies using Treasury Reporting Rates of Exchange API.

## Features

### Requirement #1: Store Purchase Transactions
- Accept and store purchase transactions with description, date, and amount (USD)
- Assign unique identifiers to each transaction
- Validate transactions using Hibernate validators:
  - Description: max 50 characters
  - Transaction Date: valid date format (not in future)
  - Purchase Amount: positive value, rounded to nearest cent
  - Unique Identifier: auto-generated primary key

### Requirement #2: Retrieve Purchase Transactions with Currency Conversion
- Retrieve stored transactions converted to supported currencies
- Utilize Treasury Reporting Rates of Exchange API for real-time exchange rates
- Currency conversion rules:
  - Uses exchange rate equal to or before the purchase date
  - Searches within the last 6 months for available rates
  - Returns error if no rate available within 6 months
  - Converts amounts rounded to 2 decimal places

### Additional Features
- Basic HTTP authentication (username: admin, password: admin123; username: user, password: user123)
- Comprehensive error handling with detailed error responses
- RESTful API endpoints for full transaction lifecycle management
- Embedded H2 database for easy setup and testing
- Extensive unit and integration tests

## Project Structure

```
card-transaction-management/
├── src/main/java/com/cardtransaction/
│   ├── config/
│   │   └── SecurityConfig.java          # Security configuration with basic auth
│   ├── controller/
│   │   └── PurchaseTransactionController.java  # REST API endpoints
│   ├── dto/
│   │   ├── PurchaseTransactionRequest.java     # Request DTO with validation
│   │   ├── PurchaseTransactionResponse.java    # Response DTO
│   │   └── ConvertedPurchaseResponse.java      # Currency conversion response
│   ├── entity/
│   │   └── PurchaseTransaction.java    # JPA entity with validators
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java # Centralized exception handling
│   │   ├── ResourceNotFoundException.java
│   │   ├── CurrencyConversionException.java
│   │   └── InvalidTransactionException.java
│   ├── repository/
│   │   └── PurchaseTransactionRepository.java  # JPA repository
│   ├── service/
│   │   ├── PurchaseTransactionService.java    # Transaction business logic
│   │   └── CurrencyConversionService.java     # Currency conversion logic
│   └── CardTransactionManagementApplication.java  # Main Spring Boot app
├── src/main/resources/
│   └── application.yml                 # Application configuration
├── src/test/java/com/cardtransaction/
│   ├── controller/
│   │   ├── PurchaseTransactionControllerIT.java  # Integration tests for CRUD
│   │   └── CurrencyConversionControllerIT.java   # Integration tests for conversion
│   ├── service/
│   │   ├── PurchaseTransactionServiceTest.java   # Unit tests for transactions
│   │   └── CurrencyConversionServiceTest.java    # Unit tests for conversion
│   ├── entity/
│   │   └── PurchaseTransactionValidationTest.java # Validation tests
│   └── exception/
│       └── GlobalExceptionHandlerIT.java  # Exception handler tests
├── src/test/resources/
│   └── application.yml                 # Test configuration
├── pom.xml                             # Maven configuration
└── README.md                           # This file
```

## Technology Stack

- **Framework**: Spring Boot 3.1.5
- **Java Version**: 17+
- **Build Tool**: Maven
- **Database**: H2 (local/dev) / PostgreSQL (uat/prod)
- **Authentication**: Spring Security with Basic Auth (legacy) and optional HMAC token-based authentication
- **Testing**: JUnit 5, Mockito
- **Validation**: Hibernate Validator (Jakarta)
- **HTTP Client**: RestTemplate / WebFlux

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- Git

## Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd CardTransactionManagement
   ```

2. **Build the project**:
   ```bash
   mvn clean install
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

   The application will start on `http://localhost:8080/api`

4. **Run tests**:
   ```bash
   mvn test
   ```

## API Endpoints

### Authentication
The application supports two authentication methods:

1) Basic Authentication (legacy / default)
- All endpoints (except `/h2-console` and `/actuator`) continue to support Basic Authentication for administrative and test access.
- Username: `admin` or `user`
- Password: `admin123` or `user123`

2) HMAC token-based authentication (new)
- A token-issuing endpoint is available at `POST /api/auth/token`. Clients may obtain a short-lived HMAC token by authenticating with Basic Auth (username/password) or other configured credential methods and calling this endpoint.
- Example request (using Basic Auth to obtain a token):

```bash
curl -u user:user123 -X POST http://localhost:8080/api/auth/token
```

- Example token response (JSON):

```json
{
  "token": "b3d2f7...",
  "expiresAt": "2026-05-15T13:45:30Z",
  "ttlSeconds": 3600
}
```

- How to use the token: include it on subsequent API requests using the configured header and scheme. The default header is `Authorization` and default scheme is `HMAC`:

```bash
curl -H "Authorization: HMAC b3d2f7..." http://localhost:8080/api/v1/transactions
```

- Alternatively some deployments may accept `X-Auth-Token: <token>` depending on configuration.

- Token TTL and signing secret are configurable (see "Token configuration" below).

Notes on security and options:
- Replay protection: the token service can be configured to enable replay protection (nonces/timestamps) which requires a small nonce store (in-memory for single-node deployments or Redis/DB for clustered setups).
- JWT / asymmetric signing: if you prefer stateless tokens or want public-key verification, the project can be switched to issue JWTs signed with an asymmetric keypair instead of HMAC. See the configuration section for options.

### Transaction Management

#### Create a New Transaction
```bash
POST /api/v1/transactions
Content-Type: application/json

{
  "description": "Laptop Purchase",
  "transactionDate": "2024-05-10",
  "purchaseAmount": 1299.99
}
```

**Response (201 Created)**:
```json
{
  "id": 1,
  "description": "Laptop Purchase",
  "transactionDate": "2024-05-10",
  "purchaseAmount": 1299.99,
  "createdAt": "2024-05-13"
}
```

#### Retrieve All Transactions
```bash
GET /api/v1/transactions
```

**Response (200 OK)**:
```json
[
  {
    "id": 1,
    "description": "Laptop Purchase",
    "transactionDate": "2024-05-10",
    "purchaseAmount": 1299.99,
    "createdAt": "2024-05-13"
  }
]
```

#### Retrieve a Specific Transaction
```bash
GET /api/v1/transactions/{id}
```

**Response (200 OK)**:
```json
{
  "id": 1,
  "description": "Laptop Purchase",
  "transactionDate": "2024-05-10",
  "purchaseAmount": 1299.99,
  "createdAt": "2024-05-13"
}
```

#### Update a Transaction
```bash
PUT /api/v1/transactions/{id}
Content-Type: application/json

{
  "description": "Updated Purchase",
  "transactionDate": "2024-05-15",
  "purchaseAmount": 1399.99
}
```

**Response (200 OK)**:
```json
{
  "id": 1,
  "description": "Updated Purchase",
  "transactionDate": "2024-05-15",
  "purchaseAmount": 1399.99,
  "createdAt": "2024-05-13"
}
```

#### Delete a Transaction
```bash
DELETE /api/v1/transactions/{id}
```

**Response (204 No Content)**

### Currency Conversion

#### Convert Transaction to Target Currency
```bash
GET /api/v1/transactions/{id}/convert?currency=EUR
```

**Response (200 OK)**:
```json
{
  "id": 1,
  "description": "Laptop Purchase",
  "transactionDate": "2024-05-10",
  "originalPurchaseAmount": 1299.99,
  "originalCurrency": "USD",
  "targetCurrency": "EUR",
  "exchangeRate": 0.92,
  "convertedAmount": 1195.99,
  "exchangeRateDate": "2024-05-10"
}
```

**Error Response (422 Unprocessable Entity)**:
```json
{
  "status": 422,
  "message": "No currency conversion rate available for XYZ on or before 2024-05-10 within the last 6 months",
  "timestamp": "2024-05-13T15:30:00",
  "path": "/api/v1/transactions/1/convert"
}
```

## Error Handling

The application implements comprehensive error handling with appropriate HTTP status codes:

- **400 Bad Request**: Validation errors, invalid input
- **401 Unauthorized**: Missing or invalid authentication
- **404 Not Found**: Resource not found
- **422 Unprocessable Entity**: Currency conversion unavailable
- **500 Internal Server Error**: Unexpected server errors

**Error Response Format**:
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2024-05-13T15:30:00",
  "path": "/api/v1/transactions",
  "errors": [
    "description: must not exceed 50 characters"
  ]
}
```

## Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=PurchaseTransactionServiceTest
```

### Run with Coverage
```bash
mvn test jacoco:report
```

### Test Categories

1. **Unit Tests**:
   - `PurchaseTransactionServiceTest`: Business logic for transaction management
   - `CurrencyConversionServiceTest`: Currency conversion logic and edge cases

2. **Integration Tests**:
   - `PurchaseTransactionControllerIT`: REST endpoints with authentication
   - `CurrencyConversionControllerIT`: Currency conversion endpoints
   - `GlobalExceptionHandlerIT`: Error handling and exception mapping

3. **Validation Tests**:
   - `PurchaseTransactionValidationTest`: Hibernate validator constraints

## Database Access

### H2 Console
- URL: `http://localhost:8080/api/h2-console`
- JDBC URL: `jdbc:h2:mem:carddb`
- Username: `sa`
- Password: (leave empty)

## Configuration

### Environment Profiles

The application supports multiple environment profiles for different deployment scenarios:

- **local**: Local development with H2 in-memory database and detailed logging
- **dev**: Development environment with file-based H2 database
- **uat**: User Acceptance Testing with PostgreSQL and moderate logging
- **prod**: Production environment with PostgreSQL, optimized settings, and minimal logging

### Running with Different Profiles

```bash
# Local development (default)
mvn spring-boot:run

# Development environment
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# UAT environment
mvn spring-boot:run -Dspring-boot.run.profiles=uat

# Production environment
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Environment Variables

For production and UAT environments, configure the following environment variables:

```bash
# Database Configuration
export DATABASE_URL=jdbc:postgresql://localhost:5432/carddb
export DB_USERNAME=carduser
export DB_PASSWORD=cardpass

# Security Configuration
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=securepassword

# Server Configuration
export SERVER_PORT=8080

# External API Configuration
export TREASURY_API_URL=https://api.fiscaldata.treasury.gov/

# Token / Authentication Configuration (optional)
# HMAC shared secret (use a strong random value; keep out of source control)
export TOKEN_SECRET=change-me
# Token lifetime (seconds)
export TOKEN_TTL_SECONDS=3600
# Whether to enable replay-protection (true/false)
export TOKEN_REPLAY_PROTECTION=false
# Token header and scheme (optional)
export TOKEN_HEADER=Authorization
export TOKEN_SCHEME=HMAC
# Use JWT instead of HMAC: set TOKEN_TYPE=jwt and configure key files below
export TOKEN_TYPE=hmac
# JWT key files (when using JWT)
export JWT_PRIVATE_KEY_FILE=/path/to/private-key.pem
export JWT_PUBLIC_KEY_FILE=/path/to/public-key.pem
```

### Application Properties (application.yml)

The main `application.yml` contains common configuration:

```yaml
spring:
  application:
    name: card-transaction-management

# External API Configuration
external:
  api:
    treasury:
      base-url: https://fiscal.treasury.gov/
```

### Token configuration

The HMAC token service is configurable via `application.yml` or environment variables. Example properties (default names used by the application):

```yaml
security:
  token:
    # HMAC shared secret (can be set via environment variable TOKEN_SECRET)
    secret: ${TOKEN_SECRET:change-me}

    # Token TTL in seconds (default 3600)
    ttl-seconds: ${TOKEN_TTL_SECONDS:3600}

    # Header and scheme used to present the token
    header: ${TOKEN_HEADER:Authorization}
    scheme: ${TOKEN_SCHEME:HMAC}

    # Replay protection: when true, the service will track nonces/timestamps to prevent replay attacks
    replay-protection-enabled: ${TOKEN_REPLAY_PROTECTION:false}

    # Optional: token type switch (hmac or jwt). If set to jwt, configure jwt keys below.
    type: ${TOKEN_TYPE:hmac}

  # JWT keys (used only when type=jwt)
  jwt:
    private-key-file: ${JWT_PRIVATE_KEY_FILE:}
    public-key-file: ${JWT_PUBLIC_KEY_FILE:}
```

Environment variables supported:

```bash
TOKEN_SECRET           # HMAC shared secret
TOKEN_TTL_SECONDS      # Token lifetime in seconds
TOKEN_HEADER           # Header name to use (default Authorization)
TOKEN_SCHEME           # Scheme/prefix to use in Authorization header (default HMAC)
TOKEN_REPLAY_PROTECTION # true/false to enable replay protection storage
TOKEN_TYPE             # hmac | jwt
JWT_PRIVATE_KEY_FILE   # path to private key when using JWT
JWT_PUBLIC_KEY_FILE    # path to public key when using JWT
```

Recommendations:
- Use a strong random secret for `TOKEN_SECRET` and keep it out of source control (use environment variables or a secret manager).
- For scaled/clustered deployments enable `replay-protection-enabled` and back it with Redis or a shared DB.
- If you need stateless verification by third parties, consider switching to `type: jwt` with an asymmetric keypair.


### Operational notes

- Enabling replay protection

  If you set `security.token.replay-protection-enabled: true` (or `TOKEN_REPLAY_PROTECTION=true`) the application will track used nonces/timestamps to mitigate replay attacks. For single-node deployments an in-memory store is sufficient; for clustered environments configure a shared store such as Redis and set the connection accordingly in your environment or profile.

  Example (enable replay protection and point to Redis):

  ```bash
  export TOKEN_REPLAY_PROTECTION=true
  export REDIS_URL=redis://localhost:6379/0
  ```

  Note: the application must be configured to read `REDIS_URL` (see `application-*.yml`) if you want a shared nonce store. Using Redis is recommended for multi-node deployments.

- Switching to JWT (asymmetric signing)

  If you prefer stateless tokens or want third-party verification without sharing secrets, set `security.token.type=jwt` (or `TOKEN_TYPE=jwt`) and provide an RSA keypair. Example using OpenSSL to generate a 2048-bit RSA keypair:

  ```bash
  # Generate private key
  openssl genpkey -algorithm RSA -out jwt-private.pem -pkeyopt rsa_keygen_bits:2048

  # Extract public key
  openssl rsa -pubout -in jwt-private.pem -out jwt-public.pem
  ```

  Then set environment variables to point to the key files:

  ```bash
  export TOKEN_TYPE=jwt
  export JWT_PRIVATE_KEY_FILE=/full/path/to/jwt-private.pem
  export JWT_PUBLIC_KEY_FILE=/full/path/to/jwt-public.pem
  ```

  The private key stays on the issuer; the public key can be distributed to trusted verifiers.

  Important: when switching token types, make sure your clients and any reverse proxies expect the new token format and validation rules.


### Profile-Specific Configurations

#### Local Profile (application-local.yml)
- H2 in-memory database
- Detailed debug logging
- H2 console enabled
- All SQL statements logged

#### Dev Profile (application-dev.yml)
- H2 file-based database
- Info-level logging
- File logging enabled
- Environment variable support for passwords

#### UAT Profile (application-uat.yml)
- PostgreSQL database
- Warn-level logging
- Connection pooling optimized
- JMX metrics enabled

#### Prod Profile (application-prod.yml)
- PostgreSQL database
- Error-level logging
- Production optimizations
- Security hardening
- Log rotation and size limits
