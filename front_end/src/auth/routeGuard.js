import { useAuth } from "./useAuth";

export function createAuthGuard(router) {
    router.beforeEach(async (to) => {
        const { init, isLoggedIn, user } = useAuth();
        await init();

        if (to.meta?.requiresAuth && !isLoggedIn()) {
            return { path: "/auth/login", query: { redirect: to.fullPath } };
        }
        if (to.meta?.roles && to.meta.roles.length) {
            const roles = user.value?.roles || [];
            const allowed = to.meta.roles.some((r) => roles.includes(r));
            if (!allowed) return { path: "/auth/error", query: { code: 403 } };
        }
        return true;
    });
}
