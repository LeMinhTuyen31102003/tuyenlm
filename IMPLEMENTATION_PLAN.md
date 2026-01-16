# E-COMMERCE BACKEND SYSTEM - IMPLEMENTATION PLAN (Phase 1)

**Project**: Hung Hypebeast - Local Brand E-commerce Backend  
**Timeline**: 2 tuần (14 ngày)  
**Tech Stack**: Spring Boot 4.0.1, Java 21, PostgreSQL, Spring Data JPA  
**Delivery Date**: Deadline cho đợt sale cuối tháng

---

## 📋 I. PHÂN TÍCH YÊU CẦU (REQUIREMENT ANALYSIS)

### 1.1. Must-Have Features (Bắt buộc hoàn thiện trong 2 tuần)

#### ✅ **Priority 1: Critical Business Logic**

**A. Catalog Management (Quản lý sản phẩm)**

- ✓ Hiển thị danh sách sản phẩm với pagination
- ✓ Filter theo khoảng giá (priceMin, priceMax)
- ✓ Filter theo category (áo thun, hoodie, v.v.)
- ✓ Hiển thị product variants (Size, Màu) cho từng sản phẩm
- ✓ API get product detail

**B. Shopping Cart (Giỏ hàng)**

- ✓ Thêm sản phẩm vào giỏ (theo SKU - ProductVariant)
- ✓ Tăng/giảm số lượng
- ✓ Xóa item khỏi giỏ
- ✓ Validation tồn kho real-time khi thêm vào giỏ
- ✓ Hiển thị tổng giá trị giỏ hàng

**C. Inventory Management (Quản lý tồn kho) - CRITICAL**

- ✓ **Inventory Reservation**: Giữ hàng 10-15 phút khi checkout
- ✓ **Last Item Problem**: Xử lý concurrent requests cho item cuối cùng
- ✓ **Auto-release**: Scheduled job tự động nhả hàng khi hết timeout
- ✓ Validation: Không cho đặt hàng vượt quá stock available
- ✓ Stock reduction khi đơn hàng confirmed

**D. Checkout & Order (Thanh toán & Đơn hàng)**

- ✓ Tạo đơn hàng từ giỏ hàng
- ✓ Nhập thông tin shipping (name, phone, address)
- ✓ Chọn payment method: COD hoặc Bank Transfer
- ✓ Commit inventory reservation thành order
- ✓ Tính tổng tiền đơn hàng (subtotal, shipping, total)

**E. Order Tracking (Theo dõi đơn hàng)**

- ✓ Gửi email xác nhận đơn hàng với tracking link
- ✓ API public tracking (không cần login) bằng token
- ✓ Hiển thị order status timeline

**F. Admin APIs**

- ✓ GET /admin/orders - Danh sách đơn hàng (filter, pagination)
- ✓ PATCH /admin/orders/{id}/status - Cập nhật trạng thái đơn

#### ✅ **Priority 2: System Requirements**

- ✓ Global Exception Handling
- ✓ Request/Response DTOs
- ✓ Validation (@Valid, custom validators)
- ✓ Transaction management
- ✓ Logging (request/response, errors)

---

### 1.2. Nice-to-Have Features (Có thể defer sang Phase 2)

#### 🔄 **Deferred to Phase 2**

- ⏸ Admin Product Management (Tạo/Sửa/Xóa sản phẩm) - Email nói "để phase sau"
- ⏸ SePay Integration thật - Có thể mock webhook callback
- ⏸ User Authentication/Authorization - Admin APIs có thể tạm open
- ⏸ Product Reviews & Ratings
- ⏸ Discount/Coupon system
- ⏸ Advanced search (full-text, autocomplete)
- ⏸ File upload cho product images
- ⏸ Export orders to Excel

---

### 1.3. Gap Analysis (So sánh hiện trạng vs yêu cầu)

| Component              | Hiện trạng                           | Yêu cầu                                          | Gap              |
| ---------------------- | ------------------------------------ | ------------------------------------------------ | ---------------- |
| **Entities**           | Product, ProductVariant (2 entities) | Cần 8+ entities (Cart, Order, Reservation, etc.) | ❌ 75% thiếu     |
| **Repositories**       | Không có (empty folder)              | Cần 8+ repositories                              | ❌ 100% thiếu    |
| **Services**           | Không có                             | Cần 6+ services với business logic               | ❌ 100% thiếu    |
| **Controllers**        | Không có                             | Cần 5+ REST controllers                          | ❌ 100% thiếu    |
| **DTOs**               | Không có                             | Cần 20+ Request/Response DTOs                    | ❌ 100% thiếu    |
| **Security**           | Không có                             | Admin endpoints cần protection (defer?)          | ⚠️ Defer Phase 2 |
| **Email Service**      | Không có                             | Spring Mail integration                          | ❌ 100% thiếu    |
| **Validation**         | Không có                             | Bean Validation setup                            | ❌ 100% thiếu    |
| **Exception Handling** | Không có                             | Global error handler                             | ❌ 100% thiếu    |
| **Database**           | PostgreSQL configured                | ✓ OK                                             | ✅ Sẵn sàng      |
| **Dependencies**       | Spring Web, JPA, PostgreSQL          | Cần thêm Mail, Validation, Scheduler             | ⚠️ Thiếu 3 deps  |

**Kết luận**: Đây là **skeleton project**, cần implement ~90% code từ đầu.

---

## 🗄️ II. DATABASE DESIGN (ERD)

### 2.1. Entities & Relationships

