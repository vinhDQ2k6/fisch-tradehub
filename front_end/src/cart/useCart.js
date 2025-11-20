import { apiFetch } from "@/auth/fetchClient";
import { useAuth } from "@/auth/useAuth";
import { computed, ref, watch } from "vue";

const items = ref([]);

export function useCart() {
    const { user, isLoggedIn } = useAuth();

    const fetchCart = async () => {
        if (!isLoggedIn()) {
            items.value = [];
            return;
        }
        try {
            const { data } = await apiFetch("/api/cart");
            if (data) {
                items.value = data.map((item) => ({
                    fishId: item.fishId,
                    name: item.fishName,
                    rarity: item.fishRarity,
                    price: item.fishValue,
                    quantity: item.quantity,
                    total: item.fishValue * item.quantity,
                }));
            }
        } catch (e) {
            console.error("Failed to fetch cart", e);
        }
    };

    const addItem = async (fish) => {
        if (!isLoggedIn()) {
            // For now, just log warning. In a real app, redirect to login.
            console.warn("User must be logged in to add to cart");
            return;
        }
        try {
            await apiFetch("/api/cart", {
                method: "POST",
                body: { fishId: fish.id, quantity: 1 },
            });
            await fetchCart();
        } catch (e) {
            console.error("Failed to add item", e);
            throw e;
        }
    };

    const removeItem = async (fishId) => {
        if (!isLoggedIn()) return;
        try {
            await apiFetch(`/api/cart/${fishId}`, { method: "DELETE" });
            await fetchCart();
        } catch (e) {
            console.error("Failed to remove item", e);
        }
    };

    const clear = async () => {
        if (!isLoggedIn()) return;
        try {
            await apiFetch("/api/cart", { method: "DELETE" });
            await fetchCart();
        } catch (e) {
            console.error("Failed to clear cart", e);
        }
    };

    const checkout = async () => {
        if (!isLoggedIn()) return;
        try {
            const { data } = await apiFetch("/api/bills/checkout", {
                method: "POST",
            });
            await fetchCart(); // Cart should be empty after checkout
            return data;
        } catch (e) {
            console.error("Failed to checkout", e);
            throw e;
        }
    };

    const itemCount = computed(() =>
        items.value.reduce((sum, i) => sum + i.quantity, 0),
    );

    const totalValue = computed(() =>
        items.value.reduce((sum, i) => sum + i.total, 0),
    );

    // Watch for user login/logout to refresh cart
    watch(
        user,
        (newUser) => {
            if (newUser) {
                fetchCart();
            } else {
                items.value = [];
            }
        },
        { immediate: true },
    );

    return {
        items,
        addItem,
        removeItem,
        clear,
        checkout,
        itemCount,
        totalValue,
        fetchCart,
    };
}
