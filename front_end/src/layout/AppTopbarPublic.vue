<script setup>
import { useLayout } from "@/layout/composables/layout";
import { onBeforeUnmount, onMounted, ref } from "vue";
import AppConfigurator from "./AppConfigurator.vue";

const { toggleDarkMode, isDarkTheme } = useLayout();

function smoothScroll(id) {
    document.body.click();
    const element = document.getElementById(id);
    if (element) {
        element.scrollIntoView({
            behavior: "smooth",
            block: "start",
        });
    }
}

// Simple auth detection: consider logged in if a common token is present in localStorage or cookies.
const isLogged = ref(false);

const checkAuth = () => {
    try {
        isLogged.value = Boolean(
            localStorage.getItem("accessToken") ||
                localStorage.getItem("token") ||
                document.cookie.includes("JSESSIONID") ||
                document.cookie.includes("XSRF-TOKEN"),
        );
    } catch (e) {
        isLogged.value = false;
    }
};

onMounted(() => {
    checkAuth();
    // update when other tabs change auth state
    window.addEventListener("storage", checkAuth);
});

onBeforeUnmount(() => {
    window.removeEventListener("storage", checkAuth);
});
</script>

<template>
    <div class="layout-topbar">
        <div class="layout-topbar-logo-container">
            <router-link to="/" class="layout-topbar-logo">
                <img
                    src="https://static.wikitide.net/fischwiki/thumb/b/bf/WikiIcon16.png/450px-WikiIcon16.png"
                    alt="Logo"
                    class="h-12 mr-2"
                />
                <span
                    class="text-surface-900 dark:text-surface-0 font-medium text-2xl leading-normal mr-20 whitespace-nowrap"
                    >Fisch-TradeHub</span
                >
            </router-link>
        </div>

        <div
            class="layout-topbar-actions items-center bg-surface-0 dark:bg-surface-900 grow justify-between hidden lg:flex absolute lg:static w-full left-0 top-full px-12 lg:px-0 z-20 rounded-border"
        >
            <ul
                class="list-none p-0 m-0 flex lg:items-center select-none flex-col lg:flex-row cursor-pointer gap-8"
            >
                <li>
                    <a
                        @click="smoothScroll('hero')"
                        class="px-0 py-4 text-surface-900 dark:text-surface-0 font-medium text-xl"
                    >
                        <span>Home</span>
                    </a>
                </li>
                <li>
                    <a
                        @click="smoothScroll('footer')"
                        class="px-0 py-4 text-surface-900 dark:text-surface-0 font-medium text-xl"
                    >
                        <span>Footer</span>
                    </a>
                </li>
            </ul>

            <button
                class="layout-topbar-menu-button layout-topbar-action"
                v-styleclass="{
                    selector: '@next',
                    enterFromClass: 'hidden',
                    enterActiveClass: 'animate-scalein',
                    leaveToClass: 'hidden',
                    leaveActiveClass: 'animate-fadeout',
                    hideOnOutsideClick: true,
                }"
            >
                <i class="pi pi-ellipsis-v"></i>
            </button>

            <div class="layout-topbar-menu hidden lg:block">
                <div class="layout-topbar-menu-content">
                    <div
                        class="flex border-t lg:border-t-0 border-surface py-4 lg:py-0 mt-4 lg:mt-0 gap-2"
                    >
                        <template v-if="!isLogged">
                            <Button
                                label="Login"
                                text
                                as="router-link"
                                to="/auth/login"
                                rounded
                            ></Button>
                            <Button
                                label="Register"
                                as="router-link"
                                to="/auth/register"
                                rounded
                            ></Button>
                        </template>
                    </div>

                    <div class="layout-config-menu">
                        <button
                            type="button"
                            class="layout-topbar-action"
                            @click="toggleDarkMode"
                        >
                            <i
                                :class="[
                                    'pi',
                                    {
                                        'pi-moon': isDarkTheme,
                                        'pi-sun': !isDarkTheme,
                                    },
                                ]"
                            ></i>
                        </button>
                        <div class="relative">
                            <button
                                v-styleclass="{
                                    selector: '@next',
                                    enterFromClass: 'hidden',
                                    enterActiveClass: 'animate-scalein',
                                    leaveToClass: 'hidden',
                                    leaveActiveClass: 'animate-fadeout',
                                    hideOnOutsideClick: true,
                                }"
                                type="button"
                                class="layout-topbar-action layout-topbar-action-highlight"
                            >
                                <i class="pi pi-palette"></i>
                            </button>
                            <AppConfigurator />
                        </div>
                    </div>
                    <button
                        v-if="isLogged"
                        type="button"
                        class="layout-topbar-action"
                    >
                        <i class="pi pi-user"></i>
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>
