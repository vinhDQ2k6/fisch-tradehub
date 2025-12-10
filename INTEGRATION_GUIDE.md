# Integration Guide - Fisch TradeHub

Complete integration documentation for the Fisch TradeHub full-stack application.

## Overview

This guide explains how the frontend and backend integrate, ensuring smooth communication and consistent behavior across the entire system.

## Architecture Integration

### Full Stack Overview

```
┌─────────────────────────────────────────────────────────┐
│                     Client Browser                      │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Vue 3 Frontend (Port 5173)                             │
│  ├─ Components (Views, Layouts)                         │
│  ├─ Composables (useAuth, useCart)                      │
│  ├─ Services (authService, fetchClient)                 │
│  └─ Constants (API_ENDPOINTS, ROLES)                    │
│                         │                               │
│                         │ HTTP/JSON + Cookies           │
│                         ▼                               │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  Spring Boot Backend (Port 8080)                        │
│  ├─ Controllers (REST API)                              │
│  ├─ Services (Business Logic)                           │
│  ├─ Repositories (Data Access)                          │
│  ├─ Security (Auth + Authorization)                     │
│  └─ Constants (Error messages, Roles)                   │
│                         │                               │
│                         ▼                               │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  MySQL Database (Port 3306)                             │
│  └─ Tables (user, fish, cart, bill, etc.)               │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

## API Contract

### Endpoint Mapping

Frontend constants directly map to backend routes:

| Frontend Constant              | Backend Controller Method                                |
| ------------------------------ | -------------------------------------------------------- |
| `API_ENDPOINTS.AUTH.LOGIN`     | `POST /api/auth/login` → `AuthController.login()`        |
| `API_ENDPOINTS.AUTH.REGISTER`  | `POST /api/auth/register` → `AuthController.register()`  |
| `API_ENDPOINTS.AUTH.ME`        | `GET /api/auth/me` → `AuthController.me()`               |
| `API_ENDPOINTS.CART.BASE`      | `GET /api/cart` → `CartController.getCart()`             |
| `API_ENDPOINTS.CART.BASE`      | `POST /api/cart` → `CartController.addToCart()`          |
| `API_ENDPOINTS.BILLS.CHECKOUT` | `POST /api/bills/checkout` → `BillController.checkout()` |
| `API_ENDPOINTS.FISH.BASE`      | `GET /api/fish` → `FishController.getAllFish()`          |

### Request/Response Flow

#### Authentication Example

```
Frontend                           Backend
────────                           ───────

1. User submits login form
   │
   ├─ authService.login()
   │  POST /api/auth/login
   │  Body: username=alice&password=***
   │  Headers: Content-Type: application/x-www-form-urlencoded
   │
   └─────────────────────────────→ AuthController.login()
                                    │
                                    ├─ AuthenticationManager validates
                                    ├─ Creates SecurityContext
                                    ├─ Saves session
                                    ├─ Returns UserDTO
                                    │
   ←─────────────────────────────  HTTP 200 OK
   Response: {                      Set-Cookie: JSESSIONID=...
     id: 1,                         Body: UserDTO
     username: "alice",
     email: "alice@example.com",
     roles: ["ROLE_USER"]
   }
   │
   ├─ authService.currentUser()
   │  (validates session works)
   │
   ├─ useAuth().user = data
   │
   └─ Router pushes to /dashboard
```

#### Cart Operation Example

```
Frontend                           Backend
────────                           ───────

1. User clicks "Add to Cart"
   │
   ├─ useCart().addItem(fish)
   │  POST /api/cart
   │  Headers:
   │    Cookie: JSESSIONID=...
   │    Content-Type: application/json
   │    X-XSRF-TOKEN: <token>
   │  Body: { fishId: 5, quantity: 1 }
   │
   └─────────────────────────────→ CartController.addToCart()
                                    │
                                    ├─ Extract username from SecurityContext
                                    ├─ CartService.addToCart()
                                    │  ├─ Find user
                                    │  ├─ Find fish
                                    │  ├─ Find or create cart item
                                    │  └─ Save cart item
                                    │
   ←─────────────────────────────  HTTP 200 OK
   Response: void
   │
   ├─ useCart().fetchCart()
   │  GET /api/cart
   │
   └─────────────────────────────→ CartController.getCart()
                                    │
                                    └─ Returns cart items
   ←─────────────────────────────
   Response: [
     {
       fishId: 5,
       fishName: "Golden Carp",
       fishValue: 100.00,
       quantity: 1
     }
   ]
   │
   └─ useCart().items updated
      Components re-render with new data
