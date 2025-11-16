import { apiFetch } from "./fetchClient";

export async function login({ username, password, rememberMe }) {
    const form = new URLSearchParams({
        username,
        password,
        "remember-me": String(!!rememberMe),
    });
    function buildApiUrl(path) {
        const base = import.meta.env.VITE_API_BASE;
        if (!base) throw new Error("Missing: VITE_API_BASE");
        const trimmedBase = base.endsWith("/") ? base.slice(0, -1) : base;
        const p = path.startsWith("/") ? path : `/${path}`;
        return `${trimmedBase}${p}`;
    }
    const url = buildApiUrl("/api/auth/login");
    const res = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: form.toString(),
        credentials: "include",
    });
    const data = await res.json().catch(() => null);
    if (!res.ok) {
        throw {
            status: res.status,
            data,
            message: data?.message || "Login failed",
        };
    }
    return data; // { username, roles, ... }
}

export async function logout() {
    return apiFetch("/api/auth/logout", { method: "POST" });
}

export async function currentUser() {
    return apiFetch("/api/auth/me", { method: "GET", csrf: false });
}

export async function register(payload) {
    // payload: { username, email, password, ... }
    return apiFetch("/api/auth/register", { method: "POST", body: payload });
}
