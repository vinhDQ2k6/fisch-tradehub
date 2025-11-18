# Security Guide: Vue + Spring Security (Auth & Authorization with Remember-Me)

Legend:

-   ☑ = already done in this project
-   ☐ = still to do / to verify

---

## 0. High-Level Architecture

| #   | Step                                                                              | Status                                   |
| --- | --------------------------------------------------------------------------------- | ---------------------------------------- |
| 0.1 | Decide on session-based auth (Spring Security + JSESSIONID + remember-me cookies) | ☑ (current design)                       |
| 0.2 | Frontend is a SPA (Vue 3 + PrimeVue + fetch)                                      | ☑                                        |
| 0.3 | Backend is Spring Boot + Spring Security + JPA + MySQL                            | ☑ (assumed / to wire)                    |
| 0.4 | Data model uses users, roles, and possibly permissions tables                     | ☑                                        |
| 0.5 | Transport uses `fetch` with `credentials: "include"` for cookies                  | ☑ (`apiFetch` + `login` already do this) |

---

## 1. Backend: Domain + Database (MySQL)

| #   | Step                                                                                       | Status                         |
| --- | ------------------------------------------------------------------------------------------ | ------------------------------ |
| 1.1 | Create `users` table (id, username, password, email, enabled, locked, etc.)                | ☑ (H2/MySQL entity exists)     |
| 1.2 | Create `roles` table (id, name: `ROLE_USER`, `ROLE_ADMIN`, …)                              | ☑ (via enum/role field)        |
| 1.3 | Create join table `user_roles` (user_id, role_id)                                          | ☐ (only simple role field now) |
| 1.4 | Optional: create `remember_me_tokens` table if using persistent remember-me                | ☐                              |
| 1.5 | Configure Spring Boot to connect to MySQL via `application.yml` / `application.properties` | ☐ (uses default dev DB)        |
| 1.6 | Add JPA entities `User`, `Role` and repositories                                           | ☑ (User + UserRepository)      |

**Backend outline (conceptual)**

-   `User` entity with fields:
    -   `username`, `password`, `email`, `enabled`, `accountNonLocked`, etc.
    -   `roles: Set<Role>`
-   `Role` entity with `name` (e.g. `ROLE_USER`).
-   Spring Data repositories:
    -   `UserRepository` with `findByUsername(String username)`.

---

## 2. Backend: Spring Security Configuration (Session + Remember-Me)

| #   | Step                                                                                                               | Status                        |
| --- | ------------------------------------------------------------------------------------------------------------------ | ----------------------------- |
| 2.1 | Add Spring Security dependency in `pom.xml` / `build.gradle`                                                       | ☑                             |
| 2.2 | Implement `UserDetailsService` backed by repository (`UserRepository`)                                             | ☑                             |
| 2.3 | Configure password encoder (`BCryptPasswordEncoder`)                                                               | ☑                             |
| 2.4 | Configure HTTP security with `/api/auth/login`, `/api/auth/logout`, `/api/auth/me`, `/api/auth/register` endpoints | ☑                             |
| 2.5 | Enable CSRF with cookie token (`XSRF-TOKEN`) compatible with SPA                                                   | ☑ (CookieCsrfTokenRepository) |
| 2.6 | Enable `remember-me` using Spring Security (token-based cookie)                                                    | ☑                             |
| 2.7 | Configure session management (max sessions, invalid session handling)                                              | ☐ (uses defaults)             |
| 2.8 | Add role-based authorization rules (`/admin/**` → `ROLE_ADMIN`, etc.)                                              | ☑ (basic antMatchers)         |

**Key Spring Security decisions**

-   **Authentication**:
    -   Form login endpoint at `/auth/login` (or `/login`), accept `username`, `password`, `"remember-me"` from form.
    -   On success, set `JSESSIONID` and optionally remember-me cookie.
-   **CSRF**:
    -   Use cookie `XSRF-TOKEN` and expect header `X-XSRF-TOKEN` on mutating requests.
-   **Remember-me**:
    -   Either “token-based cookie” or persistent token stored in `remember_me_tokens` table.
-   **Authorization**:
    -   Map routes and methods to required roles, e.g.:
        -   `/api/user/**` → `hasRole('USER')`
        -   `/api/admin/**` → `hasRole('ADMIN')`

---

## 3. Backend: Auth Endpoints