```
┌─────────────────┐
│    Category     │
├─────────────────┤
│ id (PK)         │
│ name            │
│ slug            │
│ description     │
│ active          │
└─────────────────┘
        │ 1
        │
        │ N
┌─────────────────┐
│    Product      │──────┐
├─────────────────┤      │
│ id (PK)         │      │ 1
│ category_id (FK)│      │
│ name            │      │
│ description     │      │
│ active          │      │ N
│ slug            │      │
│ created_at      │      ┌──────────────────┐
│ updated_at      │      │ ProductVariant   │
└─────────────────┘      ├──────────────────┤
                         │ id (PK)          │
                         │ product_id (FK)  │
                         │ sku (UNIQUE)     │
                         │ size             │
                         │ color            │
                         │ price            │
                         │ stock_quantity   │
                         │ active           │
                         │ created_at       │
                         │ updated_at       │
                         └──────────────────┘
                                  │ 1
                                  │
                         ┌────────┴────────┐
                         │ N               │ N
              ┌──────────────────┐  ┌──────────────────────┐
              │    CartItem      │  │ InventoryReservation │
              ├──────────────────┤  ├──────────────────────┤
              │ id (PK)          │  │ id (PK)              │
              │ cart_id (FK)     │  │ variant_id (FK)      │
              │ variant_id (FK)  │  │ order_id (FK)        │
              │ quantity         │  │ quantity             │
              │ price_snapshot   │  │ reserved_at          │
              │ created_at       │  │ expires_at           │
              │ updated_at       │  │ status (ENUM)        │
              └──────────────────┘  │ - ACTIVE             │
                         │ N        │ - EXPIRED            │
                         │          │ - COMMITTED          │
                         │          └──────────────────────┘
                         │ 1                     │ 1
              ┌──────────────────┐               │
              │      Cart        │               │
              ├──────────────────┤               │
              │ id (PK)          │               │
              │ session_id       │               │
              │ created_at       │               │
              │ updated_at       │               │ N
              └──────────────────┘      ┌──────────────────┐
                                        │      Order       │
                                        ├──────────────────┤
                                        │ id (PK)          │
                                        │ order_number     │
                                        │ customer_name    │
                                        │ customer_email   │
                                        │ customer_phone   │
                                        │ shipping_address │
                                        │ subtotal         │
                                        │ shipping_fee     │
                                        │ total            │
                                        │ payment_method   │
                                        │ status (ENUM)    │
                                        │ tracking_token   │
                                        │ created_at       │
                                        │ updated_at       │
                                        └──────────────────┘
                                                 │ 1
                                                 │
                                                 │ N
                                        ┌──────────────────┐
                                        │   OrderItem      │
                                        ├──────────────────┤
                                        │ id (PK)          │
                                        │ order_id (FK)    │
                                        │ variant_id (FK)  │
                                        │ quantity         │
                                        │ price_snapshot   │
                                        │ sku_snapshot     │
                                        │ name_snapshot    │
                                        └──────────────────┘
```

---

### 2.2. Chi tiết các bảng quan trọng

#### **Category** (categories)

```sql
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sample data: Áo thun, Hoodie, Áo khoác, Quần jean
```

#### **Product** (products) - ✅ Đã có, cần sửa

```sql
-- Hiện tại thiếu category_id, cần thêm FK
ALTER TABLE products ADD COLUMN category_id BIGINT REFERENCES categories(id);
ALTER TABLE products ADD COLUMN slug VARCHAR(200) UNIQUE;
```

#### **ProductVariant** (product_variants) - ✅ Đã có

```sql
-- Đã OK, có đủ: sku, size, color, price, stock_quantity
-- Cần thêm INDEX cho query nhanh:
CREATE INDEX idx_variant_sku ON product_variants(sku);
CREATE INDEX idx_variant_product_id ON product_variants(product_id);
```

#### **Cart** (carts)

```sql
CREATE TABLE carts (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(255) UNIQUE NOT NULL, -- UUID hoặc user session
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### **CartItem** (cart_items)

```sql
CREATE TABLE cart_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    price_snapshot DECIMAL(10,2) NOT NULL, -- Giá tại thời điểm add
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(cart_id, variant_id) -- 1 cart chỉ 1 dòng cho mỗi variant
);
```

#### **InventoryReservation** (inventory_reservations) - CRITICAL

```sql
CREATE TABLE inventory_reservations (
    id BIGSERIAL PRIMARY KEY,
    variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    order_id BIGINT REFERENCES orders(id), -- NULL khi đang reserve, filled khi committed
    quantity INT NOT NULL CHECK (quantity > 0),
    reserved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL, -- reserved_at + 15 minutes
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, EXPIRED, COMMITTED
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'EXPIRED', 'COMMITTED'))
);

-- Index cho query scheduled job release
CREATE INDEX idx_reservation_expires ON inventory_reservations(expires_at, status);
CREATE INDEX idx_reservation_variant ON inventory_reservations(variant_id, status);
```

**Business Logic cho Inventory Reservation**:

1. **Checkout**: Tạo reservation với `expires_at = NOW() + 15 minutes`, status = ACTIVE
2. **Available Stock Calculation**:
   ```
   available = product_variant.stock_quantity
             - SUM(reservations WHERE status='ACTIVE' AND variant_id=X)
   ```
3. **Scheduled Job** (chạy mỗi 1 phút):
   ```sql
   UPDATE inventory_reservations
   SET status = 'EXPIRED'
   WHERE expires_at < NOW() AND status = 'ACTIVE';
   ```
4. **Order Confirmed**: Update reservation set `status='COMMITTED', order_id=X`, trừ stock_quantity

#### **Order** (orders)

```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(20) UNIQUE NOT NULL, -- ORD-20260116-0001
    customer_name VARCHAR(100) NOT NULL,
    customer_email VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    shipping_address TEXT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    shipping_fee DECIMAL(10,2) DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL, -- COD, BANK_TRANSFER
    status VARCHAR(30) DEFAULT 'PENDING',
    -- PENDING, PAID, CONFIRMED, PROCESSING, SHIPPING, DELIVERED, CANCELLED
    tracking_token VARCHAR(64) UNIQUE NOT NULL, -- UUID cho tracking link
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_payment_method CHECK (payment_method IN ('COD', 'BANK_TRANSFER')),
    CONSTRAINT chk_order_status CHECK (status IN ('PENDING', 'PAID', 'CONFIRMED', 'PROCESSING', 'SHIPPING', 'DELIVERED', 'CANCELLED'))
);

