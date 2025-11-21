import { apiFetch } from "./fetchClient";
import { API_ENDPOINTS } from "@/common/constants";

/**
 * Authenticate user with credentials.
 * @param {Object} credentials - Login credentials
 * @param {string} credentials.username - Username
 * @param {string} credentials.password - Password
 * @param {boolean} [credentials.rememberMe=false] - Remember me option
 * @returns {Promise<Object>} User data after successful login
 * @throws {Object} Error with status, data, and message
 */
export async function login({ username, password, rememberMe }) {
    const form = new URLSearchParams();
    form.set("username", username);
    form.set("password", password);
    if (rememberMe) {
        form.set("remember-me", "on");
    }

    const base = import.meta.env.VITE_API_BASE;
    if (!base) throw new Error("Missing: VITE_API_BASE");
    const trimmed = base.endsWith("/") ? base.slice(0, -1) : base;
    const url = `${trimmed}${API_ENDPOINTS.AUTH.LOGIN}`;

    const res = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: form.toString(),
        credentials: "include",
    });

    if (!res.ok) {
        const data = await res.json().catch(() => null);
        throw {
            status: res.status,
            data,
            message: data?.message || "Login failed",
        };
    }

    // Fetch user data after successful login
    const { data } = await currentUser();
    return data;
}

/**
 * Logout current user and clear session.
 * @returns {Promise<Object>} Logout response
 */
export async function logout() {
    return apiFetch(API_ENDPOINTS.AUTH.LOGOUT, { method: "POST" });
}

/**
 * Get current authenticated user information.
 * @returns {Promise<Object>} Response with user data
 * @throws {Object} Error if user not authenticated
 */
export async function currentUser() {
    return apiFetch(API_ENDPOINTS.AUTH.ME, { method: "GET", csrf: false });
}

/**
 * Register a new user account.
 * @param {Object} payload - Registration data
 * @param {string} payload.username - Desired username
 * @param {string} payload.email - Email address
 * @param {string} payload.password - Password
 * @returns {Promise<Object>} Response with created user data
 * @throws {Object} Error if registration fails (e.g., username/email exists)
 */
export async function register(payload) {
    return apiFetch(API_ENDPOINTS.AUTH.REGISTER, { method: "POST", body: payload });
}
