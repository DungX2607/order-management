---
name: spring-boot-api
description: Kỹ năng xây dựng REST API với Spring Boot theo best practices
tags: [java, spring-boot, rest-api, backend]
---

# Spring Boot REST API Development

## Controller Layer Best Practices

### Structure
```java
@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService service;
    
    @GetMapping
    public ResponseEntity<List<ResourceDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }
    
    @PostMapping
    public ResponseEntity<ResourceDto> create(@Valid @RequestBody CreateResourceRequest request) {
        ResourceDto created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ResourceDto> update(
        @PathVariable Long id,
        @Valid @RequestBody UpdateResourceRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Validation
- Dùng `@Valid` cho request body
- Dùng Bean Validation annotations: `@NotNull`, `@NotBlank`, `@Size`, `@Email`
- Custom validator cho business rules phức tạp

### Response Status
- 200 OK — GET, PUT thành công
- 201 Created — POST thành công
- 204 No Content — DELETE thành công
- 400 Bad Request — validation lỗi
- 404 Not Found — resource không tồn tại
- 409 Conflict — duplicate key
- 422 Unprocessable Entity — business rule violation

## Service Layer Best Practices

### Structure
```java
@Service
@RequiredArgsConstructor
@Transactional
public class ResourceService {
    private final ResourceRepository repository;
    
    @Transactional(readOnly = true)
    public List<ResourceDto> findAll() {
        return repository.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }
    
    public ResourceDto create(CreateResourceRequest request) {
        // Validate business rules
        if (repository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Resource already exists");
        }
        
        Resource entity = new Resource();
        entity.setName(request.getName());
        
        Resource saved = repository.save(entity);
        return toDto(saved);
    }
    
    private ResourceDto toDto(Resource entity) {
        return ResourceDto.builder()
            .id(entity.getId())
            .name(entity.getName())
            .build();
    }
}
```

### Transaction Management
- `@Transactional` ở class level cho write operations
- `@Transactional(readOnly = true)` cho read operations
- Tránh lazy loading exception bằng cách fetch data trong transaction

## Repository Layer

### JPA Repository
```java
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    boolean existsByName(String name);
    Optional<Resource> findByName(String name);
    List<Resource> findByStatus(Status status);
    
    @Query("SELECT r FROM Resource r WHERE r.category.id = :categoryId")
    List<Resource> findByCategoryId(@Param("categoryId") Long categoryId);
}
```

### Query Methods
- Dùng method name convention: `findBy`, `existsBy`, `countBy`
- Dùng `@Query` cho query phức tạp
- Dùng `@Param` cho named parameters

## Exception Handling

### Global Exception Handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Conflict")
            .message(ex.getMessage())
            .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
            
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message(message)
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

## Security

### JWT Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## Testing

### Unit Test với Mockito
```java
@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {
    
    @Mock
    private ResourceRepository repository;
    
    @InjectMocks
    private ResourceService service;
    
    @Test
    void shouldCreateResourceSuccessfully() {
        // Given
        CreateResourceRequest request = new CreateResourceRequest("Test");
        when(repository.existsByName("Test")).thenReturn(false);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        // When
        ResourceDto result = service.create(request);
        
        // Then
        assertNotNull(result);
        assertEquals("Test", result.getName());
        verify(repository).save(any());
    }
    
    @Test
    void shouldThrowExceptionWhenDuplicate() {
        // Given
        CreateResourceRequest request = new CreateResourceRequest("Test");
        when(repository.existsByName("Test")).thenReturn(true);
        
        // When & Then
        assertThrows(DuplicateResourceException.class, () -> service.create(request));
        verify(repository, never()).save(any());
    }
}
```

### Integration Test với Testcontainers
```java
@SpringBootTest
@Testcontainers
class ResourceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Autowired
    private ResourceRepository repository;
    
    @Test
    void shouldPersistAndRetrieveResource() {
        Resource resource = new Resource();
        resource.setName("Test");
        
        Resource saved = repository.save(resource);
        Optional<Resource> found = repository.findById(saved.getId());
        
        assertTrue(found.isPresent());
        assertEquals("Test", found.get().getName());
    }
}
```