| #   | Step                                                                                  | Status                         |
| --- | ------------------------------------------------------------------------------------- | ------------------------------ |
| 3.1 | Implement `/api/auth/login` to authenticate user and issue session/remember-me        | ☑ (JSON body → `LoginRequest`) |
| 3.2 | Implement `/api/auth/logout` (POST) that clears session and remember-me cookie        | ☑ (via Spring `LogoutHandler`) |
| 3.3 | Implement `/api/auth/me` (GET) to return current user info and roles if authenticated | ☑                              |
| 3.4 | Implement `/api/auth/register` (POST) to create users in DB                           | ☑                              |
| 3.5 | Ensure `/api/auth/me` and `/api/auth/logout` require authentication                   | ☑                              |
| 3.6 | Ensure `/api/auth/register` and `/api/auth/login` are publicly accessible             | ☑                              |

**Recommended JSON responses**

-   `/auth/me` → `{ "username": "...", "roles": ["ROLE_USER", "ROLE_ADMIN"], ... }`
-   `/auth/register` → basic success or created user info:
    -   On conflict (user exists) respond with `409` and `{ "message": "User already exists" }`.

---

## 4. Frontend: Environment & Fetch Client

| #   | Step                                                                        | Status                        |
| --- | --------------------------------------------------------------------------- | ----------------------------- |
| 4.1 | Add `VITE_API_BASE` in `.env` (e.g. `http://localhost:8080`)                | ☑ (`front_end/.env`)          |
| 4.2 | Create `apiFetch` wrapper with base URL, CSRF, and JSON handling            | ☑ (`src/auth/fetchClient.js`) |
| 4.3 | Implement `getCsrfToken` reading `XSRF-TOKEN` cookie                        | ☑ (`src/auth/csrf.js`)        |
| 4.4 | Ensure `apiFetch` uses `credentials: "include"`                             | ☑                             |
| 4.5 | Ensure only non-GET/HEAD requests attach `X-XSRF-TOKEN` header              | ☑                             |
| 4.6 | Standardize error object thrown from `apiFetch` `{ status, data, message }` | ☑                             |

**Practical effects**

-   Every frontend request goes through `apiFetch` or explicit `fetch(...)` with:
    -   `credentials: "include"` so cookies (JSESSIONID + remember-me) work.
    -   Automatic JSON parsing and a consistent error shape, used by `showError`.

---

## 5. Frontend: Auth Service (Login/Logout/Me/Register)

| #   | Step                                                                                          | Status                  |
| --- | --------------------------------------------------------------------------------------------- | ----------------------- |
| 5.1 | Create `login` method that POSTs `/auth/login` with URL-encoded body and `"remember-me"` flag | ☑ (`authService.login`) |
| 5.2 | In `login`, when response not OK, throw `{ status, data, message }`                           | ☑                       |
| 5.3 | Create `logout` method using `apiFetch("/auth/logout", { method: "POST" })`                   | ☑                       |
| 5.4 | Create `currentUser` using `apiFetch("/auth/me", { csrf: false })`                            | ☑                       |
| 5.5 | Create `register` using `apiFetch("/auth/register", { method: "POST", body })`                | ☑                       |
| 5.6 | Ensure these functions are imported only in composables/views, not globally                   | ☑                       |

---

## 6. Frontend: Global Auth State (`useAuth` composable)

| #   | Step                                                                                                | Status                                 |
| --- | --------------------------------------------------------------------------------------------------- | -------------------------------------- |
| 6.1 | Create `useAuth` composable with `user`, `loading`, `ready` refs                                    | ☑ (`src/auth/useAuth.js`)              |
| 6.2 | Add `init()` that calls `currentUser()` on app start to hydrate `user`                              | ☑ (`App.vue` calls `useAuth().init()`) |
| 6.3 | Add `doLogin({ username, password, rememberMe })` that calls `authService.login` and updates `user` | ☑                                      |
| 6.4 | Add `doLogout()` that calls `authService.logout` and clears `user`                                  | ☑                                      |
| 6.5 | Add `isLoggedIn()` helper reading from `user`                                                       | ☑                                      |
| 6.6 | Keep this composable singleton-style (module-scoped refs)                                           | ☑                                      |

---

## 7. Frontend: Error & Success Handling

