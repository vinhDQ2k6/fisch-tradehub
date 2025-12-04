# KẾ HOẠCH XÂY DỰNG: BỔ SUNG CHỨC NĂNG BACKEND

> 📋 **Tài liệu liên quan**: [API Requirements & Testing](./api-requirements-testing.prompt.md)

---

## 0. ĐÃ HOÀN THÀNH: BILL STATUS STATE MACHINE

### 0.1 Vấn đề đã fix

❌ **Trước đây:** `updateBillStatus()` cho phép thay đổi status tùy ý (có thể quay ngược)

✅ **Đã fix:** Thêm State Machine vào `BillStatus` enum

### 0.2 Bill Status Flow

```
PENDING_PAYMENT ──────────────────▶ PROCESSING ──────────────────▶ COMPLETED
       │                                  │                            (terminal)
       │                                  │
       ▼                                  ▼
   CANCELLED ◀────────────────────────────┘
   (terminal)
```

### 0.3 Cancel Bill Rules

| Role      | Có thể cancel khi                       | Method                         |
| --------- | --------------------------------------- | ------------------------------ |
| **USER**  | Chỉ `PENDING_PAYMENT` (chưa thanh toán) | `cancelBill(billId, username)` |
| **ADMIN** | `PENDING_PAYMENT` hoặc `PROCESSING`     | `adminCancelBill(billId)`      |

> ⚠️ **Không ai** có thể cancel bill đã `COMPLETED` hoặc `CANCELLED`

### 0.4 Files đã sửa

| File                  | Thay đổi                                                             |
| --------------------- | -------------------------------------------------------------------- |
| `BillStatus.java`     | Thêm `getValidTransitions()`, `canTransitionTo()`                    |
| `BillService.java`    | Xóa `updateBillStatus()`, thêm `adminCancelBill()`, `completeBill()` |
| `BillController.java` | Xóa `/all` và `/{id}/status` (chuyển sang AdminBillController)       |
| `Constants.java`      | Thêm `INVALID_STATUS_TRANSITION`, `BILL_NOT_PROCESSING`              |

### 0.5 Admin Bill Methods (BillService)

| Method                    | Chức năng              | Điều kiện                       |
| ------------------------- | ---------------------- | ------------------------------- |
| `completeBill(billId)`    | PROCESSING → COMPLETED | Chỉ bill đang PROCESSING        |
| `adminCancelBill(billId)` | → CANCELLED            | PENDING_PAYMENT hoặc PROCESSING |
| `getAllBills()`           | Lấy tất cả bills       | -                               |

### 0.6 Code đã thêm

**BillStatus.java:**

```java
public Set<BillStatus> getValidTransitions() {
    return switch (this) {
        case PENDING_PAYMENT -> EnumSet.of(PROCESSING, CANCELLED);
        case PROCESSING -> EnumSet.of(COMPLETED, CANCELLED);
        case COMPLETED -> EnumSet.noneOf(BillStatus.class); // Terminal
        case CANCELLED -> EnumSet.noneOf(BillStatus.class); // Terminal
    };
}

public boolean canTransitionTo(BillStatus target) {
    return getValidTransitions().contains(target);
}
```

**BillService.java (adminCancelBill):**

```java
@Transactional
public BillDTO adminCancelBill(Long billId) {
    Bill bill = billRepository.findById(billId)
        .orElseThrow(() -> new ResourceNotFoundException(Constants.BILL_NOT_FOUND));

    BillStatus currentStatus = bill.getStatus();

    if (!currentStatus.canTransitionTo(BillStatus.CANCELLED)) {
        throw new BusinessException(
            String.format(Constants.INVALID_STATUS_TRANSITION, currentStatus, BillStatus.CANCELLED)
        );
    }

    bill.setStatus(BillStatus.CANCELLED);
    return toDto(billRepository.save(bill));
}
```

---

## 1. TỔNG QUAN DỰ ÁN

### 1.1 Mục tiêu

Bổ sung 4 chức năng còn thiếu cho hệ thống TradeHub:

