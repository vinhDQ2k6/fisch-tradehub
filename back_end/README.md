# Fisch TradeHub Backend

A Spring Boot-based REST API for a fish shop trading system with authentication, cart management, and order processing.

## Overview

Fisch TradeHub is a learning project demonstrating professional backend architecture using:
- **Java 17** with Spring Boot 3.5.7
- **Spring Security** for authentication and authorization
- **Spring Data JPA** with MySQL database
- **Flyway** for database migrations
- **Lombok** for reducing boilerplate code

## Architecture

The backend follows a clean **layered architecture**:

```
├── entity/          # JPA entities (domain models)
├── repository/      # Data access layer (Spring Data JPA)
├── service/         # Business logic layer
├── web/
│   ├── api/        # REST controllers
│   ├── dto/        # Data Transfer Objects
│   └── exception/  # Exception handlers
├── security/        # Security configuration
├── common/          # Shared constants
└── exception/       # Custom exception types
```

### Design Principles

1. **Separation of Concerns**: Clear boundaries between layers
2. **Single Responsibility**: Each class has one focused purpose
3. **Dependency Injection**: Managed by Spring IoC container
4. **Exception Handling**: Centralized via `@RestControllerAdvice`
5. **Transaction Management**: Declarative with `@Transactional`

## Domain Model

The system manages:
- **Users** with roles (USER, STAFF, ADMIN) and profiles
- **Fish** (products) with rarity, price, and weight
- **Cart** items for shopping
- **Bills** (orders) with line items and status tracking

## Getting Started

### Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6+ (included via Maven Wrapper)

### Database Setup

1. Create a MySQL database:
```sql
CREATE DATABASE ftradehub;
```

2. Update connection settings in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ftradehub
spring.datasource.username=root
spring.datasource.password=root
```

### Build and Run

1. Clone the repository
2. Navigate to the backend directory:
```bash
cd back_end/tradehub_core
```

3. Build the project:
```bash
./mvnw clean install
```

4. Run the application:
```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

### Database Migrations

Flyway automatically runs migrations on startup. Migration files are in:
```
src/main/resources/db/migration/
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and create session
- `GET /api/auth/me` - Get current user
- `POST /api/auth/logout` - Logout and clear session

### Fish Management
- `GET /api/fish` - List all fish (public)
- `GET /api/fish/{id}` - Get fish details
- `POST /api/fish` - Create fish (admin only)
- `PUT /api/fish/{id}` - Update fish (admin only)
- `DELETE /api/fish/{id}` - Delete fish (admin only)

### Cart Operations
- `GET /api/cart` - Get current user's cart
- `POST /api/cart` - Add item to cart
- `DELETE /api/cart/{fishId}` - Remove item from cart
- `DELETE /api/cart` - Clear entire cart

### Order Management
- `POST /api/bills/checkout` - Create order from cart
- `GET /api/bills` - Get user's orders
- `GET /api/bills/{id}` - Get order details
- `POST /api/bills/{id}/pay` - Pay for order
- `POST /api/bills/{id}/cancel` - Cancel pending order

### User Profile
- `GET /api/user/info` - Get profile information
- `PUT /api/user/info` - Update profile information

## Security

### Authentication
Session-based authentication using Spring Security:
- Login creates `JSESSIONID` cookie
- Cookie sent with each request for authentication
- Logout invalidates session and clears cookie

### Authorization
Role-based access control:
- **Public**: Fish listing, registration, login
- **ADMIN**: Fish CRUD, all orders, user management
- **USER**: Cart, orders, profile

### CORS Configuration
Configured for Vue.js frontend at `http://localhost:5173`

## Code Structure

### Exception Handling

Custom exceptions for better error semantics:
- `ResourceNotFoundException` - Entity not found (404)
- `DuplicateResourceException` - Duplicate entry (409)
- `UnauthorizedAccessException` - Forbidden access (403)
- `BusinessException` - Business rule violation (400)

All exceptions handled by `GlobalExceptionHandler` which returns appropriate HTTP responses.

### Constants

Application-wide constants defined in `Constants.java`:
- User roles
- Error messages
- Reusable strings

### DTOs

Data Transfer Objects for API requests/responses:
- Decouple internal entities from API contracts
- Validation annotations for input
- Prevent exposing sensitive data

## Development

### Testing

Run tests with:
```bash
./mvnw test
```

### Code Style

The project uses:
- Lombok for reducing boilerplate
- Constructor injection via `@RequiredArgsConstructor`
- Javadoc on public methods
- Consistent naming conventions

## Project Structure

```
tradehub_core/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/fisch_tradehub/tradehub_core/
│   │   │       ├── TradehubCoreApplication.java
│   │   │       ├── common/         # Constants
│   │   │       ├── entity/         # JPA entities
│   │   │       ├── exception/      # Custom exceptions
│   │   │       ├── repository/     # Data access
│   │   │       ├── security/       # Security config
│   │   │       ├── service/        # Business logic
│   │   │       └── web/           # Controllers & DTOs
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/      # Flyway migrations
│   └── test/
└── pom.xml
```

## Contributing

When contributing:
1. Follow existing code style and structure
2. Add Javadoc for public methods
3. Use custom exceptions instead of generic `RuntimeException`
4. Keep services focused and testable
5. Update documentation for new features

## Documentation

- [System Specification](SYSTEM_SPECIFICATION.md) - Detailed technical documentation
- [Changelog](CHANGELOG.md) - Recent updates and changes

## License

This is a learning project for educational purposes.
