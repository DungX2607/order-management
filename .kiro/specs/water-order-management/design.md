# Tài liệu Thiết kế: Hệ thống Quản lý Đặt Nước

## Tổng quan

Hệ thống quản lý đặt nước cho phép các thành viên trung tâm đặt đồ uống theo chu kỳ 2 tuần/lần. Hệ thống gồm hai vai trò chính: **Admin** (quản lý menu, tài khoản, chu kỳ, xác nhận lấy nước, xuất Excel) và **Member** (đăng nhập, đặt và sửa đơn trong chu kỳ mở).

Tech stack:
- **Frontend**: HTML, CSS, JavaScript thuần (không framework)
- **Backend**: Java Spring Boot (REST API)
- **Database**: PostgreSQL

---

## Kiến trúc hệ thống

### Tổng quan kiến trúc

Hệ thống theo mô hình **Client–Server** truyền thống với REST API:

```
┌─────────────────────────────────────────────────────────┐
│                     BROWSER (Client)                    │
│  HTML/CSS/JS thuần — fetch() gọi REST API               │
└────────────────────────┬────────────────────────────────┘
                         │ HTTP/JSON
┌────────────────────────▼────────────────────────────────┐
│              Java Spring Boot (Backend)                 │
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────┐ │
│  │  Controller  │  │   Service    │  │  Repository   │ │
│  │  (REST API)  │→ │ (Logic/Auth) │→ │  (JPA/JDBC)   │ │
│  └──────────────┘  └──────────────┘  └───────┬───────┘ │
│                                               │         │
│  ┌────────────────────────────────────────────▼───────┐ │
│  │           Spring Security (JWT Auth)               │ │
│  └────────────────────────────────────────────────────┘ │
│                                                         │
│  ┌────────────────────────────────────────────────────┐ │
│  │     Spring Scheduler (Cycle auto open/close)       │ │
│  └────────────────────────────────────────────────────┘ │
└────────────────────────┬────────────────────────────────┘
                         │ JDBC
┌────────────────────────▼────────────────────────────────┐
│                    PostgreSQL                           │
└─────────────────────────────────────────────────────────┘
```

### Các thành phần chính

| Thành phần | Công nghệ | Trách nhiệm |
|---|---|---|
| Frontend | HTML/CSS/JS | Giao diện người dùng, gọi REST API |
| REST API | Spring Boot | Xử lý request, business logic |
| Auth | Spring Security + JWT | Xác thực, phân quyền |
| Scheduler | Spring `@Scheduled` | Tự động mở/đóng Cycle |
| ORM | Spring Data JPA | Truy vấn database |
| Export | Apache POI | Xuất file Excel |
| Database | PostgreSQL | Lưu trữ dữ liệu |

---

## Các thành phần và giao diện

### Cấu trúc package Backend

```
com.center.waterorder
├── config/
│   ├── SecurityConfig.java
│   └── SchedulerConfig.java
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── CategoryController.java
│   ├── MenuItemController.java
│   ├── CycleController.java
│   ├── OrderController.java
│   └── ExportController.java
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── MenuService.java
│   ├── CycleService.java
│   ├── OrderService.java
│   └── ExportService.java
├── repository/
│   ├── UserRepository.java
│   ├── CategoryRepository.java
│   ├── MenuItemRepository.java
│   ├── CycleRepository.java
│   └── OrderRepository.java
├── model/
│   ├── User.java
│   ├── Category.java
│   ├── MenuItem.java
│   ├── Cycle.java
│   └── Order.java
├── dto/
│   ├── LoginRequest.java / LoginResponse.java
│   ├── UserDto.java
│   ├── CategoryDto.java
│   ├── MenuItemDto.java
│   ├── CycleDto.java
│   └── OrderDto.java
└── scheduler/
    └── CycleScheduler.java
```

### Cấu trúc Frontend

