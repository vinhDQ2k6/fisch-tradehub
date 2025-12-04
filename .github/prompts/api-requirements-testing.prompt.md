# API REQUIREMENTS & TESTING

> 📋 **Tài liệu liên quan**: [Kế hoạch xây dựng](./plan-backendFeatures.prompt.md)

---

## 1. TỔNG QUAN API

### 1.1 Base URL

```
Development: http://localhost:8080/api
Production:  https://api.tradehub.com/api
```

### 1.2 Authentication

| Type          | Header | Value                     |
| ------------- | ------ | ------------------------- |
| Session-based | Cookie | `JSESSIONID=<session_id>` |

### 1.3 Response Format

**Success Response:**

```json
{
  "data": { ... },
  "message": "Success message (optional)"
}
```

**Error Response:**

```json
{
  "timestamp": "2024-12-02T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Detailed error message",
  "path": "/api/endpoint"
}
```

### 1.4 HTTP Status Codes

| Code | Meaning                        |
| ---- | ------------------------------ |
| 200  | OK - Request succeeded         |
| 201  | Created - Resource created     |
| 204  | No Content - Delete successful |
| 400  | Bad Request - Validation error |
| 401  | Unauthorized - Not logged in   |
| 403  | Forbidden - No permission      |
| 404  | Not Found - Resource not found |
| 409  | Conflict - Duplicate resource  |
| 500  | Internal Server Error          |

---

## 2. API SPECIFICATION

### 2.1 Admin User Management APIs

#### GET /api/admin/users

Lấy danh sách tất cả users.

**Authorization:** `ROLE_ADMIN`

**Response 200:**

```json
[
  {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "role": "ROLE_ADMIN",
    "active": true,
    "createdAt": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "username": "user1",
    "email": "user1@example.com",
    "role": "ROLE_USER",
    "active": true,
    "createdAt": "2024-02-20T14:00:00"
  }
]
```

**Response 401:** Unauthorized (not logged in)

**Response 403:** Forbidden (not admin)

---

#### GET /api/admin/users/{id}

Lấy thông tin chi tiết 1 user.

**Authorization:** `ROLE_ADMIN`

**Path Parameters:**

| Name | Type | Required | Description |
| ---- | ---- | -------- | ----------- |
| id   | Long | Yes      | User ID     |

**Response 200:**

```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@example.com",
  "role": "ROLE_ADMIN",
  "active": true,
  "createdAt": "2024-01-15T10:30:00"
}
```

**Response 404:** User not found

---

#### PUT /api/admin/users/{id}

Cập nhật thông tin user (role, active status).

**Authorization:** `ROLE_ADMIN`

**Path Parameters:**

| Name | Type | Required | Description |
| ---- | ---- | -------- | ----------- |
| id   | Long | Yes      | User ID     |

**Request Body:**

```json
{
  "role": "ROLE_STAFF",
  "active": true
}
```

| Field  | Type    | Required | Validation                                 |
| ------ | ------- | -------- | ------------------------------------------ |
| role   | String  | Yes      | Must be: ROLE_USER, ROLE_STAFF, ROLE_ADMIN |
| active | Boolean | No       | If null, no change                         |

**Response 200:**

```json
{
  "id": 1,
  "username": "user1",
  "email": "user1@example.com",
  "role": "ROLE_STAFF",
  "active": true,
  "createdAt": "2024-02-20T14:00:00"
}
```

**Response 400:** Invalid role / Cannot disable self

**Response 404:** User not found

---

#### DELETE /api/admin/users/{id}

Soft delete user (set active=false).

**Authorization:** `ROLE_ADMIN`

**Path Parameters:**

| Name | Type | Required | Description |
| ---- | ---- | -------- | ----------- |
| id   | Long | Yes      | User ID     |

**Response 204:** No Content (success)

**Response 400:** Cannot disable self

**Response 404:** User not found

---

### 2.2 Admin Bill Management APIs

#### GET /api/admin/bills

Lấy danh sách tất cả bills (có thể filter theo status).

**Authorization:** `ROLE_ADMIN`

**Query Parameters:**

| Name   | Type       | Required | Description      |
| ------ | ---------- | -------- | ---------------- |
| status | BillStatus | No       | Filter by status |

**BillStatus Values:** `PENDING_PAYMENT`, `PROCESSING`, `COMPLETED`, `CANCELLED`

**Response 200:**

