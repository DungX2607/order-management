# Kế hoạch Triển khai: Đăng ký Tài khoản Member

## Tổng quan

Triển khai tính năng đăng ký tài khoản Member theo thiết kế: tạo RegisterRequest DTO, UsernameAlreadyExistsException, thêm endpoint và logic register vào AuthController/AuthService, cập nhật GlobalExceptionHandler, và thêm form đăng ký trên frontend. Mỗi bước xây dựng trên bước trước, kết thúc bằng việc nối tất cả lại với nhau.

## Tasks

- [x] 1. Tạo RegisterRequest DTO và UsernameAlreadyExistsException
  - [x] 1.1 Tạo `RegisterRequest` DTO với validation annotations
    - Tạo file `src/main/java/com/center/waterorder/dto/RegisterRequest.java`
    - Các trường: `username` (@NotBlank, @Size(max=50)), `password` (@NotBlank, @Size(min=8)), `fullName` (@NotBlank)
    - Dùng Lombok annotations: @Data, @NoArgsConstructor, @AllArgsConstructor
    - Tham khảo `CreateUserRequest.java` và `LoginRequest.java` cho convention
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

  - [x] 1.2 Tạo `UsernameAlreadyExistsException` custom exception
    - Tạo file `src/main/java/com/center/waterorder/exception/UsernameAlreadyExistsException.java`
    - Extends `RuntimeException`, nhận message qua constructor
    - _Requirements: 3.1_

- [x] 2. Implement logic đăng ký trong AuthService
  - [x] 2.1 Thêm method `register(RegisterRequest)` và `toDto(User)` vào AuthService
    - Kiểm tra username trùng bằng `userRepository.existsByUsername()`
    - Nếu trùng → throw `UsernameAlreadyExistsException("Username đã tồn tại")`
    - Tạo User mới: set username, encode password bằng `passwordEncoder.encode()`, set fullName, role="MEMBER", active=true
    - Lưu bằng `userRepository.save()`, trả về `UserDto` qua method `toDto()`
    - Đánh dấu method `@Transactional`
    - _Requirements: 1.2, 1.3, 1.5, 3.1, 3.2, 5.2, 5.3_

  - [ ]* 2.2 Viết unit test cho AuthService.register()
    - Tạo file `src/test/java/com/center/waterorder/service/AuthServiceTest.java`
    - Dùng JUnit 5 + Mockito, mock UserRepository và PasswordEncoder
    - Test cases:
      - `shouldCreateMemberAccountWhenValidRequest()` — happy path, verify UserDto trả về đúng
      - `shouldHashPasswordWhenRegistering()` — verify `passwordEncoder.encode()` được gọi
      - `shouldThrowUsernameAlreadyExistsExceptionWhenDuplicateUsername()` — verify exception khi username trùng
      - `shouldSetRoleToMemberWhenRegistering()` — verify role luôn là "MEMBER"
      - `shouldSetActiveToTrueWhenRegistering()` — verify active luôn là true
    - _Requirements: 1.2, 1.3, 1.5, 3.1, 5.2, 5.3_

  - [ ]* 2.3 Viết property test — Property 1: Đăng ký bảo toàn input và gán đúng giá trị mặc định
    - Tạo file `src/test/java/com/center/waterorder/service/AuthServicePropertyTest.java`
    - **Property 1: Đăng ký bảo toàn input và gán đúng giá trị mặc định**
    - Generate random valid RegisterRequest (username non-blank max 50, password non-blank min 8, fullName non-blank)
    - Verify: username và fullName khớp request, role="MEMBER", active=true, id≠null, createdAt≠null
    - Dùng jqwik, tối thiểu 100 iterations
    - **Validates: Requirements 1.2, 1.4, 1.5, 5.2**

  - [ ]* 2.4 Viết property test — Property 2: Mật khẩu luôn được hash
    - **Property 2: Mật khẩu luôn được hash**
    - Generate random password ≥8 ký tự
    - Verify: password lưu trong DB khác plain text gốc VÀ BCrypt.matches(plain, stored) = true
    - Dùng jqwik, tối thiểu 100 iterations
    - **Validates: Requirements 1.3, 5.3**

  - [ ]* 2.5 Viết property test — Property 3: Trường blank bị từ chối
    - **Property 3: Trường blank bị từ chối**
    - Generate whitespace-only strings cho username/fullName (spaces, tabs, newlines, empty)
    - Verify: hệ thống từ chối request, danh sách user không thay đổi
    - Dùng jqwik, tối thiểu 100 iterations
    - **Validates: Requirements 2.1, 2.4**

  - [ ]* 2.6 Viết property test — Property 4: Mật khẩu quá ngắn bị từ chối
    - **Property 4: Mật khẩu quá ngắn bị từ chối**
    - Generate random strings length 1-7
    - Verify: hệ thống từ chối request và trả về lỗi validation
    - Dùng jqwik, tối thiểu 100 iterations
    - **Validates: Requirements 2.3**

  - [ ]* 2.7 Viết property test — Property 5: Username quá dài bị từ chối
    - **Property 5: Username quá dài bị từ chối**
    - Generate random strings length 51+
    - Verify: hệ thống từ chối request và trả về lỗi validation
    - Dùng jqwik, tối thiểu 100 iterations
    - **Validates: Requirements 2.5**

  - [ ]* 2.8 Viết property test — Property 6: Username trùng lặp trả về conflict
    - **Property 6: Username trùng lặp trả về conflict**
    - Generate random valid request, register lần đầu thành công, register lần hai cùng username
    - Verify: lần hai thất bại với UsernameAlreadyExistsException
    - Dùng jqwik, tối thiểu 100 iterations
    - **Validates: Requirements 3.1, 3.2**

