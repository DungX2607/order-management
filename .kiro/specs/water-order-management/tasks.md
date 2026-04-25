# Kế hoạch Triển khai: Hệ thống Quản lý Đặt Nước

## Tổng quan

Triển khai hệ thống quản lý đặt nước theo kiến trúc Client–Server với Java Spring Boot (backend), HTML/CSS/JS thuần (frontend), và PostgreSQL (database). Các task được sắp xếp theo thứ tự tăng dần, mỗi bước xây dựng trên bước trước và kết thúc bằng việc kết nối toàn bộ hệ thống.

## Tasks

- [ ] 1. Khởi tạo dự án và cấu hình cơ sở
  - Tạo dự án Spring Boot với các dependency: Spring Web, Spring Security, Spring Data JPA, Spring Scheduler, PostgreSQL Driver, Apache POI, jqwik
  - Cấu hình `application.properties`: datasource PostgreSQL, JWT secret, server port
  - Tạo cấu trúc package theo thiết kế: `config`, `controller`, `service`, `repository`, `model`, `dto`, `scheduler`
  - Tạo cấu trúc thư mục frontend: `static/`, `static/member/`, `static/admin/`, `static/css/`, `static/js/`
  - _Requirements: Tất cả_

- [ ] 2. Tạo schema database và các Entity JPA
  - [ ] 2.1 Viết script SQL tạo bảng `users`, `categories`, `menu_items`, `cycles`, `orders`
    - Tạo file `schema.sql` với đầy đủ DDL theo thiết kế (constraints, indexes, UNIQUE)
    - Tạo file `data.sql` với tài khoản Admin mặc định và dữ liệu seed ban đầu
    - _Requirements: 2.4_

  - [ ] 2.2 Tạo các Entity JPA tương ứng
    - Viết `User.java`, `Category.java`, `MenuItem.java`, `Cycle.java`, `Order.java` với annotation JPA
    - Đảm bảo mapping quan hệ: `Order` → `User`, `Cycle`, `MenuItem`; `MenuItem` → `Category`
    - _Requirements: 3.1, 5.2_

  - [ ] 2.3 Tạo các Repository interface
    - Viết `UserRepository`, `CategoryRepository`, `MenuItemRepository`, `CycleRepository`, `OrderRepository`
    - Thêm các custom query method cần thiết: `findByUsername`, `findByStatus`, `findByUserIdAndCycleId`
    - _Requirements: 1.1, 4.7, 5.4_

- [ ] 3. Triển khai xác thực JWT
  - [ ] 3.1 Tạo `SecurityConfig.java` và JWT utility
    - Cấu hình Spring Security: public endpoints (`/api/auth/**`, static files), protected endpoints yêu cầu JWT
    - Viết `JwtUtil.java`: generate token, validate token, extract claims (username, role)
    - Viết `JwtAuthFilter.java`: filter xác thực JWT trên mỗi request
    - _Requirements: 1.3, 1.4_

  - [ ] 3.2 Viết `AuthService.java` và `AuthController.java`
    - `AuthService`: xác thực username/password bằng BCrypt, tạo JWT với role và thời hạn 8 giờ
    - `AuthController`: POST `/api/auth/login` trả về `{token, role, fullName}`, POST `/api/auth/logout`
    - _Requirements: 1.1, 1.2, 1.3_

  - [ ]* 3.3 Viết property test cho xác thực JWT
    - **Property 1: Login round-trip trả về token với đúng role**
    - **Validates: Requirements 1.1, 1.3**

  - [ ]* 3.4 Viết property test từ chối đăng nhập sai
    - **Property 2: Từ chối đăng nhập với thông tin sai**
    - **Validates: Requirements 1.2**

  - [ ]* 3.5 Viết property test bảo vệ endpoint
    - **Property 3: Protected endpoint từ chối request không có token**
    - **Validates: Requirements 1.4**

- [ ] 4. Checkpoint — Đảm bảo tất cả test pass, hỏi người dùng nếu có thắc mắc.

