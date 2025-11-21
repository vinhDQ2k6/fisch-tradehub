/**
 * Application-wide constants for the frontend.
 * Centralizes API endpoints, roles, and reusable strings.
 */

// API Endpoints
export const API_ENDPOINTS = {
    // Authentication
    AUTH: {
        LOGIN: '/api/auth/login',
        LOGOUT: '/api/auth/logout',
        REGISTER: '/api/auth/register',
        ME: '/api/auth/me',
    },
    // Cart operations
    CART: {
        BASE: '/api/cart',
        BY_FISH: (fishId) => `/api/cart/${fishId}`,
    },
    // Bills (orders)
    BILLS: {
        BASE: '/api/bills',
        CHECKOUT: '/api/bills/checkout',
        BY_ID: (id) => `/api/bills/${id}`,
        PAY: (id) => `/api/bills/${id}/pay`,
        CANCEL: (id) => `/api/bills/${id}/cancel`,
    },
    // Fish (products)
    FISH: {
        BASE: '/api/fish',
        BY_ID: (id) => `/api/fish/${id}`,
    },
    // User profile
    USER: {
        INFO: '/api/user/info',
    },
};

// User roles (must match backend)
export const ROLES = {
    USER: 'ROLE_USER',
    STAFF: 'ROLE_STAFF',
    ADMIN: 'ROLE_ADMIN',
};

// HTTP Status codes
export const HTTP_STATUS = {
    OK: 200,
    CREATED: 201,
    BAD_REQUEST: 400,
    UNAUTHORIZED: 401,
    FORBIDDEN: 403,
    NOT_FOUND: 404,
    CONFLICT: 409,
    SERVER_ERROR: 500,
};

// Error messages (user-friendly)
export const ERROR_MESSAGES = {
    NETWORK: 'Network error. Please check your connection.',
    INVALID_CREDENTIALS: 'Invalid credentials or session expired.',
    PERMISSION_DENIED: "You don't have permission to perform this action.",
    NOT_FOUND: 'Resource not found.',
    ALREADY_EXISTS: 'Conflict: the resource already exists.',
    VALIDATION_FAILED: 'Validation failed. Please review the form.',
    SERVER_ERROR: 'Server error. Please try again later.',
    UNEXPECTED: 'Unexpected error occurred.',
    LOGIN_REQUIRED: 'You must be logged in to perform this action.',
};

// Success messages
export const SUCCESS_MESSAGES = {
    LOGIN: 'Successfully logged in!',
    LOGOUT: 'Successfully logged out!',
    REGISTER: 'Account created successfully!',
    PROFILE_UPDATED: 'Profile updated successfully!',
    ITEM_ADDED: 'Item added to cart!',
    ITEM_REMOVED: 'Item removed from cart!',
    CART_CLEARED: 'Cart cleared!',
    ORDER_PLACED: 'Order placed successfully!',
    ORDER_PAID: 'Payment processed successfully!',
    ORDER_CANCELLED: 'Order cancelled!',
};

// Toast lifetimes (milliseconds)
export const TOAST_DURATION = {
    SHORT: 3000,
    NORMAL: 4000,
    LONG: 6000,
};

// Validation rules
export const VALIDATION = {
    USERNAME: {
        MIN_LENGTH: 3,
        MAX_LENGTH: 50,
    },
    PASSWORD: {
        MIN_LENGTH: 6,
        MAX_LENGTH: 100,
    },
    EMAIL: {
        PATTERN: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
    },
};

// Route names (for programmatic navigation)
export const ROUTE_NAMES = {
    // Public routes
    LANDING: 'landing',
    FISHES: 'fishes',
    
    // Auth routes
    LOGIN: 'login',
    REGISTER: 'register',
    PROFILE: 'profile',
    ACCESS_DENIED: 'accessDenied',
    ERROR: 'error',
    
    // User routes
    MY_ORDERS: 'myOrders',
    
    // Admin routes
    DASHBOARD: 'dashboard',
    ADMIN_BILLS: 'adminBills',
    
    // Utility routes
    NOT_FOUND: 'notfound',
};

// Route paths (for URL construction)
export const ROUTE_PATHS = {
    // Public
    HOME: '/',
    FISHES: '/fishes',
    
    // Auth
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    PROFILE: '/auth/profile',
    ACCESS_DENIED: '/auth/access',
    ERROR: '/auth/error',
    
    // User
    MY_ORDERS: '/my/orders',
    
    // Admin
    ADMIN: '/admin',
    DASHBOARD: '/admin/dashboard',
    ADMIN_BILLS: '/admin/bills',
    
    // Utility
    NOT_FOUND: '/not-found',
};

// Local storage keys
export const STORAGE_KEYS = {
    THEME: 'app-theme',
    LANGUAGE: 'app-language',
};

/**
 * Check if user has required role.
 * @param {Object} user - User object with roles array
 * @param {string|string[]} requiredRoles - Required role(s)
 * @returns {boolean} True if user has at least one required role
 */
export function hasRole(user, requiredRoles) {
    if (!user || !user.roles) return false;
    const roles = Array.isArray(requiredRoles) ? requiredRoles : [requiredRoles];
    return roles.some(role => user.roles.includes(role));
}

/**
 * Check if user is admin.
 * @param {Object} user - User object
 * @returns {boolean} True if user is admin
 */
export function isAdmin(user) {
    return hasRole(user, ROLES.ADMIN);
}

/**
 * Check if user is staff or admin.
 * @param {Object} user - User object
 * @returns {boolean} True if user is staff or admin
 */
export function isStaffOrAdmin(user) {
    return hasRole(user, [ROLES.STAFF, ROLES.ADMIN]);
}
