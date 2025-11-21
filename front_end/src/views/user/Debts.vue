<script setup>
import { apiFetch } from "@/auth/fetchClient";
import { useToast } from "primevue/usetoast";
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";

const toast = useToast();
const router = useRouter();
const debts = ref([]);
const loading = ref(true);

onMounted(async () => {
    loading.value = true;
    try {
        const { data } = await apiFetch("/api/bills");
        if (data) {
            debts.value = data.map((bill) => ({
                ...bill,
                itemCount: bill.items.reduce(
                    (acc, item) => acc + item.amount,
                    0,
                ),
            }));
        }
    } catch (e) {
        console.error("Failed to load bills", e);
        toast.add({
            severity: "error",
            summary: "Error",
            detail: "Failed to load bills",
            life: 3000,
        });
    } finally {
        loading.value = false;
    }
});

const getSeverity = (status) => {
    switch (status) {
        case "COMPLETED":
            return "success";
        case "PROCESSING":
            return "info";
        case "PENDING_PAYMENT":
            return "warn";
        case "CANCELLED":
            return "danger";
        default:
            return null;
    }
};

const expandedRows = ref([]);

const formatCurrency = (value) => {
    return new Intl.NumberFormat("en-US", {
        style: "currency",
        currency: "USD",
    }).format(value);
};

const formatDate = (dateString) => {
    if (!dateString) return "";
    return new Date(dateString).toLocaleDateString("en-US", {
        year: "numeric",
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });
};

const payDialogVisible = ref(false);
const cancelDialogVisible = ref(false);
const selectedBill = ref(null);
const isPaying = ref(false);
const isCancelling = ref(false);

const openPayDialog = (bill) => {
    selectedBill.value = bill;
    payDialogVisible.value = true;
};

const openCancelDialog = (bill) => {
    selectedBill.value = bill;
    cancelDialogVisible.value = true;
};

const closePayDialog = () => {
    payDialogVisible.value = false;
    selectedBill.value = null;
};

const closeCancelDialog = () => {
    cancelDialogVisible.value = false;
    selectedBill.value = null;
};

const confirmPay = async () => {
    if (!selectedBill.value) return;

    isPaying.value = true;
    try {
        const { data } = await apiFetch(
            `/api/bills/${selectedBill.value.id}/pay`,
            {
                method: "POST",
            },
        );

        if (data) {
            updateBillInList(data);
            toast.add({
                severity: "success",
                summary: "Payment Successful",
                detail: "Your payment is being processed.",
                life: 3000,
            });
        }
    } catch (e) {
        console.error("Payment failed", e);
        toast.add({
            severity: "error",
            summary: "Payment Failed",
            detail: "Failed to process payment. Please try again.",
            life: 3000,
        });
    } finally {
        isPaying.value = false;
        closePayDialog();
    }
};

const confirmCancel = async () => {
    if (!selectedBill.value) return;

    isCancelling.value = true;
    try {
        const { data } = await apiFetch(
            `/api/bills/${selectedBill.value.id}/cancel`,
            {
                method: "POST",
            },
        );

        if (data) {
            updateBillInList(data);
            toast.add({
                severity: "info",
                summary: "Order Cancelled",
                detail: "Your order has been cancelled.",
                life: 3000,
            });
        }
    } catch (e) {
        console.error("Cancellation failed", e);
        toast.add({
            severity: "error",
            summary: "Cancellation Failed",
            detail: "Failed to cancel order. Please try again.",
            life: 3000,
        });
    } finally {
        isCancelling.value = false;
        closeCancelDialog();
    }
};

const updateBillInList = (updatedBill) => {
    const index = debts.value.findIndex((b) => b.id === updatedBill.id);
    if (index !== -1) {
        debts.value[index] = {
            ...updatedBill,
            itemCount: updatedBill.items.reduce(
                (acc, item) => acc + item.amount,
                0,
            ),
        };
    }
};
</script>

