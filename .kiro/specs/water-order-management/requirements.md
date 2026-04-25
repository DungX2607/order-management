# Requirements Document

## Introduction

Hệ thống quản lý đặt nước cho trung tâm giúp các thành viên đặt đồ uống theo chu kỳ 2 tuần/lần, tránh nhầm lẫn đơn hàng giữa các thành viên. Hệ thống bao gồm: quản lý menu theo danh mục nhà cung cấp, đặt hàng có ghi chú, theo dõi trạng thái lấy nước, và xuất báo cáo Excel. Admin quản lý menu, tài khoản, và xác nhận lấy nước; thành viên đăng nhập để đặt và sửa đơn trong chu kỳ mở.

Tech stack: Frontend — HTML/CSS/JS thuần. Backend — Java Spring Boot. Database — PostgreSQL.

## Glossary

- **System**: Hệ thống quản lý đặt nước
- **Admin**: Tài khoản quản trị viên có quyền quản lý menu, thêm thành viên, tích nhận nước và xuất Excel
- **Member**: Thành viên trung tâm có tài khoản đăng nhập để đặt và sửa đơn
- **Order**: Đơn đặt nước của một thành viên trong một chu kỳ, gồm món đã chọn và ghi chú
- **Cycle**: Chu kỳ đặt nước kéo dài 4 tiếng, tự động mở lúc 9:00 sáng thứ Năm cách tuần
- **Category**: Danh mục nhà cung cấp đồ uống (ví dụ: Highland, The Coffee House, Sói Coffee)
- **MenuItem**: Một món đồ uống thuộc một Category trong menu
- **PickupList**: Danh sách theo dõi trạng thái lấy nước của từng thành viên trong một Cycle

---

## Requirements

### Requirement 1: Xác thực người dùng

**User Story:** As a thành viên trung tâm, I want đăng nhập bằng tài khoản/mật khẩu, so that tôi có thể đặt và sửa đơn của mình.

#### Acceptance Criteria

1. WHEN một người dùng gửi yêu cầu đăng nhập với tên đăng nhập và mật khẩu hợp lệ, THE System SHALL trả về phiên đăng nhập thành công và chuyển hướng đến trang chủ.
2. WHEN một người dùng gửi yêu cầu đăng nhập với tên đăng nhập hoặc mật khẩu không hợp lệ, THE System SHALL trả về thông báo lỗi xác thực.
3. THE System SHALL phân biệt vai trò Admin và Member dựa trên thông tin tài khoản sau khi đăng nhập thành công.
4. WHEN một người dùng chưa đăng nhập truy cập trang đặt nước hoặc trang quản trị, THE System SHALL chuyển hướng người dùng đó đến trang đăng nhập.

---

### Requirement 2: Quản lý tài khoản thành viên

**User Story:** As an Admin, I want thêm thành viên mới vào hệ thống, so that tôi có thể cấp tài khoản cho từng người một cách chủ động.

#### Acceptance Criteria

1. WHEN Admin nhập thông tin thành viên mới (tên đăng nhập và họ tên) và xác nhận tạo, THE System SHALL tạo tài khoản Member với mật khẩu mặc định là `123456aA@`.
2. IF tên đăng nhập đã tồn tại trong hệ thống, THEN THE System SHALL hiển thị thông báo lỗi và không tạo tài khoản trùng lặp.
3. THE System SHALL đảm bảo tên đăng nhập của mỗi thành viên là duy nhất trong hệ thống.
4. THE System SHALL hỗ trợ khởi tạo dữ liệu thành viên ban đầu thông qua script insert database để nạp hàng loạt tài khoản một lần duy nhất khi triển khai hệ thống.

---

### Requirement 3: Quản lý menu đồ uống

**User Story:** As an Admin, I want thêm, sửa, xóa danh mục và món đồ uống, so that menu luôn phản ánh đúng các lựa chọn hiện có.

#### Acceptance Criteria

1. THE System SHALL tổ chức MenuItem theo Category (nhà cung cấp).
2. WHEN Admin tạo một Category mới với tên hợp lệ, THE System SHALL lưu Category đó và hiển thị trong danh sách menu.
3. WHEN Admin tạo một MenuItem mới với tên và Category hợp lệ, THE System SHALL lưu MenuItem đó dưới Category tương ứng.
4. WHEN Admin cập nhật tên hoặc thông tin của một MenuItem, THE System SHALL lưu thông tin mới và hiển thị ngay lập tức.
5. WHEN Admin xóa một MenuItem, THE System SHALL xóa MenuItem đó khỏi menu và không hiển thị trong các Cycle tiếp theo.
6. WHEN Admin xóa một Category, THE System SHALL yêu cầu xác nhận và chỉ xóa khi Category không còn MenuItem nào.
7. WHEN một Member đang xem menu, THE System SHALL hiển thị danh sách Category và MenuItem theo thứ tự đã được Admin sắp xếp.

---