```
src/main/resources/static/
├── index.html          (trang đăng nhập)
├── member/
│   └── order.html      (trang đặt nước của Member)
├── admin/
│   ├── dashboard.html  (trang quản trị chính)
│   ├── menu.html       (quản lý menu)
│   └── users.html      (quản lý tài khoản)
├── css/
│   └── style.css
└── js/
    ├── auth.js
    ├── order.js
    ├── admin-dashboard.js
    ├── admin-menu.js
    └── admin-users.js
```

### Giao diện REST API

#### Authentication
| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| POST | `/api/auth/login` | Đăng nhập | Public |
| POST | `/api/auth/logout` | Đăng xuất | JWT |

#### User Management (Admin only)
| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| GET | `/api/users` | Danh sách thành viên | Admin |
| POST | `/api/users` | Tạo thành viên mới | Admin |

#### Menu Management
| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| GET | `/api/categories` | Danh sách category | JWT |
| POST | `/api/categories` | Tạo category | Admin |
| PUT | `/api/categories/{id}` | Cập nhật category | Admin |
| DELETE | `/api/categories/{id}` | Xóa category | Admin |
| GET | `/api/menu-items` | Danh sách menu item | JWT |
| POST | `/api/menu-items` | Tạo menu item | Admin |
| PUT | `/api/menu-items/{id}` | Cập nhật menu item | Admin |
| DELETE | `/api/menu-items/{id}` | Xóa menu item | Admin |

#### Cycle Management
| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| GET | `/api/cycles/current` | Cycle hiện tại | JWT |
| POST | `/api/cycles/open` | Mở Cycle thủ công | Admin |
| POST | `/api/cycles/close` | Đóng Cycle thủ công | Admin |
| GET | `/api/cycles` | Lịch sử Cycle | Admin |

#### Order Management
| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| GET | `/api/orders/my` | Đơn của Member hiện tại | Member |
| POST | `/api/orders` | Tạo đơn mới | Member |
| PUT | `/api/orders/{id}` | Sửa đơn | Member |
| GET | `/api/orders` | Tất cả đơn trong Cycle | Admin |
| PATCH | `/api/orders/{id}/pickup` | Tích/bỏ tích lấy nước | Admin |

#### Export
| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| GET | `/api/export/excel` | Xuất Excel Cycle hiện tại | Admin |

---

## Data Models

### Entity Relationship Diagram

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│    users     │       │  categories  │       │  menu_items  │
├──────────────┤       ├──────────────┤       ├──────────────┤
│ id (PK)      │       │ id (PK)      │◄──────│ category_id  │
│ username     │       │ name         │       │ id (PK)      │
│ password     │       │ display_order│       │ name         │
│ full_name    │       │ created_at   │       │ display_order│
│ role         │       └──────────────┘       │ is_active    │
│ is_active    │                              │ created_at   │
│ created_at   │                              └──────────────┘
└──────┬───────┘                                      │
       │                                              │
       │       ┌──────────────┐                       │
       │       │    cycles    │                       │
       │       ├──────────────┤                       │
       │       │ id (PK)      │                       │
       │       │ status       │                       │
       │       │ opened_at    │                       │
       │       │ closed_at    │                       │
       │       │ scheduled_   │                       │
       │       │   close_at   │                       │
       │       └──────┬───────┘                       │
       │              │                               │
       │       ┌──────▼───────────────────────────────┤
       │       │           orders                     │
       │       ├──────────────────────────────────────┤
       └──────►│ user_id (FK → users)                 │
               │ cycle_id (FK → cycles)               │
               │◄─────────────────────────────────────┘
               │ menu_item_id (FK → menu_items)       │
               │ id (PK)                              │
               │ note                                 │
               │ is_picked_up                         │
               │ picked_up_at                         │
               │ created_at                           │
               │ updated_at                           │
               └──────────────────────────────────────┘