- [ ] 5. Triển khai quản lý tài khoản thành viên
  - [ ] 5.1 Viết `UserService.java` và `UserController.java`
    - `UserService`: tạo Member với mật khẩu mặc định `123456aA@` (BCrypt hash), kiểm tra username trùng
    - `UserController`: GET `/api/users` (Admin), POST `/api/users` (Admin)
    - Trả về 409 Conflict nếu username đã tồn tại
    - _Requirements: 2.1, 2.2, 2.3_

  - [ ]* 5.2 Viết property test tạo thành viên với mật khẩu mặc định
    - **Property 4: Tạo thành viên với mật khẩu mặc định**
    - **Validates: Requirements 2.1**

  - [ ]* 5.3 Viết property test username là duy nhất
    - **Property 5: Username là duy nhất trong hệ thống**
    - **Validates: Requirements 2.2, 2.3**

- [ ] 6. Triển khai quản lý menu (Category và MenuItem)
  - [ ] 6.1 Viết `MenuService.java`, `CategoryController.java`, `MenuItemController.java`
    - CRUD Category: GET `/api/categories`, POST, PUT `/{id}`, DELETE `/{id}`
    - CRUD MenuItem: GET `/api/menu-items`, POST, PUT `/{id}`, DELETE `/{id}`
    - Trả về 422 khi xóa Category còn MenuItem; trả về 404 khi resource không tồn tại
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

  - [ ] 6.2 Đảm bảo sắp xếp theo `display_order`
    - Query Category và MenuItem luôn ORDER BY `display_order ASC`
    - _Requirements: 3.7_

  - [ ]* 6.3 Viết property test MenuItem luôn thuộc Category hợp lệ
    - **Property 6: MenuItem luôn thuộc một Category hợp lệ**
    - **Validates: Requirements 3.1**

  - [ ]* 6.4 Viết property test menu creation round-trip
    - **Property 7: Menu creation round-trip**
    - **Validates: Requirements 3.2, 3.3**

  - [ ]* 6.5 Viết property test MenuItem update round-trip
    - **Property 8: MenuItem update round-trip**
    - **Validates: Requirements 3.4**

  - [ ]* 6.6 Viết property test MenuItem sau khi xóa không xuất hiện
    - **Property 9: MenuItem sau khi xóa không xuất hiện trong menu**
    - **Validates: Requirements 3.5**

  - [ ]* 6.7 Viết property test Category có MenuItem không thể bị xóa
    - **Property 10: Category có MenuItem không thể bị xóa**
    - **Validates: Requirements 3.6**

  - [ ]* 6.8 Viết property test menu trả về theo thứ tự display_order
    - **Property 11: Menu được trả về theo thứ tự display_order**
    - **Validates: Requirements 3.7**

- [ ] 7. Checkpoint — Đảm bảo tất cả test pass, hỏi người dùng nếu có thắc mắc.

- [ ] 8. Triển khai quản lý Cycle
  - [ ] 8.1 Viết `CycleService.java` và `CycleController.java`
    - `CycleService`: mở Cycle (kiểm tra không có Cycle OPEN), đóng Cycle, lấy Cycle hiện tại, tính `timeLeft`
    - `CycleController`: GET `/api/cycles/current`, POST `/api/cycles/open` (Admin), POST `/api/cycles/close` (Admin), GET `/api/cycles` (Admin)
    - _Requirements: 4.3, 4.4, 4.5, 4.6, 4.7_

  - [ ] 8.2 Viết `CycleScheduler.java`
    - `openCycle()`: cron `0 0 9 ? * THU/2` — tự động mở Cycle mỗi thứ Năm 09:00 cách tuần
    - `checkAndCloseCycles()`: cron `0 * * * * *` — kiểm tra mỗi phút, đóng Cycle quá `scheduled_close_at`
    - _Requirements: 4.1, 4.2_

  - [ ]* 8.3 Viết property test lịch sử Cycle và Order được bảo toàn
    - **Property 12: Lịch sử Cycle và Order được bảo toàn**
    - **Validates: Requirements 4.7**

  - [ ]* 8.4 Viết property test thời gian còn lại của Cycle OPEN luôn dương
    - **Property 13: Thời gian còn lại của Cycle OPEN luôn dương**
    - **Validates: Requirements 4.5**

  - [ ]* 8.5 Viết integration test Scheduler tự động mở/đóng Cycle
    - Test `CycleScheduler.openCycle()` với Testcontainers + PostgreSQL
    - Test `CycleScheduler.checkAndCloseCycles()` với Cycle quá hạn
    - _Requirements: 4.1, 4.2_

