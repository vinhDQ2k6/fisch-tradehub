# Fisch TradeHub Frontend

A modern Vue 3 single-page application for the fish shop trading system with PrimeVue UI components.

## Overview

The frontend is built with:

- **Vue 3** with Composition API
- **PrimeVue 4** - Enterprise-grade UI components
- **Vue Router** - Client-side routing with guards
- **Vite** - Fast build tool and dev server
- **TailwindCSS** - Utility-first CSS framework

## Architecture

### Project Structure

```
src/
├── auth/                  # Authentication module
│   ├── authService.js    # API calls for auth
│   ├── useAuth.js        # Auth state composable
│   ├── fetchClient.js    # HTTP client with CSRF
│   ├── handleError.js    # Error handling utilities
│   ├── routeGuard.js     # Route protection
│   └── csrf.js           # CSRF token management
├── cart/                  # Shopping cart module
│   └── useCart.js        # Cart state composable
├── common/                # Shared utilities
│   └── constants.js      # API endpoints, roles, messages
├── components/            # Reusable UI components
│   ├── dashboard/        # Dashboard widgets
│   └── landing/          # Landing page sections
├── layout/                # Layout components
│   ├── AppLayoutPrivate.vue    # Authenticated layout
│   ├── AppLayoutPublic.vue     # Public layout
│   ├── AppMenuPrivate.vue      # Navigation menu
│   └── composables/            # Layout composables
├── router/                # Route configuration
│   └── index.js          # Routes and navigation
├── service/               # API services (demo data)
├── views/                 # Page components
│   ├── pages/            # Feature pages
│   │   ├── auth/         # Authentication pages
│   │   ├── FischTrade.vue
│   │   ├── Debts.vue
│   │   └── Bills.vue
│   └── Dashboard.vue
├── App.vue                # Root component
└── main.js                # Application entry point
```

### Design Patterns

#### Composition API Pattern

All state management uses Vue 3 Composition API:

- **Composables** (`useAuth`, `useCart`) - Reusable reactive state
- **Singleton pattern** - Shared state across components
- **Dependency injection** - Clean separation of concerns

#### State Management

- **Module-scoped singletons** - No Vuex/Pinia needed
- **Reactive refs** - Vue's built-in reactivity
- **Computed properties** - Derived state
- **Watchers** - Side effects and synchronization

#### Error Handling

- **Centralized** - All errors through `handleError.js`
- **User-friendly** - HTTP status → readable messages
- **Toast notifications** - PrimeVue toast service
- **Consistent** - Same patterns everywhere

## Getting Started

### Prerequisites

- Node.js 16+
- npm or yarn
- Backend API running (see `back_end/README.md`)

### Environment Setup

Create `.env` file in `front_end/`:

```env
VITE_API_BASE=http://localhost:8080
```

### Installation

```bash
cd front_end
npm install
```

### Development

Start dev server with hot reload:

```bash
npm run dev
```

Application runs at `http://localhost:5173`

### Building

Build for production:

```bash
npm run build
```

Output in `dist/` directory.

### Linting

Run ESLint with auto-fix:

```bash
npm run lint
```

## Key Features

### Authentication

**Session-based authentication:**

- Login/logout with remember-me
- Session persistence via cookies
- Automatic session check on startup
- Protected routes with route guards

**Files:**

- `auth/authService.js` - API calls
- `auth/useAuth.js` - State management
- `auth/routeGuard.js` - Route protection

### Shopping Cart

**Reactive cart state:**

- Add/remove items
- Quantity management
- Real-time total calculation
- Persists in backend

**Files:**

- `cart/useCart.js` - Cart composable

### Route Protection

**Guard system:**

- Check authentication status
- Verify user roles
- Redirect to login or access denied
- Remember intended destination

**Configuration:**

```javascript
{
  path: '/dashboard',
  meta: {
    requiresAuth: true,
    roles: ['ADMIN']
  }
}
```

### Error Handling

**Consistent error display:**

- HTTP status → user message mapping
- Validation error extraction
- Network error detection
- Toast notifications

**Usage:**

```javascript
import { showError, showSuccess } from "@/auth/handleError";

try {
    await apiCall();
    showSuccess(toast, "Operation successful!");
} catch (err) {
    showError(toast, err);
}
```

## API Integration

### API Client

**Features:**

- Automatic JSON handling
- CSRF token injection
- Credential inclusion (cookies)
- Error standardization

**Usage:**

```javascript
import { apiFetch } from "@/auth/fetchClient";

// GET request
const { data } = await apiFetch("/api/fish");

// POST request
await apiFetch("/api/cart", {
    method: "POST",
    body: { fishId: 1, quantity: 2 },
});
```

