<script setup>
import AppTopbar from "./AppTopbarPublic.vue";
</script>

<template>
    <app-topbar></app-topbar>
    <router-view></router-view>
    <Toast />
</template>
