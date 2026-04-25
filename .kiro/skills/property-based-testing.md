---
name: property-based-testing
description: Kỹ năng viết property-based tests với jqwik cho Java
tags: [testing, property-based-testing, jqwik, java]
---

# Property-Based Testing với jqwik

## Khái niệm

Property-based testing kiểm tra các thuộc tính phổ quát của hệ thống trên nhiều input ngẫu nhiên, thay vì test từng ví dụ cụ thể. Mỗi property test chạy 100+ iterations với dữ liệu được generate tự động.

## Setup jqwik

### Maven Dependency
```xml
<dependency>
    <groupId>net.jqwik</groupId>
    <artifactId>jqwik</artifactId>
    <version>1.7.4</version>
    <scope>test</scope>
</dependency>
```

### Gradle Dependency
```gradle
testImplementation 'net.jqwik:jqwik:1.7.4'
```

## Cấu trúc Property Test

### Basic Template
```java
import net.jqwik.api.*;

class MyPropertyTest {
    
    // Feature: feature-name, Property X: Description
    @Property(tries = 100)
    void propertyName(@ForAll("generator") InputType input) {
        // Arrange
        setupTestData(input);
        
        // Act
        ResultType result = systemUnderTest.operation(input);
        
        // Assert
        assertThat(result).satisfies(propertyCondition);
    }
    
    @Provide
    Arbitrary<InputType> generator() {
        return Arbitraries.integers().between(1, 100)
            .map(i -> new InputType(i));
    }
}
```

## Generators (Arbitraries)

### Primitive Types
```java
@Provide
Arbitrary<String> usernames() {
    return Arbitraries.strings()
        .alpha()
        .ofMinLength(3)
        .ofMaxLength(20);
}

@Provide
Arbitrary<Integer> positiveIntegers() {
    return Arbitraries.integers().between(1, Integer.MAX_VALUE);
}

@Provide
Arbitrary<Boolean> booleans() {
    return Arbitraries.of(true, false);
}
```

### Complex Objects
```java
@Provide
Arbitrary<User> users() {
    Arbitrary<String> usernames = Arbitraries.strings()
        .alpha().ofMinLength(3).ofMaxLength(20);
    
    Arbitrary<String> passwords = Arbitraries.strings()
        .withCharRange('a', 'z')
        .withCharRange('A', 'Z')
        .withCharRange('0', '9')
        .ofMinLength(8);
    
    return Combinators.combine(usernames, passwords)
        .as((username, password) -> new User(username, password));
}
```

### Lists và Collections
```java
@Provide
Arbitrary<List<String>> menuItems() {
    return Arbitraries.strings()
        .alpha()
        .ofMinLength(3)
        .list()
        .ofMinSize(1)
        .ofMaxSize(10);
}
```

## Ví dụ Property Tests cho Water Order Management

### Property 1: Login round-trip
```java
// Feature: water-order-management, Property 1: Login round-trip trả về token với đúng role
@Property(tries = 100)
void loginRoundTripReturnsTokenWithCorrectRole(
    @ForAll("validUsers") User user
) {
    // Arrange
    userRepository.save(user);
    LoginRequest request = new LoginRequest(user.getUsername(), "rawPassword");
    
    // Act
    LoginResponse response = authService.login(request);
    String roleFromToken = jwtUtil.extractRole(response.getToken());
    
    // Assert
    assertThat(response.getToken()).isNotNull();
    assertThat(roleFromToken).isEqualTo(user.getRole());
}

@Provide
Arbitrary<User> validUsers() {
    Arbitrary<String> usernames = Arbitraries.strings()
        .alpha().ofMinLength(3).ofMaxLength(20);
    
    Arbitrary<String> roles = Arbitraries.of("ADMIN", "MEMBER");
    
    return Combinators.combine(usernames, roles)
        .as((username, role) -> {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode("rawPassword"));
            user.setRole(role);
            return user;
        });
}
```

### Property 5: Username uniqueness
```java
// Feature: water-order-management, Property 5: Username là duy nhất trong hệ thống
@Property(tries = 100)
void usernameUniquenessInvariant(
    @ForAll("usernames") String username,
    @ForAll("fullNames") List<String> fullNames
) {
    Assume.that(fullNames.size() >= 2);
    
    // Arrange - tạo user đầu tiên thành công
    userService.createMember(username, fullNames.get(0));
    long countBefore = userRepository.count();
    
    // Act - cố gắng tạo user thứ hai với cùng username
    assertThatThrownBy(() -> 
        userService.createMember(username, fullNames.get(1))
    ).isInstanceOf(DuplicateUsernameException.class);
    
    // Assert - tổng số user không tăng
    long countAfter = userRepository.count();
    assertThat(countAfter).isEqualTo(countBefore);
}

@Provide
Arbitrary<String> usernames() {
    return Arbitraries.strings()
        .alpha()
        .ofMinLength(3)
        .ofMaxLength(20);
}

@Provide
Arbitrary<List<String>> fullNames() {
    return Arbitraries.strings()
        .alpha()
        .ofMinLength(5)
        .list()
        .ofMinSize(2)
        .ofMaxSize(5);
}
```

