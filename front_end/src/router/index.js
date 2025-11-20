import { createAuthGuard } from "@/auth/routeGuard";
import AppLayoutPrivate from "@/layout/AppLayoutPrivate.vue";
import AppLayoutPublic from "@/layout/AppLayoutPublic.vue";
import { createRouter, createWebHistory } from "vue-router";

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: "/dashboard",
            component: AppLayoutPrivate,
            meta: { requiresAuth: true, roles: ["ADMIN"] },
            children: [
                {
                    path: "/dashboard",
                    name: "dashboard",
                    component: () => import("@/views/Dashboard.vue"),
                },
                {
                    path: "/dashboard/bills",
                    name: "bills",
                    component: () => import("@/views/pages/Bills.vue"),
                },
            ],
        },
        {
            path: "/",
            component: AppLayoutPublic,
            children: [
                {
                    path: "/",
                    name: "landing",
                    component: () => import("@/views/pages/Landing.vue"),
                },
                {
                    path: "/fishes",
                    name: "fishes",
                    component: () => import("@/views/pages/FischTrade.vue"),
                },
                {
                    path: "/debts",
                    name: "debts",
                    component: () => import("@/views/pages/Debts.vue"),
                    meta: { requiresAuth: true },
                },
            ],
        },
        {
            path: "/pages/notfound",
            name: "notfound",
            component: () => import("@/views/pages/NotFound.vue"),
        },
        {
            path: "/auth/login",
            name: "login",
            component: () => import("@/views/pages/auth/Login.vue"),
        },
        {
            path: "/auth/register",
            name: "register",
            component: () => import("@/views/pages/auth/Register.vue"),
        },
        {
            path: "/auth/profile",
            name: "profile",
            component: () => import("@/views/pages/auth/Profile.vue"),
            meta: { requiresAuth: true },
        },
        {
            path: "/auth/access",
            name: "accessDenied",
            component: () => import("@/views/pages/auth/Access.vue"),
        },
        {
            path: "/auth/error",
            name: "error",
            component: () => import("@/views/pages/auth/Error.vue"),
        },
        {
            path: "/:pathMatch(.*)*",
            redirect: "/pages/notfound",
        },
    ],
});

createAuthGuard(router);
export default router;