```json
[
  {
    "id": 1,
    "buyerId": 2,
    "buyerUsername": "user1",
    "totalAmount": 1500000,
    "status": "PROCESSING",
    "createdAt": "2024-12-01T09:00:00",
    "closedAt": null,
    "items": [
      {
        "fishId": 1,
        "fishName": "Koi Fish",
        "quantity": 2,
        "price": 750000
      }
    ]
  }
]
```

---

#### GET /api/admin/bills/{id}

Lấy chi tiết 1 bill.

**Authorization:** `ROLE_ADMIN`

**Path Parameters:**

| Name | Type | Required | Description |
| ---- | ---- | -------- | ----------- |
| id   | Long | Yes      | Bill ID     |

**Response 200:** (same as single item in list)

**Response 404:** Bill not found

---

#### POST /api/admin/bills/{id}/complete

Đánh dấu bill đã hoàn thành (PROCESSING → COMPLETED).

**Authorization:** `ROLE_ADMIN`

**Path Parameters:**

| Name | Type | Required | Description |
| ---- | ---- | -------- | ----------- |
| id   | Long | Yes      | Bill ID     |

**Response 200:**

```json
{
  "id": 1,
  "status": "COMPLETED",
  "closedAt": "2024-12-02T10:30:00",
  ...
}
```

**Response 400:** Only PROCESSING bills can be completed

**Response 404:** Bill not found

---

### 2.3 Payment APIs

#### POST /api/payments/{billId}

Thanh toán bill.

**Authorization:** Authenticated user (bill owner only)

**Path Parameters:**

| Name   | Type | Required | Description |
| ------ | ---- | -------- | ----------- |
| billId | Long | Yes      | Bill ID     |

**Response 200:**

```json
{
  "success": true,
  "transactionId": "MOCK-A1B2C3D4",
  "message": "Payment successful (Mock)",
  "gatewayName": "MockGateway",
  "billId": 1,
  "newStatus": "PROCESSING"
}
```

**Response 400:**

- Bill is not in PENDING_PAYMENT status
- Bill already paid

**Response 403:** You can only pay for your own bills

**Response 404:** Bill not found

---

### 2.4 Password Reset APIs

#### POST /api/auth/forgot-password

Yêu cầu đặt lại mật khẩu.

**Authorization:** Public (no auth required)

**Request Body:**

```json
{
  "email": "user@example.com"
}
```

| Field | Type   | Required | Validation         |
| ----- | ------ | -------- | ------------------ |
| email | String | Yes      | Valid email format |

**Response 200:**

```json
{
  "message": "If the email exists, a reset link has been sent"
}
```

> ⚠️ **Security Note:** Response is same whether email exists or not.

---

#### POST /api/auth/reset-password

Đặt lại mật khẩu với token.

**Authorization:** Public (no auth required)

**Request Body:**

```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "newSecurePassword123"
}
```

| Field       | Type   | Required | Validation       |
| ----------- | ------ | -------- | ---------------- |
| token       | String | Yes      | UUID format      |
| newPassword | String | Yes      | Min 6 characters |

**Response 200:**

```json
{
  "message": "Password has been reset successfully"
}
```

**Response 400:**

- Invalid or expired reset token
- Reset token has expired
- Reset token has already been used

---

## 3. UNIT TEST SPECIFICATION

### 3.1 AdminUserServiceTest