- [ ] 9. Triển khai đặt và sửa đơn nước (Order)
  - [ ] 9.1 Viết `OrderService.java` và `OrderController.java`
    - `OrderService`: tạo Order (validate Cycle OPEN, kiểm tra chưa có Order trong Cycle), sửa Order, lấy Order của Member
    - `OrderController`: GET `/api/orders/my` (Member), POST `/api/orders` (Member), PUT `/api/orders/{id}` (Member)
    - Trả về 422 khi Cycle đóng; 409 khi đã có Order trong Cycle
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

  - [ ]* 9.2 Viết property test Order creation round-trip
    - **Property 14: Order creation round-trip**
    - **Validates: Requirements 5.1, 5.2**

  - [ ]* 9.3 Viết property test Order update round-trip
    - **Property 15: Order update round-trip**
    - **Validates: Requirements 5.3**

  - [ ]* 9.4 Viết property test mỗi Member chỉ có một Order trong một Cycle
    - **Property 16: Mỗi Member chỉ có một Order trong một Cycle**
    - **Validates: Requirements 5.4**

  - [ ]* 9.5 Viết property test không thể tạo/sửa Order khi Cycle đóng
    - **Property 17: Không thể tạo hoặc sửa Order khi Cycle đóng**
    - **Validates: Requirements 5.5**

- [ ] 10. Triển khai danh sách đơn và theo dõi lấy nước (Admin)
  - [ ] 10.1 Mở rộng `OrderController.java` cho Admin
    - GET `/api/orders`: lấy tất cả Order trong Cycle hiện tại, hỗ trợ filter `?status=picked|unpicked` và search `?memberName=`
    - PATCH `/api/orders/{id}/pickup`: toggle `is_picked_up`, ghi `picked_up_at`
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_

  - [ ]* 10.2 Viết property test danh sách Order chứa đầy đủ thông tin
    - **Property 18: Danh sách Order chứa đầy đủ thông tin**
    - **Validates: Requirements 6.1**

  - [ ]* 10.3 Viết property test filter Order theo trạng thái lấy nước
    - **Property 19: Filter Order theo trạng thái lấy nước là chính xác**
    - **Validates: Requirements 6.2**

  - [ ]* 10.4 Viết property test tìm kiếm Order theo tên thành viên
    - **Property 20: Tìm kiếm Order theo tên thành viên là chính xác**
    - **Validates: Requirements 6.3**

  - [ ]* 10.5 Viết property test pickup toggle round-trip
    - **Property 21: Pickup toggle round-trip**
    - **Validates: Requirements 6.4, 6.5**

  - [ ]* 10.6 Viết property test thống kê tổng đơn luôn nhất quán
    - **Property 22: Thống kê tổng đơn luôn nhất quán**
    - **Validates: Requirements 6.6**

  - [ ]* 10.7 Viết property test danh sách đơn phản ánh ngay Order mới tạo
    - **Property 23: Danh sách đơn phản ánh ngay Order mới tạo**
    - **Validates: Requirements 6.7**

- [ ] 11. Checkpoint — Đảm bảo tất cả test pass, hỏi người dùng nếu có thắc mắc.

- [ ] 12. Triển khai xuất Excel
  - [ ] 12.1 Viết `ExportService.java` và `ExportController.java`
    - `ExportService`: query Orders JOIN Users, MenuItems, Categories; dùng Apache POI tạo Workbook với header 6 cột (STT, Tên thành viên, Danh mục, Tên đồ uống, Ghi chú, Trạng thái lấy nước); nhóm theo Category; xử lý trường hợp không có Order
    - `ExportController`: GET `/api/export/excel` (Admin) — trả về file `.xlsx`
    - _Requirements: 7.1, 7.2, 7.3, 7.4_

  - [ ]* 12.2 Viết property test file Excel chứa đầy đủ dữ liệu và cột
    - **Property 24: File Excel chứa đầy đủ dữ liệu và cột**
    - **Validates: Requirements 7.1, 7.2**

  - [ ]* 12.3 Viết property test file Excel nhóm Orders theo Category
    - **Property 25: File Excel nhóm Orders theo Category**
    - **Validates: Requirements 7.3**

  - [ ]* 12.4 Viết unit test xuất Excel khi không có Order
    - Kiểm tra file Excel vẫn hợp lệ với header row và thông báo "Không có dữ liệu"
    - _Requirements: 7.4_

