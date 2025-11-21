# Frontend System Specification - Fisch TradeHub

Detailed technical specification for the Vue 3 frontend application, including architecture, state management, data flow, and component interactions.

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [State Management](#state-management)
3. [Authentication Flow](#authentication-flow)
4. [Data Flow](#data-flow)
5. [Component Specifications](#component-specifications)
6. [API Integration](#api-integration)
7. [Error Handling Strategy](#error-handling-strategy)

---

## Architecture Overview

### Technology Stack

**Core:**
- Vue 3.4.34 (Composition API)
- Vue Router 4.4.0
- Vite 5.3.1 (Build tool)

**UI:**
- PrimeVue 4.3.1 (Component library)
- TailwindCSS 4.1.17 (Utility CSS)
- PrimeIcons 7.0.0

**Validation:**
- Yup 1.7.1 (Schema validation)

### Application Architecture

```
┌─────────────────────────────────────────┐
│          Browser / Client               │
├─────────────────────────────────────────┤
│                                         │
│  ┌───────────────────────────────────┐ │
│  │        Vue Components             │ │
│  │  (Views, Layouts, UI Elements)    │ │
│  └──────────────┬────────────────────┘ │
│                 │                       │
│  ┌──────────────▼────────────────────┐ │
│  │       Composables Layer           │ │
│  │  (useAuth, useCart - State)       │ │
│  └──────────────┬────────────────────┘ │
│                 │                       │
│  ┌──────────────▼────────────────────┐ │
│  │       Service Layer               │ │
│  │  (authService, fetchClient)       │ │
│  └──────────────┬────────────────────┘ │
│                 │                       │
│  ┌──────────────▼────────────────────┐ │
│  │       HTTP Client                 │ │
│  │  (fetch with CSRF, cookies)       │ │
│  └──────────────┬────────────────────┘ │
│                 │                       │
└─────────────────┼───────────────────────┘
                  │ HTTP/JSON
                  ▼
          ┌───────────────┐
          │  Backend API  │
          │  (Spring Boot)│
          └───────────────┘
```

### Module Organization

**Principle:** Feature-based modules with clear boundaries

1. **auth/** - Authentication and session management
2. **cart/** - Shopping cart functionality
3. **common/** - Shared constants and utilities
4. **components/** - Reusable UI components
5. **layout/** - Application layouts and navigation
6. **router/** - Route definitions and guards
7. **views/** - Page-level components

---

## State Management

### Philosophy

**No centralized store** (Vuex/Pinia) - Using module-scoped singletons with Vue's reactivity.

**Rationale:**
- Simpler for small-medium apps
- Less boilerplate
- Direct imports
- Type-safe without extra tooling

### Singleton Composables Pattern

#### useAuth.js - Authentication State

**Scope:** Module-level (singleton)

```javascript
// Module-scoped state (shared across all imports)
const user = ref(null);
const loading = ref(false);
const ready = ref(false);

export function useAuth() {
  // Return same state references each time
  return { user, loading, ready, ... };
}
```

**State:**
- `user` - Current user object (null if not logged in)
- `loading` - Auth operation in progress
- `ready` - Auth state initialized

**Methods:**
- `init()` - Fetch current user on app startup
- `doLogin()` - Authenticate with credentials
- `doLogout()` - Clear session and state
- `isLoggedIn()` - Check authentication status

**Lifecycle:**
```
App Start → init() → Check session → Set user/ready
Login → doLogin() → API call → Update user
Logout → doLogout() → API call → Clear user
```

#### useCart.js - Shopping Cart State

**Scope:** Module-level (singleton)

```javascript
const items = ref([]);

export function useCart() {
  const { user, isLoggedIn } = useAuth();
  
  watch(user, (newUser) => {
    if (newUser) fetchCart();
    else items.value = [];
  });
  
  return { items, addItem, removeItem, ... };
}
```

**State:**
- `items` - Array of cart items with calculated totals

**Computed:**
- `itemCount` - Total quantity
- `totalValue` - Total price

**Methods:**
- `fetchCart()` - Load from backend
- `addItem(fish)` - Add to cart
- `removeItem(fishId)` - Remove from cart
- `clear()` - Empty cart
- `checkout()` - Create order

**Lifecycle:**
```
User logs in → Watch triggers → fetchCart()
Add item → API call → fetchCart() → Update items
Checkout → API call → fetchCart() → items cleared
User logs out → Watch triggers → items cleared
```

**Synchronization:**
- Watches `useAuth().user` for login/logout
- Refetches after mutations
- Clears on logout

---

## Authentication Flow

### Registration Flow

```
1. User → Register Page
   └─ Fill form (username, email, password)

2. Submit → authService.register()
   └─ POST /api/auth/register
   
3. Backend validates
   ├─ Success: 201 Created + UserDTO
   └─ Error: 409 Conflict (duplicate)
   
4. Frontend
   ├─ Success: showSuccess() → Redirect to login
   └─ Error: showError() → Display message
```

### Login Flow

```
1. User → Login Page
   └─ Enter credentials + remember-me

2. Submit → useAuth().doLogin()
   └─ authService.login()
       └─ POST /api/auth/login (form-urlencoded)
       
3. Backend authenticates
   ├─ Success: Set JSESSIONID cookie + 200 OK
   └─ Error: 401 Unauthorized
   
4. Frontend
   ├─ Success:
   │   ├─ Fetch user via /api/auth/me
   │   ├─ Update useAuth().user
   │   └─ Router pushes to intended or dashboard
   └─ Error:
       └─ showError() → Display message

5. Session maintained via cookie
   └─ All subsequent requests include JSESSIONID
```

### Session Check (App Startup)

```
1. App mounts → main.js
   └─ router.isReady().then(initAuth)

2. initAuth() → useAuth().init()
   └─ authService.currentUser()
       └─ GET /api/auth/me
       
3. Backend checks session
   ├─ Valid: Return UserDTO
   └─ Invalid: 401 Unauthorized
   
4. Frontend
   ├─ Valid: Set useAuth().user
   └─ Invalid: user remains null
   
5. Set ready = true
   └─ Router guards can now evaluate
```

### Logout Flow

```
1. User clicks logout → useAuth().doLogout()
   └─ authService.logout()
       └─ POST /api/auth/logout
       
2. Backend invalidates session
   └─ Clear JSESSIONID cookie
   
3. Frontend
   └─ Clear useAuth().user = null
   
4. Cart watcher triggers
   └─ useCart().items = []
   
5. Router redirects to home
```

---

## Data Flow

### Component → Composable → Service → API

**Example: Adding to Cart**

```
┌─────────────────┐
│ FischTrade.vue  │ User clicks "Add to Cart"
└────────┬────────┘
         │
         │ const { addItem } = useCart()
         │ await addItem(fish)
         ▼
┌─────────────────┐
│  useCart.js     │ Composable layer
└────────┬────────┘
         │
         │ Check isLoggedIn()
         │ await apiFetch(...)
         ▼
┌─────────────────┐
│ fetchClient.js  │ HTTP client
└────────┬────────┘
         │
         │ POST /api/cart
         │ + CSRF token
         │ + JSON body
         ▼
┌─────────────────┐
│  Backend API    │ Spring Boot
└────────┬────────┘
         │
         │ 200 OK
         ▼
┌─────────────────┐
│  useCart.js     │ Success handler
└────────┬────────┘
         │
         │ await fetchCart()
         │ items.value = updated
         ▼
┌─────────────────┐
│ FischTrade.vue  │ Reactive update
│ (Cart badge)    │ Display new count
└─────────────────┘
```

### Reactive Data Flow

**Vue's Reactivity:**
```
ref/reactive → computed → template
     ↓            ↓          ↓
  [change] → [recalc] → [re-render]
```

**Cart Example:**
```javascript
// Reactive source
const items = ref([{ quantity: 1, price: 10 }]);

// Computed (auto-updates)
const totalValue = computed(() => 
  items.value.reduce((sum, i) => sum + i.total, 0)
);

// Template (auto-renders)
<span>{{ totalValue }}</span>
```

### Route Guards and Data Flow

```
1. User navigates → /dashboard

2. Router beforeEach guard
   ├─ Check route.meta.requiresAuth
   └─ Check useAuth().ready
   
3. If requires auth
   ├─ Check useAuth().isLoggedIn()
   │   ├─ True: Allow navigation
   │   └─ False: Redirect to /auth/login
   │
   └─ If has roles requirement
       ├─ Check user.roles includes required
       │   ├─ True: Allow
       │   └─ False: Redirect to /auth/access
       
4. Component loads
   └─ Can safely assume authentication
```

---

## Component Specifications

### Page Components

#### FischTrade.vue - Fish Shopping Page

**Purpose:** Browse and purchase fish

**Dependencies:**
- `useCart()` - Add items to cart
- `apiFetch()` - Load fish list

**State:**
```javascript
const fish = ref([]); // List of available fish
const loading = ref(false);
```

**Lifecycle:**
```
mounted → loadFish()
  └─ GET /api/fish
  └─ fish.value = data
  
User clicks "Add" → addToCart(fish)
  └─ useCart().addItem(fish)
  └─ showSuccess() or showError()
```

#### Debts.vue (Bills) - Order History

**Purpose:** View user's order history

**Dependencies:**
- `useAuth()` - Get current user
- `apiFetch()` - Load bills

**State:**
```javascript
const bills = ref([]);
const selectedBill = ref(null);
```

**Actions:**
- View bill details
- Cancel pending bills
- Pay for bills

#### Dashboard.vue - Admin Dashboard

**Purpose:** Analytics and overview

**Auth:** Requires ADMIN role

**Features:**
- Stats widgets
- Recent sales
- Revenue charts

### Layout Components

#### AppLayoutPrivate.vue

**Purpose:** Authenticated pages layout

**Features:**
- Top bar with user menu
- Sidebar navigation
- Main content area
- Cart indicator

**Structure:**
```
┌─────────────────────────────────┐
│       AppTopbarPrivate          │
│  (Logo | Nav | User | Cart)     │
├─────────────┬───────────────────┤
│ AppSidebar  │                   │
│ (Menu)      │  <router-view>    │
│             │  (Page content)   │
│             │                   │
└─────────────┴───────────────────┘
```

#### AppLayoutPublic.vue

**Purpose:** Public pages layout

**Features:**
- Marketing-focused
- Simple navigation
- No authentication required

---

## API Integration

### API Client (fetchClient.js)

**Features:**

1. **Automatic JSON Handling**
   - Detects object body → stringify + set Content-Type
   - Detects JSON response → parse automatically

2. **CSRF Protection**
   - Reads token from cookie
   - Adds X-XSRF-TOKEN header to mutations
   - Skips for GET/HEAD

3. **Credential Inclusion**
   - Always sends cookies (credentials: 'include')
   - Required for session auth

4. **Error Standardization**
   - Throws structured object: `{ status, data, message }`
   - Consistent error shape for handlers

**Usage Patterns:**

```javascript
// Simple GET
const { data } = await apiFetch('/api/fish');

// POST with body
await apiFetch('/api/cart', {
  method: 'POST',
  body: { fishId: 1, quantity: 2 }
});

// DELETE
await apiFetch(`/api/cart/${fishId}`, {
  method: 'DELETE'
});

// With error handling
try {
  await apiFetch('/api/endpoint');
} catch (err) {
  // err = { status: 404, data: {...}, message: "..." }
  showError(toast, err);
}
```

### Constants (constants.js)

**Purpose:** Single source of truth for endpoints, messages, roles

**Benefits:**
- Refactoring safety
- Autocomplete support
- Type-like documentation
- Consistent strings

**Structure:**

```javascript
export const API_ENDPOINTS = {
  AUTH: {
    LOGIN: '/api/auth/login',
    LOGOUT: '/api/auth/logout',
    // ...
  },
  CART: {
    BASE: '/api/cart',
    BY_FISH: (fishId) => `/api/cart/${fishId}`,
  },
};

export const ROLES = {
  USER: 'ROLE_USER',
  ADMIN: 'ROLE_ADMIN',
};

export const ERROR_MESSAGES = {
  LOGIN_REQUIRED: 'You must be logged in...',
  // ...
};
```

---

## Error Handling Strategy

### Centralized Error Display

**handleError.js** provides consistent error handling:

1. **HTTP Status → User Message**
   - Maps status codes to friendly messages
   - Handles network errors
   - Extracts validation errors

2. **Toast Notifications**
   - Error: Red, 4s
   - Success: Green, 3s
   - Consistent placement and style

3. **Usage Pattern**

```javascript
import { showError, showSuccess } from '@/auth/handleError';

async function performAction() {
  try {
    await apiCall();
    showSuccess(toast, SUCCESS_MESSAGES.ACTION_COMPLETE);
  } catch (err) {
    showError(toast, err);
    // Optional custom message
    // showError(toast, err, { message: 'Custom message' });
  }
}
```

### Error Flow

```
API Error → fetchClient throws
  └─ { status: 409, data: {...}, message: "..." }
  
Component catch block
  └─ showError(toast, err)
  
handleError.js
  ├─ mapFriendlyMessage(err)
  │   └─ 409 → "Conflict: already exists"
  │
  ├─ extractValidationMessage(err.data)
  │   └─ Check for validation errors
  │
  └─ toast.add()
      └─ Display to user
```

---

## Security Considerations

### CSRF Protection

**Implementation:**
1. Token stored in cookie by backend
2. Read by `csrf.js`
3. Added to headers by `fetchClient.js`
4. Validated by backend

**When Applied:**
- All POST, PUT, DELETE, PATCH requests
- Skipped for GET, HEAD (safe methods)

### Session Management

**Cookie-based:**
- `JSESSIONID` set by backend
- HTTP-only flag (not accessible to JS)
- Secure flag (HTTPS only in prod)
- SameSite=Lax (CSRF protection)

**Advantages:**
- No token storage in localStorage
- Automatic expiration
- Secure by default

### Route Protection

**Implementation:**
- Route guards check `useAuth().user`
- Verify roles if specified
- Redirect unauthorized users

**Levels:**
1. **Authentication** - User logged in?
2. **Authorization** - User has role?
3. **Ownership** - User owns resource? (backend enforces)

---

## Performance Optimizations

### Code Splitting

**Automatic:**
- Route-level splitting via `() => import()`
- Each page is separate chunk
- Loaded on demand

### Reactive Performance

**Best Practices:**
- Use `computed()` for derived state
- Avoid deeply nested reactivity
- Use `readonly()` to prevent mutations
- `markRaw()` for non-reactive data

### API Calls

**Strategies:**
- Cache in composable state
- Don't refetch unnecessarily
- Debounce user inputs
- Use loading states

---

## Testing Strategy

### Manual Testing

**Critical Paths:**
1. Registration → Login → Browse → Cart → Checkout
2. Login → Profile → Update → Save
3. Admin → Dashboard → Bills → Update Status

**Test Cases:**
- Valid and invalid inputs
- Network errors
- Session expiration
- Concurrent operations

### Integration Points

**Frontend ↔ Backend:**
- Auth endpoints
- Cart operations
- Bill creation
- Profile updates

**Contract:**
- Must match backend API spec
- Check response shapes
- Handle all error codes

---

## Deployment

### Build Process

```bash
npm run build
```

**Output:**
- `dist/` directory
- Optimized assets
- Code splitting
- Minification

### Environment Variables

**Required:**
- `VITE_API_BASE` - Backend URL

**Optional:**
- `VITE_APP_TITLE` - Application title
- `VITE_ENV` - Environment name

### Hosting

**Static hosting options:**
- Vercel (configured via `vercel.json`)
- Netlify
- GitHub Pages
- S3 + CloudFront

**Requirements:**
- SPA routing support (redirect to index.html)
- HTTPS enabled
- CORS configured on backend

---

## Maintenance

### Adding New Features

1. **Create service** if API calls needed
2. **Create composable** if state needed
3. **Add constants** for endpoints/messages
4. **Create components** for UI
5. **Add routes** with guards
6. **Update documentation**

### Refactoring Guidelines

**Safe changes:**
- Extract to composable
- Move to constants
- Add JSDoc
- Improve error handling

**Risky changes:**
- Change state shape
- Modify composable API
- Remove error handling
- Change route structure

---

## Glossary

- **Composable** - Reusable function using Composition API
- **Ref** - Reactive reference (Vue's reactivity primitive)
- **Computed** - Derived reactive value
- **Guard** - Route protection function
- **SPA** - Single Page Application
- **HMR** - Hot Module Replacement (instant updates)
- **CSRF** - Cross-Site Request Forgery
- **SSR** - Server-Side Rendering (not used here)
