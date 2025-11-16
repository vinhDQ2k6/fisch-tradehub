# Backend Guideline – Hệ thống Fish Shop (Spring Boot + Security + REST + Vue)

> Mục tiêu: mô tả rõ **mô hình dữ liệu**, **luồng nghiệp vụ**, **cách tiếp cận lập trình chuẩn**, và **các quyết định kiến trúc** sao cho:
>
> - Dễ hiểu, dễ debug, dễ refactor.
> - Không over-engineer, giữ mọi thứ “thẳng” và rõ.
> - Vẫn sẵn sàng mở rộng mà không cần đập lại nền tảng (schema + kiến trúc).

---

## 1. Tổng quan hệ thống

### 1.1. Bối cảnh

- Dự án học tập dùng:
  - Backend: Java Spring Boot, Spring Security, Spring Data JPA.
  - DB: MySQL + Flyway (quản lý schema).
  - Frontend: Vue (SPA) giao tiếp qua REST API.
- Domain: “Fish shop” / “Fishing game shop” gồm:
  - Người dùng (`user`) và profile (`user_info`).
  - Sản phẩm: cá (`fish`).
  - Giỏ hàng (`cart`).
  - Hóa đơn (`bill`) và chi tiết hóa đơn (`bill_info`).

### 1.2. Nguyên tắc thiết kế

1. **Đơn giản, rõ ràng, không hack**:

   - Quan hệ one-to-many, many-to-one đúng hướng (user → cart, user → bill).
   - Không dùng username/email làm FK; dùng `id` dạng numeric cho tất cả FK.

2. **Security tập trung ở tầng backend**:

   - Spring Security quyết định quyền (role), không tin trust frontend.
   - REST API rõ ràng về public / protected endpoints.

3. **Khả năng mở rộng mà không đổi nền tảng**:

   - Schema đã đủ chuẩn để:
     - Thêm permission chi tiết.
     - Thêm thêm bảng khác (inventory, payment, v.v.) mà không phải đập lại.
   - Kiến trúc dùng layered (Controller → Service → Repository), có thể thêm module/phân tách microservice sau nếu cần.

4. **Dễ debug và dễ vá lỗi**:
   - Cấu trúc đơn giản → stack trace dễ đọc.
   - Quy ước tên bảng/cột rõ → viết query/debug SQL trực quan.

---

## 2. Mô hình CSDL (đã chốt)

### 2.1. Bảng `user`

- Vai trò: lưu thông tin account, phục vụ login + phân quyền.