<template>
    <div class="flex items-center justify-center min-h-screen p-4">
        <div
            class="card w-full max-w-6xl rounded-3xl shadow-2 flex flex-col overflow-hidden"
            style="height: 75vh"
        >
            <div
                class="flex flex-col md:flex-row justify-between items-start md:items-center mb-4 gap-4"
            >
                <div>
                    <div class="font-semibold text-xl">My Bills</div>
                    <p class="text-surface-500">
                        Manage your outstanding bills and payment history.
                    </p>
                </div>
                <Button
                    label="Back to Shop"
                    icon="pi pi-arrow-left"
                    text
                    @click="router.push('/fishes')"
                />
            </div>

            <DataTable
                v-model:expandedRows="expandedRows"
                :value="debts"
                :paginator="true"
                :rows="5"
                :rowsPerPageOptions="[5, 10, 20]"
                :loading="loading"
                class="flex-1"
                scrollable
                scrollHeight="flex"
                dataKey="id"
                tableStyle="min-width: 50rem"
            >
                <Column expander style="width: 5rem" />
                <Column field="id" header="Invoice Code" sortable>
                    <template #body="slotProps">
                        INV-{{ slotProps.data.id }}
                    </template>
                </Column>
                <Column field="createdAt" header="Date" sortable>
                    <template #body="slotProps">
                        {{ formatDate(slotProps.data.createdAt) }}
                    </template>
                </Column>
                <Column field="itemCount" header="Items" sortable></Column>
                <Column field="total" header="Amount" sortable>
                    <template #body="slotProps">
                        {{ formatCurrency(slotProps.data.total) }}
                    </template>
                </Column>
                <Column field="status" header="Status" sortable>
                    <template #body="slotProps">
                        <Tag
                            :value="slotProps.data.status"
                            :severity="getSeverity(slotProps.data.status)"
                        />
                    </template>
                </Column>
                <Column header="Actions" style="width: 12rem">
                    <template #body="slotProps">
                        <div class="flex gap-2">
                            <Button
                                v-if="
                                    slotProps.data.status === 'PENDING_PAYMENT'
                                "
                                icon="pi pi-wallet"
                                label="Pay"
                                size="small"
                                severity="success"
                                outlined
                                @click="openPayDialog(slotProps.data)"
                            />
                            <Button
                                v-if="
                                    slotProps.data.status === 'PENDING_PAYMENT'
                                "
                                icon="pi pi-times"
                                label="Cancel"
                                size="small"
                                severity="danger"
                                outlined
                                @click="openCancelDialog(slotProps.data)"
                            />
                        </div>
                    </template>
                </Column>
                <template #empty>
                    <div class="text-center p-8">
                        <i
                            class="pi pi-file text-surface-500 text-6xl mb-4"
                        ></i>
                        <div class="text-xl font-semibold mb-2">
                            No bills found
                        </div>
                        <p class="text-surface-500 mb-4">
                            You haven't made any purchases yet.
                        </p>
                        <Button
                            label="Start Shopping"
                            icon="pi pi-shopping-cart"
                            @click="router.push('/fishes')"
                        />
                    </div>
                </template>
                <template #expansion="slotProps">
                    <div class="p-3">
                        <h5 class="font-bold mb-2">Order Details</h5>
                        <DataTable
                            :value="slotProps.data.items"
                            class="p-datatable-sm"
                        >
                            <Column
                                field="fishName"
                                header="Fish Name"
                                sortable
                            ></Column>
                            <Column
                                field="amount"
                                header="Quantity"
                                sortable
                            ></Column>
                            <Column field="price" header="Price" sortable>
                                <template #body="itemProps">
                                    {{ formatCurrency(itemProps.data.price) }}
                                </template>
                            </Column>
                            <Column field="sum" header="Total" sortable>
                                <template #body="itemProps">
                                    {{ formatCurrency(itemProps.data.sum) }}
                                </template>
                            </Column>
                        </DataTable>
                    </div>
                </template>
            </DataTable>
        </div>

        <Dialog
            v-model:visible="payDialogVisible"
            header="Confirm Payment"
            :modal="true"
            :style="{ width: '450px' }"
        >
            <div class="flex items-center gap-4 mb-4">
                <i
                    class="pi pi-exclamation-triangle text-yellow-500 text-2xl"
                />
                <span v-if="selectedBill">
                    Are you sure you want to pay
                    <b>{{ formatCurrency(selectedBill.total) }}</b> for Invoice
                    <b>INV-{{ selectedBill.id }}</b
                    >?
                </span>
            </div>
            <template #footer>
                <Button
                    label="Cancel"
                    icon="pi pi-times"
                    text
                    @click="closePayDialog"
                />
                <Button
                    label="Confirm"
                    icon="pi pi-check"
                    severity="success"
                    :loading="isPaying"
                    @click="confirmPay"
                />
            </template>
        </Dialog>

        <Dialog
            v-model:visible="cancelDialogVisible"
            header="Confirm Cancellation"
            :modal="true"
            :style="{ width: '450px' }"
        >
            <div class="flex items-center gap-4 mb-4">
                <i class="pi pi-info-circle text-red-500 text-2xl" />
                <span v-if="selectedBill">
                    Are you sure you want to cancel Invoice
                    <b>INV-{{ selectedBill.id }}</b
                    >? This action cannot be undone.
                </span>
            </div>
            <template #footer>
                <Button
                    label="No, Keep it"
                    icon="pi pi-times"
                    text
                    @click="closeCancelDialog"
                />
                <Button
                    label="Yes, Cancel Order"
                    icon="pi pi-check"
                    severity="danger"
                    :loading="isCancelling"
                    @click="confirmCancel"
                />
            </template>
        </Dialog>
    </div>
</template>
