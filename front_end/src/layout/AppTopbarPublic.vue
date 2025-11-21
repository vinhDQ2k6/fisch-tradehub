<script setup>
import { useAuth } from "@/auth/useAuth";
import { useCart } from "@/cart/useCart";
import { useLayout } from "@/layout/composables/layout";
import { useToast } from "primevue/usetoast";
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import AppConfigurator from "./AppConfigurator.vue";

const { toggleDarkMode, isDarkTheme } = useLayout();
const { user, doLogout } = useAuth();
const router = useRouter();
const toast = useToast();
const isLogged = computed(() => !!user.value);
const { items, itemCount, totalValue, clear, removeItem, checkout } = useCart();
const cartPanel = ref();
const cartPage = ref(0);
const isCheckingOut = ref(false);

const pagedItems = computed(() => {
    const start = cartPage.value * 5;
    return items.value.slice(start, start + 5);
});

const pageCount = computed(() =>
    items.value.length === 0 ? 1 : Math.ceil(items.value.length / 5),
);

const showCart = (event) => {
    cartPanel.value.toggle(event);
};

const nextPage = () => {
    if (cartPage.value < pageCount.value - 1) cartPage.value++;
};

const prevPage = () => {
    if (cartPage.value > 0) cartPage.value--;
};

const onCheckout = async () => {
    isCheckingOut.value = true;
    try {
        await checkout();
        toast.add({
            severity: "success",
            summary: "Order Placed",
            detail: "Your order has been successfully placed.",
            life: 3000,
        });
        cartPanel.value.hide();
        router.push("/my/orders");
    } catch (e) {
        toast.add({
            severity: "error",
            summary: "Checkout Failed",
            detail: "Failed to process your order. Please try again.",
            life: 3000,
        });
    } finally {
        isCheckingOut.value = false;
    }
};

const onClearCart = async () => {
    await clear();
    toast.add({
        severity: "info",
        summary: "Cart Cleared",
        detail: "All items have been removed from your cart.",
        life: 3000,
    });
};