CREATE INDEX idx_order_number ON orders(order_number);
CREATE INDEX idx_tracking_token ON orders(tracking_token);
CREATE INDEX idx_order_status ON orders(status);
```

**Order Status Flow**:

```
COD:    PENDING -> CONFIRMED -> PROCESSING -> SHIPPING -> DELIVERED
                          ↓
                      CANCELLED

BANK:   PENDING -> PAID -> CONFIRMED -> PROCESSING -> SHIPPING -> DELIVERED
                     ↓
                 CANCELLED
```

#### **OrderItem** (order_items)

```sql
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    price_snapshot DECIMAL(10,2) NOT NULL, -- Giá tại thời điểm order
    sku_snapshot VARCHAR(50) NOT NULL, -- Lưu lại SKU
    name_snapshot VARCHAR(200) NOT NULL, -- Lưu lại tên product + variant
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_item_order ON order_items(order_id);
```

---

### 2.3. Giải thích thiết kế quan trọng

#### ❓ **Tại sao tách bảng Cart và CartItem?**

- **Cart**: Đại diện cho 1 session/user, có thể tồn tại nhiều ngày
- **CartItem**: Lưu từng sản phẩm trong giỏ, cho phép CRUD linh hoạt
- Quan hệ 1-N giúp query tối ưu và dễ scale

#### ❓ **Tại sao cần bảng InventoryReservation riêng?**

- Tách biệt logic "giữ hàng tạm thời" vs "đã bán"
- Dễ dàng implement timeout với scheduled job
- Có thể audit lịch sử reservation cho analytics
- Tránh race condition khi concurrent checkout

#### ❓ **Tại sao lưu price_snapshot, sku_snapshot, name_snapshot?**

- **Immutability**: Khi admin đổi giá/tên sản phẩm, đơn hàng cũ không bị ảnh hưởng
- **Audit**: Biết được khách mua với giá bao nhiêu vào thời điểm nào
- **Compliance**: Hóa đơn phải giữ nguyên thông tin lúc giao dịch

#### ❓ **Tại sao Order.tracking_token là UUID?**

- Không dùng order_id vì dễ bị brute-force (GET /track/1, /track/2...)
- UUID random (ví dụ: `a7f3e2c1-4b8d-...`) → khách không đoán được đơn người khác
- Không cần login vẫn secure

---

## 🔌 III. API DESIGN (Endpoints)

### 3.1. Public APIs (Customer-facing)

#### **A. Product Catalog**

```http
GET /api/products
Query params:
  - page=1 (default)
  - size=20 (default)
  - categoryId=1 (optional)
  - priceMin=100000 (optional)
  - priceMax=500000 (optional)
  - sortBy=price|name|createdAt (default: createdAt)
  - sortDirection=asc|desc (default: desc)

Response 200:
{
  "content": [
    {
      "id": 1,
      "name": "Áo Thun Rồng",
      "slug": "ao-thun-rong",
      "description": "Áo thun cotton 100%...",
      "category": {
        "id": 1,
        "name": "Áo thun"
      },
      "variants": [
        {
          "id": 10,
          "sku": "ATR-M-BLACK",
          "size": "M",
          "color": "Đen",
          "price": 299000,
          "stockQuantity": 15,
          "availableStock": 12 // Sau khi trừ reservations
        }
      ]
    }
  ],
  "totalElements": 50,
  "totalPages": 3,
  "currentPage": 1
}
```

```http
GET /api/products/{id}
Response 200: (Chi tiết 1 sản phẩm với tất cả variants)
```

#### **B. Shopping Cart**

```http
POST /api/cart/items
Request:
{
  "sessionId": "uuid-session-abc", // Frontend generate UUID
  "variantId": 10,
  "quantity": 2
}

Response 201:
{
  "id": 5,
  "cart": { "id": 3, "sessionId": "uuid-session-abc" },
  "variant": { "id": 10, "sku": "ATR-M-BLACK", "name": "Áo Thun Rồng - M - Đen", "price": 299000 },
  "quantity": 2,
  "subtotal": 598000
}

Error 400: { "error": "Insufficient stock. Available: 1" }
```

```http
GET /api/cart?sessionId={sessionId}
Response 200:
{
  "id": 3,
  "sessionId": "uuid-session-abc",
  "items": [
    {
      "id": 5,
      "variant": {...},
      "quantity": 2,
      "priceSnapshot": 299000,
      "subtotal": 598000
    }
  ],
  "totalItems": 2,
  "totalAmount": 598000
}
```

```http
PATCH /api/cart/items/{itemId}
Request: { "quantity": 3 }
Response 200: (Updated CartItem)
```

```http
DELETE /api/cart/items/{itemId}
Response 204 No Content
```

#### **C. Checkout**

```http
POST /api/checkout
Request:
{
  "sessionId": "uuid-session-abc",
  "customerName": "Nguyễn Văn A",
  "customerEmail": "a@example.com",
  "customerPhone": "0901234567",
  "shippingAddress": "123 Nguyễn Huệ, Q.1, TP.HCM",
  "paymentMethod": "COD" // hoặc "BANK_TRANSFER"
}

Response 200:
{
  "orderId": 15,
  "orderNumber": "ORD-20260116-0015",
  "trackingToken": "a7f3e2c1-4b8d-9e0f-1234567890ab",
  "trackingUrl": "https://hunghypebeast.com/track/a7f3e2c1-4b8d-9e0f-1234567890ab",
  "total": 598000,
  "status": "PENDING",
  "reservationExpiresAt": "2026-01-16T15:45:00Z" // 15 phút sau
}