### Requirement 4: Quản lý chu kỳ đặt nước

**User Story:** As a thành viên, I want biết khi nào có thể đặt nước, so that tôi không bỏ lỡ đợt đặt hàng.

#### Acceptance Criteria

1. THE System SHALL tự động tạo một Cycle mới và mở trạng thái nhận đơn vào lúc 09:00 sáng thứ Năm cách tuần.
2. WHEN một Cycle đã mở được 4 tiếng, THE System SHALL tự động đóng Cycle đó và không nhận thêm đơn mới.
3. WHEN Admin nhấn nút mở Cycle thủ công, THE System SHALL mở Cycle ngay lập tức bất kể lịch tự động.
4. WHEN Admin nhấn nút đóng Cycle thủ công, THE System SHALL đóng Cycle ngay lập tức và không nhận thêm đơn mới.
5. WHILE một Cycle đang mở, THE System SHALL hiển thị thời gian còn lại đến khi đóng Cycle cho người dùng.
6. WHILE một Cycle đang đóng, THE System SHALL hiển thị thời gian đến Cycle tiếp theo cho người dùng.
7. THE System SHALL lưu trữ lịch sử tất cả các Cycle đã kết thúc cùng với các Order thuộc Cycle đó.

---

### Requirement 5: Đặt và sửa đơn nước

**User Story:** As a Member, I want chọn đồ uống và thêm ghi chú khi đặt, so that đơn hàng của tôi phản ánh đúng yêu cầu cá nhân.

#### Acceptance Criteria

1. WHILE một Cycle đang mở, THE System SHALL cho phép Member đã đăng nhập tạo một Order mới nếu Member đó chưa có Order trong Cycle hiện tại.
2. WHEN Member chọn một MenuItem và nhấn xác nhận đặt, THE System SHALL lưu Order với thông tin: tên Member, MenuItem đã chọn, ghi chú (nếu có), và trạng thái chưa lấy.
3. WHILE một Cycle đang mở, THE System SHALL cho phép Member sửa MenuItem hoặc ghi chú trong Order hiện tại của mình.
4. IF một Member đã có Order trong Cycle hiện tại, THEN THE System SHALL hiển thị đơn hiện tại và không cho phép tạo thêm Order mới trong cùng Cycle.
5. WHILE một Cycle đang đóng, THE System SHALL không cho phép Member tạo hoặc sửa Order.
6. WHEN Member lưu Order thành công, THE System SHALL hiển thị thông báo xác nhận kèm thông tin đơn vừa đặt.

---

### Requirement 6: Xem danh sách đơn và theo dõi lấy nước

**User Story:** As an Admin, I want xem danh sách tất cả đơn trong chu kỳ, lọc và tìm kiếm theo nhu cầu, và tích nhận nước cho từng người, so that tôi kiểm soát được ai đã lấy và ai chưa lấy.

#### Acceptance Criteria

1. THE System SHALL hiển thị danh sách tất cả Order trong Cycle hiện tại, bao gồm: tên Member, tên MenuItem, Category, ghi chú, và trạng thái lấy nước.
2. THE System SHALL cho phép Admin lọc danh sách Order theo trạng thái lấy nước (đã lấy hoặc chưa lấy).
3. THE System SHALL cho phép Admin tìm kiếm Order theo tên thành viên.
4. WHEN Admin tích xác nhận đã lấy nước cho một Order trong danh sách đang hiển thị (kể cả sau khi đã filter hoặc tìm kiếm), THE System SHALL cập nhật trạng thái Order đó thành "đã lấy" và ghi nhận thời điểm xác nhận.
5. WHEN Admin bỏ tích xác nhận của một Order, THE System SHALL cập nhật trạng thái Order đó trở lại "chưa lấy".
6. THE System SHALL hiển thị tổng số đơn, số đơn đã lấy, và số đơn chưa lấy trong Cycle hiện tại.
7. WHILE một Cycle đang mở, THE System SHALL cập nhật danh sách đơn theo thời gian thực khi có Order mới hoặc Order được sửa.

---

### Requirement 7: Xuất báo cáo Excel

**User Story:** As an Admin, I want xuất file Excel danh sách đơn của chu kỳ hiện tại, so that tôi có thể chia sẻ hoặc lưu trữ thông tin đặt hàng.

#### Acceptance Criteria

1. WHEN Admin nhấn nút xuất Excel, THE System SHALL tạo và tải xuống file Excel chứa tất cả Order của Cycle hiện tại.
2. THE System SHALL bao gồm các cột trong file Excel: STT, Tên thành viên, Danh mục (Category), Tên đồ uống (MenuItem), Ghi chú, Trạng thái lấy nước.
3. THE System SHALL nhóm các Order trong file Excel theo Category để dễ đọc.
4. IF Cycle hiện tại không có Order nào, THEN THE System SHALL vẫn tạo file Excel với hàng tiêu đề và thông báo không có dữ liệu.
