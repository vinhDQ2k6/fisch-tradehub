<script setup>
import { useAuth } from "@/auth/useAuth";
import { computed } from "vue";

const { user } = useAuth();
const isLogged = computed(() => !!user.value);
</script>

<template>
    <div
        id="hero"
        class="relative flex flex-col pt-12 px-6 lg:px-20 overflow-hidden text-white"
        style="
            /* layered colors + soft caustics + faint ripples */
            background-image:
                radial-gradient(
                    1200px 600px at 20% 10%,
                    rgba(150, 210, 255, 0.95) 0%,
                    rgba(80, 170, 235, 0.8) 25%,
                    rgba(20, 95, 165, 0.6) 60%,
                    rgba(4, 24, 54, 0.98) 100%
                ),
                linear-gradient(
                    180deg,
                    rgba(120, 200, 255, 0.09) 0%,
                    rgba(6, 24, 48, 0.98) 100%
                ),
                radial-gradient(
                    circle at 10% 20%,
                    rgba(255, 255, 255, 0.06) 0 2%,
                    transparent 6%
                ),
                radial-gradient(
                    circle at 70% 80%,
                    rgba(255, 255, 255, 0.04) 0 2.5%,
                    transparent 7%
                ),
                repeating-linear-gradient(
                    -45deg,
                    rgba(255, 255, 255, 0.02) 0 1px,
                    transparent 1px 10px
                );
            background-blend-mode: overlay, normal, screen, screen, overlay;
            /* different sizes for each layer to add depth */
            background-size:
                100% 100%,
                100% 100%,
                220% 220%,
                300% 300%,
                400% 400%;
            /* subtle color tuning */
            filter: saturate(1.05) contrast(1.02) brightness(0.98);
            /* soft rounded crop for organic 'bubbles' / ocean curve */
            clip-path: ellipse(150% 87% at 93% 13%);
        "
    >
        <div class="mx-6 md:mx-20 mt-0 md:mt-6">
            <h1 class="text-9xl font-bold text-gray-900 leading-tight">
                <span class="font-light block">Welcome to</span>Fisch-TradeHub
            </h1>
            <div class="flex gap-4 w-fit" v-if="!isLogged">
                <Button
                    label="Login"
                    as="router-link"
                    to="/auth/login"
                    rounded
                    class="text-xl! px-4! w-30"
                ></Button>
                <Button
                    label="Register"
                    severity="secondary"
                    as="router-link"
                    to="/auth/register"
                    rounded
                    class="text-xl! px-4! w-30"
                ></Button>
            </div>
        </div>
        <div class="flex justify-center md:justify-end">
            <img
                src="https://static.wikitide.net/fischwiki/thumb/b/bf/WikiIcon16.png/450px-WikiIcon16.png"
                alt="Hero Image"
                class="w-9/12 md:w-auto"
            />
        </div>
    </div>
</template>