```

## Security Integration

### Authentication Flow

```
┌─────────────────────────────────────────────────────────┐
│                    Session-Based Auth                   │
└─────────────────────────────────────────────────────────┘

Frontend Login
   ↓
POST /api/auth/login (form-urlencoded)
   ↓
Backend: Spring Security
   ├─ UserDetailsManager loads user
   ├─ PasswordEncoder compares hash
   ├─ Creates Authentication object
   └─ Saves in SecurityContext + HttpSession
   ↓
Response: Set-Cookie: JSESSIONID=xyz; HttpOnly; Secure
   ↓
Frontend stores cookie automatically
   ↓
All subsequent requests include JSESSIONID
   ↓
Backend: SecurityContextRepository
   └─ Loads SecurityContext from session
   ↓
Controllers access via @AuthenticationPrincipal
```

### CSRF Protection

```
┌─────────────────────────────────────────────────────────┐
│                    CSRF Token Flow                      │
└─────────────────────────────────────────────────────────┘

Backend (Spring Security)
   └─ Generates CSRF token
   └─ Stores in cookie: XSRF-TOKEN
   ↓
Frontend (csrf.js)
   └─ Reads token from cookie
   ↓
Frontend (fetchClient.js)
   └─ Adds X-XSRF-TOKEN header to mutations
   ↓
Backend (Spring Security)
   └─ Validates token matches
   ↓
Request processed or rejected (403)
```

### Role-Based Authorization

#### Frontend (Route Guards)

```javascript
// router/index.js
{
  path: '/dashboard',
  meta: {
    requiresAuth: true,
    roles: ['ADMIN']
  }
}

// routeGuard.js
if (route.meta.requiresAuth) {
  if (!useAuth().isLoggedIn()) {
    return '/auth/login';
  }

  if (route.meta.roles) {
    const user = useAuth().user.value;
    const hasRole = route.meta.roles.some(
      role => user.roles.includes(role)
    );
    if (!hasRole) {
      return '/auth/access'; // Access denied
    }
  }
}
```

#### Backend (Spring Security)

```java
// SecurityConfig.java
http.authorizeHttpRequests(auth -> auth
  .requestMatchers("/api/auth/**").permitAll()
  .requestMatchers("/api/admin/**").hasRole("ADMIN")
  .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
  .anyRequest().authenticated()
);

// In services
if (!bill.getBuyer().getId().equals(user.getId())) {
  throw new UnauthorizedAccessException(Constants.UNAUTHORIZED_BILL_ACCESS);
}
```

## Constants Alignment

### Role Names

**Frontend:**

```javascript
export const ROLES = {
  USER: "ROLE_USER",
  STAFF: "ROLE_STAFF",
  ADMIN: "ROLE_ADMIN",
};
```

**Backend:**

```java
public static final String ROLE_USER = "ROLE_USER";
public static final String ROLE_STAFF = "ROLE_STAFF";
public static final String ROLE_ADMIN = "ROLE_ADMIN";
```

**Why identical:** Spring Security requires "ROLE\_" prefix. Frontend must match exactly for role checks.

### Error Messages

**Frontend:**

```javascript
export const ERROR_MESSAGES = {
  NOT_FOUND: "Resource not found.",
  ALREADY_EXISTS: "Conflict: the resource already exists.",
  PERMISSION_DENIED: "You don't have permission...",
};
```

**Backend:**

```java
public static final String USER_NOT_FOUND = "User not found";
public static final String USERNAME_EXISTS = "Username already exists";
```

**Mapping via HTTP Status:**

```javascript
// Frontend handleError.js
case HTTP_STATUS.NOT_FOUND:
  return ERROR_MESSAGES.NOT_FOUND;
case HTTP_STATUS.CONFLICT:
  return ERROR_MESSAGES.ALREADY_EXISTS;
```

### HTTP Status Codes

**Backend Exceptions → HTTP Status:**

- `ResourceNotFoundException` → 404
- `DuplicateResourceException` → 409
- `UnauthorizedAccessException` → 403
- `BusinessException` → 400

**Frontend Detection:**

```javascript
// handleError.js
function mapFriendlyMessage(err) {
  switch (err.status) {
    case 404:
      return ERROR_MESSAGES.NOT_FOUND;
    case 409:
      return ERROR_MESSAGES.ALREADY_EXISTS;
    case 403:
      return ERROR_MESSAGES.PERMISSION_DENIED;
    case 400:
      return ERROR_MESSAGES.VALIDATION_FAILED;
  }
}
```

## Data Models

### User Data Transfer

**Backend DTO:**

```java
public record UserDTO(
    Long id,
    String username,
    String email,
    List<String> roles
) {}
```

**Frontend Usage:**

```javascript
// After login, useAuth().user contains:
{
  id: 1,
  username: "alice",
  email: "alice@example.com",
  roles: ["ROLE_USER"]
}

