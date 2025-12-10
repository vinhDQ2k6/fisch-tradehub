<script setup>
import { useAuth } from "@/auth/useAuth";
import { computed } from "vue";

const { user } = useAuth();
const isLogged = computed(() => !!user.value);

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
</script>

<template>
    <a class="flex items-center" href="#">
        <img
            src="https://static.wikitide.net/fischwiki/thumb/b/bf/WikiIcon16.png/450px-WikiIcon16.png"
            alt="Logo"
            class="h-12 mr-2"
        />
        <span
            class="text-surface-900 dark:text-surface-0 font-medium text-2xl leading-normal mr-20 whitespace-nowrap"
            >Fisch-TradeHub</span
        >
    </a>
    <Button
        class="lg:hidden!"
        text
        severity="secondary"
        rounded
        v-styleclass="{
            selector: '@next',
            enterFromClass: 'hidden',
            enterActiveClass: 'animate-scalein',
            leaveToClass: 'hidden',
            leaveActiveClass: 'animate-fadeout',
            hideOnOutsideClick: true,
        }"
    >
        <i class="pi pi-bars text-2xl!"></i>
    </Button>
    <div
        class="items-center bg-surface-0 dark:bg-surface-900 grow justify-between hidden lg:flex absolute lg:static w-full left-0 top-full px-12 lg:px-0 z-20 rounded-border"
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
        <div
            v-if="!isLogged"
            class="flex border-t lg:border-t-0 border-surface py-4 lg:py-0 mt-4 lg:mt-0 gap-2"
        >
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
        </div>
    </div>
</template>