Error 400:
{
  "error": "Cart is empty"
}

Error 409:
{
  "error": "Variant 'ATR-M-BLACK' is out of stock",
  "details": {
    "variantId": 10,
    "requested": 5,
    "available": 2
  }
}
```

**Business Logic**:

1. Validate cart tồn tại và không rỗng
2. **FOR EACH cart item**: Check available stock (sau khi trừ active reservations)
3. **Transaction**:
   - Tạo Order (status=PENDING)
   - Tạo OrderItems (snapshot giá/tên)
   - Tạo InventoryReservations cho từng variant (expires_at = NOW + 15min)
4. Gửi email confirmation với tracking link
5. Clear cart sau khi checkout thành công

#### **D. Order Tracking (Public - No Auth)**

```http
GET /api/orders/track/{trackingToken}
Response 200:
{
  "orderNumber": "ORD-20260116-0015",
  "status": "CONFIRMED",
  "statusHistory": [
    { "status": "PENDING", "timestamp": "2026-01-16T14:30:00Z" },
    { "status": "CONFIRMED", "timestamp": "2026-01-16T14:35:00Z" }
  ],
  "customerName": "Nguyễn Văn A",
  "total": 598000,
  "items": [
    {
      "name": "Áo Thun Rồng - M - Đen",
      "sku": "ATR-M-BLACK",
      "quantity": 2,
      "price": 299000
    }
  ],
  "shippingAddress": "123 Nguyễn Huệ, Q.1, TP.HCM",
  "createdAt": "2026-01-16T14:30:00Z"
}

Error 404: { "error": "Order not found" }
```

#### **E. Payment Webhook (SePay Mock)**

```http
POST /api/webhooks/sepay
Request:
{
  "transactionId": "TXN123456",
  "orderNumber": "ORD-20260116-0015",
  "amount": 598000,
  "status": "SUCCESS",
  "timestamp": "2026-01-16T14:35:00Z"
}

Response 200: { "success": true }

Business Logic:
1. Tìm order theo orderNumber
2. Validate amount khớp với order.total
3. Update order.status: PENDING -> PAID
4. Commit inventory reservations (status ACTIVE -> COMMITTED)
5. Trừ stock_quantity của các variants
6. Gửi email "Payment confirmed"
```

---

### 3.2. Admin APIs

#### **F. Order Management**

```http
GET /admin/orders
Query params:
  - page=1
  - size=20
  - status=PENDING|CONFIRMED|SHIPPING... (optional)
  - orderNumber=ORD-... (optional, search)
  - customerPhone=090... (optional, search)
  - fromDate=2026-01-01 (optional)
  - toDate=2026-01-31 (optional)

Response 200:
{
  "content": [
    {
      "id": 15,
      "orderNumber": "ORD-20260116-0015",
      "customerName": "Nguyễn Văn A",
      "customerPhone": "0901234567",
      "total": 598000,
      "paymentMethod": "COD",
      "status": "PENDING",
      "createdAt": "2026-01-16T14:30:00Z"
    }
  ],
  "totalElements": 120,
  "totalPages": 6,
  "currentPage": 1
}
```

```http
GET /admin/orders/{id}
Response 200: (Chi tiết đầy đủ order + items)
```

```http
PATCH /admin/orders/{id}/status
Request:
{
  "status": "CONFIRMED", // Chuyển từ PENDING -> CONFIRMED
  "note": "Đã xác nhận đơn với khách hàng qua điện thoại"
}

Response 200:
{
  "id": 15,
  "orderNumber": "ORD-20260116-0015",
  "status": "CONFIRMED",
  "updatedAt": "2026-01-16T15:00:00Z"
}

Error 400:
{
  "error": "Invalid status transition: SHIPPING -> PENDING not allowed"
}
```

**Allowed Status Transitions**:

```
PENDING     -> CONFIRMED | CANCELLED
PAID        -> CONFIRMED | CANCELLED
CONFIRMED   -> PROCESSING | CANCELLED
PROCESSING  -> SHIPPING | CANCELLED
SHIPPING    -> DELIVERED
DELIVERED   -> (final state)
CANCELLED   -> (final state)
```

**Business Logic khi update status**:

- **CONFIRMED** (từ PENDING/PAID):
  - Commit inventory reservations → trừ stock
- **CANCELLED**:
  - Expire inventory reservations → nhả hàng về stock
  - Gửi email thông báo hủy đơn

---

### 3.3. API Error Response Format (Chuẩn hóa)

```json
{
  "timestamp": "2026-01-16T14:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/cart/items",
  "details": {
    "variantId": "must not be null",
    "quantity": "must be greater than 0"
  }
}
```

---

## 🔧 IV. TECHNICAL DESIGN (Chi tiết kỹ thuật)

### 4.1. Solution cho "Last Item Problem" (Bài toán item cuối cùng)

**Scenario**: 2 khách hàng cùng checkout 1 item cuối cùng trong kho (stock_quantity=1) trong cùng 1 thời điểm.

#### **Phương án: Pessimistic Locking + Atomic Reservation**

**Step 1: Check Available Stock (trong Transaction)**

```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public CheckoutResponse processCheckout(CheckoutRequest request) {
    // 1. Lock variant record
    ProductVariant variant = variantRepository.findByIdWithLock(variantId);

    // 2. Calculate available stock
    int reservedStock = reservationRepository
        .sumQuantityByVariantAndStatus(variantId, ReservationStatus.ACTIVE);
    int availableStock = variant.getStockQuantity() - reservedStock;

    // 3. Validate
    if (availableStock < requestedQuantity) {
        throw new InsufficientStockException(
            String.format("Only %d items available", availableStock)
        );
    }

    // 4. Create reservation (atomic)
    InventoryReservation reservation = new InventoryReservation();
    reservation.setVariant(variant);
    reservation.setQuantity(requestedQuantity);
    reservation.setExpiresAt(LocalDateTime.now().plusMinutes(15));
    reservation.setStatus(ReservationStatus.ACTIVE);
    reservationRepository.save(reservation);

    // 5. Create order...
}
```

**Repository Implementation**:

```java
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM ProductVariant v WHERE v.id = :id")
    Optional<ProductVariant> findByIdWithLock(@Param("id") Long id);
}
```

**SQL Thực thi**:

```sql
-- Transaction 1 (Request A) bắt đầu trước:
BEGIN;
SELECT * FROM product_variants WHERE id = 10 FOR UPDATE; -- LOCK row
-- Stock = 1, Reserved = 0, Available = 1 ✓

