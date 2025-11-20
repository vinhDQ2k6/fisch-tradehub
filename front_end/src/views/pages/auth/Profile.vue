<script setup>
import { apiFetch } from "@/auth/fetchClient";
import { useAuth } from "@/auth/useAuth";
import FloatingConfigurator from "@/components/FloatingConfigurator.vue";
import { yupResolver } from "@primevue/forms/resolvers/yup";
import { IconField } from "primevue";
import { useToast } from "primevue/usetoast";
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import * as yup from "yup";

const toast = useToast();
const router = useRouter();
const { user } = useAuth();
const loading = ref(true);
const submitting = ref(false);
const error = ref(null);

const initialValues = ref({
    fullname: "",
    email: "",
    username: "",
    age: null,
    gender: null,
    password: "",
});

const genderOptions = [
    { label: "Male", value: true },
    { label: "Female", value: false },
];

const resolver = yupResolver(
    yup.object({
        fullname: yup.string().max(100, "Max 100 characters"),
        age: yup.number().min(0).max(150).nullable().label("Age"),
    }),
);

onMounted(async () => {
    try {
        if (user.value) {
            initialValues.value.email = user.value.email || "";
            initialValues.value.username = user.value.username || "";
        }

        const { data } = await apiFetch("/api/user/info");
        if (data) {
            initialValues.value.fullname = data.fullname || "";
            initialValues.value.age = data.age;
            initialValues.value.gender = data.gender;
        }
    } catch (e) {
        console.error("Failed to load user info", e);
        error.value = e;
        if (e.status === 403) {
            toast.add({
                severity: "error",
                summary: "Access Denied",
                detail: "You do not have permission to view this profile.",
                life: 5000,
            });
        } else {
            toast.add({
                severity: "error",
                summary: "Error",
                detail: "Failed to load profile data.",
                life: 3000,
            });
        }
    } finally {
        loading.value = false;
    }
});

const onFormSubmit = async ({ valid, values }) => {
    if (!valid) {
        toast.add({
            severity: "warn",
            summary: "Validation error",
            detail: "Please fix the highlighted fields.",
            life: 3500,
        });
        return;
    }

    submitting.value = true;
    try {
        const payload = {
            fullname: values.fullname,
            age: values.age,
            gender: values.gender,
        };

        await apiFetch("/api/user/info", {
            method: "POST",
            body: payload,
        });

        toast.add({
            severity: "success",
            summary: "Profile saved",
            detail: "Your profile has been updated.",
            life: 3000,
        });
    } catch (e) {
        toast.add({
            severity: "error",
            summary: "Error",
            detail: "Failed to update profile.",
            life: 3000,
        });
    } finally {
        submitting.value = false;
    }
};

const goBack = () => {
    router.back();
};
</script>