| #   | Chức năng             | Mô tả                                  | Độ ưu tiên |
| --- | --------------------- | -------------------------------------- | ---------- |
| 1   | Admin User CRUD       | Quản lý users (xem, sửa role, disable) | High       |
| 2   | Admin Bill Management | Quản lý đơn hàng (xem, hoàn thành)     | High       |
| 3   | Payment Gateway       | Tích hợp thanh toán (Strategy Pattern) | Medium     |
| 4   | Password Reset        | Đặt lại mật khẩu qua email token       | Medium     |

### 1.2 Hiện trạng codebase

| Package      | Files hiện có                                                                                          |
| ------------ | ------------------------------------------------------------------------------------------------------ |
| `entity`     | User, Bill, BillInfo, BillStatus, Cart, Fish, UserInfo                                                 |
| `repository` | UserRepository, BillRepository, BillInfoRepository, CartRepository, FishRepository, UserInfoRepository |
| `service`    | AuthService, BillService, CartService, FishService, UserInfoService                                    |
| `web/api`    | AuthController, BillController, CartController, FishController, UserInfoController                     |
| `web/dto`    | UserDTO, BillDTO, FishDTO, CartItemDTO, FishRequest, LoginRequest, RegisterRequest, etc.               |
| `exception`  | ResourceNotFoundException, UnauthorizedAccessException, DuplicateResourceException, BusinessException  |
| `common`     | Constants.java (roles, error messages)                                                                 |

---

## 2. THỨ TỰ THỰC HIỆN

| Bước | Phần                  | Độ khó      | Thời gian ước tính |
| ---- | --------------------- | ----------- | ------------------ |
| 1    | Admin User CRUD       | ⭐ Easy     | 1-2 giờ            |
| 2    | Admin Bill Management | ⭐ Easy     | 1-2 giờ            |
| 3    | Payment Gateway       | ⭐⭐ Medium | 2-3 giờ            |
| 4    | Password Reset        | ⭐⭐⭐ Hard | 3-4 giờ            |

---

## 3. PHẦN 1: ADMIN USER CRUD

### 3.1 Files cần tạo mới

| File                       | Package   | Mô tả                                 |
| -------------------------- | --------- | ------------------------------------- |
| `AdminUserController.java` | `web/api` | REST endpoints cho admin quản lý user |
| `AdminUserService.java`    | `service` | Business logic quản lý user           |
| `AdminUserDTO.java`        | `web/dto` | DTO hiển thị user cho admin           |
| `UpdateUserRequest.java`   | `web/dto` | Request body để update user           |

### 3.2 Chi tiết Implementation

**AdminUserDTO.java:**

```java
public record AdminUserDTO(
    Long id,
    String username,
    String email,
    String role,
    boolean active,
    LocalDateTime createdAt
) {
    public static AdminUserDTO from(User user) {
        return new AdminUserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.isActive(),
            user.getCreatedAt()
        );
    }
}
```

**UpdateUserRequest.java:**

```java
public record UpdateUserRequest(
    @NotBlank String role,
    Boolean active  // nullable = không thay đổi
) {}
```

**AdminUserService.java:**

```java
@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository userRepository;

    private static final Set<String> VALID_ROLES = Set.of(
        "ROLE_USER", "ROLE_STAFF", "ROLE_ADMIN"
    );

    public List<AdminUserDTO> getAllUsers() {
        return userRepository.findAll().stream()
            .map(AdminUserDTO::from)
            .toList();
    }

    public AdminUserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return AdminUserDTO.from(user);
    }

    @Transactional
    public AdminUserDTO updateUser(Long id, UpdateUserRequest request, String currentUsername) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Constraint: không tự disable chính mình
        if (user.getUsername().equals(currentUsername) &&
            Boolean.FALSE.equals(request.active())) {
            throw new BusinessException(Constants.CANNOT_DISABLE_SELF);
        }

        // Constraint: role phải hợp lệ
        if (!VALID_ROLES.contains(request.role())) {
            throw new BusinessException(Constants.INVALID_ROLE);
        }

        user.setRole(request.role());
        if (request.active() != null) {
            user.setActive(request.active());
        }

        return AdminUserDTO.from(userRepository.save(user));
    }

    @Transactional
    public void softDeleteUser(Long id, String currentUsername) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getUsername().equals(currentUsername)) {
            throw new BusinessException(Constants.CANNOT_DISABLE_SELF);
        }

        user.setActive(false);
        userRepository.save(user);
    }
}
```