// Access in components
const { user } = useAuth();
console.log(user.value.username); // "alice"
```

### Cart Item Mapping

**Backend DTO:**

```java
public record CartItemDTO(
    Long fishId,
    String fishName,
    String fishRarity,
    BigDecimal fishValue,
    BigDecimal fishWeight,
    Integer quantity
) {}
```

**Frontend Transformation:**

```javascript
// useCart.js
items.value = data.map((item) => ({
  fishId: item.fishId,
  name: item.fishName,
  rarity: item.fishRarity,
  price: item.fishValue,
  quantity: item.quantity,
  total: item.fishValue * item.quantity, // Computed
}));
```

### Bill (Order) Structure

**Backend:**

```java
public record BillDTO(
    Long id,
    String buyerUsername,
    BigDecimal total,
    BillStatus status,
    LocalDateTime createdAt,
    List<BillInfoDTO> items
) {}
```

**Frontend Display:**

```vue
<template>
  <div v-for="bill in bills" :key="bill.id">
    <h3>Order #{{ bill.id }}</h3>
    <p>Total: ${{ bill.total }}</p>
    <p>Status: {{ bill.status }}</p>
    <ul>
      <li v-for="item in bill.items" :key="item.fishId">
        {{ item.fishName }} x{{ item.amount }} = ${{ item.sum }}
      </li>
    </ul>
  </div>
</template>
```

## Error Handling Integration

### Error Flow

```
Backend throws exception
   ↓
GlobalExceptionHandler catches
   ↓
Returns HTTP status + JSON body
   ↓
Frontend fetchClient.js receives
   ↓
Throws structured error:
{
  status: 404,
  data: { message: "User not found" },
  message: "User not found"
}
   ↓
Component catch block
   ↓
showError(toast, err)
   ↓
handleError.js
   ├─ Maps status to friendly message
   ├─ Extracts validation errors
   └─ Displays toast notification
```

### Example: Duplicate Username

**Backend:**

```java
if (userRepository.findByUsername(request.username()).isPresent()) {
    throw new DuplicateResourceException(Constants.USERNAME_EXISTS);
}
// GlobalExceptionHandler maps to 409 Conflict
```

**Frontend:**

```javascript
try {
  await authService.register({ username, email, password });
  showSuccess(toast, SUCCESS_MESSAGES.REGISTER);
} catch (err) {
  // err.status === 409
  // handleError shows: "Conflict: the resource already exists"
  showError(toast, err);
}
```

## State Synchronization

### Cart Synchronization

**Trigger:** User login/logout

```javascript
// useCart.js
watch(user, (newUser) => {
  if (newUser) {
    fetchCart(); // Load from backend
  } else {
    items.value = []; // Clear local state
  }
});
```

**Consistency:**

- Frontend state mirrors backend
- After each mutation, refetch to stay in sync
- Logout clears frontend state

### Session Persistence

**On Page Refresh:**

```javascript
// main.js
async function initAuth() {
  await useAuth().init(); // Check existing session
}

router.isReady().then(initAuth);
```

**Flow:**

1. Browser sends JSESSIONID with every request
2. Backend validates session
3. If valid, returns user data
4. Frontend hydrates state
5. User stays logged in across refreshes

## Testing Integration

### End-to-End Test Scenario

**Critical Path: Complete Purchase Flow**

```
1. Registration
   Frontend: POST /api/auth/register
   Backend: Create user, return 201
   ✓ User account created

2. Login
   Frontend: POST /api/auth/login
   Backend: Validate, create session, return 200
   ✓ Session cookie set
   ✓ useAuth().user populated

3. Browse Fish
   Frontend: GET /api/fish
   Backend: Return fish list, 200
   ✓ Fish displayed

4. Add to Cart
   Frontend: POST /api/cart
   Backend: Create cart item, return 200
   ✓ Cart count updated
   ✓ Badge shows "1"

5. Checkout
   Frontend: POST /api/bills/checkout
   Backend: Create bill + bill_info, clear cart, return 200
   ✓ Bill created
   ✓ Cart cleared
   ✓ Redirect to orders page

6. View Order
   Frontend: GET /api/bills
   Backend: Return user's bills, 200
   ✓ Order displayed
   ✓ Status shows "Pending Payment"
