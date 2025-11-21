# System Specification - Fisch TradeHub Backend

This document provides detailed technical specifications for the Fisch TradeHub backend system, including data flow, component interactions, and implementation details.

## Table of Contents
1. [System Architecture](#system-architecture)
2. [Data Model](#data-model)
3. [Component Specifications](#component-specifications)
4. [Data Flow](#data-flow)
5. [Security Implementation](#security-implementation)
6. [Error Handling](#error-handling)

---

## System Architecture

### Layered Architecture Pattern

The application follows a strict layered architecture with clear separation of concerns:

```
┌─────────────────────────────────────┐
│      Presentation Layer             │
│  (REST Controllers + DTOs)          │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Business Logic Layer           │
│         (Services)                  │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Data Access Layer              │
│      (Repositories)                 │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│         Database (MySQL)            │
└─────────────────────────────────────┘
```

**Layer Responsibilities:**

1. **Presentation Layer** (`web.api`): 
   - Handle HTTP requests/responses
   - Validate input via Bean Validation
   - Convert between DTOs and service calls
   - Apply security constraints

2. **Business Logic Layer** (`service`):
   - Implement business rules
   - Manage transactions
   - Coordinate between repositories
   - Throw domain exceptions

3. **Data Access Layer** (`repository`):
   - Interface with database
   - Provide CRUD operations
   - Custom queries

4. **Domain Layer** (`entity`):
   - Define data models
   - JPA mappings
   - Entity relationships

---

## Data Model

### Entity Relationship Diagram

```
┌──────────────┐         ┌──────────────┐
│     User     │1       *│  UserInfo    │
│              ├─────────┤              │
│  id (PK)     │         │  user_id(PK) │
│  username    │         │  fullname    │
│  email       │         │  age         │
│  password    │         │  gender      │
│  role        │         └──────────────┘
│  active      │
└──────┬───────┘
       │1
       │
       │*
┌──────▼───────┐         ┌──────────────┐
│     Cart     │*       1│     Fish     │
│              ├─────────┤              │
│  id (PK)     │         │  id (PK)     │
│  user_id(FK) │         │  name        │
│  fish_id(FK) │         │  rarity      │
│  quantity    │         │  value       │
└──────────────┘         │  weight      │
                         └──────┬───────┘
                                │1
┌──────────────┐                │
│     Bill     │                │
│              │                │
│  id (PK)     │                │*
│  buyer_id(FK)│         ┌──────▼───────┐
│  seller_id   │         │   BillInfo   │
│  total       │1       *│              │
│  status      ├─────────┤  id (PK)     │
│  created_at  │         │  bill_id(FK) │
│  closed_at   │         │  fish_id(FK) │
│  rating      │         │  price       │
└──────────────┘         │  amount      │
                         │  sum         │
                         └──────────────┘
```

### Entity Specifications

#### User
**Purpose:** Authentication and authorization  
**Key Features:**
- Unique username and email
- BCrypt hashed password
- Single role per user (ROLE_USER, ROLE_STAFF, ROLE_ADMIN)
- Active flag for account status
- Automatic timestamp management

**Relationships:**
- One-to-one with UserInfo
- One-to-many with Cart
- One-to-many with Bill (as buyer)

#### UserInfo
**Purpose:** Extended user profile information  
**Key Features:**
- Separated from authentication data
- Optional fields (can be null)
- Shares primary key with User

#### Fish
**Purpose:** Product catalog  
**Key Features:**
- Name and rarity classification
- Decimal precision for monetary values
- Weight tracking

#### Cart
**Purpose:** Shopping cart item  
**Key Features:**
- Unique constraint on (user_id, fish_id)
- Quantity tracking
- No price stored (retrieved from Fish)

**Business Rules:**
- One cart item per user per fish
- Adding same fish updates quantity

#### Bill
**Purpose:** Order record  
**Key Features:**
- Immutable after creation
- Status-based state machine
- Optional seller (marketplace support)
- Rating support

**Status Values:**
- PENDING_PAYMENT (0)
- PROCESSING (1)
- CANCELLED (2)
- COMPLETED (3)

#### BillInfo
**Purpose:** Order line item  
**Key Features:**
- Snapshot of fish price at purchase time
- Calculated sum field
- Unique constraint on (bill_id, fish_id)

---

## Component Specifications

### Service Layer

#### AuthService
**Responsibilities:**
- User registration with password hashing
- User lookup for authentication
- DTO conversion

**Key Methods:**
- `register(RegisterRequest)` - Create new user account
  - Validates username/email uniqueness
  - Hashes password with BCrypt
  - Assigns default USER role
  - Throws DuplicateResourceException on conflict

- `getUserDto(String username)` - Get user details
  - Looks up user by username
  - Converts to DTO (no password exposed)
  - Throws ResourceNotFoundException if not found

#### BillService
**Responsibilities:**
- Order creation from cart
- Order management
- Payment processing

**Key Methods:**
- `checkout(String username)` - Create order
  - Transaction-managed
  - Calculates total from cart items
  - Creates Bill and BillInfo records
  - Clears user's cart
  - Throws BusinessException if cart empty

- `payBill(Long billId, String username)` - Process payment
  - Validates ownership
  - Checks status (must be PENDING_PAYMENT)
  - Updates status to PROCESSING
  - Throws UnauthorizedAccessException if not owner

- `cancelBill(Long billId, String username)` - Cancel order
  - Validates ownership and status
  - Only pending bills can be cancelled
  - Updates status to CANCELLED

#### CartService
**Responsibilities:**
- Shopping cart management
- Item quantity updates

**Key Methods:**
- `addToCart(String username, AddToCartRequest)` - Add/update item
  - Finds or creates cart item
  - Adds to existing quantity
  - Saves to database

- `removeFromCart(String username, Long fishId)` - Remove item
  - Deletes cart item if exists
  - Silent if item not found

- `clearCart(String username)` - Empty cart
  - Deletes all user's cart items

#### FishService
**Responsibilities:**
- Product catalog management
- CRUD operations

**Key Methods:**
- `findAll()` - List all fish, sorted by name
- `findById(Long id)` - Get fish details
- `create(FishRequest)` - Add new fish (admin)
- `update(Long id, FishRequest)` - Update fish (admin)
- `delete(Long id)` - Remove fish (admin)

#### UserInfoService
**Responsibilities:**
- User profile management

**Key Methods:**
- `getUserInfo(String username)` - Get profile
  - Returns null if not set
- `updateUserInfo(String username, UserInfoDTO)` - Update profile
  - Creates if doesn't exist

---

## Data Flow

### User Registration Flow

```
1. Frontend → POST /api/auth/register
   ├─ Body: { username, email, password }
   │
2. AuthController.register()
   ├─ @Valid validates input
   ├─ Calls AuthService.register()
   │
3. AuthService.register()
   ├─ Check username exists → throw DuplicateResourceException
   ├─ Check email exists → throw DuplicateResourceException
   ├─ Hash password with BCrypt
   ├─ Create User entity with role=ROLE_USER
   ├─ Save to database
   └─ Return UserDTO
   │
4. AuthController
   ├─ Return 201 Created
   └─ Location header: /api/auth/users/{id}
```

### Login Flow

```
1. Frontend → POST /api/auth/login
   ├─ Body: { username, password }
   │
2. AuthController.login()
   ├─ Create UsernamePasswordAuthenticationToken
   ├─ Call AuthenticationManager.authenticate()
   │   ├─ UserDetailsManager loads user
   │   ├─ Compare hashed passwords
   │   └─ Return Authentication or throw BadCredentialsException
   ├─ Create SecurityContext
   ├─ Save to HttpSession
   ├─ Return JSESSIONID cookie
   └─ Return UserDTO
```

### Checkout Flow

```
1. Frontend → POST /api/bills/checkout
   │
2. BillController.checkout()
   ├─ Extract username from SecurityContext
   ├─ Call BillService.checkout()
   │
3. BillService.checkout() [@Transactional]
   ├─ Load User by username
   ├─ Load Cart items with Fish details
   ├─ Validate cart not empty
   ├─ Calculate total = Σ(fish.value × quantity)
   ├─ Create Bill entity
   │   ├─ buyer = current user
   │   ├─ total = calculated total
   │   └─ status = PENDING_PAYMENT
   ├─ Save Bill
   ├─ For each cart item:
   │   ├─ Create BillInfo
   │   │   ├─ price = fish.value (snapshot)
   │   │   ├─ amount = cart.quantity
   │   │   └─ sum = price × amount
   │   └─ Save BillInfo
   ├─ Delete all cart items
   └─ Return BillDTO with items
   │
4. Transaction commits (all or nothing)
5. Return 200 OK with bill details
```

### Add to Cart Flow

```
1. Frontend → POST /api/cart
   ├─ Body: { fishId, quantity }
   │
2. CartController.addToCart()
   ├─ Extract username from SecurityContext
   ├─ Validate request
   ├─ Call CartService.addToCart()
   │
3. CartService.addToCart() [@Transactional]
   ├─ Load User
   ├─ Load Fish (throw if not found)
   ├─ Find existing Cart item by (user_id, fish_id)
   │   ├─ If exists: quantity += request.quantity
   │   └─ If not exists: create new with quantity
   ├─ Save Cart item
   └─ Return
   │
4. Return 200 OK
```

---

## Security Implementation

### Authentication Mechanism

**Type:** Session-based (HTTP Session + Cookie)

**Flow:**
1. User logs in with credentials
2. Spring Security validates against database
3. Creates SecurityContext with Authentication
4. Stores SecurityContext in HttpSession
5. Returns JSESSIONID cookie to client
6. Client sends cookie with each request
7. SecurityContextRepository loads context from session

**Configuration:**
- Password encoding: BCrypt (strength 10)
- Session management: HTTP Session
- Cookie name: JSESSIONID
- Remember-me: Optional, 24-hour validity

### Authorization Rules

**Endpoint Security:**
```java
/api/auth/**           → permitAll()
/api/admin/**          → hasRole('ADMIN')
/api/user/**           → hasAnyRole('USER', 'ADMIN')
/api/fish (GET)        → permitAll()
/api/fish (POST/PUT)   → hasRole('ADMIN')
/api/cart/**           → authenticated()
/api/bills/**          → authenticated()
```

**Method-Level Security:**
- Services check ownership in business logic
- Example: BillService verifies user owns bill before operations

### CORS Configuration

**Allowed Origins:** `http://localhost:5173`  
**Allowed Methods:** GET, POST, PUT, DELETE, OPTIONS  
**Allowed Headers:** * (all)  
**Expose Headers:** Authorization  
**Credentials:** true (cookies allowed)

---

## Error Handling

### Exception Hierarchy

```
RuntimeException
├── ResourceNotFoundException (404)
│   └── Used when: Entity not found in database
│
├── DuplicateResourceException (409)
│   └── Used when: Unique constraint violation
│
├── UnauthorizedAccessException (403)
│   └── Used when: User lacks ownership/permission
│
└── BusinessException (400)
    └── Used when: Business rule violation
```

### Global Exception Handler

**Class:** `GlobalExceptionHandler` (@RestControllerAdvice)

**Mappings:**
- `BadCredentialsException` → 401 Unauthorized
- `ResourceNotFoundException` → 404 Not Found
- `UnauthorizedAccessException` → 403 Forbidden
- `DuplicateResourceException` → 409 Conflict
- `BusinessException` → 400 Bad Request
- `MethodArgumentNotValidException` → 400 Bad Request (validation errors)
- `RuntimeException` → 409 Conflict (legacy fallback)
- `Exception` → 500 Internal Server Error

**Response Format:**
- Simple string message for single errors
- Map<String, String> for validation errors (field → message)

### Error Messages

Standardized messages in `Constants.java`:
- Clear and actionable
- No sensitive information
- Consistent language
- Client-friendly

---

## Transaction Management

### Strategy
- **Read-only by default** (`@Transactional(readOnly = true)` on service classes)
- **Write operations** marked explicitly with `@Transactional`
- **Isolation level:** Default (READ_COMMITTED)
- **Propagation:** REQUIRED (join existing or create new)

### Critical Transactions

**Checkout:**
- Creates Bill
- Creates multiple BillInfo records
- Deletes Cart items
- Must be atomic (all or nothing)

**Add to Cart:**
- Find or create Cart item
- Update quantity
- Simple, short transaction

---

## Performance Considerations

### Database
- Indexes on foreign keys
- Unique constraints enforce data integrity at DB level
- Timestamp columns use DB defaults

### JPA
- Lazy loading for relationships (default)
- Eager loading only when necessary
- DTO projections avoid entity conversion overhead

### Query Optimization
- Repository methods use Spring Data JPA conventions
- Custom queries only when needed
- Sorting at database level

---

## Extensibility

### Adding New Entities
1. Create entity class in `entity/`
2. Create repository interface in `repository/`
3. Create service class in `service/`
4. Create DTOs in `web/dto/`
5. Create controller in `web/api/`
6. Add Flyway migration

### Adding New Roles
1. Add constant to `Constants.java`
2. Update `SecurityConfig` authorization rules
3. Update service layer permission checks

### Adding Business Rules
- Implement in service layer
- Throw appropriate custom exception
- Document in service method Javadoc

---

## Deployment Considerations

### Environment Configuration
- Database credentials via environment variables
- Different profiles for dev/test/prod
- Flyway validates schema on startup

### Health Checks
- Spring Boot Actuator endpoints available
- Database connectivity check
- Application readiness probe

### Logging
- SQL logging configurable via `spring.jpa.show-sql`
- Service-level logging for business operations
- Exception logging in GlobalExceptionHandler

---

## Future Enhancements

**Potential Additions:**
1. Multi-role support per user
2. Payment gateway integration
3. Inventory management
4. Order shipping and tracking
5. Advanced search and filtering
6. Rate limiting and throttling
7. Audit logging
8. API versioning
9. GraphQL endpoint
10. Caching layer (Redis)

**Migration Path:**
- Current schema designed to accommodate extensions
- Use of surrogate keys (ID) allows easy joins
- Flyway supports versioned migrations
- Service layer isolates business logic from data model

---

## Glossary

- **DTO (Data Transfer Object):** Object for transferring data between layers
- **JPA (Java Persistence API):** ORM specification for database access
- **BCrypt:** Adaptive password hashing function
- **CORS (Cross-Origin Resource Sharing):** HTTP-header based mechanism for cross-domain requests
- **FK (Foreign Key):** Reference to primary key in another table
- **PK (Primary Key):** Unique identifier for table row
- **CRUD:** Create, Read, Update, Delete operations