| #   | Step                                                                                         | Status                        |
| --- | -------------------------------------------------------------------------------------------- | ----------------------------- |
| 7.1 | Create `handleError` helper that uses `useToast()`                                           | ☑ (`src/auth/handleError.js`) |
| 7.2 | Map common HTTP statuses to friendly messages (400, 401, 403, 404, 409, 422, 429, 5xx, etc.) | ☑ (extended mapping)          |
| 7.3 | Detect network/abort errors without HTTP status and show appropriate messages                | ☑                             |
| 7.4 | Extract validation messages from typical API shapes (`data.errors`, `data.message`)          | ☑                             |
| 7.5 | Expose `showError(err, opts?)` and `showSuccess(message, opts?)`                             | ☑                             |
| 7.6 | Use `showError/showSuccess` inside `Login.vue` and `Register.vue`                            | ☑                             |

---

## 8. Frontend: Login View (Authentication Flow)

| #   | Step                                                                                    | Status                  |
| --- | --------------------------------------------------------------------------------------- | ----------------------- |
| 8.1 | Build `Login.vue` with PrimeVue `Form` & `yup` validation                               | ☑                       |
| 8.2 | Add fields: `username`, `password`, `rememberMe`                                        | ☑                       |
| 8.3 | On submit, validate; if invalid, show warning toast and stop                            | ☑                       |
| 8.4 | On valid submit, call `doLogin({ username, password, rememberMe })`                     | ☑                       |
| 8.5 | On success, show `showSuccess("Welcome back..., ...", { summary: "Login successful" })` | ☑                       |
| 8.6 | On success, navigate to `/` using `useRouter().push({ path: "/" })`                     | ☑                       |
| 8.7 | On error, call `showError(err, { summary: "Login failed" })`                            | ☑                       |
| 8.8 | Ensure `rememberMe` flag reaches backend as `"remember-me"` in body                     | ☑ (`authService.login`) |

---

## 9. Frontend: Register View (User Creation + Optional Auto-Login)

| #   | Step                                                                                                     | Status |
| --- | -------------------------------------------------------------------------------------------------------- | ------ |
| 9.1 | Build `Register.vue` with `username`, `email`, `password`, `confirmPassword`, `rememberMe`               | ☑      |
| 9.2 | Add `yup` validation for all fields and confirm password                                                 | ☑      |
| 9.3 | On invalid submit, show warning toast and stop                                                           | ☑      |
| 9.4 | On valid submit, call `authService.register({ username, email, password })`                              | ☑      |
| 9.5 | On success, show `showSuccess("Your account has been created.", { summary: "Registration successful" })` | ☑      |
| 9.6 | If `rememberMe` is checked, immediately call `doLogin(...)` then navigate `/`                            | ☑      |
| 9.7 | If `rememberMe` is not checked, navigate to `/auth/login`                                                | ☑      |
| 9.8 | On error (e.g. 409 user exists), call `showError(err, { summary: "Registration failed" })`               | ☑      |

---

## 10. Frontend: Route Guard & Protected Routes (Authorization)

| #    | Step                                                                                                        | Status                       |
| ---- | ----------------------------------------------------------------------------------------------------------- | ---------------------------- |
| 10.1 | Create `routeGuard.js` that uses `router.beforeEach`                                                        | ☑ (`src/auth/routeGuard.js`) |
| 10.2 | Mark protected routes in `router/index.js` with `meta.requiresAuth`                                         | ☑ (Dashboard, etc.)          |
| 10.3 | Optionally add `meta.roles: ["ROLE_ADMIN"]` for role-restricted routes                                      | ☑ (pattern supported)        |
| 10.4 | In guard, if `requiresAuth` and user not logged in, redirect to `/auth/login` and preserve `redirect` query | ☑                            |
| 10.5 | In guard, if `roles` are defined and user lacks roles, redirect to an access-denied page                    | ☑ (logic exists in scaffold) |
| 10.6 | Use `useAuth().init()` before first navigation so guard has user state                                      | ☑ (`App.vue`)                |

---

## 11. Frontend: Topbar & Layout Integration

| #    | Step                                                             | Status                    |
| ---- | ---------------------------------------------------------------- | ------------------------- |
| 11.1 | Use `useAuth()` in topbar to show Login/Register when logged out | ☑ (`AppTopbarPublic.vue`) |
| 11.2 | Show Profile / account button when logged in                     | ☑                         |
| 11.3 | Keep Configurator visible in both cases                          | ☑                         |
| 11.4 | Wire Logout action from topbar/profile menu using `doLogout()`   | ☑                         |
| 11.5 | Optionally show username in topbar (e.g. `user.value.username`)  | ☐                         |

