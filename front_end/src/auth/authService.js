import { apiFetch } from "./fetchClient";

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
    const url = `${trimmed}/api/auth/login`;

    const res = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: form.toString(),
        credentials: "include",
    });

    // On success, cookies are set; fetch user via /api/auth/me
    if (!res.ok) {
        const data = await res.json().catch(() => null);
        throw {
            status: res.status,
            data,
            message: data?.message || "Login failed",
        };
    }

    // Immediately hydrate user from backend
    const { data } = await currentUser(); // re-use your apiFetch-based currentUser
    return data;
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
