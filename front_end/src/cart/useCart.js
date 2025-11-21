import { apiFetch } from "@/auth/fetchClient";
import { useAuth } from "@/auth/useAuth";
import { API_ENDPOINTS, ERROR_MESSAGES } from "@/common/constants";
import { computed, ref, watch } from "vue";

/**
 * Shopping cart composable for managing cart state and operations.
 * Provides singleton state and methods for adding, removing items and checkout.
 * 
 * @returns {Object} Cart state and methods
 * @property {Ref<Array>} items - Cart items
 * @property {Function} addItem - Add item to cart
 * @property {Function} removeItem - Remove item from cart
 * @property {Function} clear - Clear entire cart
 * @property {Function} checkout - Create order from cart
 * @property {ComputedRef<number>} itemCount - Total quantity of items
 * @property {ComputedRef<number>} totalValue - Total cart value
 * @property {Function} fetchCart - Manually refresh cart from backend
 */

const items = ref([]);

export function useCart() {
    const { user, isLoggedIn } = useAuth();

    /**
     * Fetch cart items from backend.
     * Automatically called when user logs in.
     * 
     * @returns {Promise<void>}
     */
    const fetchCart = async () => {
        if (!isLoggedIn()) {
            items.value = [];
            return;
        }
        try {
            const { data } = await apiFetch(API_ENDPOINTS.CART.BASE);
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

    /**
     * Add item to cart or update quantity if already exists.
     * 
     * @param {Object} fish - Fish item to add
     * @param {number} fish.id - Fish ID
     * @returns {Promise<void>}
     * @throws {Error} If user not logged in or operation fails
     */
    const addItem = async (fish) => {
        if (!isLoggedIn()) {
            console.warn(ERROR_MESSAGES.LOGIN_REQUIRED);
            throw new Error(ERROR_MESSAGES.LOGIN_REQUIRED);
        }
        try {
            await apiFetch(API_ENDPOINTS.CART.BASE, {
                method: "POST",
                body: { fishId: fish.id, quantity: 1 },
            });
            await fetchCart();
        } catch (e) {
            console.error("Failed to add item", e);
            throw e;
        }
    };

    /**
     * Remove item from cart.
     * 
     * @param {number} fishId - Fish ID to remove
     * @returns {Promise<void>}
     */
    const removeItem = async (fishId) => {
        if (!isLoggedIn()) return;
        try {
            await apiFetch(API_ENDPOINTS.CART.BY_FISH(fishId), { method: "DELETE" });
            await fetchCart();
        } catch (e) {
            console.error("Failed to remove item", e);
        }
    };

    /**
     * Clear all items from cart.
     * 
     * @returns {Promise<void>}
     */
    const clear = async () => {
        if (!isLoggedIn()) return;
        try {
            await apiFetch(API_ENDPOINTS.CART.BASE, { method: "DELETE" });
            await fetchCart();
        } catch (e) {
            console.error("Failed to clear cart", e);
        }
    };

    /**
     * Create order from current cart items.
     * Cart will be cleared after successful checkout.
     * 
     * @returns {Promise<Object>} Created bill/order data
     * @throws {Error} If checkout fails
     */
    const checkout = async () => {
        if (!isLoggedIn()) {
            throw new Error(ERROR_MESSAGES.LOGIN_REQUIRED);
        }
        try {
            const { data } = await apiFetch(API_ENDPOINTS.BILLS.CHECKOUT, {
                method: "POST",
            });
            await fetchCart(); // Cart should be empty after checkout
            return data;
        } catch (e) {
            console.error("Failed to checkout", e);
            throw e;
        }
    };

    /**
     * Total quantity of items in cart.
     */
    const itemCount = computed(() =>
        items.value.reduce((sum, i) => sum + i.quantity, 0),
    );

    /**
     * Total value of all items in cart.
     */
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