**AdminUserController.java:**

```java
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<AdminUserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminUserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
            adminUserService.updateUser(id, request, userDetails.getUsername())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        adminUserService.softDeleteUser(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
```

### 3.3 Business Rules

| Rule   | Mô tả                                               |
| ------ | --------------------------------------------------- |
| BR-U01 | Admin không được tự disable chính mình              |
| BR-U02 | Role chỉ được là: ROLE_USER, ROLE_STAFF, ROLE_ADMIN |
| BR-U03 | Delete = Soft delete (set active=false)             |

---

## 4. PHẦN 2: ADMIN BILL MANAGEMENT

### 4.1 Files cần tạo/sửa

| File                       | Action  | Mô tả                        |
| -------------------------- | ------- | ---------------------------- |
| `AdminBillController.java` | Tạo mới | REST endpoints cho admin     |
| `BillController.java`      | Sửa     | Xóa endpoints admin-only     |
| `BillService.java`         | Sửa     | Thêm method `completeBill()` |

### 4.2 Chi tiết Implementation

**BillService.java (bổ sung):**

```java
@Transactional
public BillDTO completeBill(Long billId) {
    Bill bill = billRepository.findById(billId)
        .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

    if (bill.getStatus() != BillStatus.PROCESSING) {
        throw new BusinessException("Only PROCESSING bills can be completed");
    }

    bill.setStatus(BillStatus.COMPLETED);
    bill.setClosedAt(LocalDateTime.now());

    return BillDTO.from(billRepository.save(bill));
}

public List<BillDTO> getAllBills() {
    return billRepository.findAll().stream()
        .map(BillDTO::from)
        .toList();
}

public List<BillDTO> getBillsByStatus(BillStatus status) {
    return billRepository.findByStatus(status).stream()
        .map(BillDTO::from)
        .toList();
}
```

**AdminBillController.java:**

```java
@RestController
@RequestMapping("/api/admin/bills")
@RequiredArgsConstructor
public class AdminBillController {
    private final BillService billService;

    @GetMapping
    public ResponseEntity<List<BillDTO>> getAllBills(
            @RequestParam(required = false) BillStatus status) {
        if (status != null) {
            return ResponseEntity.ok(billService.getBillsByStatus(status));
        }
        return ResponseEntity.ok(billService.getAllBills());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillDTO> getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(billService.getBillById(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<BillDTO> completeBill(@PathVariable Long id) {
        return ResponseEntity.ok(billService.completeBill(id));
    }
}
```

### 4.3 Business Rules

| Rule   | Mô tả                                 |
| ------ | ------------------------------------- |
| BR-B01 | Chỉ bill PROCESSING mới được complete |
| BR-B02 | Khi complete → set closedAt = now()   |
| BR-B03 | Admin không tự tạo bill               |

---

## 5. PHẦN 3: PAYMENT GATEWAY (Strategy Pattern)

### 5.1 Kiến trúc

```
┌─────────────────────────────────────────────────────────┐
│                    PaymentController                     │
│                  POST /api/payments/{id}                 │
└─────────────────────────┬───────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                     PaymentService                       │
│    - Validate bill ownership & status                    │
│    - Call gateway.processPayment()                       │
│    - Update bill status if success                       │
└─────────────────────────┬───────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│              «interface» PaymentGateway                  │
│    + processPayment(request): PaymentResult              │
│    + getGatewayName(): String                            │
└────────────┬────────────────────────────┬───────────────┘
             │                            │
             ▼                            ▼
┌────────────────────────┐    ┌────────────────────────┐
│   MockPaymentGateway   │    │   VNPayGateway (TBD)   │
│      @Primary          │    │      (Future)          │
└────────────────────────┘    └────────────────────────┘
```

