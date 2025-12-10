<script setup>
import { apiFetch } from "@/auth/fetchClient";
import { useCart } from "@/cart/useCart";
import { useToast } from "primevue/usetoast";
import { computed, onMounted, ref } from "vue";

const fish = ref([]);
const loading = ref(true);
const error = ref(null);
const toast = useToast();

onMounted(async () => {
  loading.value = true;
  try {
    const { data } = await apiFetch("/api/fish");
    if (data) {
      fish.value = data.map((item) => ({
        ...item,
        totalValue: item.value,
        valuePerWeight: item.weight > 0 ? item.value / item.weight : 0,
      }));
    }
  } catch (e) {
    console.error("Failed to load fishes", e);
    error.value = e;
  } finally {
    loading.value = false;
  }
});

const search = ref("");
const selectedRarity = ref(null);
const minTotal = ref(null);
const maxTotal = ref(null);

const rarityOptions = ["Common", "Uncommon", "Rare", "Epic", "Legendary"];

const raritySeverity = (rarity) => {
  switch (rarity) {
    case "Common":
      return "secondary";
    case "Uncommon":
      return "success";
    case "Rare":
      return "info";
    case "Epic":
      return "warn";
    case "Legendary":
      return "danger";
    default:
      return null;
  }
};

const formatCurrency = (value) => {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(value);
};

const formatUnitPrice = (value) => {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(value);
};

const clearValueFilter = () => {
  minTotal.value = null;
  maxTotal.value = null;
};

const filteredFish = computed(() => {
  return fish.value.filter((item) => {
    const matchesSearch =
      !search.value ||
      item.name.toLowerCase().includes(search.value.toLowerCase());

    const matchesRarity =
      !selectedRarity.value || item.rarity === selectedRarity.value;

    const matchesMin =
      minTotal.value == null || item.totalValue >= minTotal.value;

    const matchesMax =
      maxTotal.value == null || item.totalValue <= maxTotal.value;

    return matchesSearch && matchesRarity && matchesMin && matchesMax;
  });
});

const { addItem } = useCart();

const onAddToCart = async (item) => {
  try {
    await addItem(item);
    toast.add({
      severity: "success",
      summary: "Added to Cart",
      detail: `${item.name} added to your cart`,
      life: 3000,
    });
  } catch (e) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: "Failed to add to cart. Please try again.",
      life: 3000,
    });
  }
};
</script>

<template>
  <div class="flex items-center justify-center min-h-screen">
    <div
      class="card w-full max-w-6xl rounded-3xl shadow-2 flex flex-col"
      style="height: 75vh"
    >
      <div class="font-semibold text-xl mb-4">Fisch Trade</div>

      <div v-if="loading" class="flex justify-center items-center flex-1">
        <i class="pi pi-spin pi-spinner text-4xl"></i>
      </div>

      <div
        v-else-if="error"
        class="flex justify-center items-center flex-1 text-red-500"
      >
        Failed to load fishes. Please try again later.
      </div>

      <template v-else>
        <div class="flex justify-between mb-4 gap-3 flex-wrap">
          <IconField class="p-input-icon-left w-full md:w-64">
            <InputIcon class="pi pi-search" />
            <InputText
              v-model="search"
              class="w-full"
              placeholder="Search fish"
            />
          </IconField>

          <div class="flex gap-3 flex-wrap items-center">
            <Dropdown
              v-model="selectedRarity"
              :options="rarityOptions"
              class="w-44"
              placeholder="Filter by rarity"
              showClear
            />
            <div class="flex items-center gap-2">
              <span class="text-sm text-surface-500 dark:text-surface-400">
                Total value from
              </span>
              <InputNumber
                v-model="minTotal"
                inputClass="w-24"
                mode="decimal"
                :min="0"
              />
              <span class="text-sm text-surface-500 dark:text-surface-400">
                to
              </span>
              <InputNumber
                v-model="maxTotal"
                inputClass="w-24"
                mode="decimal"
                :min="0"
              />
              <Button
                icon="pi pi-filter-slash"
                severity="secondary"
                text
                rounded
                v-tooltip.top="'Clear value range'"
                @click="clearValueFilter"
              />
            </div>
          </div>
        </div>

        <DataTable
          :value="filteredFish"
          paginator
          :rows="5"
          class="p-datatable-sm flex-1"
          scrollable
          scrollHeight="flex"
        >
          <Column field="name" header="Fish Name" sortable />
          <Column field="rarity" header="Rarity">
            <template #body="slotProps">
              <Badge
                :value="slotProps.data.rarity"
                :severity="raritySeverity(slotProps.data.rarity)"
              />
            </template>
          </Column>
          <Column field="weight" header="Weight (kg)" class="text-right">
            <template #body="slotProps">
              {{ slotProps.data.weight.toFixed(2) }}
            </template>
          </Column>
          <Column field="valuePerWeight" header="Value / kg" class="text-right">
            <template #body="slotProps">
              {{ formatUnitPrice(slotProps.data.valuePerWeight) }}
            </template>
          </Column>
          <Column
            field="totalValue"
            header="Total Value"
            sortable
            class="text-right"
          >
            <template #body="slotProps">
              {{ formatCurrency(slotProps.data.totalValue) }}
            </template>
          </Column>
          <Column header="">
            <template #body="slotProps">
              <Button
                icon="pi pi-shopping-cart"
                rounded
                text
                @click="onAddToCart(slotProps.data)"
              />
            </template>
          </Column>
          <template #empty>
            <div class="text-center p-4 text-surface-500">
              No fish found matching your criteria.
            </div>
          </template>
        </DataTable>
      </template>
    </div>
  </div>
</template>