```java
@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    // ========== Test Data Helpers ==========

    private User createTestUser(Long id, String username, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    // ========== getAllUsers Tests ==========

    @Test
    @DisplayName("getAllUsers - Should return all users")
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        List<User> users = List.of(
            createTestUser(1L, "admin", "ROLE_ADMIN"),
            createTestUser(2L, "user1", "ROLE_USER")
        );
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<AdminUserDTO> result = adminUserService.getAllUsers();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).username()).isEqualTo("admin");
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllUsers - Should return empty list when no users")
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsers() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<AdminUserDTO> result = adminUserService.getAllUsers();

        assertThat(result).isEmpty();
    }

    // ========== getUserById Tests ==========

    @Test
    @DisplayName("getUserById - Should return user when exists")
    void getUserById_ShouldReturnUser_WhenExists() {
        User user = createTestUser(1L, "admin", "ROLE_ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AdminUserDTO result = adminUserService.getUserById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("admin");
    }

    @Test
    @DisplayName("getUserById - Should throw when user not found")
    void getUserById_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.getUserById(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found");
    }

    // ========== updateUser Tests ==========

    @Test
    @DisplayName("updateUser - Should update role successfully")
    void updateUser_ShouldUpdateRole_WhenValid() {
        User user = createTestUser(1L, "user1", "ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UpdateUserRequest request = new UpdateUserRequest("ROLE_STAFF", null);
        AdminUserDTO result = adminUserService.updateUser(1L, request, "admin");

        assertThat(result.role()).isEqualTo("ROLE_STAFF");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser - Should throw when admin disables self")
    void updateUser_ShouldThrow_WhenAdminDisablesSelf() {
        User adminUser = createTestUser(1L, "admin", "ROLE_ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        UpdateUserRequest request = new UpdateUserRequest("ROLE_ADMIN", false);

        assertThatThrownBy(() ->
            adminUserService.updateUser(1L, request, "admin"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Cannot disable your own account");
    }

    @Test
    @DisplayName("updateUser - Should throw when invalid role")
    void updateUser_ShouldThrow_WhenInvalidRole() {
        User user = createTestUser(1L, "user1", "ROLE_USER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UpdateUserRequest request = new UpdateUserRequest("ROLE_INVALID", null);

        assertThatThrownBy(() ->
            adminUserService.updateUser(1L, request, "admin"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Invalid role");
    }

    // ========== softDeleteUser Tests ==========

    @Test
    @DisplayName("softDeleteUser - Should set active false")
    void softDeleteUser_ShouldSetActiveFalse() {
        User user = createTestUser(2L, "user1", "ROLE_USER");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        adminUserService.softDeleteUser(2L, "admin");

        assertThat(user.isActive()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("softDeleteUser - Should throw when deleting self")
    void softDeleteUser_ShouldThrow_WhenDeletingSelf() {
        User adminUser = createTestUser(1L, "admin", "ROLE_ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThatThrownBy(() ->
            adminUserService.softDeleteUser(1L, "admin"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Cannot disable your own account");
    }
}
```

---

### 3.2 BillServiceTest (Bổ sung completeBill)

```java
@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private BillService billService;

    // ========== completeBill Tests ==========

    @Test
    @DisplayName("completeBill - Should complete when status is PROCESSING")
    void completeBill_ShouldComplete_WhenStatusProcessing() {
        // Arrange
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setStatus(BillStatus.PROCESSING);

        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(billRepository.save(any(Bill.class))).thenReturn(bill);

        // Act
        BillDTO result = billService.completeBill(1L);

        // Assert
        assertThat(result.status()).isEqualTo(BillStatus.COMPLETED);
        assertThat(bill.getClosedAt()).isNotNull();
        verify(billRepository).save(bill);
    }

    @Test
    @DisplayName("completeBill - Should throw when status is PENDING_PAYMENT")
    void completeBill_ShouldThrow_WhenStatusPendingPayment() {
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setStatus(BillStatus.PENDING_PAYMENT);

        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        assertThatThrownBy(() -> billService.completeBill(1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Only PROCESSING bills can be completed");
    }

    @Test
    @DisplayName("completeBill - Should throw when status is COMPLETED")
    void completeBill_ShouldThrow_WhenAlreadyCompleted() {
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setStatus(BillStatus.COMPLETED);

        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        assertThatThrownBy(() -> billService.completeBill(1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Only PROCESSING bills can be completed");
    }

    @Test
    @DisplayName("completeBill - Should throw when bill not found")
    void completeBill_ShouldThrow_WhenBillNotFound() {
        when(billRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> billService.completeBill(999L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Bill not found");
    }

    // ========== getBillsByStatus Tests ==========

    @Test
    @DisplayName("getBillsByStatus - Should filter by status")
    void getBillsByStatus_ShouldFilterByStatus() {
        Bill bill1 = createBillWithStatus(1L, BillStatus.PROCESSING);
        Bill bill2 = createBillWithStatus(2L, BillStatus.PROCESSING);

        when(billRepository.findByStatus(BillStatus.PROCESSING))
            .thenReturn(List.of(bill1, bill2));

        List<BillDTO> result = billService.getBillsByStatus(BillStatus.PROCESSING);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(b -> b.status() == BillStatus.PROCESSING);
    }

    private Bill createBillWithStatus(Long id, BillStatus status) {
        Bill bill = new Bill();
        bill.setId(id);
        bill.setStatus(status);
        return bill;
    }
}
```

---

### 3.3 PaymentServiceTest