### Property 14: Order creation round-trip
```java
// Feature: water-order-management, Property 14: Order creation round-trip
@Property(tries = 100)
void orderCreationRoundTrip(
    @ForAll("validOrderRequests") OrderRequest request
) {
    // Arrange
    Cycle cycle = createOpenCycle();
    Member member = createMember();
    MenuItem menuItem = createMenuItem();
    
    // Act
    Order created = orderService.createOrder(member.getId(), request);
    Order retrieved = orderService.findById(created.getId());
    
    // Assert
    assertThat(retrieved.getMemberId()).isEqualTo(member.getId());
    assertThat(retrieved.getMenuItemId()).isEqualTo(request.getMenuItemId());
    assertThat(retrieved.getNote()).isEqualTo(request.getNote());
    assertThat(retrieved.isPickedUp()).isFalse();
}

@Provide
Arbitrary<OrderRequest> validOrderRequests() {
    Arbitrary<Long> menuItemIds = Arbitraries.longs().between(1L, 100L);
    Arbitrary<String> notes = Arbitraries.strings()
        .alpha()
        .numeric()
        .withChars(' ', ',', '.')
        .ofMaxLength(200);
    
    return Combinators.combine(menuItemIds, notes)
        .as(OrderRequest::new);
}
```

### Property 22: Thống kê tổng đơn nhất quán
```java
// Feature: water-order-management, Property 22: Thống kê tổng đơn luôn nhất quán
@Property(tries = 100)
void orderStatisticsAreConsistent(
    @ForAll("orderLists") List<Order> orders
) {
    // Arrange
    Cycle cycle = createOpenCycle();
    orders.forEach(order -> {
        order.setCycleId(cycle.getId());
        orderRepository.save(order);
    });
    
    // Act
    OrderStatistics stats = orderService.getStatistics(cycle.getId());
    
    // Assert
    assertThat(stats.getTotal())
        .isEqualTo(stats.getPicked() + stats.getUnpicked());
}

@Provide
Arbitrary<List<Order>> orderLists() {
    Arbitrary<Boolean> pickedUpStatus = Arbitraries.of(true, false);
    
    return pickedUpStatus
        .map(picked -> {
            Order order = new Order();
            order.setPickedUp(picked);
            return order;
        })
        .list()
        .ofMinSize(1)
        .ofMaxSize(50);
}
```

## Best Practices

### 1. Sử dụng Assume để filter input
```java
@Property
void property(@ForAll int value) {
    Assume.that(value > 0);  // Chỉ test với giá trị dương
    // test logic
}
```

### 2. Shrinking — tìm counterexample nhỏ nhất
jqwik tự động shrink input khi test fail để tìm ví dụ đơn giản nhất gây lỗi.

### 3. Seed để reproduce
```java
@Property(seed = "1234567890")  // Reproduce với seed cố định
void reproducibleProperty(@ForAll int value) {
    // test logic
}
```

### 4. Stateful testing
```java
@Property
void statefulProperty(@ForAll("actions") ActionSequence<OrderSystem> actions) {
    actions.run(new OrderSystem());
}
```

### 5. Tag property với comment
Luôn thêm comment tham chiếu property trong design:
```java
// Feature: water-order-management, Property 5: Username là duy nhất trong hệ thống
// Validates: Requirements 2.2, 2.3
@Property(tries = 100)
void usernameUniquenessInvariant(...) { }
```

## Integration với Spring Boot Test

```java
@SpringBootTest
@Testcontainers
class OrderPropertyTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }
    
    // Feature: water-order-management, Property 14: Order creation round-trip
    @Property(tries = 100)
    void orderCreationRoundTrip(@ForAll("validOrderRequests") OrderRequest request) {
        // test với real database
    }
}
```

## Khi nào dùng Property Test vs Unit Test

**Property Test** — kiểm tra thuộc tính phổ quát:
- Round-trip properties (create → retrieve)
- Invariants (username uniqueness, statistics consistency)
- Idempotence (gọi nhiều lần cho kết quả giống nhau)
- Commutativity (thứ tự không quan trọng)

**Unit Test** — kiểm tra ví dụ cụ thể:
- Edge cases (empty list, null values)
- Error handling (specific exceptions)
- Business rules phức tạp với context cụ thể
