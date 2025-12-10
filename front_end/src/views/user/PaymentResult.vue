<script setup>
import { onMounted, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { apiFetch } from "@/auth/fetchClient";

const props = defineProps({
  status: {
    type: String,
    required: true,
  },
});

const router = useRouter();
const route = useRoute();
const countdown = ref(5);
const isProcessing = ref(false);
const pollingAttempts = ref(0);
const maxAttempts = 10; // 20 seconds total (2s interval)

onMounted(async () => {
  if (props.status === "success") {
    const orderCode = route.query.orderCode;
    if (orderCode) {
      isProcessing.value = true;
      await pollStatus(orderCode);
    }
  }

  startCountdown();
});

const pollStatus = async (id) => {
  const interval = setInterval(async () => {
    pollingAttempts.value++;
    try {
      const { data } = await apiFetch(`/api/bills/${id}`);
      if (data && data.status === "PROCESSING") {
        isProcessing.value = false;
        clearInterval(interval);
      } else if (pollingAttempts.value >= maxAttempts) {
        isProcessing.value = false; // Timeout, just show success and let user check later
        clearInterval(interval);
      }
    } catch (e) {
      console.error("Polling failed", e);
    }
  }, 2000);
};

const startCountdown = () => {
  const timer = setInterval(() => {
    if (!isProcessing.value) {
      countdown.value--;
      if (countdown.value <= 0) {
        clearInterval(timer);
        router.push("/my/orders");
      }
    }
  }, 1000);
};
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-[60vh] gap-4">
    <div v-if="status === 'success'" class="text-center">
      <div v-if="isProcessing">
        <i class="pi pi-spin pi-spinner text-blue-500 text-6xl mb-4"></i>
        <h1 class="text-3xl font-bold mb-2">Processing Payment...</h1>
        <p class="text-gray-600">Please wait while we confirm your payment.</p>
      </div>
      <div v-else>
        <i class="pi pi-check-circle text-green-500 text-6xl mb-4"></i>
        <h1 class="text-3xl font-bold mb-2">Payment Successful!</h1>
        <p class="text-gray-600">
          Thank you for your purchase. Your order has been confirmed.
        </p>
      </div>
    </div>
    <div v-else class="text-center">
      <i class="pi pi-times-circle text-red-500 text-6xl mb-4"></i>
      <h1 class="text-3xl font-bold mb-2">Payment Cancelled</h1>
      <p class="text-gray-600">You have cancelled the payment process.</p>
    </div>

    <p v-if="!isProcessing" class="text-sm text-gray-500 mt-4">
      Redirecting to your orders in {{ countdown }} seconds...
    </p>

    <Button label="Go to My Orders" @click="router.push('/my/orders')" />
  </div>
</template>