```java
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private BillRepository billRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentService paymentService;

    // ========== Test Data Helpers ==========

    private Bill createTestBill(Long id, String buyerUsername, BillStatus status) {
        User buyer = new User();
        buyer.setUsername(buyerUsername);

        Bill bill = new Bill();
        bill.setId(id);
        bill.setBuyer(buyer);
        bill.setStatus(status);
        bill.setTotalAmount(new BigDecimal("1000000"));
        return bill;
    }

    // ========== processPayment Tests ==========

    @Test
    @DisplayName("processPayment - Should succeed when bill is valid")
    void processPayment_ShouldSucceed_WhenBillValid() {
        // Arrange
        Bill bill = createTestBill(1L, "user1", BillStatus.PENDING_PAYMENT);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(paymentGateway.processPayment(any()))
            .thenReturn(new PaymentResult(true, "TXN-123", "OK", "Mock"));
        when(billRepository.save(any(Bill.class))).thenReturn(bill);

        // Act
        PaymentResponse result = paymentService.processPayment(1L, "user1");

        // Assert
        assertThat(result.success()).isTrue();
        assertThat(result.transactionId()).isEqualTo("TXN-123");
        assertThat(bill.getStatus()).isEqualTo(BillStatus.PROCESSING);
        verify(paymentGateway).processPayment(any());
    }

    @Test
    @DisplayName("processPayment - Should throw when not owner")
    void processPayment_ShouldThrow_WhenNotOwner() {
        Bill bill = createTestBill(1L, "user1", BillStatus.PENDING_PAYMENT);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        assertThatThrownBy(() ->
            paymentService.processPayment(1L, "user2"))
            .isInstanceOf(UnauthorizedAccessException.class)
            .hasMessageContaining("You can only pay for your own bills");

        verify(paymentGateway, never()).processPayment(any());
    }

    @Test
    @DisplayName("processPayment - Should throw when bill already paid")
    void processPayment_ShouldThrow_WhenBillAlreadyPaid() {
        Bill bill = createTestBill(1L, "user1", BillStatus.PROCESSING);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        assertThatThrownBy(() ->
            paymentService.processPayment(1L, "user1"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("not in PENDING_PAYMENT status");

        verify(paymentGateway, never()).processPayment(any());
    }

    @Test
    @DisplayName("processPayment - Should throw when bill not found")
    void processPayment_ShouldThrow_WhenBillNotFound() {
        when(billRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            paymentService.processPayment(999L, "user1"))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Bill not found");
    }

    @Test
    @DisplayName("processPayment - Should not update bill when gateway fails")
    void processPayment_ShouldNotUpdateBill_WhenGatewayFails() {
        Bill bill = createTestBill(1L, "user1", BillStatus.PENDING_PAYMENT);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(paymentGateway.processPayment(any()))
            .thenReturn(new PaymentResult(false, null, "Payment failed", "Mock"));

        PaymentResponse result = paymentService.processPayment(1L, "user1");

        assertThat(result.success()).isFalse();
        assertThat(bill.getStatus()).isEqualTo(BillStatus.PENDING_PAYMENT);
        verify(billRepository, never()).save(any());
    }
}
```

---

### 3.4 PasswordResetServiceTest

