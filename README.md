# Hệ thống Quản lý Đặt Nước

Hệ thống giúp các thành viên trung tâm đặt đồ uống theo chu kỳ 2 tuần/lần, tránh nhầm lẫn đơn hàng.

## Tech Stack

- **Frontend**: HTML, CSS, JavaScript thuần
- **Backend**: Java 17 + Spring Boot 3.2.0
- **Database**: PostgreSQL 15+
- **Authentication**: JWT

## Yêu cầu hệ thống

- Java 17 trở lên
- Maven 3.6+
- PostgreSQL 15+

## Cài đặt và chạy

### 1. Setup PostgreSQL

```bash
# Tạo database
createdb water_order_db

# Hoặc dùng psql
psql -U postgres
CREATE DATABASE water_order_db;
\q
```

### 2. Cấu hình database

Mở file `src/main/resources/application.properties` và cập nhật thông tin database:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/water_order_db
spring.datasource.username=postgres
spring.datasource.password=your_password_here
```

### 3. Chạy ứng dụng

```bash
# Build và chạy
mvn spring-boot:run

# Hoặc build jar và chạy
mvn clean package
java -jar target/water-order-management-1.0.0.jar
```

Ứng dụng sẽ chạy tại: http://localhost:8080

### 4. Truy cập ứng dụng

- **URL**: http://localhost:8080
- **Tài khoản Admin mặc định**:
  - Username: `admin`
  - Password: `123456aA@`

## Cấu trúc dự án

```
water-order-management/
├── src/main/
│   ├── java/com/center/waterorder/
│   │   ├── config/          # Security, JWT configuration
│   │   ├── controller/      # REST API controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── model/           # JPA entities
│   │   ├── repository/      # Spring Data repositories
│   │   ├── service/         # Business logic
│   │   └── WaterOrderApplication.java
│   └── resources/
│       ├── static/          # Frontend files
│       │   ├── css/
│       │   ├── js/
│       │   ├── member/
│       │   ├── admin/
│       │   └── index.html
│       ├── application.properties
│       ├── schema.sql       # Database schema
│       └── data.sql         # Seed data
└── pom.xml
```

## API Endpoints

### Authentication
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/logout` - Đăng xuất

### Users (Admin only)
- `GET /api/users` - Danh sách thành viên
- `POST /api/users` - Tạo thành viên mới

### Categories (Admin only)
- `GET /api/categories` - Danh sách danh mục
- `POST /api/categories` - Tạo danh mục
- `PUT /api/categories/{id}` - Cập nhật danh mục
- `DELETE /api/categories/{id}` - Xóa danh mục

### Menu Items (Admin only)
- `GET /api/menu-items` - Danh sách món
- `POST /api/menu-items` - Tạo món mới
- `PUT /api/menu-items/{id}` - Cập nhật món
- `DELETE /api/menu-items/{id}` - Xóa món

### Cycles
- `GET /api/cycles/current` - Chu kỳ hiện tại
- `POST /api/cycles/open` - Mở chu kỳ (Admin)
- `POST /api/cycles/close` - Đóng chu kỳ (Admin)

### Orders
- `GET /api/orders/my` - Đơn của member hiện tại
- `POST /api/orders` - Tạo đơn mới
- `PUT /api/orders/{id}` - Sửa đơn
- `GET /api/orders` - Tất cả đơn (Admin)
- `PATCH /api/orders/{id}/pickup` - Tích/bỏ tích lấy nước (Admin)

### Export
- `GET /api/export/excel` - Xuất Excel (Admin)

## Tính năng

### Member
- Đăng nhập
- Xem trạng thái chu kỳ (mở/đóng, thời gian còn lại)
- Xem menu đồ uống theo danh mục
- Đặt đơn với ghi chú
- Sửa đơn trong khi chu kỳ còn mở

### Admin
- Tất cả tính năng của Member
- Quản lý tài khoản thành viên
- Quản lý menu (danh mục và món)
- Mở/đóng chu kỳ thủ công
- Xem danh sách tất cả đơn
- Filter theo trạng thái (đã lấy/chưa lấy)
- Tìm kiếm theo tên thành viên
- Tích xác nhận đã lấy nước
- Xuất file Excel danh sách đơn

## Chu kỳ tự động

- Tự động mở lúc 09:00 sáng thứ Năm cách tuần
- Tự động đóng sau 4 tiếng
- Admin có thể override bằng cách mở/đóng thủ công

## Troubleshooting

### Lỗi kết nối database
```
Caused by: org.postgresql.util.PSQLException: Connection refused
```
**Giải pháp**: Kiểm tra PostgreSQL đang chạy và thông tin kết nối trong `application.properties`

### Lỗi JWT secret
```
JWT signature does not match locally computed signature
```
**Giải pháp**: Đảm bảo `jwt.secret` trong `application.properties` có ít nhất 256 bits (32 ký tự)

### Port 8080 đã được sử dụng
```
Web server failed to start. Port 8080 was already in use
```
**Giải pháp**: Thay đổi port trong `application.properties`:
```properties
server.port=8081
```

## Development

### Chạy tests
```bash
mvn test
```

### Build production
```bash
mvn clean package -DskipTests
```

## License

MIT