```

### Chi tiết bảng dữ liệu

#### Bảng `users`
```sql
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,          -- BCrypt hash
    full_name   VARCHAR(100) NOT NULL,
    role        VARCHAR(10) NOT NULL DEFAULT 'MEMBER', -- 'ADMIN' | 'MEMBER'
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);
```

#### Bảng `categories`
```sql
CREATE TABLE categories (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL UNIQUE,
    display_order INT NOT NULL DEFAULT 0,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);
```

#### Bảng `menu_items`
```sql
CREATE TABLE menu_items (
    id            BIGSERIAL PRIMARY KEY,
    category_id   BIGINT NOT NULL REFERENCES categories(id),
    name          VARCHAR(200) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);
```

#### Bảng `cycles`
```sql
CREATE TABLE cycles (
    id                  BIGSERIAL PRIMARY KEY,
    status              VARCHAR(10) NOT NULL DEFAULT 'OPEN', -- 'OPEN' | 'CLOSED'
    opened_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    closed_at           TIMESTAMP,
    scheduled_close_at  TIMESTAMP NOT NULL  -- opened_at + 4 giờ
);
```

#### Bảng `orders`
```sql
CREATE TABLE orders (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES users(id),
    cycle_id      BIGINT NOT NULL REFERENCES cycles(id),
    menu_item_id  BIGINT NOT NULL REFERENCES menu_items(id),
    note          TEXT,
    is_picked_up  BOOLEAN NOT NULL DEFAULT FALSE,
    picked_up_at  TIMESTAMP,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, cycle_id)  -- mỗi member chỉ có 1 đơn/cycle
);
```

---

## Luồng xử lý chính

### Luồng đăng nhập

```
Client                    Backend                   DB
  │                          │                       │
  │── POST /api/auth/login ──►│                       │
  │   {username, password}   │── SELECT user ────────►│
  │                          │◄── user record ────────│
  │                          │ verify BCrypt password │
  │                          │ generate JWT token     │
  │◄── {token, role, name} ──│                       │
  │ store token in           │                       │
  │ localStorage             │                       │
```

### Luồng đặt nước (Member)

```
Member                    Backend                   DB
  │                          │                       │
  │── GET /api/cycles/current►│── SELECT cycle ───────►│
  │◄── {status, timeLeft} ───│◄── cycle record ───────│
  │                          │                       │
  │── GET /api/categories ───►│── SELECT categories ──►│
  │── GET /api/menu-items ───►│── SELECT menu_items ──►│
  │◄── menu data ────────────│                       │
  │                          │                       │
  │── GET /api/orders/my ────►│── SELECT order ───────►│
  │◄── existing order / null ─│                       │
  │                          │                       │
  │── POST /api/orders ──────►│ validate cycle OPEN   │
  │   {menuItemId, note}     │ check no existing order│
  │                          │── INSERT order ───────►│
  │◄── {order details} ──────│◄── saved order ────────│
```

### Luồng tự động mở/đóng Cycle

```
Spring Scheduler
  │
  │ Mỗi thứ Năm 09:00 (cách tuần)
  │── CycleScheduler.openCycle()
  │   ├── Kiểm tra không có Cycle OPEN nào
  │   ├── INSERT cycle (status=OPEN, scheduled_close_at = now+4h)
  │   └── Log sự kiện
  │
  │ Mỗi phút: kiểm tra Cycle quá hạn
  │── CycleScheduler.checkAndCloseCycles()
  │   ├── SELECT cycles WHERE status=OPEN AND scheduled_close_at <= NOW()
  │   └── UPDATE status=CLOSED, closed_at=NOW()
```

### Luồng xuất Excel

```
Admin                     Backend (ExportService)
  │                          │
  │── GET /api/export/excel ─►│
  │                          │ SELECT orders JOIN users
  │                          │   JOIN menu_items JOIN categories
  │                          │   WHERE cycle_id = current_cycle
  │                          │   ORDER BY category.display_order,
  │                          │            user.full_name
  │                          │
  │                          │ Apache POI: tạo Workbook
  │                          │   - Header row
  │                          │   - Group by Category
  │                          │   - Fill data rows
  │                          │
  │◄── file .xlsx ───────────│
