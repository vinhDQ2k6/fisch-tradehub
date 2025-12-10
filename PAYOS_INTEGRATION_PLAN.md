# PayOS Integration Plan

## 1. Current State Analysis

### Frontend

- **Checkout**: `useCart.js` calls `/api/bills/checkout` to create a bill.
- **Payment**: `Debts.vue` calls `/api/bills/{id}/pay` which immediately marks the bill as `PROCESSING` (Mock payment).
- **Cancellation**: `Debts.vue` currently has a "Cancel" button calling `/api/bills/{id}/cancel`.

### Backend

- **BillService.checkout**: Creates `Bill` with status `PENDING_PAYMENT`.
- **BillService.payBill**: Updates `Bill` status to `PROCESSING`.

## 2. Integration Strategy

We will replace the mock `payBill` logic with a real PayOS integration.

### Flow

1.  **User** clicks "Pay" in `Debts.vue`.
2.  **Frontend** calls `POST /api/payment/create-payment-link/{billId}`.
3.  **Backend**:
    - Validates Bill ownership and status.
    - Calls PayOS API to create a payment link.
    - **Return URLs**:
      - `returnUrl`: `http://localhost:5173/payment/success` (or similar)
      - `cancelUrl`: `http://localhost:5173/payment/cancel`
    - Returns `checkoutUrl` to Frontend.
4.  **Frontend** redirects user to `checkoutUrl`.
5.  **User** interacts with PayOS gateway.

### Handling Outcomes

#### Scenario A: Payment Success

1.  **PayOS** redirects user to `returnUrl`.
2.  **PayOS** sends Webhook (`type: "success"`) to Backend.
3.  **Backend** verifies webhook and updates Bill status: `PENDING_PAYMENT` -> `PROCESSING`.

#### Scenario B: Payment Failed / Cancelled / Expired

1.  **User** clicks "Cancel" on PayOS -> Redirects to `cancelUrl`.
2.  **PayOS** sends Webhook (`type: "cancelled"` or similar, if supported) OR Backend handles timeout.
3.  **Backend** updates Bill status: `PENDING_PAYMENT` -> `CANCELLED`.
    - _Note_: Since PayOS handles the cancellation/expiration, we can rely on the Webhook or the Admin API to cancel the bill.
    - **User Cancel API**: The user's manual "Cancel" button on the frontend will strictly call the User API (`/api/bills/{id}/cancel`). It will NOT use the Admin API. This allows users to cancel their own pending bills.
    - **Decision**: We will keep the User Cancel API active. Users can cancel their bills manually. If PayOS reports a cancellation/failure, the system will also update the status to `CANCELLED`.

## 3. Implementation Steps

### Step 1: Backend Dependencies & Configuration

- Add `payos-java` dependency (or use `RestClient`).
- Add configuration in `application.properties`:
  ```properties
  payos.client-id=${PAYOS_CLIENT_ID}
  payos.api-key=${PAYOS_API_KEY}
  payos.checksum-key=${PAYOS_CHECKSUM_KEY}
  ```

### Step 2: Backend Payment Service

- Create `PayOSService` to handle interaction with PayOS API.
- Create `PaymentController` with endpoints:
  - `POST /api/payment/create-payment-link/{billId}`
  - `POST /api/payment/payos-webhook` (Publicly accessible)
- **Cleanup**: Remove `POST /api/bills/{id}/pay` from `BillController`. The user cannot manually mark a bill as paid anymore; they must go through PayOS.
- **Admin API**: Confirmed that `AdminBillController` only has `complete` and `cancel`. It correctly relies on the system/webhook to handle the `PENDING` -> `PROCESSING` transition.

### Step 3: Frontend Updates

- Modify `Debts.vue`:
  - Change "Pay" button action to call the new payment link endpoint.
  - Handle the redirect to the returned URL.
  - (Optional) Remove "Cancel" button if we want to strictly follow the "PayOS handles everything" logic, but recommended to keep for pre-payment cancellation.
- Create `PaymentResult.vue` to handle `returnUrl` and `cancelUrl` redirects (show success/fail message).

### Step 4: Webhook Handling

- Implement `handleWebhook` in `PaymentController`.
- Verify signature.
- **Logic**:
  - **Success**: Call `BillService.confirmPayment(billId)`. (Need to add this method to `BillService` to allow system-level update without username).
  - **Cancelled**: Call `BillService.adminCancelBill(billId)` (or similar system method).
- **Idempotency**: Ensure `confirmPayment` checks if the bill is already `PROCESSING` or `COMPLETED`. If so, ignore the request to prevent duplicate processing.
- **Note**: The Webhook does NOT call the Admin API endpoints. It calls the `BillService` directly to update the database.

### Step 5: Handling Race Conditions (Frontend)

- When the user is redirected to `returnUrl` (Success), the Webhook might not have arrived yet.
- **Strategy**: The `PaymentResult.vue` page should display a "Processing..." state and poll the `GET /api/bills/{id}` endpoint every few seconds until the status changes to `PROCESSING`, or timeout after a minute.

## 4. Detailed Tasks

1.  **Add Dependency**: Add `com.lib:payos-java` (if available) or setup `RestClient`.
2.  **Config**: Update `application.properties`.
3.  **Service**:
    - Implement `PayOSService.createPaymentLink(Bill bill)`.
    - Add `BillService.confirmPayment(Long billId)` (System level payment confirmation).
4.  **Controller**: Implement `PaymentController`.
5.  **Security**: Allow anonymous access to `/api/payment/payos-webhook`.
6.  **Frontend**: Update `Debts.vue` and add `PaymentResult.vue`.

## 5. Data Model Changes

- No major changes needed to `Bill` entity.
- Might need to store `paymentLinkId` or `orderCode` if different from `billId`. (PayOS requires integer orderCode, `billId` is Long, so it fits).