<template>
    <FloatingConfigurator />
    <div
        class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-screen overflow-hidden"
    >
        <div class="flex flex-col items-center justify-center">
            <div
                style="
                    border-radius: 56px;
                    padding: 0.3rem;
                    background: linear-gradient(
                        180deg,
                        var(--primary-color) 10%,
                        rgba(33, 150, 243, 0) 30%
                    );
                "
            >
                <div
                    class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20"
                    style="border-radius: 53px"
                >
                    <div class="text-center mb-8">
                        <div
                            class="text-surface-900 dark:text-surface-0 text-3xl font-medium mb-2"
                        >
                            Profile Settings
                        </div>
                        <span class="text-muted-color font-medium">
                            View and update your account information.
                        </span>
                    </div>

                    <div v-if="loading" class="flex justify-center p-10">
                        <i class="pi pi-spin pi-spinner text-4xl"></i>
                    </div>

                    <Form
                        v-else
                        :initialValues
                        :resolver
                        @submit="onFormSubmit"
                        class="grid grid-cols-1 gap-4"
                    >
                        <FormField
                            v-slot="$field"
                            name="fullname"
                            initialValue=""
                            class="flex flex-col gap-1"
                            fluid
                        >
                            <label
                                for="fullname"
                                class="block text-surface-900 dark:text-surface-0 text-xl font-medium"
                                >Full Name</label
                            >
                            <InputText
                                id="fullname"
                                type="text"
                                placeholder="Your full name"
                                v-model="$field.value"
                                fluid
                            />
                            <Message
                                v-if="$field?.invalid"
                                severity="error"
                                size="small"
                                variant="simple"
                                >{{ $field.error?.message }}</Message
                            >
                        </FormField>

                        <div class="grid grid-cols-2 gap-4">
                            <FormField
                                v-slot="$field"
                                name="age"
                                class="flex flex-col gap-1"
                                fluid
                            >
                                <label
                                    for="age"
                                    class="block text-surface-900 dark:text-surface-0 text-xl font-medium"
                                    >Age</label
                                >
                                <InputNumber
                                    id="age"
                                    placeholder="Age"
                                    v-model="$field.value"
                                    fluid
                                />
                                <Message
                                    v-if="$field?.invalid"
                                    severity="error"
                                    size="small"
                                    variant="simple"
                                    >{{ $field.error?.message }}</Message
                                >
                            </FormField>

                            <FormField
                                v-slot="$field"
                                name="gender"
                                class="flex flex-col gap-1"
                                fluid
                            >
                                <label
                                    for="gender"
                                    class="block text-surface-900 dark:text-surface-0 text-xl font-medium"
                                    >Gender</label
                                >
                                <Dropdown
                                    id="gender"
                                    :options="genderOptions"
                                    optionLabel="label"
                                    optionValue="value"
                                    placeholder="Select Gender"
                                    v-model="$field.value"
                                    fluid
                                />
                                <Message
                                    v-if="$field?.invalid"
                                    severity="error"
                                    size="small"
                                    variant="simple"
                                    >{{ $field.error?.message }}</Message
                                >
                            </FormField>
                        </div>

                        <FormField
                            v-slot="$field"
                            name="email"
                            initialValue=""
                            class="flex flex-col gap-1"
                            fluid
                        >
                            <label
                                for="email"
                                class="block text-surface-900 dark:text-surface-0 text-xl font-medium"
                                >Email</label
                            >
                            <IconField>
                                <InputIcon class="pi pi-envelope" />
                                <InputText
                                    id="email"
                                    type="email"
                                    placeholder="Email address"
                                    v-model="$field.value"
                                    disabled
                                    fluid
                                />
                            </IconField>
                            <Message
                                v-if="$field?.invalid"
                                severity="error"
                                size="small"
                                variant="simple"
                                >{{ $field.error?.message }}</Message
                            >
                        </FormField>

                        <FormField
                            v-slot="$field"
                            name="username"
                            initialValue=""
                            class="flex flex-col gap-1"
                            fluid
                        >
                            <label
                                for="username"
                                class="block text-surface-900 dark:text-surface-0 text-xl font-medium"
                                >Username</label
                            >
                            <IconField>
                                <InputIcon class="pi pi-user" />
                                <InputText
                                    id="username"
                                    type="text"
                                    placeholder="Username"
                                    v-model="$field.value"
                                    disabled
                                    fluid
                                />
                            </IconField>
                            <Message
                                v-if="$field?.invalid"
                                severity="error"
                                size="small"
                                variant="simple"
                                >{{ $field.error?.message }}</Message
                            >
                        </FormField>

                        <FormField
                            v-slot="$field"
                            name="password"
                            initialValue=""
                            class="flex flex-col gap-1"
                            fluid
                        >
                            <label
                                for="password"
                                class="block text-surface-900 dark:text-surface-0 font-medium text-xl"
                                >Password</label
                            >
                            <IconField>
                                <InputIcon class="pi pi-lock" />
                                <Password
                                    id="password"
                                    toggleMask
                                    promptLabel="Choose a strong password"
                                    weakLabel="Weak"
                                    mediumLabel="Medium"
                                    strongLabel="Strong"
                                    placeholder="********"
                                    v-model="$field.value"
                                    disabled
                                    fluid
                                />
                            </IconField>
                            <Message
                                v-if="$field?.invalid"
                                severity="error"
                                size="small"
                                variant="simple"
                                >{{ $field.error?.message }}</Message
                            >
                        </FormField>

                        <div class="flex gap-4">
                            <Button
                                type="button"
                                label="Return"
                                icon="pi pi-arrow-left"
                                severity="secondary"
                                fluid
                                class="flex-1"
                                @click="goBack"
                            />
                            <Button
                                type="submit"
                                icon="pi pi-save"
                                label="Save changes"
                                fluid
                                class="flex-1"
                                :loading="submitting"
                                :disabled="!!error"
                            />
                        </div>
                    </Form>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.pi-eye {
    transform: scale(1.6);
    margin-right: 1rem;
}

.pi-eye-slash {
    transform: scale(1.6);
    margin-right: 1rem;
}
</style>
