import { createAuthGuard } from "@/auth/routeGuard";
import AppLayoutPrivate from "@/layout/AppLayoutPrivate.vue";
import AppLayoutPublic from "@/layout/AppLayoutPublic.vue";
import { createRouter, createWebHistory } from "vue-router";

/**
 * Application router configuration.
 * Routes are organized by category for maintainability:
 * 1. Public routes (no auth required)
 * 2. Authentication routes
 * 3. User protected routes
 * 4. Admin protected routes
 * 5. Error/utility routes
 */
const router = createRouter({
    history: createWebHistory(),
    routes: [
        // ============================================================
        // PUBLIC ROUTES (No authentication required)
        // ============================================================
        {
            path: "/",
            component: AppLayoutPublic,
            children: [
                {
                    path: "",
                    name: "landing",
                    component: () => import("@/views/public/Landing.vue"),
                },
                {
                    path: "fishes",
                    name: "fishes",
                    component: () => import("@/views/public/FischTrade.vue"),
                },
            ],
        },

        // ============================================================
        // AUTHENTICATION ROUTES
        // ============================================================
        {
            path: "/auth",
            children: [
                {
                    path: "login",
                    name: "login",
                    component: () => import("@/views/pages/auth/Login.vue"),
                },
                {
                    path: "register",
                    name: "register",
                    component: () => import("@/views/pages/auth/Register.vue"),
                },
                {
                    path: "profile",
                    name: "profile",
                    component: () => import("@/views/pages/auth/Profile.vue"),
                    meta: { requiresAuth: true },
                },
                {
                    path: "access",
                    name: "accessDenied",
                    component: () => import("@/views/pages/auth/Access.vue"),
                },
                {
                    path: "error",
                    name: "error",
                    component: () => import("@/views/pages/auth/Error.vue"),
                },
            ],
        },

        // ============================================================
        // USER PROTECTED ROUTES (Authentication required)
        // ============================================================
        {
            path: "/my",
            component: AppLayoutPublic,
            meta: { requiresAuth: true },
            children: [
                {
                    path: "orders",
                    name: "myOrders",
                    component: () => import("@/views/user/Debts.vue"),
                },
            ],
        },

        // ============================================================
        // ADMIN ROUTES (Admin role required)
        // ============================================================
        {
            path: "/admin",
            component: AppLayoutPrivate,
            meta: { requiresAuth: true, roles: ["ADMIN"] },
            children: [
                {
                    path: "",
                    redirect: "/admin/dashboard",
                },
                {
                    path: "dashboard",
                    name: "dashboard",
                    component: () => import("@/views/admin/Dashboard.vue"),
                },
                {
                    path: "bills",
                    name: "adminBills",
                    component: () => import("@/views/admin/Bills.vue"),
                },
            ],
        },

        // ============================================================
        // ERROR & UTILITY ROUTES
        // ============================================================
        {
            path: "/not-found",
            name: "notfound",
            component: () => import("@/views/public/NotFound.vue"),
        },
        {
            path: "/:pathMatch(.*)*",
            redirect: "/not-found",
        },
    ],
});

createAuthGuard(router);
export default router;
