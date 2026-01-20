# 🛍️ E-Commerce Backend API

Hệ thống backend cho ứng dụng thương mại điện tử được xây dựng với Spring Boot, hỗ trợ quản lý sản phẩm, giỏ hàng, đơn hàng và thanh toán.

## 📋 Tính năng

### 🛒 Guest Checkout (Không cần đăng nhập)

- Mua hàng nhanh chóng mà không cần tạo tài khoản
- Quản lý giỏ hàng dựa trên session
- Nhập thông tin giao hàng trực tiếp khi thanh toán

### 🎯 Quản lý sản phẩm

- CRUD sản phẩm với nhiều variants (kích thước, màu sắc)
- Phân loại sản phẩm theo danh mục
- Quản lý tồn kho tự động
- Hệ thống đặt trước hàng (inventory reservation)

### 📦 Quản lý đơn hàng

- Tạo đơn hàng từ giỏ hàng
- Theo dõi trạng thái đơn hàng (PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
- Tracking token cho khách hàng tra cứu đơn hàng
- Admin có thể cập nhật trạng thái đơn hàng

### 📧 Email tự động

- Email xác nhận đơn hàng sau khi checkout
- Email thông báo khi admin cập nhật trạng thái
- Template HTML responsive với gradient đẹp mắt
- Hiển thị đầy đủ thông tin sản phẩm, giá, địa chỉ giao hàng

### 🔐 Bảo mật

- JWT authentication cho admin
- Session-based cart cho guest users
- Validation dữ liệu đầu vào
- CORS configuration

### 🔄 Tính năng nâng cao

- Scheduled tasks dọn dẹp inventory reservations hết hạn
- Data seeder cho dữ liệu mẫu (admin, categories, products)
- Global exception handling
- API documentation với Swagger/OpenAPI

## 🛠️ Tech Stack

- **Framework:** Spring Boot 4.0.1
- **Java Version:** 17
- **Database:** PostgreSQL 18.0
- **ORM:** Hibernate/JPA
- **Security:** Spring Security + JWT
- **Email:** JavaMail API (Gmail SMTP)
- **Mapping:** MapStruct
- **Validation:** Jakarta Validation
- **Documentation:** Springdoc OpenAPI (Swagger)
- **Build Tool:** Maven

## 📦 Cấu trúc dự án

```
src/main/java/example/ecommerce/tuyenlm/
├── config/
│   ├── DataSeeder.java           # Seed dữ liệu mẫu khi khởi động
│   ├── ScheduledTasks.java       # Dọn dẹp reservations hết hạn
│   ├── SecurityConfig.java       # Cấu hình bảo mật
│   └── SwaggerConfig.java        # Cấu hình API docs
├── controller/
│   ├── AuthController.java       # Login, Register
│   ├── ProductController.java    # CRUD sản phẩm
│   ├── CartController.java       # Quản lý giỏ hàng
│   ├── CheckoutController.java   # Thanh toán
│   ├── OrderController.java      # Theo dõi đơn hàng
│   └── AdminOrderController.java # Admin quản lý đơn hàng
├── entity/
│   ├── User.java
│   ├── Product.java
│   ├── ProductVariant.java
│   ├── Category.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Order.java
│   ├── OrderItem.java
│   └── InventoryReservation.java
├── dto/
│   ├── request/                  # Request DTOs
│   └── response/                 # Response DTOs
├── service/
│   ├── impl/                     # Service implementations
│   └── inter/                    # Service interfaces
├── repository/                   # JPA Repositories
├── mapping/                      # MapStruct mappers
├── security/                     # JWT, UserDetails
└── exception/                    # Custom exceptions
```

## 🚀 Cài đặt và chạy

### Yêu cầu

- JDK 17+
- PostgreSQL 18+
- Maven 3.6+

### Bước 1: Clone repository

```bash
git clone <repository-url>
cd tuyenlm
```

### Bước 2: Tạo database

```sql
CREATE DATABASE ecommerce_crud_db;
```

### Bước 3: Cấu hình application.properties

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce_crud_db
spring.datasource.username=postgres
spring.datasource.password=12345

# Email (Gmail)
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# JWT Secret (đổi sang secret key mạnh hơn cho production)
app.jwt.secret=your-secret-key-here
```

### Bước 4: Build và chạy

```bash
# Sử dụng Maven wrapper
./mvnw clean compile spring-boot:run

# Hoặc build JAR
./mvnw clean package
java -jar target/tuyenlm-0.0.1-SNAPSHOT.jar
```

### Bước 5: Truy cập ứng dụng

- **API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Docs:** http://localhost:8080/v3/api-docs

## 📚 API Endpoints

### 🔓 Public APIs (không cần authentication)

#### Authentication

```
POST   /api/auth/register        # Đăng ký tài khoản
POST   /api/auth/login           # Đăng nhập
```

#### Products

```
GET    /api/products             # Danh sách sản phẩm (có phân trang)
GET    /api/products/{id}        # Chi tiết sản phẩm
GET    /api/products/category/{categoryId}  # Sản phẩm theo danh mục
```

#### Cart (Session-based)

```
POST   /api/cart/items           # Thêm sản phẩm vào giỏ
GET    /api/cart                 # Xem giỏ hàng
PUT    /api/cart/items/{itemId}  # Cập nhật số lượng
DELETE /api/cart/items/{itemId}  # Xóa sản phẩm khỏi giỏ
DELETE /api/cart/clear            # Xóa toàn bộ giỏ hàng
```

#### Checkout

```
POST   /api/checkout             # Thanh toán (guest checkout)
```

#### Order Tracking

```
GET    /api/orders/track/{trackingToken}  # Tra cứu đơn hàng
```

### 🔐 Admin APIs (cần JWT token)

#### Product Management

```
POST   /api/products             # Tạo sản phẩm mới
PUT    /api/products/{id}        # Cập nhật sản phẩm
DELETE /api/products/{id}        # Xóa sản phẩm
```

#### Order Management

```
GET    /api/admin/orders         # Danh sách tất cả đơn hàng
GET    /api/admin/orders/{id}    # Chi tiết đơn hàng
PATCH  /api/admin/orders/{id}/status  # Cập nhật trạng thái
```

## 🔑 Authentication

### Lấy JWT Token

```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "admin123"
}
```

Response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "admin@example.com",
  "role": "ADMIN"
}
```