INSERT INTO inventory_reservations (variant_id, quantity, expires_at, status)
VALUES (10, 1, '2026-01-16 15:00:00', 'ACTIVE');
-- Reserved = 1 now

COMMIT;

-- Transaction 2 (Request B) phải đợi lock release:
BEGIN;
SELECT * FROM product_variants WHERE id = 10 FOR UPDATE; -- WAIT...
-- (Sau khi Transaction 1 commit)
-- Stock = 1, Reserved = 1, Available = 0 ❌

-- Throw InsufficientStockException → Rollback
```

**Kết quả**: Request A thành công, Request B nhận lỗi "Out of stock" → Đúng nghiệp vụ!

---

### 4.2. Scheduled Job: Auto-Release Expired Reservations

**Implementation**:

```java
@Component
@EnableScheduling
public class ReservationCleanupScheduler {

    @Autowired
    private InventoryReservationRepository reservationRepository;

    // Chạy mỗi phút
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void releaseExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();

        int updated = reservationRepository.expireReservations(now);

        if (updated > 0) {
            log.info("Released {} expired reservations", updated);
        }
    }
}
```

**Repository Method**:

```java
@Query("UPDATE InventoryReservation r " +
       "SET r.status = 'EXPIRED' " +
       "WHERE r.expiresAt < :now AND r.status = 'ACTIVE'")
@Modifying
int expireReservations(@Param("now") LocalDateTime now);
```

**SQL**:

```sql
UPDATE inventory_reservations
SET status = 'EXPIRED'
WHERE expires_at < '2026-01-16 14:45:00'
  AND status = 'ACTIVE';
```

---

### 4.3. Email Service Integration

**Dependency (thêm vào pom.xml)**:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

**Configuration (application.properties)**:

```properties
# Gmail SMTP
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=hunghypebeast@gmail.com
spring.mail.password=app-specific-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Application config
app.frontend.url=https://hunghypebeast.com
```

**Email Template (HTML)**:

```java
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendOrderConfirmation(Order order) {
        String trackingUrl = frontendUrl + "/track/" + order.getTrackingToken();

        String htmlContent = String.format("""
            <html>
            <body>
                <h2>Cảm ơn bạn đã đặt hàng tại Hung Hypebeast!</h2>
                <p>Mã đơn hàng: <strong>%s</strong></p>
                <p>Tổng tiền: <strong>%,d VNĐ</strong></p>
                <p>Trạng thái: <strong>%s</strong></p>
                <br>
                <a href="%s" style="background:#000;color:#fff;padding:10px 20px;text-decoration:none;">
                    Theo dõi đơn hàng
                </a>
                <p style="margin-top:20px;color:#666;">
                    Hoặc copy link: %s
                </p>
            </body>
            </html>
            """,
            order.getOrderNumber(),
            order.getTotal().longValue(),
            order.getStatus(),
            trackingUrl,
            trackingUrl
        );

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(order.getCustomerEmail());
        helper.setSubject("Xác nhận đơn hàng " + order.getOrderNumber());
        helper.setText(htmlContent, true); // true = HTML

        mailSender.send(message);
    }
}
```

---

### 4.4. Global Exception Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(
            InsufficientStockException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Conflict")
            .message(ex.getMessage())
            .path(request.getRequestURI())
            .details(ex.getDetails())
            .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Invalid request parameters")
            .path(request.getRequestURI())
            .details(errors)
            .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request) {
        // ... 404 response
    }
}
```

---

### 4.5. DTO Pattern & Validation

**Request DTO Example**:

```java
@Data
@Builder
public class CheckoutRequest {

    @NotBlank(message = "Session ID is required")
    private String sessionId;

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    private String customerName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Invalid Vietnamese phone number")
    private String customerPhone;

    @NotBlank(message = "Shipping address is required")
    @Size(min = 10, max = 500, message = "Address must be 10-500 characters")
    private String shippingAddress;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod; // Enum: COD, BANK_TRANSFER
}
```

**Controller Usage**:

```java
@PostMapping("/checkout")
public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
    // Spring tự động validate, throw MethodArgumentNotValidException nếu fail
    CheckoutResponse response = checkoutService.processCheckout(request);
    return ResponseEntity.ok(response);
}
```

---

### 4.6. Transaction Management Strategy

**Isolation Levels cho các use cases**:

| Use Case                  | Isolation Level   | Lý do                                           |
| ------------------------- | ----------------- | ----------------------------------------------- |
| Checkout (Last Item)      | `SERIALIZABLE`    | Tránh phantom read, dirty read khi check stock  |
| Update Cart               | `READ_COMMITTED`  | Đủ cho thao tác CRUD đơn giản                   |
| View Products             | `READ_COMMITTED`  | Không cần lock, cho phép concurrent reads       |
| Admin Update Order Status | `REPEATABLE_READ` | Đảm bảo order state nhất quán trong transaction |

**Example**:

