<script setup>
import FloatingConfigurator from "@/components/FloatingConfigurator.vue";
import { yupResolver } from "@primevue/forms/resolvers/yup";
import { IconField } from "primevue";
import { useToast } from "primevue/usetoast";
import { ref } from "vue";
import * as yup from "yup";

const toast = useToast();
const initialValues = ref({
    username: "",
    email: "",
    password: "",
    confirmPassword: "",
    rememberme: false,
});

const resolver = ref(
    yupResolver(
        yup.object({
            username: yup.string().required("Username is required"),
            email: yup
                .string()
                .email("Enter a valid email")
                .required("Email is required"),
            password: yup
                .string()
                .min(6, "Password must be at least 6 characters")
                .required("Password is required"),
            confirmPassword: yup
                .string()
                .oneOf([yup.ref("password")], "Passwords must match")
                .required("Please confirm your password"),
        }),
    ),
);

const onFormSubmit = async (e) => {
    if (!e.valid) {
        toast.add({
            severity: "warn",
            summary: "Validation error",
            detail: "Please fix the highlighted fields.",
            life: 3500,
        });
        return;
    }

    const url = "https://dummyjson.com/users/add";
    try {
        const res = await fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                username: e.values.username,
                email: e.values.email,
                password: e.values.password,
            }),
        });

        console.log(res);

        if (!res) {
            toast.add({
                severity: "error",
                summary: "Registration failed",
                detail: "No response from server.",
                life: 4500,
            });
            return;
        }

        switch (res.status) {
            case 200:
            case 201: {
                const data = await res.json();
                toast.add({
                    severity: "success",
                    summary: "Registration successful",
                    detail: "Your account has been created.",
                    life: 3500,
                });
                console.log("register success", data);
                // TODO: redirect to login or auto-login
                return;
            }
            case 409:
                toast.add({
                    severity: "error",
                    summary: "Registration failed",
                    detail: "User already exists.",
                    life: 4500,
                });
                return;
            case 429:
                toast.add({
                    severity: "error",
                    summary: "Too many requests",
                    detail: "Please wait and try again later.",
                    life: 4500,
                });
                return;
            default:
                toast.add({
                    severity: "error",
                    summary: "Registration failed",
                    detail: `Server returned status ${res.status}.`,
                    life: 4500,
                });
                return;
        }
    } catch (err) {
        toast.add({
            severity: "error",
            summary: "Network error",
            detail:
                err?.message || "Network error. Please check your connection.",
            life: 4500,
        });
        console.error("Register error", err);
    }
};
</script>

<template>
    <FloatingConfigurator />
    <Toast></Toast>
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
                        <RouterLink to="/" class="block w-fit mx-auto">
                            <img
                                src="https://static.wikitide.net/fischwiki/8/8c/FischWikiFavicon192x192.png"
                                alt="Fisch-TradeHub Logo"
                                class="mb-8 w-16 shrink-0"
                            />
                        </RouterLink>
                        <div
                            class="text-surface-900 dark:text-surface-0 text-3xl font-medium mb-4"
                        >
                            Welcome to Fisch-TradeHub!
                        </div>
                        <span class="text-muted-color font-medium"
                            >Already have an account?
                            <RouterLink
                                to="/auth/login"
                                class="text-primary font-semibold"
                                >Login here!</RouterLink
                            >
                        </span>
                    </div>

                    <Form
                        :initialValues
                        :resolver
                        @submit="onFormSubmit"
                        class="grid grid-cols-1 gap-4"
                    >
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
                                    v-model="username"
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
                                    placeholder="Email"
                                    v-model="email"
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
                                    placeholder="Password"
                                    v-model="password"
                                    :feedback="false"
                                    fluid
                                ></Password>
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
                            name="confirmPassword"
                            initialValue=""
                            class="flex flex-col gap-1"
                            fluid
                        >
                            <label
                                for="confirmPassword"
                                class="block text-surface-900 dark:text-surface-0 font-medium text-xl"
                                >Confirm Password</label
                            >
                            <IconField>
                                <InputIcon class="pi pi-lock" />
                                <Password
                                    id="confirmPassword"
                                    toggleMask
                                    placeholder="Confirm Password"
                                    v-model="confirmPassword"
                                    :feedback="false"
                                    fluid
                                ></Password>
                            </IconField>
                            <Message
                                v-if="$field?.invalid"
                                severity="error"
                                size="small"
                                variant="simple"
                                >{{ $field.error?.message }}</Message
                            >
                        </FormField>

                        <div class="flex items-center justify-between gap-8">
                            <div class="flex items-center">
                                <Checkbox
                                    name="rememberme"
                                    v-model="rememberme"
                                    id="rememberme"
                                    binary
                                    class="mr-2"
                                ></Checkbox>
                                <label for="rememberme"
                                    >Remember me after I created my
                                    account.</label
                                >
                            </div>
                        </div>

                        <Button type="submit" label="Register" fluid></Button>
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
