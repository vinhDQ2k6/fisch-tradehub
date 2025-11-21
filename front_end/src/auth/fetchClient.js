import { getCsrfToken } from "./csrf";

/**
 * Fetch client for making API requests with automatic JSON handling,
 * CSRF protection, and credential inclusion.
 */

const JSON_HEADERS = { "Content-Type": "application/json" };

/**
 * Normalize API base URL by removing trailing slash.
 * @param {string} base - Base URL
 * @returns {string} Normalized URL
 * @throws {Error} If base URL is missing
 */
function normalizeBase(base) {
    if (!base) throw new Error("Missing: VITE_API_BASE");
    return base.endsWith("/") ? base.slice(0, -1) : base;
}

/**
 * Normalize path by ensuring it starts with slash.
 * @param {string} path - API path
 * @returns {string} Normalized path
 * @throws {Error} If path is missing
 */
function normalizePath(path) {
    if (!path) throw new Error("apiFetch: path is required");
    return path.startsWith("/") ? path : `/${path}`;
}

/**
 * Build full API URL from path.
 * @param {string} path - API path or full URL
 * @returns {string} Complete URL
 * @throws {Error} If path is missing
 */
function buildUrl(path) {
    if (!path) throw new Error("apiFetch: path is required");
    if (path.startsWith("http")) return path;
    const base = normalizeBase(import.meta.env.VITE_API_BASE);
    const p = normalizePath(path);
    return `${base}${p}`;
}

/**
 * Make an API request with automatic JSON handling and CSRF protection.
 * 
 * @param {string} path - API endpoint path or full URL
 * @param {Object} [opts={}] - Request options
 * @param {string} [opts.method='GET'] - HTTP method
 * @param {Object} [opts.headers={}] - Additional headers
 * @param {Object|FormData} [opts.body] - Request body (auto-stringified if object)
 * @param {boolean} [opts.csrf=true] - Whether to include CSRF token
 * @returns {Promise<Object>} Response with status and data
 * @throws {Object} Error with status, data, and message
 * 
 * @example
 * // GET request
 * const { data } = await apiFetch('/api/fish');
 * 
 * @example
 * // POST request with body
 * const { data } = await apiFetch('/api/cart', {
 *   method: 'POST',
 *   body: { fishId: 1, quantity: 2 }
 * });
 */
export async function apiFetch(path, opts = {}) {
    const {
        method = "GET",
        headers = {},
        body,
        csrf = true,
    } = opts;

    const url = buildUrl(path);
    const finalHeaders = { ...headers };

    // Set JSON content-type if body is a plain object
    const isJsonBody =
        body && typeof body === "object" && !(body instanceof FormData);
    if (isJsonBody && !finalHeaders["Content-Type"]) {
        Object.assign(finalHeaders, JSON_HEADERS);
    }

    // Add CSRF token for mutating requests
    if (csrf && method !== "GET" && method !== "HEAD") {
        const token = getCsrfToken();
        if (token) finalHeaders["X-XSRF-TOKEN"] = token;
    }

    const response = await fetch(url, {
        method,
        headers: finalHeaders,
        body: isJsonBody ? JSON.stringify(body) : body,
        credentials: "include",
    });

    const contentType = response.headers.get("Content-Type") || "";
    let data = null;
    if (contentType.includes("application/json")) {
        try {
            data = await response.json();
        } catch {}
    } else {
        try {
            data = await response.text();
        } catch {}
    }

    if (!response.ok) {
        const message =
            (data && data.message) ||
            `Request failed with status ${response.status}`;
        throw { status: response.status, data, message };
    }
    return { status: response.status, data };
}