> TODO: add username/avatar display in `AppTopbarPublic.vue` once `/api/auth/me` returns it consistently.

---

## 12. Backend: Role-Based Authorization with MySQL

| #    | Step                                                                     | Status                                          |
| ---- | ------------------------------------------------------------------------ | ----------------------------------------------- |
| 12.1 | Ensure `UserDetails` implementation exposes roles from DB                | ☐                                               |
| 12.2 | Ensure `/auth/me` endpoint returns roles in JSON                         | ☐                                               |
| 12.3 | Configure `HttpSecurity` to protect resources by role (e.g. `/admin/**`) | ☐                                               |
| 12.4 | Confirm that `useAuth().user.value.roles` reflects backend roles         | ☐                                               |
| 12.5 | Use `meta.roles` and route guard to enforce role on frontend             | ☑ (guard logic exists, needs data from backend) |
| 12.6 | Optional: use roles in the UI (hide/show admin menu, buttons)            | ☐                                               |

---

## 13. Testing the Full Flow

| #    | Step                                                                             | Status          |
| ---- | -------------------------------------------------------------------------------- | --------------- |
| 13.1 | Start backend (Spring Boot with MySQL/H2)                                        | ☑ (dev profile) |
| 13.2 | Start frontend (`npm install`, then `npm run dev`)                               | ☑               |
| 13.3 | Register a user, with remember-me checked                                        | ☑ (manual test) |
| 13.4 | Confirm session + remember-me cookies in browser                                 | ☑ (verified)    |
| 13.5 | Close browser, reopen, navigate to app → still authenticated (remember-me works) | ☑ (verified)    |
| 13.6 | Logout and confirm both session and remember-me cleared                          | ☑ (verified)    |
| 13.7 | Attempt to access protected route when logged out → redirected to login          | ☑ (route guard) |
| 13.8 | Attempt to access admin route as non-admin → access denied                       | ☐ (to verify)   |

---

### Next Steps

-   Finish DB hardening (roles join table, persistent remember-me tokens if needed).
-   Add role JSON to `/api/auth/me` and wire it into route guard + UI.
-   Display the current username from `useAuth().user` in the topbar.
-   Verify admin-only routes behave correctly with real role data.

---

## Appendix: Logout & Cookie Clearing

This project uses Spring Security's standard logout handling plus a custom `/api/auth/logout` endpoint. The goal is that logout invalidates the HTTP session **and** removes the remember-me cookie so the browser does not auto-login again.

### Backend: ensure logout clears cookies

-   In `SecurityConfig`:

    -   Configure logout to invalidate the HTTP session and delete cookies, typically:

        ```java
        http
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .deleteCookies("JSESSIONID", "remember-me")
                .invalidateHttpSession(true)
            );
        ```

-   If you call logout manually (e.g. in `AuthController.logout`):

    -   Use `SecurityContextLogoutHandler` which already invalidates the session and clears authentication.
    -   After that, explicitly remove the remember-me cookie:

        ```java
        @PostMapping("/api/auth/logout")
        public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response, Authentication auth) {
            new SecurityContextLogoutHandler().logout(request, response, auth);

            Cookie remember = new Cookie("remember-me", "");
            remember.setMaxAge(0);
            remember.setPath("/");
            response.addCookie(remember);

            return ResponseEntity.ok().build();
        }
        ```

> Use the **same cookie name** you configured in `.rememberMe()` (default is `remember-me`).

### Frontend: calling logout

-   In `authService.logout` (already implemented):
    -   Call `apiFetch("/api/auth/logout", { method: "POST" })` with `credentials: "include"`.
-   In `useAuth.doLogout()`:
    -   Await `logout()` and then set `user.value = null`.
-   In `AppTopbarPublic.vue`:
    -   Wire the logout button/menu item to `await doLogout()` and then navigate to `/`.

Once backend and frontend parts are wired as above, logging out will:

-   Invalidate the Spring Security session.
-   Delete the `remember-me` cookie in the browser.
-   Clear the client-side auth state so the topbar shows Login/Register again.
