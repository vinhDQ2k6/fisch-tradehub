<script setup>
import { apiFetch } from "@/auth/fetchClient";
import { FilterMatchMode } from "@primevue/core/api";
import { useToast } from "primevue/usetoast";
import { onMounted, ref } from "vue";

const toast = useToast();
const dt = ref();
const bills = ref([]);
const selectedBills = ref();
const filters = ref({
    global: { value: null, matchMode: FilterMatchMode.CONTAINS },
});
const loading = ref(true);

const loadBills = async () => {
    loading.value = true;
    try {
        const { data } = await apiFetch("/api/bills/all");
        if (data) {
            bills.value = data
                .filter((bill) => bill.status === "PROCESSING")
                .map((bill) => ({
                    ...bill,
                    code: `BILL-${bill.id}`,
                    date: new Date(bill.createdAt).toLocaleDateString(),
                    amount: bill.total,
                    items: bill.items.reduce(
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
};

onMounted(() => {
    loadBills();
});

function formatCurrency(value) {
    if (value)
        return value.toLocaleString("en-US", {
            style: "currency",
            currency: "USD",
        });
    return;
}

function getStatusLabel(status) {
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
}

async function updateStatus(bill, status) {
    try {
        const { data } = await apiFetch(
            `/api/bills/${bill.id}/status?status=${status}`,
            {
                method: "POST",
            },
        );
        if (data) {
            // Remove from list as we only show PROCESSING bills
            bills.value = bills.value.filter((b) => b.id !== bill.id);
            return true;
        }
    } catch (e) {
        console.error("Failed to update status", e);
        toast.add({
            severity: "error",
            summary: "Error",
            detail: "Failed to update status",
            life: 3000,
        });
        return false;
    }
}

async function solveBill(data) {
    if (await updateStatus(data, "COMPLETED")) {
        toast.add({
            severity: "success",
            summary: "Successful",
            detail: "Bill Solved",
            life: 3000,
        });
    }
}

async function closeBill(data) {
    if (await updateStatus(data, "CANCELLED")) {
        toast.add({
            severity: "info",
            summary: "Successful",
            detail: "Bill Closed",
            life: 3000,
        });
    }
}

async function solveSelectedBills() {
    const promises = selectedBills.value.map((bill) =>
        updateStatus(bill, "COMPLETED"),
    );
    await Promise.all(promises);

    selectedBills.value = null;
    toast.add({
        severity: "success",
        summary: "Successful",
        detail: "Bills Solved",
        life: 3000,
    });
}

async function closeSelectedBills() {
    const promises = selectedBills.value.map((bill) =>
        updateStatus(bill, "CANCELLED"),
    );
    await Promise.all(promises);

    selectedBills.value = null;
    toast.add({
        severity: "info",
        summary: "Successful",
        detail: "Bills Closed",
        life: 3000,
    });
}
</script>

<template>
    <div>
        <div class="card">
            <Toolbar class="mb-6">
                <template #start>
                    <Button
                        label="Solve"
                        icon="pi pi-check"
                        severity="success"
                        class="mr-2"
                        @click="solveSelectedBills"
                        :disabled="!selectedBills || !selectedBills.length"
                    />
                    <Button
                        label="Close"
                        icon="pi pi-times"
                        severity="danger"
                        @click="closeSelectedBills"
                        :disabled="!selectedBills || !selectedBills.length"
                    />
                </template>

                <template #end>
                    <!-- Export button removed -->
                </template>
            </Toolbar>

            <DataTable
                ref="dt"
                v-model:selection="selectedBills"
                :value="bills"
                dataKey="id"
                :paginator="true"
                :rows="10"
                :filters="filters"
                :loading="loading"
                paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                :rowsPerPageOptions="[5, 10, 25]"
                currentPageReportTemplate="Showing {first} to {last} of {totalRecords} bills"
            >
                <template #header>
                    <div
                        class="flex flex-wrap gap-2 items-center justify-between"
                    >
                        <h4 class="m-0">Manage Bills</h4>
                        <IconField>
                            <InputIcon>
                                <i class="pi pi-search" />
                            </InputIcon>
                            <InputText
                                v-model="filters['global'].value"
                                placeholder="Search..."
                            />
                        </IconField>
                    </div>
                </template>

                <Column
                    selectionMode="multiple"
                    style="width: 3rem"
                    :exportable="false"
                ></Column>
                <Column
                    field="code"
                    header="Code"
                    sortable
                    style="min-width: 10rem"
                ></Column>
                <Column
                    field="customer"
                    header="Customer"
                    sortable
                    style="min-width: 14rem"
                ></Column>
                <Column
                    field="date"
                    header="Date"
                    sortable
                    style="min-width: 10rem"
                ></Column>
                <Column
                    field="items"
                    header="Items"
                    sortable
                    style="min-width: 8rem"
                ></Column>
                <Column
                    field="amount"
                    header="Amount"
                    sortable
                    style="min-width: 10rem"
                >
                    <template #body="slotProps">
                        {{ formatCurrency(slotProps.data.amount) }}
                    </template>
                </Column>
                <Column
                    field="status"
                    header="Status"
                    sortable
                    style="min-width: 10rem"
                >
                    <template #body="slotProps">
                        <Tag
                            :value="slotProps.data.status.toUpperCase()"
                            :severity="getStatusLabel(slotProps.data.status)"
                        />
                    </template>
                </Column>
                <Column :exportable="false" style="min-width: 10rem">
                    <template #body="slotProps">
                        <Button
                            icon="pi pi-check"
                            outlined
                            rounded
                            class="mr-2"
                            severity="success"
                            @click="solveBill(slotProps.data)"
                        />
                        <Button
                            icon="pi pi-times"
                            outlined
                            rounded
                            severity="danger"
                            @click="closeBill(slotProps.data)"
                        />
                    </template>
                </Column>
            </DataTable>
        </div>
    </div>
</template>