```java
@Transactional(isolation = Isolation.SERIALIZABLE, timeout = 10)
public CheckoutResponse processCheckout(CheckoutRequest request) {
    // Critical section với highest isolation
}

@Transactional(isolation = Isolation.READ_COMMITTED)
public CartResponse addItemToCart(AddCartItemRequest request) {
    // Normal CRUD operation
}
```

---

## 📅 V. IMPLEMENTATION TIMELINE (14 Ngày)

### **Week 1: Foundation & Core Features**

#### **Day 1-2: Analysis & Design (Ngày 16-17/01)**

- [x] ✅ Đọc email khách hàng, soạn đặc tả yêu cầu
- [ ] Xác định Must-have vs Nice-to-have
- [ ] Đánh giá scope: Tích hợp SePay thật hay mock?
- [ ] Vẽ ERD đầy đủ (8 bảng)
- [ ] Thiết kế API Contract (15+ endpoints)
- [ ] Viết Sequence Diagram cho:
  - Checkout với Inventory Reservation
  - Last Item concurrent scenario
- [ ] Review với giảng viên, điều chỉnh scope nếu cần
- **Deliverable**: Technical Design Document (Draft)

---

#### **Day 3-4: Setup & Database (Ngày 18-19/01)**

- [ ] Update `pom.xml`: Thêm dependencies (Mail, Validation, Scheduler)
- [ ] Tạo entities mới: `Category`, `Cart`, `CartItem`, `InventoryReservation`, `Order`, `OrderItem`
- [ ] Update entities hiện có:
  - [Product.java](src/main/java/example/ecommerce/tuyenlm/entity/Product.java): Thêm `category_id`, `slug`, relationship `@OneToMany variants`
  - [ProductVariant.java](src/main/java/example/ecommerce/tuyenlm/entity/ProductVariant.java): Giữ nguyên, thêm indexes
- [ ] Tạo Enums: `PaymentMethod`, `OrderStatus`, `ReservationStatus`
- [ ] Viết migration scripts hoặc rely on `ddl-auto=update`
- [ ] Seed data mẫu:
  - 2-3 Categories (Áo thun, Hoodie, Áo khoác)
  - 10-15 Products
  - 30-50 ProductVariants với stock đa dạng (0, 1, 5, 100...)
- [ ] Test connection: Run app, check logs, verify tables created
- **Deliverable**: Database schema + seed data

---

#### **Day 5-6: Product Catalog APIs (Ngày 20-21/01)**

- [ ] Tạo `CategoryRepository`, `ProductRepository`, `ProductVariantRepository`
- [ ] Implement `ProductService`:
  - `getProducts(filters, pagination)` với Specification pattern
  - `getProductById(id)` với variants
  - Calculate `availableStock` cho mỗi variant (stock - active reservations)
- [ ] Tạo DTOs:
  - `ProductResponse`, `ProductDetailResponse`, `VariantResponse`
  - `ProductFilterRequest` (categoryId, priceMin, priceMax, sortBy)
- [ ] `ProductController`:
  - `GET /api/products` (pagination + filters)
  - `GET /api/products/{id}`
- [ ] Unit tests cho ProductService (filter logic)
- [ ] Test bằng Postman: Pagination, filter giá, filter category
- **Deliverable**: Working Catalog APIs

---

#### **Day 7: Shopping Cart APIs (Ngày 22/01)**

- [ ] Tạo `CartRepository`, `CartItemRepository`
- [ ] Implement `CartService`:
  - `addItemToCart(sessionId, variantId, quantity)` → Validate stock
  - `getCart(sessionId)` → Trả về items + total
  - `updateCartItem(itemId, quantity)` → Re-validate stock
  - `removeCartItem(itemId)`
  - `clearCart(sessionId)` → Sau checkout
- [ ] DTOs: `AddCartItemRequest`, `UpdateCartItemRequest`, `CartResponse`, `CartItemResponse`
- [ ] `CartController`: 4 endpoints (GET, POST, PATCH, DELETE)
- [ ] Test concurrent add same item → Quantity tăng
- [ ] Test add khi out of stock → Error 400
- **Deliverable**: Working Cart APIs

---

### **Week 2: Critical Features & Finalization**

#### **Day 8-9: Inventory Reservation & Checkout (Ngày 23-24/01) - CRITICAL**

- [ ] Tạo `InventoryReservationRepository` với custom queries:
  - `sumQuantityByVariantAndStatus(variantId, ACTIVE)`
  - `expireReservations(beforeDateTime)`
- [ ] Tạo `ReservationService`:
  - `createReservation(variant, quantity, expiresAt)` → Atomic
  - `commitReservation(orderId)` → Update status, trừ stock
  - `expireReservation(id)` → Update status EXPIRED
- [ ] Implement `CheckoutService`:
  - `processCheckout(request)` với `@Transactional(SERIALIZABLE)`:
    1. Validate cart not empty
    2. **FOR EACH item**: Lock variant, check available stock
    3. Create Order (generate order_number, tracking_token)
    4. Create OrderItems (snapshot price/sku/name)
    5. Create Reservations (expires_at = NOW + 15min)
    6. Trigger email (async)
    7. Clear cart
- [ ] DTOs: `CheckoutRequest`, `CheckoutResponse`
- [ ] `CheckoutController`: `POST /api/checkout`
- [ ] **Critical Tests**:
  - Test checkout thành công
  - Test checkout khi cart empty → Error
  - Test checkout khi out of stock → Error với details
  - **Test concurrent checkout cho last item** → 1 success, 1 fail
- [ ] Setup `@EnableScheduling`, implement `ReservationCleanupScheduler`
- [ ] Test scheduled job: Tạo expired reservation, đợi 1 phút, verify status = EXPIRED
- **Deliverable**: Working Checkout + Reservation mechanism

---

#### **Day 10: Order & Email Service (Ngày 25/01)**

- [ ] Tạo `OrderRepository` với queries:
  - `findByTrackingToken(token)`
  - `findByStatusAndCreatedAtBetween(status, from, to)` → Admin search