```java
@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    // ========== Test Data Helpers ==========

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        return user;
    }

    private PasswordResetToken createToken(User user, boolean expired, boolean used) {
        return PasswordResetToken.builder()
            .id(1L)
            .token("test-token-123")
            .user(user)
            .expiryDate(expired
                ? LocalDateTime.now().minusHours(1)
                : LocalDateTime.now().plusHours(24))
            .used(used)
            .build();
    }

    // ========== createPasswordResetToken Tests ==========

    @Test
    @DisplayName("createToken - Should save token when email exists")
    void createToken_ShouldSaveToken_WhenEmailExists() {
        User user = createTestUser();
        when(userRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(user));

        passwordResetService.createPasswordResetToken("test@example.com");

        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq("test@example.com"), anyString());
    }

    @Test
    @DisplayName("createToken - Should not throw when email not exists (security)")
    void createToken_ShouldNotThrow_WhenEmailNotExists() {
        when(userRepository.findByEmail("unknown@example.com"))
            .thenReturn(Optional.empty());

        // Should not throw - security measure
        assertDoesNotThrow(() ->
            passwordResetService.createPasswordResetToken("unknown@example.com"));

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    // ========== resetPassword Tests ==========

    @Test
    @DisplayName("resetPassword - Should update password when token valid")
    void resetPassword_ShouldUpdatePassword_WhenTokenValid() {
        User user = createTestUser();
        PasswordResetToken token = createToken(user, false, false);

        when(tokenRepository.findByToken("test-token-123"))
            .thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newPassword123"))
            .thenReturn("encodedPassword");

        passwordResetService.resetPassword("test-token-123", "newPassword123");

        assertThat(user.getPassword()).isEqualTo("encodedPassword");
        assertThat(token.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    @DisplayName("resetPassword - Should throw when token not found")
    void resetPassword_ShouldThrow_WhenTokenNotFound() {
        when(tokenRepository.findByToken("invalid-token"))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            passwordResetService.resetPassword("invalid-token", "newPassword"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Invalid or expired reset token");
    }

    @Test
    @DisplayName("resetPassword - Should throw when token expired")
    void resetPassword_ShouldThrow_WhenTokenExpired() {
        User user = createTestUser();
        PasswordResetToken expiredToken = createToken(user, true, false);

        when(tokenRepository.findByToken("expired-token"))
            .thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() ->
            passwordResetService.resetPassword("expired-token", "newPassword"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Reset token has expired");
    }

    @Test
    @DisplayName("resetPassword - Should throw when token already used")
    void resetPassword_ShouldThrow_WhenTokenAlreadyUsed() {
        User user = createTestUser();
        PasswordResetToken usedToken = createToken(user, false, true);

        when(tokenRepository.findByToken("used-token"))
            .thenReturn(Optional.of(usedToken));

        assertThatThrownBy(() ->
            passwordResetService.resetPassword("used-token", "newPassword"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Reset token has already been used");
    }
}
```

---

## 4. CHECKLIST KIỂM TRA

### 4.1 Part 1: Admin User CRUD

#### API Endpoints

- [ ] `GET /api/admin/users` - Trả về list users
- [ ] `GET /api/admin/users/{id}` - Trả về user detail
- [ ] `PUT /api/admin/users/{id}` - Cập nhật role/active
- [ ] `DELETE /api/admin/users/{id}` - Soft delete user

#### Business Rules

- [ ] BR-U01: Admin không được tự disable chính mình
- [ ] BR-U02: Role phải là ROLE_USER, ROLE_STAFF, hoặc ROLE_ADMIN
- [ ] BR-U03: Delete = Soft delete (active=false)

#### Authorization

- [ ] Chỉ ADMIN role truy cập được
- [ ] Return 401 khi chưa login
- [ ] Return 403 khi không phải admin

#### Unit Test

```bash
./mvnw test -Dtest=AdminUserServiceTest
```

- [ ] getAllUsers_ShouldReturnAllUsers ✓
- [ ] getAllUsers_ShouldReturnEmptyList_WhenNoUsers ✓
- [ ] getUserById_ShouldReturnUser_WhenExists ✓
- [ ] getUserById_ShouldThrow_WhenUserNotFound ✓
- [ ] updateUser_ShouldUpdateRole_WhenValid ✓
- [ ] updateUser_ShouldThrow_WhenAdminDisablesSelf ✓
- [ ] updateUser_ShouldThrow_WhenInvalidRole ✓
- [ ] softDeleteUser_ShouldSetActiveFalse ✓
- [ ] softDeleteUser_ShouldThrow_WhenDeletingSelf ✓

---

### 4.2 Part 2: Admin Bill Management

#### API Endpoints

- [ ] `GET /api/admin/bills` - Trả về list bills
- [ ] `GET /api/admin/bills?status=PROCESSING` - Filter by status
- [ ] `GET /api/admin/bills/{id}` - Trả về bill detail
- [ ] `POST /api/admin/bills/{id}/complete` - Complete bill

#### Business Rules

- [ ] BR-B01: Chỉ bill PROCESSING mới complete được
- [ ] BR-B02: Complete → set closedAt = now()
- [ ] BR-B03: Admin không tự tạo bill

#### Unit Test

```bash
./mvnw test -Dtest=BillServiceTest
```

- [ ] completeBill_ShouldComplete_WhenStatusProcessing ✓
- [ ] completeBill_ShouldThrow_WhenStatusPendingPayment ✓
- [ ] completeBill_ShouldThrow_WhenAlreadyCompleted ✓
- [ ] completeBill_ShouldThrow_WhenBillNotFound ✓
- [ ] getBillsByStatus_ShouldFilterByStatus ✓