- [ ] 13. Xây dựng giao diện Frontend
  - [ ] 13.1 Tạo trang đăng nhập `index.html` và `auth.js`
    - Form đăng nhập (username, password), gọi POST `/api/auth/login`
    - Lưu JWT token vào `localStorage`, redirect theo role (Admin → `/admin/dashboard.html`, Member → `/member/order.html`)
    - Xử lý lỗi 401: hiển thị thông báo lỗi xác thực
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

  - [ ] 13.2 Tạo trang đặt nước Member `member/order.html` và `order.js`
    - Hiển thị trạng thái Cycle (OPEN/CLOSED) và thời gian còn lại / thời gian đến Cycle tiếp theo
    - Load menu theo Category và MenuItem từ API
    - Hiển thị đơn hiện tại nếu đã có; form chọn MenuItem và nhập ghi chú
    - Gọi POST `/api/orders` hoặc PUT `/api/orders/{id}` tùy trường hợp
    - Disable form khi Cycle đóng
    - _Requirements: 4.5, 4.6, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

  - [ ] 13.3 Tạo trang quản trị Admin `admin/dashboard.html` và `admin-dashboard.js`
    - Hiển thị danh sách tất cả Order trong Cycle hiện tại (tên Member, MenuItem, Category, ghi chú, trạng thái)
    - Bộ lọc theo trạng thái lấy nước và ô tìm kiếm theo tên thành viên
    - Checkbox tích/bỏ tích lấy nước cho từng Order (gọi PATCH `/api/orders/{id}/pickup`)
    - Hiển thị thống kê: tổng đơn, đã lấy, chưa lấy
    - Nút mở/đóng Cycle thủ công, nút xuất Excel
    - _Requirements: 4.3, 4.4, 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 7.1_

  - [ ] 13.4 Tạo trang quản lý menu `admin/menu.html` và `admin-menu.js`
    - Hiển thị danh sách Category và MenuItem theo `display_order`
    - Form thêm/sửa/xóa Category và MenuItem
    - Xử lý lỗi 422 khi xóa Category còn MenuItem
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

  - [ ] 13.5 Tạo trang quản lý tài khoản `admin/users.html` và `admin-users.js`
    - Hiển thị danh sách thành viên
    - Form thêm thành viên mới (username, họ tên)
    - Xử lý lỗi 409 khi username trùng
    - _Requirements: 2.1, 2.2, 2.3_

  - [ ] 13.6 Tạo `style.css` dùng chung
    - Style cơ bản cho layout, form, bảng, nút, thông báo lỗi/thành công
    - _Requirements: Tất cả_

- [ ] 14. Xử lý lỗi toàn cục và GlobalExceptionHandler
  - Viết `GlobalExceptionHandler.java` với `@RestControllerAdvice`
  - Xử lý các exception: `EntityNotFoundException` (404), `DuplicateKeyException` (409), `BusinessRuleException` (422), `AccessDeniedException` (403)
  - Trả về cấu trúc JSON chuẩn: `{timestamp, status, error, message, path}`
  - _Requirements: 1.2, 2.2, 3.6, 5.4, 5.5_

- [ ] 15. Kết nối và tích hợp toàn bộ hệ thống
  - [ ] 15.1 Viết integration test full flow với Testcontainers
    - Test flow: đăng nhập → xem menu → đặt đơn → Admin tích lấy nước → xuất Excel
    - _Requirements: 1.1, 5.1, 5.2, 6.4, 7.1_

  - [ ] 15.2 Kiểm tra và đảm bảo CORS, security headers đúng cấu hình
    - Cấu hình CORS cho phép frontend gọi API
    - _Requirements: 1.4_

- [ ] 16. Checkpoint cuối — Đảm bảo tất cả test pass, hỏi người dùng nếu có thắc mắc.

## Ghi chú

- Task đánh dấu `*` là tùy chọn, có thể bỏ qua để triển khai MVP nhanh hơn
- Mỗi task tham chiếu đến requirements cụ thể để đảm bảo traceability
- Property test dùng thư viện **jqwik** với tối thiểu 100 iterations mỗi test
- Unit test và integration test dùng **JUnit 5 + Mockito + Testcontainers**
- Các checkpoint đảm bảo kiểm tra tăng dần sau mỗi nhóm tính năng
