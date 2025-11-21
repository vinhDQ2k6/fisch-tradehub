import { ref } from "vue";
import {
    currentUser as svcCurrentUser,
    login as svcLogin,
    logout as svcLogout,
} from "./authService";

/**
 * Authentication composable for managing user state.
 * Provides singleton state and methods for login, logout, and user data.
 * 
 * @returns {Object} Authentication state and methods
 * @property {Ref<Object|null>} user - Current user data (null if not logged in)
 * @property {Ref<boolean>} loading - Loading state for auth operations
 * @property {Ref<boolean>} ready - Whether auth state has been initialized
 * @property {Function} init - Initialize auth state by fetching current user
 * @property {Function} doLogin - Login with credentials
 * @property {Function} doLogout - Logout current user
 * @property {Function} isLoggedIn - Check if user is logged in
 */

// Module-scoped singleton state
const user = ref(null);
const loading = ref(false);
const ready = ref(false);

export function useAuth() {
    /**
     * Initialize authentication state.
     * Fetches current user from backend if session exists.
     * Should be called once on app startup.
     * 
     * @returns {Promise<void>}
     */
    async function init() {
        if (ready.value) return;
        try {
            const { data } = await svcCurrentUser();
            user.value = data;
        } catch (_) {
            user.value = null;
        } finally {
            ready.value = true;
        }
    }

    /**
     * Login user with credentials.
     * 
     * @param {Object} credentials - Login credentials
     * @param {string} credentials.username - Username
     * @param {string} credentials.password - Password
     * @param {boolean} [credentials.rememberMe=false] - Remember me option
     * @returns {Promise<Object>} User data after successful login
     * @throws {Object} Error if login fails
     */
    async function doLogin({ username, password, rememberMe }) {
        loading.value = true;
        try {
            const data = await svcLogin({ username, password, rememberMe });
            user.value = data;
            return data;
        } finally {
            loading.value = false;
        }
    }

    /**
     * Logout current user and clear local state.
     * 
     * @returns {Promise<void>}
     */
    async function doLogout() {
        try {
            await svcLogout();
        } finally {
            user.value = null;
        }
    }

    /**
     * Check if user is currently logged in.
     * 
     * @returns {boolean} True if user is logged in
     */
    function isLoggedIn() {
        return !!user.value;
    }

    return { user, loading, ready, init, doLogin, doLogout, isLoggedIn };
}
