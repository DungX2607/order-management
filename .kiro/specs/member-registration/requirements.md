# Tài liệu Yêu cầu — Đăng ký Tài khoản Member

## Giới thiệu

Chức năng cho phép người dùng tự đăng ký tài khoản Member trên hệ thống quản lý đặt nước mà không cần Admin tạo hộ. Người dùng cung cấp username, mật khẩu và họ tên đầy đủ thông qua form đăng ký công khai. Sau khi đăng ký thành công, tài khoản được kích hoạt ngay và có thể đăng nhập.

## Thuật ngữ

- **Registration_Service**: Service xử lý logic đăng ký tài khoản Member mới
- **Auth_Controller**: Controller xử lý các endpoint xác thực (login, logout, register) tại `/api/auth`
- **Registration_Form**: Giao diện form đăng ký trên trang đăng nhập (`index.html`)
- **Password_Encoder**: Component mã hóa mật khẩu bằng BCrypt trước khi lưu vào database
- **Member**: Vai trò người dùng thông thường, có quyền đặt nước và xem menu
- **Username**: Tên đăng nhập duy nhất trong hệ thống, tối đa 50 ký tự

## Yêu cầu

### Yêu cầu 1: Đăng ký tài khoản Member

**User Story:** Là một người dùng mới, tôi muốn tự đăng ký tài khoản Member, để tôi có thể sử dụng hệ thống đặt nước mà không cần nhờ Admin tạo hộ.

#### Tiêu chí chấp nhận

1. THE Auth_Controller SHALL cung cấp endpoint `POST /api/auth/register` cho phép truy cập công khai mà không yêu cầu JWT token
2. WHEN người dùng gửi yêu cầu đăng ký với username, password và fullName hợp lệ, THE Registration_Service SHALL tạo tài khoản Member mới với trạng thái active là true
3. WHEN tài khoản được tạo thành công, THE Registration_Service SHALL mã hóa mật khẩu bằng Password_Encoder trước khi lưu vào database
4. WHEN tài khoản được tạo thành công, THE Auth_Controller SHALL trả về HTTP 201 Created kèm thông tin tài khoản (id, username, fullName, role)
5. THE Registration_Service SHALL gán role "MEMBER" cho mọi tài khoản được tạo qua đăng ký

### Yêu cầu 2: Validation dữ liệu đăng ký

**User Story:** Là một người dùng mới, tôi muốn nhận phản hồi rõ ràng khi nhập dữ liệu không hợp lệ, để tôi có thể sửa và hoàn tất đăng ký.

#### Tiêu chí chấp nhận

1. WHEN username để trống hoặc chỉ chứa khoảng trắng, THE Auth_Controller SHALL trả về HTTP 400 Bad Request với thông báo "Username không được để trống"
2. WHEN password để trống, THE Auth_Controller SHALL trả về HTTP 400 Bad Request với thông báo "Mật khẩu không được để trống"
3. WHEN password có độ dài dưới 8 ký tự, THE Auth_Controller SHALL trả về HTTP 400 Bad Request với thông báo lỗi mô tả yêu cầu độ dài tối thiểu
4. WHEN fullName để trống hoặc chỉ chứa khoảng trắng, THE Auth_Controller SHALL trả về HTTP 400 Bad Request với thông báo "Họ tên không được để trống"
5. WHEN username có độ dài vượt quá 50 ký tự, THE Auth_Controller SHALL trả về HTTP 400 Bad Request với thông báo lỗi mô tả giới hạn độ dài

### Yêu cầu 3: Xử lý username trùng lặp

**User Story:** Là một người dùng mới, tôi muốn biết ngay nếu username đã tồn tại, để tôi có thể chọn username khác.

#### Tiêu chí chấp nhận

1. WHEN người dùng đăng ký với username đã tồn tại trong hệ thống, THE Registration_Service SHALL trả về HTTP 409 Conflict với thông báo "Username đã tồn tại"
2. THE Registration_Service SHALL kiểm tra tính duy nhất của username trước khi tạo tài khoản

### Yêu cầu 4: Giao diện đăng ký

**User Story:** Là một người dùng mới, tôi muốn có form đăng ký trực quan trên trang đăng nhập, để tôi có thể tạo tài khoản dễ dàng.

#### Tiêu chí chấp nhận

1. THE Registration_Form SHALL hiển thị trên trang đăng nhập (`index.html`) với các trường: username, password, fullName
2. THE Registration_Form SHALL cho phép chuyển đổi qua lại giữa form đăng nhập và form đăng ký
3. WHEN đăng ký thành công, THE Registration_Form SHALL hiển thị thông báo thành công và chuyển về form đăng nhập
4. WHEN đăng ký thất bại, THE Registration_Form SHALL hiển thị thông báo lỗi từ server cho người dùng
5. WHILE đang gửi yêu cầu đăng ký, THE Registration_Form SHALL vô hiệu hóa nút submit và hiển thị trạng thái đang xử lý

### Yêu cầu 5: Bảo mật endpoint đăng ký

**User Story:** Là quản trị viên hệ thống, tôi muốn endpoint đăng ký được bảo vệ khỏi lạm dụng, để hệ thống hoạt động ổn định.

#### Tiêu chí chấp nhận

1. THE Auth_Controller SHALL đặt endpoint đăng ký dưới path `/api/auth/register` để tận dụng cấu hình permitAll hiện có cho `/api/auth/**`
2. THE Registration_Service SHALL chỉ cho phép tạo tài khoản với role "MEMBER", không cho phép chỉ định role khác qua request đăng ký
3. THE Password_Encoder SHALL mã hóa mật khẩu bằng BCrypt, không lưu mật khẩu dạng plain text