- [ ] Implement `OrderService`:
  - `getOrderByTrackingToken(token)` → Public tracking
  - `getOrders(filters, pagination)` → Admin list
  - `updateOrderStatus(orderId, newStatus, note)` → Validate transitions
- [ ] Setup Email Service:
  - Configure SMTP trong [application.properties](src/main/resources/application.properties)
  - `sendOrderConfirmation(order)` → HTML email với tracking link
  - `sendPaymentConfirmation(order)` → Khi SePay callback
  - `sendOrderCancellation(order)` → Khi admin hủy đơn
- [ ] DTOs: `OrderResponse`, `OrderTrackingResponse`, `UpdateOrderStatusRequest`
- [ ] `OrderController`:
  - `GET /api/orders/track/{token}` (Public, no auth)
- [ ] Test email: Checkout thành công → Nhận email → Click link → Thấy tracking page
- **Deliverable**: Order tracking + Email integration

---

#### **Day 11: Admin APIs & Payment Webhook (Ngày 26/01)**

- [ ] `AdminOrderController`:
  - `GET /admin/orders` (pagination + filters: status, orderNumber, phone, dateRange)
  - `GET /admin/orders/{id}` (Chi tiết đầy đủ)
  - `PATCH /admin/orders/{id}/status` → Validate state transitions
- [ ] Implement status transition logic:
  - `PENDING/PAID → CONFIRMED`: Commit reservations, trừ stock
  - `* → CANCELLED`: Expire reservations, hoàn stock (nếu chưa committed)
  - `CONFIRMED → PROCESSING → SHIPPING → DELIVERED`: Update status only
- [ ] `PaymentWebhookController`:
  - `POST /api/webhooks/sepay` (Mock SePay callback)
  - Validate signature (nếu có), update order status PENDING → PAID
  - Trigger email "Payment confirmed"
- [ ] Test admin workflow:
  - List orders, filter by status
  - Update status từ PENDING → CONFIRMED → SHIPPING → DELIVERED
  - Cancel order → Verify stock restored
- [ ] Test webhook: Mock POST request → Order status updated
- **Deliverable**: Admin APIs + Payment webhook

---

#### **Day 12: Testing & Bug Fixes (Ngày 27/01)**

- [ ] Self-test toàn bộ APIs bằng Postman:
  - Happy paths: Browse → Add to cart → Checkout → Track → Admin update
  - Edge cases:
    - Add to cart khi out of stock
    - Checkout khi variant deleted/inactive
    - Concurrent checkout cho last item (2 Postman tabs)
    - Reservation timeout → Auto-release → Stock available again
    - Invalid status transitions (SHIPPED → PENDING)
- [ ] Performance testing:
  - Load 1000 products → Pagination fast?
  - Index optimization: Check EXPLAIN cho slow queries
- [ ] Security checks:
  - SQL injection trong filters?
  - Mass assignment vulnerabilities?
- [ ] Bug fixes & refactoring
- [ ] Code cleanup: Remove commented code, format, Javadoc
- **Deliverable**: Stable, tested system

---

#### **Day 13: Documentation (Ngày 28/01)**

- [ ] Viết [README.md](README.md):

  ```markdown
  # Hung Hypebeast E-commerce Backend

  ## Prerequisites

  - Java 21
  - PostgreSQL 15+
  - Maven 3.8+

  ## Setup

  1. Clone repo
  2. Create database: `createdb ecommerce_crud_db`
  3. Update `application.properties` (DB credentials, SMTP)
  4. Run: `mvn spring-boot:run`

  ## Database Seeding

  - Auto-seeding on startup (see `DataSeeder.java`)
  - Sample data: 3 categories, 15 products, 50 variants

  ## API Documentation

  - Swagger UI: http://localhost:8080/swagger-ui.html
  - Postman Collection: `postman_collection.json`

  ## Testing

  - Unit tests: `mvn test`
  - Integration tests: `mvn verify`
  ```

- [ ] Export Postman Collection:
  - Tạo folder structure: Catalog, Cart, Checkout, Order, Admin, Webhook
  - Add environment variables: `{{baseUrl}}`, `{{sessionId}}`, `{{trackingToken}}`
- [ ] Setup Swagger (nếu có thời gian):
  - Dependency: `springdoc-openapi-starter-webmvc-ui`
  - Annotations: `@Operation`, `@ApiResponse` cho controllers
- [ ] Capture screenshots cho demo
- **Deliverable**: Complete documentation

---

#### **Day 14: Final Report & Submission (Ngày 29/01)**

- [ ] Viết **Technical Report** (PDF/Markdown):

  **1. Đánh giá sơ bộ & Phân tích yêu cầu**

  - Email requirements breakdown
  - Must-have vs Nice-to-have classification
  - Gap Analysis table (hiện trạng vs yêu cầu)
  - Scope adjustment: SePay mock, defer admin product CRUD
  - Cam kết: 90% core features hoàn thiện, 10% defer Phase 2

  **2. Thiết kế hệ thống**

  - **ERD**: 8 tables với relationships, data types, constraints
  - Giải thích thiết kế:
    - Tại sao tách Cart/CartItem?
    - Tại sao cần InventoryReservation riêng?
    - Tại sao snapshot price/sku/name?
    - Tại sao tracking_token dùng UUID?
  - **API Endpoints**: 15 endpoints với Method, URL, Description
  - **Sequence Diagrams**:
    - Checkout flow với reservation (swimlanes: Client, API, DB, Scheduler)
    - Last Item concurrent scenario (2 parallel requests)
  - **Technical Decisions**:
    - Pessimistic lock cho last item
    - Scheduled job cho auto-release
    - Email async với @Async

  **3. Challenges & Solutions**

  - Challenge: Race condition cho last item
    - Solution: Pessimistic lock + atomic reservation
  - Challenge: Reservation timeout
    - Solution: Scheduled job mỗi 1 phút
  - Challenge: Email sending slow
    - Solution: @Async + Thread pool

  **4. Testing Strategy**

  - Unit tests: Service layer (JUnit 5, Mockito)
  - Integration tests: API endpoints (MockMvc)
  - Manual tests: Postman collection
  - Concurrency tests: JMeter/Postman Runner

  **5. Future Enhancements (Phase 2)**

  - Real SePay integration
  - Admin product CRUD APIs
  - JWT authentication
  - File upload cho product images
  - Discount/Coupon system
  - Analytics dashboard

