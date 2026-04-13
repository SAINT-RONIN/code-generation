# Banking API - Backend

Spring Boot REST API for a banking application with JWT authentication, role-based access control, and H2 in-memory database.

## Tech Stack

- **Java 21** + **Spring Boot 3.4.5**
- **Spring Data JPA** with Hibernate
- **Spring Security** + JWT (JJWT 0.12.6)
- **H2 In-Memory Database**
- **SpringDoc OpenAPI** (Swagger UI)
- **Maven** for build management
- **JUnit 5** for testing

## Project Structure

```
backend/
├── pom.xml
└── src/
    ├── main/java/com/banking/
    │   ├── BankingApplication.java          # Entry point
    │   ├── config/
    │   │   ├── DataInitializer.java         # Seeds default users & accounts
    │   │   └── OpenApiConfig.java           # Swagger UI configuration
    │   ├── controller/
    │   │   ├── AuthController.java          # Login & registration
    │   │   ├── AccountController.java       # Account queries
    │   │   ├── TransactionController.java   # Transfers & transaction history
    │   │   ├── AtmController.java           # ATM deposit & withdrawal
    │   │   └── EmployeeController.java      # Employee operations
    │   ├── dto/
    │   │   ├── AuthRequest.java
    │   │   ├── AuthResponse.java
    │   │   ├── RegisterRequest.java
    │   │   ├── TransferRequest.java
    │   │   └── TransactionDto.java
    │   ├── exception/
    │   │   └── GlobalExceptionHandler.java  # Centralized error handling
    │   ├── model/
    │   │   ├── User.java                    # CUSTOMER / EMPLOYEE roles
    │   │   ├── Account.java                 # CHECKING / SAVINGS with limits
    │   │   └── Transaction.java             # TRANSFER / DEPOSIT / WITHDRAWAL
    │   ├── repository/
    │   │   ├── UserRepository.java
    │   │   ├── AccountRepository.java
    │   │   └── TransactionRepository.java
    │   ├── security/
    │   │   ├── SecurityConfig.java          # Role-based endpoint security
    │   │   ├── JwtUtil.java                 # JWT token generation & validation
    │   │   └── JwtAuthenticationFilter.java # Request filter for JWT
    │   └── service/
    │       ├── AuthService.java
    │       └── TransactionService.java
    └── main/resources/
        └── application.properties           # H2, JWT, server config
```

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+

### Run

```bash
cd backend
mvn spring-boot:run
```

The API starts at `http://localhost:8080`.

### Build

```bash
mvn clean package
```

The JAR is generated at `target/banking-api-0.0.1-SNAPSHOT.jar`.

### Run Tests

```bash
mvn test
```

## API Documentation

Swagger UI is available at: **http://localhost:8080/swagger-ui.html**

H2 Database Console: **http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:mem:bankingdb`)

## API Endpoints

### Authentication (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new customer |
| POST | `/api/auth/login` | Login and receive JWT |

### Accounts (Authenticated)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/accounts/my?email=` | CUSTOMER | View own accounts |
| GET | `/api/accounts/all` | EMPLOYEE | View all accounts (paginated) |
| GET | `/api/accounts/{iban}` | Any | Get account by IBAN |

### Transactions (Authenticated)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/transactions` | EMPLOYEE | View all transactions (paginated) |
| GET | `/api/transactions/account/{iban}` | Any | View account transactions with filters |
| POST | `/api/transactions/transfer` | Any | Transfer between accounts |

### ATM (Customer)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/atm/deposit` | CUSTOMER | Deposit money |
| POST | `/api/atm/withdraw` | CUSTOMER | Withdraw money |
| GET | `/api/atm/accounts/{email}` | CUSTOMER | Get checking accounts |

### Employee (Employee)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/employees/customers/pending` | EMPLOYEE | Customers without accounts |
| POST | `/api/employees/customers/{id}/approve` | EMPLOYEE | Approve & create accounts |
| PUT | `/api/employees/accounts/{id}/limits` | EMPLOYEE | Update transfer limits |
| DELETE | `/api/employees/accounts/{id}` | EMPLOYEE | Close an account |
| GET | `/api/employees/customers` | EMPLOYEE | All customers (paginated) |
| GET | `/api/employees/customers/{id}/transactions` | EMPLOYEE | Customer transactions |
| POST | `/api/employees/transfer` | EMPLOYEE | Transfer between customers |
| GET | `/api/employees/customers/search` | Any | Search customers by name |

## Authentication

All protected endpoints require a JWT token in the `Authorization` header:

```
Authorization: Bearer <token>
```

Tokens are obtained via `/api/auth/login` and are valid for 24 hours.

## Test Credentials

| Role | Email | Password |
|------|-------|----------|
| Employee | employee@bank.com | password |
| Customer | customer@test.com | password |

## Configuration

Key settings in `application.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8080 | API server port |
| `jwt.secret` | (set) | JWT signing key |
| `jwt.expiration` | 86400000 | Token TTL in ms (24h) |
| `spring.jpa.hibernate.ddl-auto` | update | Auto-create tables |
