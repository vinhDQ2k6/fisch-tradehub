import { ref } from "vue";
import {
    currentUser as svcCurrentUser,
    login as svcLogin,
    logout as svcLogout,
} from "./authService";

// module-scoped singleton state
const user = ref(null);
const loading = ref(false);
const ready = ref(false);

export function useAuth() {
    async function init() {
        if (ready.value) return;
        try {
            const { data } = await svcCurrentUser();
            user.value = data;
        } catch (_) {
            user.value = null;
        } finally {
            ready.value = true;
        }
    }

    async function doLogin({ username, password, rememberMe }) {
        loading.value = true;
        try {
            const data = await svcLogin({ username, password, rememberMe });
            user.value = data;
            return data;
        } finally {
            loading.value = false;
        }
    }

    async function doLogout() {
        try {
            await svcLogout();
        } finally {
            user.value = null;
        }
    }

    function isLoggedIn() {
        return !!user.value;
    }

    return { user, loading, ready, init, doLogin, doLogout, isLoggedIn };
}