- [ ] Review checklist:
  - ✓ Source code clean, theo Clean Architecture
  - ✓ README.md đầy đủ, test được
  - ✓ Postman Collection hoặc Swagger
  - ✓ Technical Report (PDF) ≥10 pages
  - ✓ ERD + Sequence Diagrams rõ ràng
- [ ] Push to GitHub:
  - Repo structure: `/docs`, `/postman`, `/src`, `README.md`, `pom.xml`
  - `.gitignore`: Exclude `target/`, `*.log`, `.env`
- [ ] Submit:
  - GitHub repo link
  - Technical Report PDF
  - Demo video (5-10 phút) nếu yêu cầu
- **Deliverable**: Complete submission package

---

## 📦 VI. DEPENDENCIES CẦN THÊM (pom.xml)

```xml
<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Email -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- Swagger UI (Optional) -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>

<!-- Transaction & Scheduling đã có sẵn trong spring-boot-starter -->
```

---

## 🎯 VII. SUCCESS CRITERIA (Tiêu chí đánh giá)

### Chức năng (60%)

- [ ] ✅ Catalog APIs work (pagination, filters)
- [ ] ✅ Cart CRUD operations work
- [ ] ✅ Inventory reservation prevents overselling
- [ ] ✅ Last item handled correctly (concurrent test)
- [ ] ✅ Checkout creates order + reservation + email
- [ ] ✅ Scheduled job releases expired reservations
- [ ] ✅ Order tracking works (public link)
- [ ] ✅ Admin can view/update order status
- [ ] ✅ Payment webhook updates order status

### Kỹ thuật (25%)

- [ ] ✅ Clean code, theo naming conventions
- [ ] ✅ Proper DTO/Entity separation
- [ ] ✅ Transaction management correct
- [ ] ✅ Exception handling consistent
- [ ] ✅ Validation works (bad requests rejected)
- [ ] ✅ Database design normalized, no redundancy
- [ ] ✅ Indexes cho slow queries

### Documentation (15%)

- [ ] ✅ Technical Report đầy đủ (ERD, API list, Sequence Diagrams)
- [ ] ✅ README.md hướng dẫn setup/run rõ ràng
- [ ] ✅ Postman Collection hoặc Swagger
- [ ] ✅ Code comments cho logic phức tạp

---

## ⚠️ VIII. RISKS & MITIGATION (Rủi ro & Giải pháp)

| Risk                                           | Impact   | Mitigation                                                                       |
| ---------------------------------------------- | -------- | -------------------------------------------------------------------------------- |
| **Không kịp deadline 2 tuần**                  | High     | Defer Nice-to-have features (SePay thật, Admin product CRUD), focus on Must-have |
| **Concurrent last item vẫn bị race condition** | Critical | Review pessimistic lock implementation, add integration test, consult mentor     |
| **Scheduled job không chạy**                   | High     | Test `@EnableScheduling` config, check cron expression, add logs                 |
| **Email không gửi được (SMTP fail)**           | Medium   | Test SMTP credentials sớm, có fallback log email content to console              |
| **Database performance slow**                  | Medium   | Add indexes early (Day 4), use `EXPLAIN ANALYZE` to optimize queries             |
| **Scope creep (khách yêu cầu thêm)**           | Medium   | Stick to agreed Must-have list, defer extras to Phase 2                          |

---

## 📝 IX. NOTES & TIPS

### Development Tips

1. **Commit thường xuyên**: Mỗi feature 1 commit, message rõ ràng
2. **Test ngay khi code xong**: Đừng để tích lũy bug
3. **Log everything**: Dùng SLF4J logger cho debug, nhất là inventory logic
4. **Use Postman Environment**: Lưu `sessionId`, `trackingToken` vào variables
5. **Mock email khi dev**: Log email content thay vì gửi thật (tiết kiệm quota)

### Common Pitfalls

- ❌ Quên `@Transactional` → Race condition
- ❌ Dùng `CascadeType.ALL` bừa → Xóa nhầm data
- ❌ Không validate input → SQL injection, NPE
- ❌ Hard-code giá trị → Khó maintain (dùng Config/Enum)
- ❌ Expose entities trực tiếp qua API → Thay đổi DB ảnh hưởng frontend

### Testing Strategy

- **Unit Test**: Service layer logic (mock repositories)
- **Integration Test**: API endpoints (MockMvc)
- **Manual Test**: Postman cho happy paths + edge cases
- **Concurrency Test**: Postman Runner với 2+ threads cho last item scenario

---

## 🎓 X. LEARNING OUTCOMES

Sau khi hoàn thành project, học viên sẽ:

1. ✅ Hiểu quy trình phân tích yêu cầu từ email thô → Technical spec
2. ✅ Biết thiết kế database ERD cho e-commerce domain
3. ✅ Master Spring Boot: JPA, Transactions, Scheduling, Email
4. ✅ Giải quyết bài toán concurrency (pessimistic lock, atomic operations)
5. ✅ Thiết kế RESTful APIs theo best practices
6. ✅ Viết Technical Report như một Professional Developer

---

**Good luck! 🚀**

_Nhớ trao đổi với giảng viên sớm nếu gặp blockers, đừng chờ đến cuối tuần!_
