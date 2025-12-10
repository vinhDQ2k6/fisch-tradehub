# Frontend Directory Structure

This document explains the organized directory structure for the frontend application.

## Overview

The frontend follows a clear, organized structure optimized for maintainability and clarity. Each directory has a specific purpose and contains related files.

## Directory Organization

```
src/
├── auth/                   # Authentication & authorization
│   ├── authService.js     # API calls for auth operations
│   ├── csrf.js            # CSRF token management
│   ├── fetchClient.js     # HTTP client with security features
│   ├── handleError.js     # Error handling utilities
│   ├── routeGuard.js      # Route protection logic
│   └── useAuth.js         # Auth state composable
│
├── cart/                   # Shopping cart functionality
│   └── useCart.js         # Cart state composable
│
├── common/                 # Shared utilities & constants
│   └── constants.js       # API endpoints, roles, messages
│
├── components/             # Reusable UI components
│   ├── dashboard/         # Dashboard-specific widgets
│   └── landing/           # Landing page sections
│
├── layout/                 # Application layouts
│   ├── AppLayoutPrivate.vue    # Authenticated user layout
│   ├── AppLayoutPublic.vue     # Public pages layout
│   ├── AppMenuPrivate.vue      # Navigation for authenticated users
│   ├── AppTopbarPrivate.vue    # Top bar for authenticated users
│   ├── AppTopbarPublic.vue     # Top bar for public pages
│   └── composables/            # Layout-related composables
│
├── router/                 # Route configuration
│   └── index.js           # Router setup with organized routes
│
├── service/                # Demo/example services (from template)
│   ├── CountryService.js
│   ├── CustomerService.js
│   ├── NodeService.js
│   ├── PhotoService.js
│   └── ProductService.js
│
├── views/                  # Page components (organized by role)
│   ├── admin/             # Admin-only pages
│   │   ├── Bills.vue      # Manage all orders
│   │   └── Dashboard.vue  # Admin dashboard
│   │
│   ├── demo/              # Demo/template pages (unused)
│   │   ├── Crud.vue
│   │   ├── Documentation.vue
│   │   └── Empty.vue
│   │
│   ├── pages/
│   │   └── auth/          # Authentication pages
│   │       ├── Access.vue     # Access denied page
│   │       ├── Error.vue      # Error page
│   │       ├── Login.vue      # Login page
│   │       ├── Profile.vue    # User profile
│   │       └── Register.vue   # Registration page
│   │
│   ├── public/            # Public pages (no auth required)
│   │   ├── FischTrade.vue # Browse & shop for fish
│   │   ├── Landing.vue    # Homepage
│   │   └── NotFound.vue   # 404 error page
│   │
│   ├── uikit/             # UI kit demo pages (from template)
│   │   └── [various demo components]
│   │
│   ├── user/              # User-authenticated pages
│   │   └── Debts.vue      # User's order history
│   │
│   └── utilities/         # Utility demo pages (from template)
│
├── App.vue                 # Root application component
└── main.js                 # Application entry point
```

## Route Organization

Routes are organized by access level and purpose:

### 1. Public Routes (`/`)

- **Landing** (`/`) - Homepage
- **Fishes** (`/fishes`) - Browse fish catalog

### 2. Authentication Routes (`/auth`)

- **Login** (`/auth/login`) - User login
- **Register** (`/auth/register`) - New user registration
- **Profile** (`/auth/profile`) - User profile (requires auth)
- **Access** (`/auth/access`) - Access denied page
- **Error** (`/auth/error`) - Error page

### 3. User Routes (`/my`)

- **Orders** (`/my/orders`) - User's order history

### 4. Admin Routes (`/admin`)

- **Dashboard** (`/admin/dashboard`) - Analytics & overview
- **Bills** (`/admin/bills`) - Manage all orders

### 5. Utility Routes

- **Not Found** (`/not-found`) - 404 error page
- **Catch-all** (`/*`) - Redirects to not found

## File Naming Conventions

### Components

- **PascalCase** for component files: `MyComponent.vue`
- **Descriptive names**: `BestSellingWidget.vue`, `RecentSalesWidget.vue`

### JavaScript Files

- **camelCase** for service files: `authService.js`, `fetchClient.js`
- **Prefixed with "use"** for composables: `useAuth.js`, `useCart.js`

### Directories

- **lowercase** with hyphens if needed
- **Plural** for collections: `components/`, `views/`
- **Singular** for single-purpose: `auth/`, `cart/`, `router/`

## Module Organization Principles

### By Feature

Core features have their own directories:

- `auth/` - Everything authentication-related
- `cart/` - Everything cart-related

### By Role

Views are organized by access level:

- `public/` - No authentication required
- `user/` - User authentication required
- `admin/` - Admin role required
- `pages/auth/` - Authentication-related pages

### By Type

- `components/` - Reusable UI components
- `layout/` - Application layouts and navigation
- `service/` - API services and data fetching

## Best Practices

### Adding New Pages

1. **Determine access level**: public, user, or admin?
2. **Place in appropriate directory**:
   - Public → `views/public/`
   - User → `views/user/`
   - Admin → `views/admin/`
3. **Update router** in `router/index.js`
4. **Use constants** for route names from `common/constants.js`

### Adding New Features

1. Create feature directory: `src/feature-name/`
2. Add service file if API calls needed
3. Add composable if state management needed
4. Add components in `components/feature-name/`
5. Add routes in appropriate section

### Refactoring Guidelines

- Keep related files together
- Use clear, descriptive names
- Follow existing patterns
- Document in this file if adding new categories

## Demo Files

The following directories contain demo/template files that can be ignored or removed:

- `views/demo/` - Demo pages from template
- `views/uikit/` - UI kit documentation pages
- `views/utilities/` - Utility demo pages
- `service/` - Demo services (ProductService, etc.)

These files are kept for reference but are not part of the core application.

## Migration Notes

### Recent Changes

**Route Reorganization:**

- Changed `/debts` → `/my/orders` (clearer purpose)
- Changed `/dashboard` → `/admin/dashboard` (explicit admin namespace)
- Changed `/dashboard/bills` → `/admin/bills` (consistent admin namespace)
- Changed `/pages/notfound` → `/not-found` (cleaner URL)

**File Reorganization:**

- Moved view files into organized subdirectories
- Separated by access level (public, user, admin)
- Authentication pages remain in `pages/auth/` for consistency

## Maintenance

This structure should be maintained going forward:

1. **New pages** - Place in appropriate access level directory
2. **New features** - Create new feature directory if substantial
3. **Shared code** - Place in `common/` or appropriate module
4. **Documentation** - Update this file when adding new categories

---

**Last Updated:** 2025-11-21  
**Maintained by:** Development Team