```

### Integration Test Checklist

**Authentication:**

- [ ] Register with valid data → Success
- [ ] Register with duplicate username → 409 Conflict
- [ ] Login with valid credentials → Success + cookie
- [ ] Login with invalid credentials → 401 Unauthorized
- [ ] Access protected route without login → Redirect to login
- [ ] Logout → Session cleared, redirect to home

**Authorization:**

- [ ] User role can access user pages → Success
- [ ] User role cannot access admin pages → 403 Forbidden
- [ ] Admin role can access all pages → Success
- [ ] Frontend route guard blocks unauthorized routes

**Cart Operations:**

- [ ] Add item when logged in → Success
- [ ] Add item when not logged in → Error message
- [ ] Remove item → Success, count updated
- [ ] Clear cart → Success, cart empty
- [ ] Cart persists across page refresh → Success

**Error Handling:**

- [ ] Network error → Friendly message displayed
- [ ] 404 error → "Resource not found" displayed
- [ ] 409 error → "Already exists" displayed
- [ ] Validation error → Field-specific messages displayed

## Deployment Integration

### Environment Configuration

**Frontend (.env):**

```env
VITE_API_BASE=http://localhost:8080
```

**Backend (application.properties):**

```properties
# CORS must allow frontend origin
cors.allowed-origins=http://localhost:5173
```

**Production:**

```env
# Frontend
VITE_API_BASE=https://api.fishtradehub.com

# Backend
cors.allowed-origins=https://fishtradehub.com
```

### Deployment Checklist

**Backend:**

- [ ] MySQL database accessible
- [ ] Flyway migrations run
- [ ] CORS configured for frontend origin
- [ ] Session cookie settings (Secure, HttpOnly, SameSite)
- [ ] CSRF enabled and configured
- [ ] Environment variables set

**Frontend:**

- [ ] Built with production API URL
- [ ] Hosted with SPA routing support (redirect to index.html)
- [ ] HTTPS enabled
- [ ] CORS requests work
- [ ] Cookies sent with credentials: 'include'

**Integration:**

- [ ] Frontend can call backend API
- [ ] Login works and sets cookie
- [ ] Authenticated requests succeed
- [ ] CSRF protection works
- [ ] Role-based access control functions

## Troubleshooting

### Common Integration Issues

**Issue: CORS Error**

```
Access to fetch at 'http://localhost:8080/api/auth/login'
from origin 'http://localhost:5173' has been blocked by CORS policy
```

**Solution:**

- Check backend `SecurityConfig.java` CORS configuration
- Verify `cors.allowed-origins` matches frontend URL
- Ensure `credentials: 'include'` in fetchClient
- Check `Access-Control-Allow-Credentials: true` header

**Issue: Session Not Persisting**

```
User logs in successfully but appears logged out on refresh
```

**Solution:**

- Check `credentials: 'include'` in all fetchClient calls
- Verify backend sets `HttpOnly` cookie
- Check browser doesn't block third-party cookies
- Ensure same domain/subdomain for dev (use `localhost` for both)

**Issue: CSRF Token Missing**

```
403 Forbidden on POST/PUT/DELETE requests
```

**Solution:**

- Check CSRF token cookie is set: `XSRF-TOKEN`
- Verify `csrf.js` reads token correctly
- Ensure `fetchClient.js` adds `X-XSRF-TOKEN` header
- Check backend CSRF configuration enabled

**Issue: Role Check Fails**

```
User has ADMIN role but route guard denies access
```

**Solution:**

- Verify role name matches exactly: `ROLE_ADMIN`
- Check `user.roles` array contains role
- Ensure backend returns roles in `UserDTO`
- Confirm route meta.roles array format

## Performance Considerations

### Optimization Points

**Backend:**

- JPA lazy loading (don't over-eager fetch)
- Database indexes on FKs
- Transaction boundaries correct
- DTO projection (don't expose entities)

**Frontend:**

- Route-level code splitting (already done)
- API response caching in composables
- Debounce user inputs
- Virtual scrolling for long lists

**Network:**

- Minimize API calls (batch when possible)
- Use HTTP caching headers
- Compress responses (gzip)
- CDN for static assets

## Summary

This integration guide demonstrates:

✅ **Clear API contracts** between layers  
✅ **Consistent constants** (roles, endpoints)  
✅ **Aligned error handling** (status codes → messages)  
✅ **Secure authentication** (session + CSRF)  
✅ **Type-safe data flow** (DTOs match frontend models)  
✅ **Complete test scenarios** for validation  
✅ **Deployment readiness** with checklists

The architecture is **structured** (INTJ), **reliable** (ISTJ), and **production-ready**.

---

For detailed specifications, see:

- [Backend Specification](/back_end/SYSTEM_SPECIFICATION.md)
- [Frontend Specification](/front_end/FRONTEND_SPECIFICATION.md)
