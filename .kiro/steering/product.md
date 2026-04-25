# Dự án: Hệ thống Quản lý Đặt Nước Trung Tâm

## Mục tiêu
Hệ thống giúp các thành viên trung tâm đặt đồ uống theo chu kỳ 2 tuần/lần, tránh nhầm lẫn đơn hàng giữa các thành viên.

## Tech Stack
- **Frontend**: HTML, CSS, JavaScript thuần (không dùng framework)
- **Backend**: Java Spring Boot (REST API)
- **Database**: PostgreSQL
- **Auth**: Spring Security + JWT
- **Scheduler**: Spring `@Scheduled`
- **Excel Export**: Apache POI
- **Testing**: JUnit 5 + Mockito + jqwik (property-based) + Testcontainers

## Vai trò người dùng
- **Admin**: quản lý menu, tài khoản, chu kỳ, tích nhận nước, xuất Excel
- **Member**: đăng nhập, xem menu, đặt và sửa đơn trong chu kỳ mở

## Tính năng chính
1. Xem danh sách menu đồ uống theo danh mục (Category)
2. Đặt đơn có ghi chú, sửa đơn trong chu kỳ mở
3. Chu kỳ tự động mở lúc 09:00 thứ Năm cách tuần, đóng sau 4 tiếng — Admin có thể override thủ công
4. Danh sách tích nhận nước với filter (đã lấy/chưa lấy) và tìm kiếm theo tên
5. Xuất file Excel danh sách đơn theo chu kỳ

## Cấu trúc dự án Backend
```
com.center.waterorder
├── config/        # SecurityConfig, SchedulerConfig
├── controller/    # REST controllers
├── service/       # Business logic
├── repository/    # Spring Data JPA repositories
├── model/         # JPA entities: User, Category, MenuItem, Cycle, Order
├── dto/           # Request/Response DTOs
└── scheduler/     # CycleScheduler
```

## Cấu trúc Frontend
```
src/main/resources/static/
├── index.html          # Trang đăng nhập
├── member/order.html   # Trang đặt nước
├── admin/
│   ├── dashboard.html  # Quản lý đơn + tích nhận nước
│   ├── menu.html       # Quản lý menu
│   └── users.html      # Quản lý tài khoản
├── css/style.css
└── js/                 # auth.js, order.js, admin-*.js
```

## Spec
Tài liệu spec đầy đủ tại `.kiro/specs/water-order-management/`
- `requirements.md` — yêu cầu nghiệp vụ
- `design.md` — thiết kế kỹ thuật, data model, API, correctness properties
- `tasks.md` — kế hoạch triển khai