const onRemoveItem = async (id) => {
    await removeItem(id);
    toast.add({
        severity: "info",
        summary: "Item Removed",
        detail: "Item removed from cart.",
        life: 2000,
    });
};

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
                    <Router-link
                        to="/fishes"
                        class="px-0 py-4 text-surface-900 dark:text-surface-0 font-medium text-xl"
                    >
                        <span>Trade-Hub</span>
                    </Router-link>
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

                    <template
                        v-if="!isLogged"
                        class="flex border-t lg:border-t-0 border-surface py-4 lg:py-0 mt-4 lg:mt-0 gap-2"
                    >
                        <Button
                            label="Login"
                            text
                            as="router-link"
                            to="/auth/login"
                            rounded
                        />
                        <Button
                            label="Register"
                            as="router-link"
                            to="/auth/register"
                            rounded
                        />
                    </template>

                    <div class="flex items-center gap-2" v-else>
                        <div class="relative mr-2">
                            <button
                                type="button"
                                class="layout-topbar-action layout-topbar-action-highlight"
                                @click="showCart"
                            >
                                <i class="pi pi-shopping-cart"></i>
                            </button>
                            <Badge
                                v-if="itemCount > 0"
                                :value="itemCount"
                                severity="danger"
                                class="absolute -top-2 -right-2"
                            />
                            <OverlayPanel ref="cartPanel" style="width: 22rem">
                                <div
                                    class="flex items-center justify-between mb-3"
                                >
                                    <span class="font-semibold">Cart</span>
                                    <span class="text-sm text-surface-500">
                                        {{ itemCount }} items
                                    </span>
                                </div>

                                <div
                                    v-if="itemCount === 0"
                                    class="text-sm text-surface-500 py-4 text-center"
                                >
                                    Your cart is empty.
                                </div>
                                <div v-else class="space-y-2">
                                    <div
                                        v-for="item in pagedItems"
                                        :key="item.fishId"
                                        class="flex items-center justify-between text-sm py-1 border-b border-surface-100 dark:border-surface-800 gap-3"
                                    >
                                        <div class="flex-1">
                                            <div class="font-medium">
                                                {{ item.name }}
                                            </div>
                                            <div
                                                class="text-xs text-surface-500"
                                            >
                                                x{{ item.quantity }} ·
                                                {{ item.rarity }}
                                            </div>
                                        </div>
                                        <div
                                            class="font-semibold whitespace-nowrap"
                                        >
                                            {{ item.total.toLocaleString() }}
                                        </div>
                                        <button
                                            type="button"
                                            class="p-button p-button-text p-button-sm text-xs text-surface-500 hover:text-red-500"
                                            @click="onRemoveItem(item.fishId)"
                                        >
                                            x
                                        </button>
                                    </div>
                                    <div
                                        v-if="pageCount > 1"
                                        class="flex justify-between items-center pt-2 text-xs text-surface-500"
                                    >
                                        <Button
                                            icon="pi pi-chevron-left"
                                            text
                                            rounded
                                            :disabled="cartPage === 0"
                                            @click="prevPage"
                                        />
                                        <span>
                                            Page {{ cartPage + 1 }} /
                                            {{ pageCount }}
                                        </span>
                                        <Button
                                            icon="pi pi-chevron-right"
                                            text
                                            rounded
                                            :disabled="
                                                cartPage >= pageCount - 1
                                            "
                                            @click="nextPage"
                                        />
                                    </div>
                                </div>

                                <div
                                    class="flex items-center justify-between pt-3 mt-3 border-t border-surface-100 dark:border-surface-800"
                                >
                                    <div class="text-sm">
                                        <div class="text-surface-500">
                                            Total
                                        </div>
                                        <div class="font-semibold">
                                            {{ totalValue.toLocaleString() }}
                                        </div>
                                    </div>
                                    <div class="flex items-center gap-2">
                                        <Button
                                            label="Show Bills"
                                            size="small"
                                            severity="secondary"
                                            as="router-link"
                                            to="/my/orders"
                                        />
                                        <Button
                                            icon="pi pi-trash"
                                            severity="secondary"
                                            text
                                            rounded
                                            :disabled="itemCount === 0"
                                            @click="onClearCart"
                                        />
                                        <Button
                                            label="Purchase"
                                            size="small"
                                            :disabled="itemCount === 0"
                                            :loading="isCheckingOut"
                                            @click="onCheckout"
                                        />
                                    </div>
                                </div>
                            </OverlayPanel>
                        </div>

                        <div class="relative">
                            <button
                                type="button"
                                v-styleclass="{
                                    selector: '@next',
                                    enterFromClass: 'hidden',
                                    enterActiveClass: 'animate-scalein',
                                    leaveToClass: 'hidden',
                                    leaveActiveClass: 'animate-fadeout',
                                    hideOnOutsideClick: true,
                                }"
                                class="layout-topbar-action"
                            >
                                <i class="pi pi-user"></i>
                            </button>

                            <div
                                class="hidden absolute top-13 right-0 w-32 p-2 bg-surface-0 dark:bg-surface-900 border border-surface rounded-border origin-top shadow-[0px_3px_5px_rgba(0,0,0,0.02),0px_0px_2px_rgba(0,0,0,0.05),0px_1px_4px_rgba(0,0,0,0.08)] z-50"
                            >
                                <ul class="list-none m-0 p-2">
                                    <li>
                                        <router-link
                                            to="/auth/profile"
                                            class="flex items-center gap-2 p-2 text-surface-900 dark:text-surface-0 rounded hover:bg-surface-100 dark:hover:bg-surface-800"
                                        >
                                            <i class="pi pi-user"></i>
                                            <span>Profile</span>
                                        </router-link>
                                    </li>
                                    <li>
                                        <button
                                            type="button"
                                            @click="
                                                async () => {
                                                    await doLogout();
                                                    router.push('/');
                                                }
                                            "
                                            class="flex items-center gap-2 p-2 w-full text-left text-surface-900 dark:text-surface-0 rounded hover:bg-surface-100 dark:hover:bg-surface-800 cursor-pointer"
                                        >
                                            <i class="pi pi-sign-out"></i>
                                            <span>Log out</span>
                                        </button>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
