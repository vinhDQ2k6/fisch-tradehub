<script setup>
import { Toast } from "primevue";
import { onMounted } from "vue";
import { useAuth } from "./auth/useAuth";

const { init } = useAuth();
onMounted(() => {
    init();
});
</script>

<template>
    <router-view />
    <Toast />
</template>

<style scoped></style>