- [x] 3. Thêm endpoint register vào AuthController và handler vào GlobalExceptionHandler
  - [x] 3.1 Thêm handler cho `UsernameAlreadyExistsException` vào GlobalExceptionHandler
    - Thêm method `handleUsernameAlreadyExists()` với `@ExceptionHandler(UsernameAlreadyExistsException.class)`
    - Trả về HTTP 409 Conflict với format error response chuẩn: timestamp, status, error, message
    - Đặt handler TRƯỚC handler của `RuntimeException` để Spring ưu tiên handler cụ thể hơn
    - _Requirements: 3.1_

  - [x] 3.2 Thêm endpoint `POST /api/auth/register` vào AuthController
    - Thêm method `register(@Valid @RequestBody RegisterRequest request)`
    - Trả về `ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request))`
    - Endpoint tự động permitAll nhờ cấu hình `/api/auth/**` trong SecurityConfig
    - _Requirements: 1.1, 1.4, 5.1_

- [x] 4. Checkpoint — Đảm bảo backend hoạt động đúng
  - Đảm bảo tất cả tests pass, hỏi user nếu có thắc mắc.

- [x] 5. Cập nhật frontend — form đăng ký và API call
  - [x] 5.1 Thêm method `api.register()` vào `api.js`
    - Thêm `register: (username, password, fullName) => callApi('/auth/register', 'POST', { username, password, fullName })` vào object `api`
    - _Requirements: 4.1_

  - [x] 5.2 Cập nhật `index.html` — thêm form đăng ký và toggle login/register
    - Thêm form đăng ký với 3 trường: username, password, fullName
    - Thêm link toggle "Chưa có tài khoản? Đăng ký" / "Đã có tài khoản? Đăng nhập"
    - JavaScript show/hide giữa form đăng nhập và form đăng ký
    - Khi đăng ký thành công: hiển thị thông báo thành công, chuyển về form đăng nhập
    - Khi đăng ký thất bại: hiển thị thông báo lỗi từ server
    - Disable nút submit và hiển thị "Đang xử lý..." khi đang gửi request
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 6. Integration tests cho endpoint đăng ký
  - [ ]* 6.1 Viết integration test cho AuthController register endpoint
    - Tạo file `src/test/java/com/center/waterorder/controller/AuthControllerIntegrationTest.java`
    - Dùng @SpringBootTest + @AutoConfigureMockMvc hoặc Testcontainers
    - Test cases:
      - `shouldReturn201WhenValidRegistration()` — full flow, verify response body
      - `shouldReturn400WhenBlankUsername()` — validation lỗi trường blank
      - `shouldReturn400WhenShortPassword()` — password < 8 ký tự
      - `shouldReturn400WhenLongUsername()` — username > 50 ký tự
      - `shouldReturn409WhenDuplicateUsername()` — username đã tồn tại
      - `shouldNotRequireJwtToken()` — gọi không có token, verify không bị 401
    - _Requirements: 1.1, 1.4, 2.1, 2.3, 2.5, 3.1, 5.1_

- [x] 7. Final checkpoint — Đảm bảo toàn bộ tests pass
  - Đảm bảo tất cả tests pass, hỏi user nếu có thắc mắc.

## Ghi chú

- Tasks đánh dấu `*` là optional, có thể bỏ qua để ra MVP nhanh hơn
- Mỗi task tham chiếu requirements cụ thể để đảm bảo traceability
- Checkpoints đảm bảo kiểm tra chất lượng tăng dần
- Property tests kiểm tra 6 correctness properties từ design document
- Unit tests kiểm tra specific examples và edge cases
- SecurityConfig không cần thay đổi — `/api/auth/**` đã permitAll sẵn
- Database schema không cần migration — bảng `users` hiện có đã đủ
