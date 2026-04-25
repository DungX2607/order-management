# Coding Standards - Water Order Management

## Java Spring Boot Backend

### Naming Conventions
- **Classes**: PascalCase — `UserService`, `OrderController`
- **Methods**: camelCase — `createOrder()`, `findByUsername()`
- **Variables**: camelCase — `userId`, `menuItemId`
- **Constants**: UPPER_SNAKE_CASE — `DEFAULT_PASSWORD`, `JWT_EXPIRATION`
- **Packages**: lowercase — `com.center.waterorder.service`

### Code Organization
- **Controller**: chỉ xử lý HTTP request/response, delegate logic cho Service
- **Service**: chứa business logic, transaction management
- **Repository**: chỉ data access, không có business logic
- **DTO**: dùng cho request/response, không expose Entity trực tiếp

### Error Handling
- Dùng `@RestControllerAdvice` cho global exception handling
- Trả về JSON chuẩn: `{timestamp, status, error, message, path}`
- HTTP status codes:
  - 400 — Bad Request (dữ liệu không hợp lệ)
  - 401 — Unauthorized (chưa đăng nhập)
  - 403 — Forbidden (không đủ quyền)
  - 404 — Not Found (resource không tồn tại)
  - 409 — Conflict (trùng lặp dữ liệu)
  - 422 — Unprocessable Entity (vi phạm business rule)

### Security
- Mật khẩu luôn hash bằng BCrypt
- JWT token có thời hạn 8 giờ
- Protected endpoints yêu cầu JWT trong header `Authorization: Bearer <token>`
- Admin endpoints kiểm tra role `ADMIN`

### Testing
- **Unit test**: test service logic với mock repository
- **Property test**: dùng jqwik, tối thiểu 100 iterations, tag với comment tham chiếu property trong design
- **Integration test**: dùng Testcontainers + PostgreSQL thật
- Mỗi test phải có tên rõ ràng: `shouldReturnConflictWhenUsernameExists()`

### Database
- Entity JPA dùng annotation: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`
- Quan hệ: `@ManyToOne`, `@OneToMany` với `fetch = FetchType.LAZY`
- Constraint: `@Column(unique = true)`, `@UniqueConstraint`
- Timestamp: `@CreatedDate`, `@LastModifiedDate` hoặc `DEFAULT NOW()` trong SQL

## Frontend (HTML/CSS/JS)

### File Organization
- Mỗi trang HTML có file JS riêng: `order.html` → `order.js`
- CSS dùng chung trong `style.css`
- Tách logic auth vào `auth.js` để reuse

### JavaScript Conventions
- **Functions**: camelCase — `loadMenu()`, `submitOrder()`
- **Constants**: UPPER_SNAKE_CASE — `API_BASE_URL`, `TOKEN_KEY`
- Dùng `fetch()` cho API calls, luôn xử lý error
- JWT token lưu trong `localStorage.getItem('token')`
- Mỗi request gửi header: `Authorization: Bearer ${token}`

### API Calls Pattern
```javascript
async function callApi(endpoint, method = 'GET', body = null) {
  const token = localStorage.getItem('token');
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  };
  
  const options = { method, headers };
  if (body) options.body = JSON.stringify(body);
  
  const response = await fetch(`/api${endpoint}`, options);
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
  return response.json();
}
```

### UI/UX
- Hiển thị loading state khi gọi API
- Hiển thị thông báo lỗi/thành công rõ ràng
- Disable form khi Cycle đóng
- Validate input trước khi submit

## Git Commit Messages
- Format: `[Component] Action description`
- Examples:
  - `[Backend] Add JWT authentication`
  - `[Frontend] Implement order page`
  - `[Test] Add property test for username uniqueness`
  - `[DB] Create schema and seed data`

## Documentation
- Mỗi API endpoint có comment mô tả: method, path, auth, request/response
- Service method có JavaDoc nếu logic phức tạp
- Property test có comment tham chiếu property trong design: `// Property 5: Username là duy nhất`