### 5.2 Files cần tạo

```
src/main/java/.../
├── payment/
│   ├── PaymentGateway.java          ← Interface (Strategy)
│   ├── PaymentRequest.java          ← Input record
│   ├── PaymentResult.java           ← Output record
│   └── MockPaymentGateway.java      ← Default implementation
├── service/
│   └── PaymentService.java
└── web/
    ├── api/
    │   └── PaymentController.java
    └── dto/
        └── PaymentResponse.java
```

### 5.3 Chi tiết Implementation

**PaymentGateway.java (Interface):**

```java
public interface PaymentGateway {
    PaymentResult processPayment(PaymentRequest request);
    String getGatewayName();
}
```

**PaymentRequest.java:**

```java
public record PaymentRequest(
    Long billId,
    BigDecimal amount,
    String currency  // "VND" default
) {}
```

**PaymentResult.java:**

```java
public record PaymentResult(
    boolean success,
    String transactionId,
    String message,
    String gatewayName
) {}
```

**MockPaymentGateway.java:**

```java
@Component
@Primary  // Mặc định dùng Mock, sau này tạo VNPayGateway thì bỏ @Primary
@Slf4j
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("Processing mock payment for bill: {}, amount: {}",
            request.billId(), request.amount());

        // Giả lập thành công 100%
        String txnId = "MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return new PaymentResult(
            true,
            txnId,
            "Payment successful (Mock)",
            getGatewayName()
        );
    }

    @Override
    public String getGatewayName() {
        return "MockGateway";
    }
}
```

**PaymentResponse.java:**

```java
public record PaymentResponse(
    boolean success,
    String transactionId,
    String message,
    String gatewayName,
    Long billId,
    BillStatus newStatus
) {}
```

**PaymentService.java:**

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentGateway paymentGateway;
    private final BillRepository billRepository;
    private final UserRepository userRepository;

    @Transactional
    public PaymentResponse processPayment(Long billId, String username) {
        // 1. Find bill
        Bill bill = billRepository.findById(billId)
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        // 2. Validate ownership
        if (!bill.getBuyer().getUsername().equals(username)) {
            throw new UnauthorizedAccessException("You can only pay for your own bills");
        }

        // 3. Validate status
        if (bill.getStatus() != BillStatus.PENDING_PAYMENT) {
            throw new BusinessException("Bill is not in PENDING_PAYMENT status");
        }

        // 4. Create payment request
        PaymentRequest request = new PaymentRequest(
            billId,
            bill.getTotalAmount(),
            "VND"
        );

        // 5. Process payment via gateway
        PaymentResult result = paymentGateway.processPayment(request);

        // 6. Update bill if success
        if (result.success()) {
            bill.setStatus(BillStatus.PROCESSING);
            bill.setTransactionId(result.transactionId());
            billRepository.save(bill);
            log.info("Payment successful. Bill {} marked as PROCESSING", billId);
        }

        return new PaymentResponse(
            result.success(),
            result.transactionId(),
            result.message(),
            result.gatewayName(),
            billId,
            bill.getStatus()
        );
    }
}
```

**PaymentController.java:**

```java
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/{billId}")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable Long billId,
            @AuthenticationPrincipal UserDetails userDetails) {
        PaymentResponse response = paymentService.processPayment(
            billId,
            userDetails.getUsername()
        );
        return ResponseEntity.ok(response);
    }
}
```

### 5.4 Business Rules

| Rule   | Mô tả                                        |
| ------ | -------------------------------------------- |
| BR-P01 | Chỉ owner của bill mới được thanh toán       |
| BR-P02 | Chỉ bill PENDING_PAYMENT mới được thanh toán |
| BR-P03 | Mỗi bill chỉ thanh toán 1 lần                |
| BR-P04 | Khi thanh toán thành công → PROCESSING       |

### 5.5 Cập nhật BillController

Xóa endpoint cũ `/api/bills/{id}/pay` và thêm comment:

```java
// Payment functionality moved to PaymentController
// See: POST /api/payments/{billId}
```

---

## 6. PHẦN 4: PASSWORD RESET (Token + Email)

### 6.1 Kiến trúc

```
┌─────────────────────────────────────────────────────────┐
│                     AuthController                       │
│    POST /api/auth/forgot-password                        │
│    POST /api/auth/reset-password                         │
└─────────────────────────┬───────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│                  PasswordResetService                    │
│    - createPasswordResetToken(email)                     │
│    - resetPassword(token, newPassword)                   │
└─────────────────────────┬───────────────────────────────┘
                          │
          ┌───────────────┴───────────────┐
          ▼                               ▼
