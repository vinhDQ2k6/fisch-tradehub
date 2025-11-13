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
    password: "",
    rememberme: false,
});
const resolver = ref(
    yupResolver(
        yup.object({
            username: yup.string().required("Username is required"),
            password: yup.string().required("Password is required"),
        }),
    ),
);

const onFormSubmit = async (e) => {
    if (!e.valid) {
        toast.add({
            severity: "warn",
            summary: "Validation error",
            detail: "Please fill in all required fields.",
            life: 3500,
        });
        return;
    }

    const url = "";
    const payload = new URLSearchParams({
        username: e.values.username,
        password: e.values.password,
        "remember-me": String(Boolean(e.values.rememberme)),
    }).toString();

    try {
        const res = await fetch(url, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: payload,
            credentials: "include",
        });

        switch (res.status) {
            case 200:
            case 201: {
                const data = await res.json();

                if (data?.accessToken) {
                    toast.add({
                        severity: "success",
                        summary: "Login successful",
                        detail: `Welcome back!, ${data.username}`,
                        life: 3000,
                    });
                    console.log("Login successful:", data);

                    return;
                }

                toast.add({
                    severity: "error",
                    summary: "Login failed",
                    detail:
                        data?.message ||
                        `Invalid credentials, ${data.username}`,
                    life: 4500,
                });
                return;
            }
            case 400:
            case 401:
                toast.add({
                    severity: "error",
                    summary: "Login failed",
                    detail: "Invalid username or password.",
                    life: 4500,
                });
                return;
            case 429:
                toast.add({
                    severity: "error",
                    summary: "Too many attempts",
                    detail: "Please wait and try again later.",
                    life: 4500,
                });
                return;
            case 423:
                toast.add({
                    severity: "error",
                    summary: "Account locked",
                    detail: "Your account is locked. Contact support.",
                    life: 4500,
                });
                return;
            default:
                toast.add({
                    severity: "error",
                    summary: "Login failed",
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
        console.error("Login error", err);
    }
};
</script>

<template>
    <FloatingConfigurator />
    <Toast />
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
                            >Don't have an account?
                            <RouterLink
                                to="/auth/register"
                                class="text-primary font-semibold"
                                >Create today!</RouterLink
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

                        <div class="flex items-center justify-between gap-8">
                            <div class="flex items-center">
                                <Checkbox
                                    name="rememberme"
                                    v-model="rememberme"
                                    id="rememberme"
                                    binary
                                    class="mr-2"
                                ></Checkbox>
                                <label for="rememberme">Remember me</label>
                            </div>
                            <RouterLink
                                to="/auth/error"
                                class="font-medium no-underline ml-2 text-right cursor-pointer text-primary"
                                >Forgot password?</RouterLink
                            >
                        </div>

                        <Button
                            type="submit"
                            icon="pi pi-sign-in"
                            label="Login"
                            fluid
                        ></Button>
                    </Form>

                    <Divider align="center" type="solid">
                        <b>Or</b>
                    </Divider>

                    <div class="flex gap-4">
                        <Button
                            icon="pi pi-google"
                            severity="danger"
                            class="p-button-outlined p-button-google flex-1 w-full"
                        ></Button>
                        <Button
                            icon="pi pi-facebook"
                            severity="info"
                            class="p-button-outlined p-button-facebook flex-1 w-full"
                        ></Button>
                        <Button
                            icon="pi pi-github"
                            severity="contrast"
                            class="p-button-outlined p-button-github flex-1 w-full"
                        ></Button>
                    </div>
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
