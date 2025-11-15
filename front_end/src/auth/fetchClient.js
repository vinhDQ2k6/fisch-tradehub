import { getCsrfToken } from "./csrf";

const JSON_HEADERS = { "Content-Type": "application/json" };

function normalizeBase(base) {
    if (!base) throw new Error("Missing: VITE_API_BASE");
    // remove trailing slash
    return base.endsWith("/") ? base.slice(0, -1) : base;
}

function normalizePath(path) {
    if (!path) throw new Error("apiFetch: path is required");
    return path.startsWith("/") ? path : `/${path}`;
}

function buildUrl(path) {
    if (!path) throw new Error("apiFetch: path is required");
    if (path.startsWith("http")) return path;
    const base = normalizeBase(import.meta.env.VITE_API_BASE);
    const p = normalizePath(path);
    return `${base}${p}`;
}

export async function apiFetch(path, opts = {}) {
    const {
        method = "GET",
        headers = {},
        body,
        csrf = true, // attach CSRF header for mutating requests
    } = opts;

    const url = buildUrl(path);
    const finalHeaders = { ...headers };

    // Only set JSON content-type if body is a plain object and caller didn't override
    const isJsonBody =
        body && typeof body === "object" && !(body instanceof FormData);
    if (isJsonBody && !finalHeaders["Content-Type"]) {
        Object.assign(finalHeaders, JSON_HEADERS);
    }

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