┌──────────────────────┐    ┌──────────────────────┐
│ PasswordResetToken   │    │    EmailService      │
│      Repository      │    │  (Stub: log only)    │
└──────────────────────┘    └──────────────────────┘
```

### 6.2 Database Migration

**V2\_\_password_reset_token.sql:**

```sql
CREATE TABLE password_reset_token (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT UNSIGNED NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reset_token_user FOREIGN KEY (user_id) REFERENCES `user`(id)
);

CREATE INDEX idx_reset_token ON password_reset_token(token);
CREATE INDEX idx_reset_user ON password_reset_token(user_id);
```

### 6.3 Files cần tạo

```
src/main/java/.../
├── entity/
│   └── PasswordResetToken.java
├── repository/
│   └── PasswordResetTokenRepository.java
├── service/
│   ├── PasswordResetService.java
│   ├── EmailService.java           ← Interface
│   └── StubEmailService.java       ← Implementation (log only)
└── web/dto/
    ├── ForgotPasswordRequest.java
    └── ResetPasswordRequest.java
```

### 6.4 Chi tiết Implementation

**PasswordResetToken.java:**

```java
@Entity
@Table(name = "password_reset_token")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Builder.Default
    @Column(nullable = false)
    private boolean used = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
```

**PasswordResetTokenRepository.java:**

```java
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    List<PasswordResetToken> findByUserAndUsedFalse(User user);
}
```

**ForgotPasswordRequest.java:**

```java
public record ForgotPasswordRequest(
    @NotBlank @Email String email
) {}
```

**ResetPasswordRequest.java:**

```java
public record ResetPasswordRequest(
    @NotBlank String token,
    @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
    String newPassword
) {}
```

**EmailService.java:**

```java
public interface EmailService {
    void sendPasswordResetEmail(String to, String resetToken);
}
```

**StubEmailService.java:**

```java
@Service
@Slf4j
public class StubEmailService implements EmailService {

    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        // Chỉ log ra console, không gửi email thật
        log.info("========================================");
        log.info("       PASSWORD RESET EMAIL");
        log.info("========================================");
        log.info("To: {}", to);
        log.info("Reset Link: http://localhost:5173/reset-password?token={}", resetToken);
        log.info("Token expires in 24 hours");
        log.info("========================================");
    }
}
```

**PasswordResetService.java:**

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final int TOKEN_EXPIRY_HOURS = 24;

    @Transactional
    public void createPasswordResetToken(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        // Bảo mật: không báo lỗi nếu email không tồn tại
        if (userOptional.isEmpty()) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }

        User user = userOptional.get();
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
            .token(token)
            .user(user)
            .expiryDate(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS))
            .used(false)
            .build();

        tokenRepository.save(resetToken);
        emailService.sendPasswordResetEmail(email, token);

        log.info("Password reset token created for user: {}", user.getUsername());
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
            .orElseThrow(() -> new BusinessException(Constants.TOKEN_NOT_FOUND));

        if (resetToken.isExpired()) {
            throw new BusinessException(Constants.TOKEN_EXPIRED);
        }

        if (resetToken.isUsed()) {
            throw new BusinessException(Constants.TOKEN_ALREADY_USED);
        }

        // Update password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password reset successful for user: {}", user.getUsername());
    }
}
```