### Sử dụng Token

```bash
GET /api/admin/orders
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 📊 Data Seeding

Khi ứng dụng khởi động lần đầu, DataSeeder sẽ tự động tạo:

### Admin Account

```
Email: admin@example.com
Password: admin123
Role: ADMIN
```

### Categories

- Áo Thun
- Áo Hoodie
- Quần Jean
- Giày Sneaker

### Sample Products

- Supreme Box Logo Tee (3 variants)
- Off-White Diagonal Hoodie (2 variants)
- Yeezy Boost 350 V2 (3 variants)
- Và nhiều sản phẩm khác...

## 📧 Email Configuration

### Gmail App Password

1. Vào Google Account → Security
2. Bật 2-Step Verification
3. Tạo App Password cho ứng dụng
4. Cập nhật vào `application.properties`:

```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-16-char-app-password
```

### Email Templates

- **Order Confirmation:** Gửi ngay sau khi checkout thành công
- **Status Update:** Gửi khi admin thay đổi trạng thái đơn hàng

## 🔄 Scheduled Tasks

### Cleanup Expired Reservations

- Chạy mỗi 5 phút
- Xóa các inventory reservations đã hết hạn (> 10 phút)
- Tự động hoàn trả số lượng về kho

```java
@Scheduled(fixedRate = 300000) // 5 minutes
public void cleanupExpiredReservations()
```

## 🧪 Testing

### Chạy tests

```bash
./mvnw test
```

### Test với Swagger UI

1. Mở http://localhost:8080/swagger-ui.html
2. Test public APIs (products, cart, checkout)
3. Login để lấy JWT token
4. Click "Authorize" và nhập token
5. Test admin APIs

## 📝 Workflow mua hàng (Guest)

```
1. Browse Products
   GET /api/products

2. Add to Cart (lặp lại nhiều lần)
   POST /api/cart/items
   {
     "productVariantId": 1,
     "quantity": 2
   }

3. View Cart
   GET /api/cart

4. Checkout
   POST /api/checkout
   {
     "customerName": "Nguyễn Văn A",
     "customerEmail": "customer@example.com",
     "customerPhone": "0901234567",
     "shippingAddress": "123 Đường ABC, Quận 1, TP.HCM",
     "paymentMethod": "COD"
   }

5. Receive Email Confirmation

6. Track Order
   GET /api/orders/track/{trackingToken}
```

## 🔧 Configuration

### Database

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ecommerce_crud_db
spring.jpa.hibernate.ddl-auto=update  # update/create/create-drop
```

### CORS

```properties
app.frontend.url=http://localhost:8080
```

### JWT

```properties
app.jwt.expiration-ms=86400000  # 24 hours
```

## 🐛 Troubleshooting

### Lỗi database connection

```bash
# Kiểm tra PostgreSQL đang chạy
pg_isready

# Kiểm tra port 5432
netstat -an | findstr 5432
```

### Lỗi email không gửi được

- Kiểm tra Gmail App Password đúng chưa
- Kiểm tra "Less secure app access" (nếu dùng password thường)
- Xem logs để debug: `logging.level.example.ecommerce.tuyenlm=DEBUG`

### Port 8080 đã được sử dụng

```properties
# Đổi port trong application.properties
server.port=8081
```

## 📄 License

MIT License

## 👨‍💻 Author

Lê Minh Tuyên - Hung Hypebeast Store

---

**Note:** Đây là dự án demo, không sử dụng cho production mà không có các cải tiến về bảo mật và hiệu năng.