```sql
CREATE TABLE `user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `email` VARCHAR(255) NOT NULL,
  `username` VARCHAR(255) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `role` VARCHAR(50) NOT NULL,     -- e.g. 'ROLE_USER', 'ROLE_STAFF', 'ROLE_ADMIN'
  `active` BOOLEAN NOT NULL DEFAULT '1', -- dùng map UserDetails.isEnabled()
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `user_email_unique` (`email`),
  UNIQUE KEY `user_username_unique` (`username`),
  KEY `user_username_index` (`username`)
);
```

- Đặc điểm:
  - `id`: surrogate key, UNSIGNED, dùng cho mọi FK.
  - `email`/`username`: both unique → linh hoạt login bằng username hoặc email.
  - `role`: một role duy nhất / user (đủ cho {customer, staff, admin}).
  - `active`: đơn giản hóa trạng thái tài khoản. Sau này nếu cần lock, expire… có thể thêm cột mới mà không phá vỡ schema.

### 2.2. Bảng `user_info`

- Profile mở rộng (tách khỏi thông tin login).

```sql
CREATE TABLE `user_info` (
  `user_id` BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  `fullname` VARCHAR(255) NULL,
  `age` TINYINT UNSIGNED NULL,
  `gender` BOOLEAN NULL,
  CONSTRAINT `user_info_user_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
);
```

- Quan hệ: `user` (1) – (1) `user_info`.
- Lợi ích:
  - Không nhét mọi field vào bảng `user`.
  - Sau này mở rộng profile (địa chỉ, avatar…) bằng cách chỉ thêm cột trong `user_info`.

### 2.3. Bảng `fish`

- “Sản phẩm” chính.

```sql
CREATE TABLE `fish` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(255) NOT NULL,
  `rarity` VARCHAR(255) NOT NULL,       -- Common, Rare, Legendary...
  `value` DECIMAL(10, 2) NOT NULL,      -- giá bán
  `weight` DECIMAL(10, 2) NOT NULL
);
```

- Lưu ý:
  - Dùng `DECIMAL(10,2)` cho tiền để tránh sai số float.
  - `rarity` có thể chuyển thành enum logic phía code, DB giữ dạng string.

### 2.4. Bảng `cart`

- Giỏ hàng: mỗi dòng là một item của user.

```sql
CREATE TABLE `cart` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `fish_id` BIGINT UNSIGNED NOT NULL,
  `quantity` INT NOT NULL,
  CONSTRAINT `cart_user_fk` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `cart_fish_fk` FOREIGN KEY (`fish_id`) REFERENCES `fish` (`id`),

  KEY `cart_user_idx` (`user_id`),
  UNIQUE KEY `cart_user_fish_unique` (`user_id`, `fish_id`)
);
```

- Thiết kế `UNIQUE(user_id, fish_id)`:
  - Đảm bảo 1 user chỉ có 1 dòng cho mỗi `fish`.
  - Cho phép logic “add to cart” dùng update thay vì insert trùng:
    - Nếu đã tồn tại → tăng `quantity`.
    - Nếu chưa có → insert dòng.

### 2.5. Bảng `bill`

- Hóa đơn (order).

```sql
CREATE TABLE `bill` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `buyer_id` BIGINT UNSIGNED NOT NULL,
  `seller_id` BIGINT UNSIGNED NULL,
  `total` DECIMAL(10, 2) NOT NULL,
  `status` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `closed_at` TIMESTAMP NULL,
  `rating` TINYINT NULL,
  CONSTRAINT `bill_buyer_fk` FOREIGN KEY (`buyer_id`) REFERENCES `user` (`id`),
  CONSTRAINT `bill_seller_fk` FOREIGN KEY (`seller_id`) REFERENCES `user` (`id`),

  KEY `bill_buyer_idx` (`buyer_id`),
  KEY `bill_seller_idx` (`seller_id`),
  KEY `bill_status_idx` (`status`)
);
```

- Quy ước (gợi ý):
  - `status`: 0 = pending, 1 = paid, 2 = cancelled, 3 = completed (có thể map enum trong code).
  - `seller_id`:
    - Có thể là null nếu chỉ có “system” bán.
    - Hoặc dùng để làm marketplace (nhiều seller).

### 2.6. Bảng `bill_info`

- Chi tiết hóa đơn (line items).

```sql
CREATE TABLE `bill_info` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `bill_id` BIGINT UNSIGNED NOT NULL,
  `fish_id` BIGINT UNSIGNED NOT NULL,
  `price` DECIMAL(10, 2) NOT NULL,
  `amount` INT NOT NULL,
  `sum` DECIMAL(10, 2) NOT NULL,
  CONSTRAINT `bill_info_bill_fk` FOREIGN KEY (`bill_id`) REFERENCES `bill` (`id`),
  CONSTRAINT `bill_info_fish_fk` FOREIGN KEY (`fish_id`) REFERENCES `fish` (`id`),

  KEY `bill_info_bill_idx` (`bill_id`),
  UNIQUE KEY `bill_info_bill_fish_unique` (`bill_id`, `fish_id`)
);
```

- Lưu ý:
  - `price`: giá tại thời điểm mua (copy từ `fish.value` lúc checkout).
  - `sum`: `price * amount`, lưu để không phải tính lại.
  - Unique `(bill_id, fish_id)`: mỗi bill tối đa 1 dòng per fish (gọn hơn).

---

## 3. Luồng nghiệp vụ chính

### 3.1. Đăng ký + đăng nhập + phân quyền

1. **Đăng ký**:
   - `POST /api/auth/register`:
     - Validate input.
     - Hash `password` (BCrypt).
     - Tạo `user` với `role = ROLE_USER`, `active = 1`.
     - (Option) tạo `user_info` rỗng.
2. **Đăng nhập**:
   - `POST /api/auth/login`:
     - Spring Security auth username/password.
     - Trả về token (JWT) hoặc dùng session, tùy strategy bạn chọn.
3. **Phân quyền**:
   - Dùng `role` từ `user.role` để tạo `GrantedAuthority`.
   - Ví dụ:
     - `ROLE_USER` → khách hàng.
     - `ROLE_STAFF` → nhân viên, có quyền quản lý fish.
     - `ROLE_ADMIN` → quản trị hệ thống.

### 3.2. Giỏ hàng (Cart)

- **Lấy giỏ hàng**:

  - `GET /api/cart`
  - Từ authentication (SecurityContext) lấy `userId`.
  - Query: `SELECT * FROM cart WHERE user_id = ?`.

- **Thêm/cập nhật item**:

  - `POST /api/cart` với `{ fishId, quantity }`.
  - Logic:
    - Nếu `quantity <= 0`: xóa item (delete cart row).
    - Nếu `quantity > 0`:
      - Tìm cart item theo `(user_id, fish_id)`.
      - Nếu có → update `quantity`.
      - Nếu không → insert.

- **Xóa item**:
  - `DELETE /api/cart/{cartItemId}` (hoặc `/api/cart/by-fish/{fishId}`).

### 3.3. Checkout tạo Bill

- Endpoint:
  - `POST /api/bills/checkout`
- Quy trình (bọc trong `@Transactional`):

1. Lấy `userId` của buyer từ SecurityContext.
2. Lấy list cart items của user (join fish):
   - `SELECT c.*, f.value FROM cart c JOIN fish f ON c.fish_id = f.id WHERE c.user_id = ?`.
3. Tính `total = sum(c.quantity * f.value)`; nếu cart rỗng → trả lỗi.
4. Tạo `bill` mới:
   - Insert `bill` với:
     - `buyer_id = userId`.
     - `seller_id` (nếu cố định, có thể gán 1 admin/system).
     - `total`, `status = 0 (pending)`.
5. Tạo `bill_info` cho từng item:
   - For mỗi cart row:
     - `price = f.value`.
     - `amount = c.quantity`.
     - `sum = price * amount`.
     - Insert dòng vào `bill_info`.
6. Xóa cart:
   - `DELETE FROM cart WHERE user_id = ?`.
7. Trả response:
   - `bill` + list `bill_info` (hoặc id bill).

- Sau này, bạn có thể:
  - Cập nhật `status`, `closed_at`, `rating` qua các API:
    - `PATCH /api/bills/{id}/status`
    - `POST /api/bills/{id}/rating`.

### 3.4. Lịch sử mua hàng & quản lý bill

- **User xem lịch sử**:
  - `GET /api/bills?role=buyer` → filter `buyer_id = currentUserId`.
- **Staff/admin xem theo seller**:
  - `GET /api/bills?role=seller` → filter `seller_id = currentUserId` hoặc list của shop.
- **Chi tiết bill**:
  - `GET /api/bills/{id}`:
    - lấy `bill` + `bill_info` (join fish để trả name, etc.).

---

## 4. Cách tiếp cận lập trình chuẩn (step-by-step)

### 4.1. Bước 1: Khởi tạo project & config hạ tầng

1. **Spring Boot project**:
   - Dependencies:
     - `spring-boot-starter-web`
     - `spring-boot-starter-security`
     - `spring-boot-starter-data-jpa`
     - `spring-boot-starter-validation`
     - `mysql-connector-j`
     - `flyway-core`
2. **application.yml**:
   - Cấu hình:
     - datasource (URL, user, password).
     - JPA (hibernate ddl-auto=none vì đã dùng Flyway).
     - Flyway: enable, locations.
3. **Flyway**:
   - Đặt file SQL schema này thành `V1__init_schema.sql`.

### 4.2. Bước 2: Định nghĩa entities + repository

Tư duy:

- Một bảng = một entity (cơ bản).
- Dùng quan hệ JPA nhưng không lạm dụng (chỉ dùng nơi cần thiết).
- Nên khai báo `@ManyToOne` phía _many_; tránh `@OneToMany` bidirectional quá phức tạp nếu chưa cần.

Gợi ý mapping:

- Entity `User`:

  - Fields: id, email, username, password, role, active, createdAt, updatedAt.
  - Có thể implement `UserDetails` hoặc tách 1 adapter class.

- Entity `UserInfo`:

  - `@OneToOne @JoinColumn(name="user_id") User user`.

- Entity `Fish`.

- Entity `Cart` (CartItem):

  - `@ManyToOne User user;`
  - `@ManyToOne Fish fish;`
  - `quantity`.

- Entity `Bill`:

  - `@ManyToOne User buyer;`
  - `@ManyToOne User seller;`
  - `total, status, createdAt, closedAt, rating`.
  - (Optional) `@OneToMany(mappedBy="bill") List<BillInfo> items;` (chỉ dùng nếu bạn thực sự cần load cascade).

- Entity `BillInfo`:
  - `@ManyToOne Bill bill;`
  - `@ManyToOne Fish fish;`
  - `price, amount, sum`.

Repositories:

- `UserRepository` (findByUsername, findByEmail).
- `FishRepository`.
- `CartRepository` (findByUserId, findByUserIdAndFishId).
- `BillRepository` (findByBuyerId, findBySellerId).
- `BillInfoRepository` (findByBillId).
- `UserInfoRepository`.

### 4.3. Bước 3: Cấu hình Security

Mục tiêu:

- Đơn giản hóa:
  - JWT access token ngắn hạn **hoặc** session-based.
- Tập trung vào:
  - Auth (login/logout).
  - Authorize (phân quyền theo role).
  - CORS cho Vue.

Luồng tối giản:

1. Implement `UserDetailsService`:

   - `loadUserByUsername()` → query `User` bằng username/email.
   - Map `role` → `SimpleGrantedAuthority(role)`.

2. `PasswordEncoder`:

   - `BCryptPasswordEncoder`.

3. `SecurityFilterChain`:

   - Permit:
     - `/api/auth/**`
     - `/api/public/**` (nếu có).
   - Require auth cho `/api/cart/**`, `/api/bills/**`, `/api/me/**`.
   - Role-based cho admin endpoint:
     - `/api/admin/**` → `hasRole('ADMIN')`.

4. CORS:
   - Cho phép origin của Vue dev server (vd: `http://localhost:5173`) + headers cần thiết.
   - Nếu dùng cookie, bật `allowCredentials`.

### 4.4. Bước 4: Service layer – chuẩn hóa logic

Xây các service chính:

- `AuthService`:

  - Register, login, getCurrentUser.

- `UserService`:

  - Lấy và update profile (`user_info`).
  - Admin: list users, deactivate user.

- `FishService`:

  - CRUD fish (admin).
  - List/search fish (public).

- `CartService`:

  - getCartByUser.
  - addOrUpdateItem(userId, fishId, quantity).
  - removeItem(userId, itemId or fishId).
  - clearCart(userId).

- `BillService`:
  - checkout(userId) → tạo bill + bill_info từ cart.
  - listBillsForBuyer(userId).
  - listBillsForSeller(userId).
  - getBillDetail(billId, currentUser).
  - updateStatus/rating (nếu cần).

**Quy tắc:**  
Mọi logic nghiệp vụ (transaction, validate, tính total) nằm ở Service, không đặt trong Controller.

### 4.5. Bước 5: REST Controllers

- `AuthController`: `/api/auth/*`
- `UserController`: `/api/me/*`, `/api/users/*` (admin).
- `FishController`: `/api/fish/*`
- `CartController`: `/api/cart/*`
- `BillController`: `/api/bills/*`

Các controller chủ yếu:

- Nhận DTO từ request.
- Gọi service.
- Trả DTO response (không trả entity thô nếu sau này cần versioning).

---

## 5. Phương hướng mở rộng & cân nhắc lợi/hại

### 5.1. Về security

**Hiện tại:**

- Bảng `user` + `role` + `active` là đủ cho Spring Security.
- Có thể dùng:
  - JWT access token ONLY (đơn giản).
  - Hoặc JWT + refresh token (sau này, dùng thêm table refresh_tokens, nhưng không ảnh hưởng schema hiện tại).

**Mở rộng về sau:**

- **Thêm multi-role**:

  - Nếu 1 user có nhiều role, tạo bảng `roles` + `users_roles`.
  - Hiện giờ `role` là 1 cột string → có thể migrate sau bằng Flyway (không phá dữ liệu).

- **Thêm permission level chi tiết**:

  - Bảng `permissions`, `roles_permissions` – tất cả có thể thêm ngoài, không phải sửa `user`.

- **Token revocation**:
  - Sau này nếu cần, thêm:
    - `refresh_tokens` + `token_version` vào `user`.

**Lợi/ hại của hiện trạng:**

- Lợi:
  - Đơn giản, dễ hiểu, dễ dạy Spring Security.
  - Ít cột, ít bảng, mapping dễ viết.
- Hại:
  - Không hỗ trợ sẵn multi-role per user; nếu nhu cầu thay đổi cần migration.

### 5.2. Về domain (cart/bill)

**Hiện trạng:**

- Đơn giản cho học:
  - Không có inventory, voucher, shipping.
  - Không có payment gateway.

**Mở rộng không phá nền:**

- Thêm:
  - `inventory` table (fish_id, stock).
  - `payment` table (bill_id, method, status).
  - `shipping_address` table.
- Các bảng này FK tới `bill` hoặc `user` – không phải thay đổi schema core hiện tại.

### 5.3. Về kỹ thuật (code)

**Hiện trạng:**

- Monolith Spring Boot với layered architecture.
- Unit test/integration test có thể thêm dần.

**Mở rộng:**

- Có thể:
  - Tách module (core/domain/security).
  - Sau này tách sang microservice nếu project lớn – DB schema vẫn dùng được.

---

## 6. Chiến lược debug, logging, và “dễ vá lỗi”

### 6.1. Logging

- Bật log SQL ở chế độ dev:
  - Giúp bạn thấy query từ JPA trùng với db schema hay không.
- Log ở service khi:
  - Checkout (log userId, billId).
  - Thao tác auth (login fail/success).

### 6.2. Sử dụng DTO

- Đừng expose entity trực tiếp qua API (nhất là `User`).
- DTO giúp:
  - Không leak password, internal fields.
  - Tự do thay đổi entity mà không break API (versioning).

### 6.3. Validation

- Dùng `@Valid` + `javax.validation`:
  - Trên DTO: `@NotBlank`, `@Email`, `@Size`, etc.
  - Kiểm soát lỗi input ở Controller.

### 6.4. Transaction

- Đặt `@Transactional` trên method service cần atomic:
  - Checkout (tạo bill + bill_info + clear cart).
- Tránh `@Transactional` trên Controller.

---

## 7. Tổng kết

- Schema bạn đang dùng **đã đủ chuẩn và sạch** để:
  - Học Spring Security + REST API nghiêm túc, không bẫy ngầm.
  - Mở rộng chức năng mà không phải đập lại nền tảng (id, FK, quan hệ, kiểu dữ liệu đều “chuẩn”).
- Quy trình tiếp cận chuẩn:
  1. Chốt schema + Flyway (đã xong).
  2. Tạo entities + repositories.
  3. Cấu hình Security (UserDetailsService, PasswordEncoder, SecurityFilterChain).
  4. Tạo service layer (Auth, User, Fish, Cart, Bill).
  5. Tạo REST controllers mỏng, dùng DTO, validate input.
  6. Test luồng: register → login → list fish → add to cart → checkout → xem bill.
- Mọi quyết định đều ưu tiên:
  - Đơn giản, rõ, dễ đọc.
  - An toàn để mở rộng dần, không over-engineer.

Bạn có thể dùng guideline này như “spec backend” để triển khai code dần, và nếu cần, ta có thể viết thêm một guideline tương tự cho phía Vue (cách tổ chức store, axios, route guard, v.v.).