### Constants

All API endpoints centralized in `common/constants.js`:

```javascript
import { API_ENDPOINTS } from "@/common/constants";

await apiFetch(API_ENDPOINTS.CART.BASE);
await apiFetch(API_ENDPOINTS.BILLS.BY_ID(123));
```

## Components

### Layouts

**AppLayoutPublic.vue**

- Public pages (landing, fish list)
- No authentication required
- Marketing-focused design

**AppLayoutPrivate.vue**

- Protected pages (dashboard, bills)
- Requires authentication
- Application-focused design

### Pages

**Landing** - Homepage with features and pricing  
**FischTrade** - Browse and shop for fish  
**Debts (Bills)** - View order history (user)  
**Dashboard** - Admin dashboard with analytics  
**Bills** - Manage all orders (admin)  
**Profile** - User profile management  
**Login/Register** - Authentication pages

## Styling

### TailwindCSS + PrimeVue

**Approach:**

- Utility-first with Tailwind
- Component library with PrimeVue
- Theme customization via `tailwindcss-primeui`
- Responsive by default

**Theme:**

- Configured in `vite.config.mjs`
- Uses PrimeVue themes
- Dark/light mode support

## Development Guidelines

### Code Style

**Follow existing patterns:**

1. Use Composition API over Options API
2. Extract reusable logic to composables
3. Add JSDoc to all public functions
4. Use constants instead of magic strings
5. Handle errors consistently

### Creating Composables

```javascript
/**
 * Description of composable.
 *
 * @returns {Object} Composable state and methods
 */
export function useFeature() {
    const state = ref(null);

    /**
     * Method description.
     * @param {string} param - Parameter description
     * @returns {Promise<void>}
     */
    async function method(param) {
        // Implementation
    }

    return { state, method };
}
```

### Adding Constants

Add to `common/constants.js`:

```javascript
export const API_ENDPOINTS = {
    NEW_FEATURE: {
        BASE: "/api/feature",
        BY_ID: (id) => `/api/feature/${id}`,
    },
};
```

### Error Messages

Use constants for consistency:

```javascript
import { ERROR_MESSAGES, SUCCESS_MESSAGES } from "@/common/constants";

showError(toast, ERROR_MESSAGES.LOGIN_REQUIRED);
showSuccess(toast, SUCCESS_MESSAGES.ORDER_PLACED);
```

## Testing

### Manual Testing

1. Start backend: `cd back_end/tradehub_core && ./mvnw spring-boot:run`
2. Start frontend: `cd front_end && npm run dev`
3. Open `http://localhost:5173`
4. Test authentication flow
5. Test cart operations
6. Test checkout process

### Integration Points

**Critical paths:**

- Register → Login → Browse → Add to Cart → Checkout → View Bill
- Admin → Dashboard → View All Bills → Update Status
- User → Profile → Update Info

## Security

### CSRF Protection

**Automatic:**

- CSRF token stored in cookie
- Token sent with mutating requests
- Managed by `csrf.js` and `fetchClient.js`

### Authentication

**Session-based:**

- `JSESSIONID` cookie
- HTTP-only, secure flags
- Backend validates all requests

### Route Guards

**Protection:**

- Check auth status before rendering
- Verify user roles
- Redirect unauthorized users

## Performance

### Optimization

**Built-in:**

- Vite's fast HMR
- Code splitting per route
- Component lazy loading
- Tree-shaking

**Best practices:**

- Use `v-once` for static content
- Lazy load images
- Debounce user input
- Cache API responses in composables

## Troubleshooting

### Common Issues

**"Missing: VITE_API_BASE"**

- Create `.env` file with `VITE_API_BASE`
- Restart dev server

**CORS errors**

- Check backend CORS configuration
- Verify origin matches frontend URL

**401 Unauthorized**

- Session expired, login again
- Check backend is running
- Verify cookie settings

**Cart not updating**

- Check user is logged in
- Verify network requests in DevTools
- Check backend cart endpoint

## Documentation

- [Frontend Specification](FRONTEND_SPECIFICATION.md) - Technical details
- [Changelog](CHANGELOG.md) - Recent changes
- [Backend README](../back_end/README.md) - API documentation

## Contributing

**Before submitting:**

1. Run linter: `npm run lint`
2. Test manually
3. Add JSDoc to new functions
4. Update constants if needed
5. Follow existing code style

## License

Educational project for learning purposes.

## Support

For questions:

1. Check this README
2. Review [Frontend Specification](FRONTEND_SPECIFICATION.md)
3. Check browser DevTools console
4. Review network requests
5. Contact development team