**AuthController (bổ sung):**

```java
// Thêm vào AuthController hiện có

@PostMapping("/forgot-password")
public ResponseEntity<Map<String, String>> forgotPassword(
        @Valid @RequestBody ForgotPasswordRequest request) {
    passwordResetService.createPasswordResetToken(request.email());
    return ResponseEntity.ok(Map.of(
        "message", Constants.PASSWORD_RESET_EMAIL_SENT
    ));
}

@PostMapping("/reset-password")
public ResponseEntity<Map<String, String>> resetPassword(
        @Valid @RequestBody ResetPasswordRequest request) {
    passwordResetService.resetPassword(request.token(), request.newPassword());
    return ResponseEntity.ok(Map.of(
        "message", "Password has been reset successfully"
    ));
}
```

### 6.5 Business Rules

| Rule   | Mô tả                                           |
| ------ | ----------------------------------------------- |
| BR-R01 | Token chỉ dùng được 1 lần                       |
| BR-R02 | Token hết hạn sau 24 giờ                        |
| BR-R03 | Không báo lỗi nếu email không tồn tại (bảo mật) |
| BR-R04 | Mỗi user có thể có nhiều token active           |

---

## 7. CẬP NHẬT CONSTANTS.JAVA

```java
// Thêm vào Constants.java

// Password Reset Messages
public static final String TOKEN_NOT_FOUND = "Invalid or expired reset token";
public static final String TOKEN_EXPIRED = "Reset token has expired";
public static final String TOKEN_ALREADY_USED = "Reset token has already been used";
public static final String PASSWORD_RESET_EMAIL_SENT = "If the email exists, a reset link has been sent";

// Admin Messages
public static final String CANNOT_DISABLE_SELF = "Cannot disable your own account";
public static final String INVALID_ROLE = "Invalid role specified";
```

---

## 8. SECURITY CONFIG

SecurityConfig hiện tại đã có:

```java
.requestMatchers("/api/admin/**").hasRole("ADMIN")
```

Các endpoint mới sẽ được bảo vệ tự động:

- `/api/admin/users/**` → ADMIN only
- `/api/admin/bills/**` → ADMIN only
- `/api/payments/**` → Authenticated users
- `/api/auth/forgot-password` → Public
- `/api/auth/reset-password` → Public

---

## 9. TỔNG KẾT FILES

### Files tạo mới

| Phần   | Files                                                                                                                                                                               |
| ------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Part 1 | AdminUserController, AdminUserService, AdminUserDTO, UpdateUserRequest                                                                                                              |
| Part 2 | AdminBillController                                                                                                                                                                 |
| Part 3 | PaymentGateway, PaymentRequest, PaymentResult, MockPaymentGateway, PaymentService, PaymentController, PaymentResponse                                                               |
| Part 4 | PasswordResetToken, PasswordResetTokenRepository, PasswordResetService, EmailService, StubEmailService, ForgotPasswordRequest, ResetPasswordRequest, V2\_\_password_reset_token.sql |

### Files sửa đổi

| File                | Thay đổi                                               |
| ------------------- | ------------------------------------------------------ |
| Constants.java      | Thêm error messages                                    |
| BillService.java    | Thêm completeBill(), getAllBills(), getBillsByStatus() |
| BillController.java | Xóa endpoint /pay (chuyển sang PaymentController)      |
| AuthController.java | Thêm forgot-password, reset-password                   |
| Bill.java (entity)  | Thêm field transactionId (nếu chưa có)                 |

---

## 10. LIÊN KẾT TÀI LIỆU

| Tài liệu                                                           | Mô tả                                    |
| ------------------------------------------------------------------ | ---------------------------------------- |
| [API Requirements & Testing](./api-requirements-testing.prompt.md) | Chi tiết API spec, test cases, checklist |
| [ROADMAP_INFJ.md](./ROADMAP_INFJ.md)                               | Lộ trình học tập                         |
