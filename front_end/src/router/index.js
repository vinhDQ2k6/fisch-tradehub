import AppLayoutPrivate from "@/layout/AppLayoutPrivate.vue";
import AppLayoutPublic from "@/layout/AppLayoutPublic.vue";
import { createRouter, createWebHistory } from "vue-router";

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: "/dashboard",
            component: AppLayoutPrivate,
            children: [
                {
                    path: "/dashboard",
                    name: "dashboard",
                    component: () => import("@/views/Dashboard.vue"),
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
            path: "/auth/access",
            name: "accessDenied",
            component: () => import("@/views/pages/auth/Access.vue"),
        },
        {
            path: "/auth/error",
            name: "error",
            component: () => import("@/views/pages/auth/Error.vue"),
        },
    ],
});

export default router;