```

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*


### Property 1: Login round-trip trả về token với đúng role

*For any* tài khoản hợp lệ trong hệ thống (Admin hoặc Member), khi đăng nhập với đúng username và password, hệ thống SHALL trả về JWT token hợp lệ và role trong token phải khớp với role được lưu trong database.

**Validates: Requirements 1.1, 1.3**

---

### Property 2: Từ chối đăng nhập với thông tin sai

*For any* cặp (username, password) không khớp với bất kỳ tài khoản nào trong hệ thống, hệ thống SHALL trả về lỗi xác thực (HTTP 401) và không cấp token.

**Validates: Requirements 1.2**

---

### Property 3: Protected endpoint từ chối request không có token

*For any* protected API endpoint, khi gọi mà không có JWT token hợp lệ trong header, hệ thống SHALL trả về HTTP 401 và không thực hiện thao tác.

**Validates: Requirements 1.4**

---

### Property 4: Tạo thành viên với mật khẩu mặc định

*For any* cặp (username, fullName) hợp lệ và chưa tồn tại trong hệ thống, khi Admin tạo tài khoản mới, hệ thống SHALL tạo tài khoản với role MEMBER và tài khoản đó có thể đăng nhập thành công bằng mật khẩu mặc định `123456aA@`.

**Validates: Requirements 2.1**

---

### Property 5: Username là duy nhất trong hệ thống

*For any* username đã tồn tại trong hệ thống, khi Admin cố gắng tạo thêm tài khoản với cùng username đó, hệ thống SHALL từ chối và tổng số tài khoản trong hệ thống không được tăng.

**Validates: Requirements 2.2, 2.3**

---

### Property 6: MenuItem luôn thuộc một Category hợp lệ

*For any* MenuItem được tạo thành công trong hệ thống, MenuItem đó SHALL có category_id trỏ đến một Category đang tồn tại trong hệ thống.

**Validates: Requirements 3.1**

---

### Property 7: Menu creation round-trip

*For any* Category hoặc MenuItem được tạo thành công bởi Admin, khi query danh sách tương ứng, item vừa tạo SHALL xuất hiện trong kết quả với đúng thông tin đã nhập.

**Validates: Requirements 3.2, 3.3**

---

### Property 8: MenuItem update round-trip

*For any* MenuItem đang tồn tại và tên mới hợp lệ, sau khi Admin cập nhật, query lại MenuItem đó SHALL trả về thông tin mới đã cập nhật.

**Validates: Requirements 3.4**

---

### Property 9: MenuItem sau khi xóa không xuất hiện trong menu

*For any* MenuItem đang tồn tại, sau khi Admin xóa, query danh sách menu SHALL không còn trả về MenuItem đó.

**Validates: Requirements 3.5**

---

### Property 10: Category có MenuItem không thể bị xóa

*For any* Category có ít nhất một MenuItem active, khi Admin cố gắng xóa Category đó, hệ thống SHALL từ chối và Category vẫn còn tồn tại trong hệ thống.

**Validates: Requirements 3.6**

---

### Property 11: Menu được trả về theo thứ tự display_order

*For any* danh sách Category hoặc MenuItem có các giá trị display_order khác nhau, khi query menu, hệ thống SHALL trả về danh sách được sắp xếp tăng dần theo display_order.

**Validates: Requirements 3.7**

---

### Property 12: Lịch sử Cycle và Order được bảo toàn

*For any* Cycle đã đóng cùng với các Order thuộc Cycle đó, khi query lịch sử Cycle, hệ thống SHALL trả về đầy đủ thông tin Cycle và tất cả Orders liên quan không bị mất.

**Validates: Requirements 4.7**

---

### Property 13: Thời gian còn lại của Cycle OPEN luôn dương

*For any* Cycle đang có status OPEN, giá trị timeLeft (thời gian còn lại) trong response SHALL bằng `scheduled_close_at - now` và phải lớn hơn 0.

**Validates: Requirements 4.5**

---

### Property 14: Order creation round-trip

*For any* Member chưa có Order trong Cycle OPEN hiện tại, khi tạo Order với menuItemId và note hợp lệ, hệ thống SHALL lưu thành công và query lại SHALL trả về đúng thông tin (memberId, menuItemId, note, is_picked_up=false).

**Validates: Requirements 5.1, 5.2**

---

### Property 15: Order update round-trip

*For any* Order đang tồn tại trong Cycle OPEN, khi Member cập nhật menuItemId hoặc note, query lại SHALL trả về thông tin mới đã cập nhật.

**Validates: Requirements 5.3**

---

### Property 16: Mỗi Member chỉ có một Order trong một Cycle

*For any* Member đã có Order trong Cycle hiện tại, khi cố gắng tạo thêm Order trong cùng Cycle đó, hệ thống SHALL từ chối và tổng số Order của Member trong Cycle đó không được tăng.

**Validates: Requirements 5.4**

---

### Property 17: Không thể tạo hoặc sửa Order khi Cycle đóng

*For any* Cycle có status CLOSED, mọi request tạo mới hoặc cập nhật Order SHALL bị từ chối với lỗi phù hợp.

**Validates: Requirements 5.5**

---

### Property 18: Danh sách Order chứa đầy đủ thông tin

*For any* Order trong Cycle hiện tại, khi Admin query danh sách, mỗi item trong response SHALL chứa đầy đủ: tên Member, tên MenuItem, tên Category, ghi chú, và trạng thái lấy nước.

**Validates: Requirements 6.1**

---

### Property 19: Filter Order theo trạng thái lấy nước là chính xác

*For any* tập hợp Orders với trạng thái khác nhau, khi filter theo trạng thái (đã lấy hoặc chưa lấy), kết quả SHALL chỉ chứa các Order có đúng trạng thái được filter.

**Validates: Requirements 6.2**

---

### Property 20: Tìm kiếm Order theo tên thành viên là chính xác

*For any* search query là tên thành viên, kết quả SHALL chỉ chứa các Order thuộc về Member có tên khớp với query (case-insensitive).

**Validates: Requirements 6.3**

---

### Property 21: Pickup toggle round-trip

*For any* Order, sau khi Admin tích xác nhận lấy nước, is_picked_up SHALL là true và picked_up_at SHALL có giá trị. Sau khi bỏ tích, is_picked_up SHALL trở lại false.

**Validates: Requirements 6.4, 6.5**

---

### Property 22: Thống kê tổng đơn luôn nhất quán

*For any* tập hợp Orders trong Cycle, giá trị `total` trong thống kê SHALL luôn bằng `picked + unpicked`.

**Validates: Requirements 6.6**

---

### Property 23: Danh sách đơn phản ánh ngay Order mới tạo

*For any* Order vừa được tạo thành công trong Cycle OPEN, khi query danh sách ngay sau đó, Order mới SHALL xuất hiện trong kết quả.

**Validates: Requirements 6.7**

---

### Property 24: File Excel chứa đầy đủ dữ liệu và cột

*For any* tập hợp N Orders trong Cycle hiện tại, file Excel được xuất SHALL có đúng N data rows và header row SHALL chứa đủ 6 cột: STT, Tên thành viên, Danh mục, Tên đồ uống, Ghi chú, Trạng thái lấy nước.

**Validates: Requirements 7.1, 7.2**

---

### Property 25: File Excel nhóm Orders theo Category

*For any* file Excel được xuất với Orders thuộc nhiều Category khác nhau, các rows thuộc cùng một Category SHALL nằm liền kề nhau trong file.

**Validates: Requirements 7.3**

---

## Xử lý lỗi

### Mã lỗi HTTP

| Mã | Tình huống |
|---|---|
| 400 Bad Request | Dữ liệu đầu vào không hợp lệ (thiếu trường, sai định dạng) |
| 401 Unauthorized | Chưa đăng nhập hoặc token hết hạn |
| 403 Forbidden | Đã đăng nhập nhưng không đủ quyền (Member gọi API Admin) |
| 404 Not Found | Resource không tồn tại (Category, MenuItem, Order, Cycle) |
| 409 Conflict | Trùng lặp dữ liệu (username đã tồn tại, đã có Order trong Cycle) |
| 422 Unprocessable Entity | Vi phạm business rule (tạo Order khi Cycle đóng, xóa Category có MenuItem) |
| 500 Internal Server Error | Lỗi server không mong đợi |

### Cấu trúc response lỗi

```json
{
  "timestamp": "2024-01-15T09:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Tên đăng nhập 'nguyen.van.a' đã tồn tại trong hệ thống",
  "path": "/api/users"
}
```

### Các trường hợp lỗi quan trọng

- **Cycle không tồn tại**: Khi không có Cycle nào đang OPEN, API đặt/sửa Order trả về 422 với message rõ ràng.
- **Token hết hạn**: JWT có thời hạn 8 giờ. Khi hết hạn, client nhận 401 và cần đăng nhập lại.
- **Xóa Category có MenuItem**: Trả về 422 kèm danh sách MenuItem còn lại để Admin biết cần xóa gì trước.
- **Xuất Excel khi không có Order**: Trả về file Excel hợp lệ với header row và 1 row thông báo "Không có dữ liệu".

---

## Chiến lược kiểm thử

### Phương pháp kiểm thử kép

Hệ thống sử dụng kết hợp **unit test** và **property-based test** để đảm bảo độ chính xác toàn diện:

- **Unit test**: Kiểm tra các ví dụ cụ thể, edge case, và điều kiện lỗi
- **Property-based test**: Kiểm tra các thuộc tính phổ quát trên nhiều input ngẫu nhiên

### Thư viện kiểm thử

| Loại | Thư viện | Mục đích |
|---|---|---|
| Unit test | JUnit 5 + Mockito | Test service layer với mock |
| Property-based test | [jqwik](https://jqwik.net/) | Property-based testing cho Java |
| Integration test | Spring Boot Test + Testcontainers | Test với PostgreSQL thật |
| API test | MockMvc | Test REST endpoints |

### Cấu hình Property-Based Test

- Mỗi property test chạy tối thiểu **100 iterations**
- Mỗi test được tag với comment tham chiếu property trong design:
  ```java
  // Feature: water-order-management, Property 5: Username là duy nhất trong hệ thống
  @Property(tries = 100)
  void usernameUniquenessInvariant(...) { ... }
  ```

### Phân tầng kiểm thử

```
┌─────────────────────────────────────────────────────┐
│  E2E / Manual Test (smoke test triển khai)          │
├─────────────────────────────────────────────────────┤
│  Integration Test (Testcontainers + PostgreSQL)     │
│  - Scheduler auto open/close Cycle (4.1, 4.2)      │
│  - Full API flow với DB thật                        │
├─────────────────────────────────────────────────────┤
│  Property-Based Test (jqwik, 100+ iterations)       │
│  - Properties 1–25 (xem Correctness Properties)    │
├─────────────────────────────────────────────────────┤
│  Unit Test (JUnit 5 + Mockito)                      │
│  - Edge cases: Excel rỗng (7.4), Cycle thủ công    │
│  - Service logic với mock repository               │
└─────────────────────────────────────────────────────┘
```

### Các test case ưu tiên

**Unit tests quan trọng:**
- Mở/đóng Cycle thủ công (4.3, 4.4)
- Xuất Excel khi không có Order (7.4)
- Tính toán nextCycleTime khi Cycle đóng (4.6)
- BCrypt password verification

**Integration tests:**
- Scheduler tự động mở Cycle vào thứ Năm 09:00 (4.1)
- Scheduler tự động đóng Cycle sau 4 tiếng (4.2)
- Full order flow: login → xem menu → đặt → admin tích → xuất Excel