---

### 4.3 Part 3: Payment Gateway

#### API Endpoints

- [ ] `POST /api/payments/{billId}` - Process payment

#### Business Rules

- [ ] BR-P01: Chỉ owner của bill mới được thanh toán
- [ ] BR-P02: Chỉ bill PENDING_PAYMENT mới thanh toán được
- [ ] BR-P03: Mỗi bill chỉ thanh toán 1 lần
- [ ] BR-P04: Thanh toán thành công → status = PROCESSING

#### Architecture

- [ ] PaymentGateway interface tồn tại
- [ ] MockPaymentGateway implement với @Primary
- [ ] PaymentService inject PaymentGateway interface

#### Unit Test

```bash
./mvnw test -Dtest=PaymentServiceTest
```

- [ ] processPayment_ShouldSucceed_WhenBillValid ✓
- [ ] processPayment_ShouldThrow_WhenNotOwner ✓
- [ ] processPayment_ShouldThrow_WhenBillAlreadyPaid ✓
- [ ] processPayment_ShouldThrow_WhenBillNotFound ✓
- [ ] processPayment_ShouldNotUpdateBill_WhenGatewayFails ✓

---

### 4.4 Part 4: Password Reset

#### API Endpoints

- [ ] `POST /api/auth/forgot-password` - Request reset
- [ ] `POST /api/auth/reset-password` - Reset with token

#### Business Rules

- [ ] BR-R01: Token chỉ dùng được 1 lần
- [ ] BR-R02: Token hết hạn sau 24 giờ
- [ ] BR-R03: Không báo lỗi nếu email không tồn tại
- [ ] BR-R04: Mỗi user có thể có nhiều token active

#### Database

- [ ] Flyway V2 migration chạy thành công
- [ ] Table password_reset_token được tạo
- [ ] Foreign key đến user hoạt động

#### Unit Test

```bash
./mvnw test -Dtest=PasswordResetServiceTest
```

- [ ] createToken_ShouldSaveToken_WhenEmailExists ✓
- [ ] createToken_ShouldNotThrow_WhenEmailNotExists ✓
- [ ] resetPassword_ShouldUpdatePassword_WhenTokenValid ✓
- [ ] resetPassword_ShouldThrow_WhenTokenNotFound ✓
- [ ] resetPassword_ShouldThrow_WhenTokenExpired ✓
- [ ] resetPassword_ShouldThrow_WhenTokenAlreadyUsed ✓

---

## 5. CHẠY TEST

### 5.1 Chạy tất cả test

```bash
cd back_end/tradehub_core
./mvnw test
```

### 5.2 Chạy test theo class

```bash
./mvnw test -Dtest=AdminUserServiceTest
./mvnw test -Dtest=BillServiceTest
./mvnw test -Dtest=PaymentServiceTest
./mvnw test -Dtest=PasswordResetServiceTest
```

### 5.3 Chạy test với coverage

```bash
./mvnw test jacoco:report
# Report: target/site/jacoco/index.html
```

---

## 6. CỬA SỔ KIỂM THỬ (cURL)

### 6.1 Login as Admin

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{"username":"admin","password":"admin123"}'
```

### 6.2 Test Admin User APIs

```bash
# Get all users
curl -X GET http://localhost:8080/api/admin/users \
  -b cookies.txt

# Update user role
curl -X PUT http://localhost:8080/api/admin/users/2 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{"role":"ROLE_STAFF","active":true}'

# Soft delete user
curl -X DELETE http://localhost:8080/api/admin/users/2 \
  -b cookies.txt
```

### 6.3 Test Payment API

```bash
# Login as user
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -c user-cookies.txt \
  -d '{"username":"user1","password":"password123"}'

# Process payment
curl -X POST http://localhost:8080/api/payments/1 \
  -b user-cookies.txt
```

### 6.4 Test Password Reset

```bash
# Request reset
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com"}'

# Reset password (copy token from console log)
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{"token":"<TOKEN_FROM_LOG>","newPassword":"newPassword123"}'
```

---

## 7. LIÊN KẾT TÀI LIỆU

| Tài liệu                                              | Mô tả                   |
| ----------------------------------------------------- | ----------------------- |
| [Kế hoạch xây dựng](./plan-backendFeatures.prompt.md) | Chi tiết implementation |
| [ROADMAP_INFJ.md](./ROADMAP_INFJ.md)                  | Lộ trình học tập        |
