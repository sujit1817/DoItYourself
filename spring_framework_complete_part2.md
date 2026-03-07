# Spring Framework Interview Guide - Part 2 (Q13-Q17)

## Q13: Spring Bean Lifecycle and Scopes {#q13-lifecycle}

### Bean Lifecycle

```
Spring Bean Lifecycle:

1. Bean Definition Loading
   └─► @ComponentScan finds classes, creates BeanDefinitions

2. Bean Instantiation
   └─► Constructor called: new UserService()

3. Dependency Injection
   └─► @Autowired fields/setters populated

4. Post-Initialization
   └─► @PostConstruct method called
   └─► InitializingBean.afterPropertiesSet()

5. Bean Ready to Use
   └─► Bean fully initialized, available for use

6. Pre-Destruction
   └─► @PreDestroy method called
   └─► DisposableBean.destroy()

7. Bean Destroyed
```

### Complete Lifecycle Example

```java
@Component
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    private DataSource dataSource;
    
    // 1. Constructor (Instantiation)
    public UserService() {
        System.out.println("1. Constructor called");
    }
    
    // 2. Setter Injection
    @Autowired
    public void setDataSource(DataSource dataSource) {
        System.out.println("2. Setter injection: DataSource set");
        this.dataSource = dataSource;
    }
    
    // 3. @PostConstruct (After dependencies injected)
    @PostConstruct
    public void init() {
        System.out.println("3. @PostConstruct: Initialization logic");
        loadConfiguration();
        warmUpCache();
    }
    
    // Bean is now ready to use
    public User findUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    // 4. @PreDestroy (On shutdown)
    @PreDestroy
    public void cleanup() {
        System.out.println("4. @PreDestroy: Cleanup logic");
        closeConnections();
        saveState();
    }
    
    private void loadConfiguration() {
        // Load config from file/database
    }
    
    private void warmUpCache() {
        // Pre-load frequently accessed data
    }
    
    private void closeConnections() {
        // Close connections, file handles
    }
}

// Output:
// 1. Constructor called
// 2. Setter injection: DataSource set
// 3. @PostConstruct: Initialization logic
// (Bean ready)
// 4. @PreDestroy: Cleanup logic (on shutdown)
```

### Bean Scopes

#### 1. Singleton Scope (Default)

```java
@Component
@Scope("singleton")  // Default
public class UserService {
    // One instance per Spring container
}

Characteristics:
✅ One instance for entire application
✅ Created when Spring context starts
✅ Shared by all requests
✅ Thread-safe concerns (keep stateless)

Example:
┌──────────┐     ┌──────────┐
│Request 1 │────▶│  Single  │
│Request 2 │────▶│UserService│
│Request 3 │────▶│ Instance │
└──────────┘     └──────────┘
```

#### 2. Prototype Scope

```java
@Component
@Scope("prototype")
public class ShoppingCart {
    private List<Item> items = new ArrayList<>();
    // New instance every time requested
}

Characteristics:
✅ New instance every request
✅ Created on demand (lazy)
✅ Spring doesn't manage destruction
✅ Client responsible for cleanup

Example:
Request 1 → Cart #1
Request 2 → Cart #2
Request 3 → Cart #3

⚠️ Important: @PreDestroy is NOT called!

@Component
@Scope("prototype")
public class PrototypeBean {
    
    @PreDestroy
    public void cleanup() {
        // ❌ NEVER called on prototype beans!
    }
}
```

#### 3. Request Scope (Web)

```java
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, 
       proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LoginRequest {
    private String username;
    private String ipAddress;
    // New instance per HTTP request
}

HTTP Request 1 → LoginRequest #1
HTTP Request 2 → LoginRequest #2
```

#### 4. Session Scope (Web)

```java
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION,
       proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserSession {
    private Long userId;
    private LocalDateTime loginTime;
    // One instance per HTTP session
}

Session 1 (User A) → UserSession #1 (persists across requests)
Session 2 (User B) → UserSession #2
```

### Scope Comparison

| Scope | Instances | When Created | When Destroyed | Use Case |
|-------|-----------|--------------|----------------|----------|
| **singleton** | 1 per container | Context startup | Context shutdown | Stateless services |
| **prototype** | Many | On request | Manual cleanup | Stateful objects |
| **request** | 1 per HTTP request | Request start | Request end | Request data |
| **session** | 1 per HTTP session | Session start | Session expiry | User session data |
| **application** | 1 per ServletContext | App startup | App shutdown | App-wide cache |

### Scoped Proxy Mode

```java
// Problem: Injecting prototype into singleton
@Component
public class SingletonService {
    
    @Autowired
    private PrototypeBean prototypeBean;
    // ❌ Always same instance!
    // Prototype injected ONCE when singleton created
}

// Solution: Use scoped proxy
@Component
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PrototypeBean {
    // Spring injects PROXY instead of actual bean
    // Proxy creates new instance on each method call
}

@Component
public class SingletonService {
    
    @Autowired
    private PrototypeBean prototypeBean;  // ✅ Proxy
    
    public void doSomething() {
        prototypeBean.someMethod();  // New instance each time!
    }
}
```

**Interview Tip:** Emphasize singleton is default and should be stateless. Prototype creates new instance each time. For web scopes, mention proxyMode. Walk through lifecycle: "Bean instantiated, dependencies injected, @PostConstruct called, ready to use, @PreDestroy on shutdown."

---

## Q14: Spring AOP (Aspect-Oriented Programming) {#q14-aop}

### What is AOP?

**AOP** allows you to modularize cross-cutting concerns (logging, security, transactions) separately from business logic.

### Core Concepts

```
┌─────────────────────────────────────────┐
│          AOP Terminology                │
├─────────────────────────────────────────┤
│ Aspect:     Module of cross-cutting     │
│             concern (e.g., logging)     │
│                                         │
│ Join Point: Point in execution          │
│             (method call, exception)    │
│                                         │
│ Advice:     Action taken at join point  │
│             (@Before, @After, @Around)  │
│                                         │
│ Pointcut:   Expression matching         │
│             join points                 │
│                                         │
│ Weaving:    Linking aspects with code   │
└─────────────────────────────────────────┘
```

### Types of Advice

```java
@Aspect
@Component
public class LoggingAspect {
    
    // 1. @Before - Runs before method
    @Before("execution(* com.example.service.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("Before: " + joinPoint.getSignature());
    }
    
    // 2. @After - Runs after method (always)
    @After("execution(* com.example.service.*.*(..))")
    public void logAfter(JoinPoint joinPoint) {
        System.out.println("After: " + joinPoint.getSignature());
    }
    
    // 3. @AfterReturning - Runs after successful return
    @AfterReturning(
        pointcut = "execution(* com.example.service.*.*(..))",
        returning = "result"
    )
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        System.out.println("Returned: " + result);
    }
    
    // 4. @AfterThrowing - Runs after exception
    @AfterThrowing(
        pointcut = "execution(* com.example.service.*.*(..))",
        throwing = "error"
    )
    public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
        System.out.println("Exception: " + error.getMessage());
    }
    
    // 5. @Around - Most powerful (wraps method)
    @Around("execution(* com.example.service.*.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("Around Before");
        
        Object result = joinPoint.proceed();  // Actual method call
        
        System.out.println("Around After");
        return result;
    }
}
```

### Real-World Use Cases

#### 1. Execution Time Logging

```java
@Aspect
@Component
public class PerformanceAspect {
    
    @Around("@annotation(LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        
        Object result = joinPoint.proceed();
        
        long executionTime = System.currentTimeMillis() - start;
        
        System.out.println(
            joinPoint.getSignature() + " executed in " + executionTime + "ms"
        );
        
        return result;
    }
}

// Custom annotation
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime { }

// Usage
@Service
public class UserService {
    
    @LogExecutionTime  // AOP applied here
    public User createUser(User user) {
        // Only business logic
        return userRepository.save(user);
    }
}

// Output: createUser executed in 245ms
```

#### 2. Security/Authorization

```java
@Aspect
@Component
public class SecurityAspect {
    
    @Before("@annotation(requiresRole)")
    public void checkAuthorization(JoinPoint joinPoint, RequiresRole requiresRole) {
        String requiredRole = requiresRole.value();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        boolean hasRole = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals(requiredRole));
        
        if (!hasRole) {
            throw new AccessDeniedException(
                "User doesn't have required role: " + requiredRole
            );
        }
    }
}

// Custom annotation
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    String value();
}

// Usage
@Service
public class AdminService {
    
    @RequiresRole("ROLE_ADMIN")  // Security check via AOP
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
```

#### 3. Exception Handling/Translation

```java
@Aspect
@Component
public class ExceptionTranslationAspect {
    
    @AfterThrowing(
        pointcut = "execution(* com.example.repository.*.*(..))",
        throwing = "ex"
    )
    public void translateException(JoinPoint joinPoint, Exception ex) {
        if (ex instanceof SQLException) {
            throw new DataAccessException(
                "Database error in " + joinPoint.getSignature(),
                ex
            );
        }
    }
}
```

#### 4. Caching

```java
@Aspect
@Component
public class CachingAspect {
    
    private Map<String, Object> cache = new ConcurrentHashMap<>();
    
    @Around("@annotation(Cacheable)")
    public Object cache(ProceedingJoinPoint joinPoint) throws Throwable {
        String key = generateKey(joinPoint);
        
        // Check cache
        if (cache.containsKey(key)) {
            System.out.println("Cache hit: " + key);
            return cache.get(key);
        }
        
        // Execute method
        Object result = joinPoint.proceed();
        
        // Store in cache
        cache.put(key, result);
        System.out.println("Cache miss: " + key);
        
        return result;
    }
    
    private String generateKey(ProceedingJoinPoint joinPoint) {
        return joinPoint.getSignature().toString() + 
               Arrays.toString(joinPoint.getArgs());
    }
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cacheable { }

// Usage
@Service
public class ProductService {
    
    @Cacheable  // Results cached via AOP
    public Product getProduct(Long id) {
        return productRepository.findById(id).orElse(null);
    }
}
```

#### 5. Auditing

```java
@Aspect
@Component
public class AuditAspect {
    
    @Autowired
    private AuditLogRepository auditRepository;
    
    @AfterReturning(
        pointcut = "@annotation(Audit)",
        returning = "result"
    )
    public void audit(JoinPoint joinPoint, Object result) {
        String username = SecurityContextHolder.getContext()
            .getAuthentication().getName();
        
        String action = joinPoint.getSignature().getName();
        String details = Arrays.toString(joinPoint.getArgs());
        
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(action);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        
        auditRepository.save(log);
    }
}

// Usage
@Service
public class UserService {
    
    @Audit  // Audit log created automatically
    public User createUser(User user) {
        return userRepository.save(user);
    }
    
    @Audit
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
```

#### 6. Retry Logic

```java
@Aspect
@Component
public class RetryAspect {
    
    @Around("@annotation(Retry)")
    public Object retry(ProceedingJoinPoint joinPoint) throws Throwable {
        int maxAttempts = 3;
        int attempt = 1;
        
        while (attempt <= maxAttempts) {
            try {
                return joinPoint.proceed();
            } catch (Exception e) {
                if (attempt == maxAttempts) {
                    throw e;
                }
                System.out.println(
                    "Attempt " + attempt + " failed. Retrying..."
                );
                attempt++;
                Thread.sleep(1000 * attempt);  // Exponential backoff
            }
        }
        
        return null;
    }
}

// Usage
@Service
public class PaymentService {
    
    @Retry  // Automatically retries on failure
    public Payment processPayment(PaymentRequest request) {
        // Might fail due to network issues
        return externalPaymentGateway.charge(request);
    }
}
```

### Pointcut Expressions

```java
@Aspect
@Component
public class PointcutExamples {
    
    // 1. Execute any method in service package
    @Before("execution(* com.example.service.*.*(..))")
    public void example1() { }
    
    // 2. Execute any method with @Transactional
    @Before("@annotation(org.springframework.transaction.annotation.Transactional)")
    public void example2() { }
    
    // 3. Execute methods that start with "get"
    @Before("execution(* com.example.service.*.get*(..))")
    public void example3() { }
    
    // 4. Execute methods in specific class
    @Before("execution(* com.example.service.UserService.*(..))")
    public void example4() { }
    
    // 5. Execute methods with specific return type
    @Before("execution(User com.example.service.*.*(..))")
    public void example5() { }
    
    // 6. Reusable pointcut
    @Pointcut("execution(* com.example.service.*.*(..))")
    public void serviceMethods() { }
    
    @Before("serviceMethods()")
    public void beforeService() { }
    
    @After("serviceMethods()")
    public void afterService() { }
}
```

### Accessing Method Parameters

```java
@Aspect
@Component
public class ParameterLoggingAspect {
    
    @Before("execution(* com.example.service.UserService.createUser(..)) && args(user)")
    public void logUserCreation(User user) {
        System.out.println("Creating user: " + user.getName());
    }
    
    @Around("execution(* com.example.service.*.*(..)) && args(id,..)")
    public Object logId(ProceedingJoinPoint joinPoint, Long id) throws Throwable {
        System.out.println("Method called with ID: " + id);
        return joinPoint.proceed();
    }
}
```

### AOP vs Filters vs Interceptors

| Feature | AOP | Filter | Interceptor |
|---------|-----|--------|-------------|
| **Scope** | Any Spring bean | HTTP requests | Spring MVC |
| **When** | Method execution | Before/after request | Before/after controller |
| **Use Case** | Business logic concerns | Security, logging | Pre/post processing |
| **Example** | Transaction, caching | Authentication | Model manipulation |

**Interview Tip:** Explain AOP separates cross-cutting concerns from business logic. Give real examples: "I used AOP for logging execution time - added @LogExecutionTime annotation to methods, aspect automatically logs duration without cluttering business code." Mention @Around is most powerful as it can control method execution.

---

## Q15: Exception Handling in Spring Boot {#q15-exceptions}

### Global Exception Handling with @ControllerAdvice

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // Handle specific exception
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
        ResourceNotFoundException ex,
        WebRequest request
    ) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .path(request.getDescription(false))
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    // Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
        MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Input validation error")
            .validationErrors(errors)
            .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    // Handle generic exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex,
        WebRequest request
    ) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred")
            .path(request.getDescription(false))
            .build();
        
        // Log full stack trace
        log.error("Unexpected error", ex);
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error);
    }
}

// Error Response DTO
@Data
@Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors;
}
```

### Custom Exception Classes

```java
// Base exception
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Specific exceptions
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s not found with id: %d", resourceName, id));
    }
}

public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}

public class InvalidOperationException extends BusinessException {
    public InvalidOperationException(String message) {
        super(message);
    }
}
```

### Using Custom Exceptions

```java
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
    
    public User createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                "User with email " + request.getEmail() + " already exists"
            );
        }
        
        User user = new User(request);
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        User user = findById(id);  // Throws ResourceNotFoundException
        
        if (user.hasActiveOrders()) {
            throw new InvalidOperationException(
                "Cannot delete user with active orders"
            );
        }
        
        userRepository.delete(user);
    }
}
```

### Validation

```java
// Entity with validation
public class UserRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 120, message = "Age must be less than 120")
    private Integer age;
    
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
        message = "Password must be at least 8 characters with letters and numbers"
    )
    private String password;
}

// Controller with validation
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @PostMapping
    public ResponseEntity<User> createUser(
        @Valid @RequestBody UserRequest request  // @Valid triggers validation
    ) {
        User created = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
```

### Response Status Exceptions

```java
@GetMapping("/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "User not found with id: " + id
        ));
}
```

### Exception Handling in Different Layers

```java
// Controller layer - catches and returns HTTP responses
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        try {
            Order order = orderService.createOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (InsufficientStockException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        } catch (PaymentFailedException e) {
            throw new ResponseStatusException(
                HttpStatus.PAYMENT_REQUIRED,
                e.getMessage()
            );
        }
    }
}

// Service layer - business exceptions
@Service
public class OrderService {
    
    @Transactional
    public Order createOrder(OrderRequest request) {
        // Check inventory
        if (!inventoryService.hasStock(request.getItems())) {
            throw new InsufficientStockException("Insufficient stock");
        }
        
        // Process payment
        Payment payment = paymentService.processPayment(request);
        if (payment.getStatus() == PaymentStatus.FAILED) {
            throw new PaymentFailedException("Payment processing failed");
        }
        
        return orderRepository.save(new Order(request));
    }
}
```

### Async Exception Handling

```java
@Configuration
public class AsyncConfig implements AsyncConfigurer {
    
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new CustomAsyncExceptionHandler();
    }
}

public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    
    @Override
    public void handleUncaughtException(
        Throwable throwable,
        Method method,
        Object... params
    ) {
        log.error(
            "Async exception in method: {} with params: {}",
            method.getName(),
            Arrays.toString(params),
            throwable
        );
    }
}
```

**Interview Tip:** Explain @ControllerAdvice provides centralized exception handling. Create custom exceptions for different scenarios. Always return meaningful error messages with proper HTTP status codes. Example: "I use @ControllerAdvice to handle all exceptions globally - ResourceNotFoundException returns 404, ValidationException returns 400 with field errors."

---

## Q16: @Transactional Annotation and How It Works {#q16-transactional}

### What is @Transactional?

Declarative transaction management in Spring. Automatically begins, commits, or rolls back transactions.

### Basic Usage

```java
@Service
public class BankingService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Transactional
    public void transferMoney(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        // 1. Begin transaction (automatic)
        
        Account fromAccount = accountRepository.findById(fromAccountId).orElseThrow();
        Account toAccount = accountRepository.findById(toAccountId).orElseThrow();
        
        // 2. Debit from account
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        accountRepository.save(fromAccount);
        
        // Simulate error
        if (amount.compareTo(new BigDecimal("1000")) > 0) {
            throw new RuntimeException("Amount too large");
        }
        
        // 3. Credit to account
        toAccount.setBalance(toAccount.getBalance().add(amount));
        accountRepository.save(toAccount);
        
        // 4. Commit transaction (automatic if no exception)
        //    OR Rollback (automatic if exception thrown)
    }
}
```

### How @Transactional Works

```
Without @Transactional:
┌──────────────────┐
│ debit(account1)  │ ✓ Committed
├──────────────────┤
│ [Exception]      │ ✗ Throws
├──────────────────┤
│ credit(account2) │ ✗ Never executes
└──────────────────┘
Result: Money lost! account1 debited but account2 not credited

With @Transactional:
┌──────────────────────────┐
│ BEGIN TRANSACTION        │
├──────────────────────────┤
│ debit(account1)          │ Temporary
├──────────────────────────┤
│ [Exception]              │ ✗ Throws
├──────────────────────────┤
│ ROLLBACK TRANSACTION     │ ✓ Automatic
└──────────────────────────┘
Result: All changes reverted, no data loss
```

### Transaction Attributes

```java
@Service
public class OrderService {
    
    // 1. Propagation
    @Transactional(propagation = Propagation.REQUIRED)  // Default
    public void createOrder() {
        // Join existing transaction or create new one
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAudit() {
        // Always create new transaction (independent)
        // Useful for audit logs that should commit even if main tx fails
    }
    
    @Transactional(propagation = Propagation.MANDATORY)
    public void updateInventory() {
        // Must be called within existing transaction
        // Throws exception if no transaction exists
    }
    
    @Transactional(propagation = Propagation.NEVER)
    public void sendEmail() {
        // Must NOT be called within transaction
    }
    
    // 2. Isolation Level
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void processOrder() {
        // Prevents dirty reads
    }
    
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void generateReport() {
        // Prevents dirty reads and non-repeatable reads
    }
    
    // 3. Rollback Rules
    @Transactional(rollbackFor = Exception.class)
    public void createUser() {
        // Rollback for checked exceptions too
    }
    
    @Transactional(noRollbackFor = NotFoundException.class)
    public void updateUser() {
        // Don't rollback for NotFoundException
    }
    
    // 4. Timeout
    @Transactional(timeout = 30)  // 30 seconds
    public void longRunningOperation() {
        // Transaction times out after 30 seconds
    }
    
    // 5. Read-only
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        // Optimization for read-only operations
        return userRepository.findAll();
    }
}
```

### Propagation Types

| Propagation | Description | Use Case |
|-------------|-------------|----------|
| **REQUIRED** (default) | Join existing or create new | Most common |
| **REQUIRES_NEW** | Always create new transaction | Audit logging |
| **MANDATORY** | Must have existing transaction | Called by transactional method |
| **SUPPORTS** | Join if exists, non-tx otherwise | Read operations |
| **NOT_SUPPORTED** | Suspend current transaction | File operations |
| **NEVER** | Throw exception if tx exists | Non-transactional operations |
| **NESTED** | Nested within current | Savepoints |

### Propagation Example

```java
@Service
public class OrderService {
    
    @Autowired
    private AuditService auditService;
    
    @Transactional
    public void createOrder(Order order) {
        // Transaction 1 starts
        orderRepository.save(order);
        
        // Call audit service
        auditService.logOrderCreation(order);
        
        // Transaction 1 commits
    }
}

@Service
public class AuditService {
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOrderCreation(Order order) {
        // Transaction 2 starts (independent)
        AuditLog log = new AuditLog("Order created: " + order.getId());
        auditRepository.save(log);
        // Transaction 2 commits
        
        // Even if order creation fails and rolls back,
        // audit log is already committed!
    }
}
```

### Isolation Levels

```java
// Dirty Read Problem
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public void problem() {
    // Transaction A updates balance to 500 (not committed)
    // Transaction B reads balance as 500 (dirty read!)
    // Transaction A rolls back
    // Transaction B used wrong data!
}

// Solution
@Transactional(isolation = Isolation.READ_COMMITTED)
public void solution() {
    // Only reads committed data
}

// Non-Repeatable Read Problem
@Transactional(isolation = Isolation.READ_COMMITTED)
public void problem() {
    // Read balance: 1000
    // Another tx updates to 500 and commits
    // Read balance again: 500 (different!)
}

// Solution
@Transactional(isolation = Isolation.REPEATABLE_READ)
public void solution() {
    // Balance stays 1000 throughout transaction
}
```

### Common Pitfalls

```java
// 1. ❌ Calling @Transactional method from same class
@Service
public class UserService {
    
    public void registerUser(User user) {
        saveUser(user);  // ❌ Transaction not applied!
        // Direct method call, no proxy involved
    }
    
    @Transactional
    public void saveUser(User user) {
        userRepository.save(user);
    }
}

// ✅ Solution: Call from another bean or self-inject
@Service
public class UserService {
    
    @Autowired
    private UserService self;  // Self-inject
    
    public void registerUser(User user) {
        self.saveUser(user);  // ✅ Goes through proxy
    }
    
    @Transactional
    public void saveUser(User user) {
        userRepository.save(user);
    }
}

// 2. ❌ @Transactional on private method
@Service
public class UserService {
    
    @Transactional  // ❌ Doesn't work on private!
    private void saveUser(User user) {
        userRepository.save(user);
    }
}

// ✅ Must be public
@Transactional
public void saveUser(User user) {
    userRepository.save(user);
}

// 3. ❌ Catching exceptions without rethrowing
@Service
public class UserService {
    
    @Transactional
    public void createUser(User user) {
        try {
            userRepository.save(user);
            throw new RuntimeException("Error!");
        } catch (Exception e) {
            // ❌ Exception swallowed, no rollback!
            log.error("Error", e);
        }
    }
}

// ✅ Rethrow or mark for rollback
@Transactional
public void createUser(User user) {
    try {
        userRepository.save(user);
    } catch (Exception e) {
        TransactionAspectSupport.currentTransactionStatus()
            .setRollbackOnly();
        throw e;
    }
}
```

### Programmatic Transactions

```java
@Service
public class UserService {
    
    @Autowired
    private TransactionTemplate transactionTemplate;
    
    public User createUser(User user) {
        return transactionTemplate.execute(status -> {
            try {
                User saved = userRepository.save(user);
                emailService.sendWelcome(saved.getEmail());
                return saved;
            } catch (Exception e) {
                status.setRollbackOnly();
                throw e;
            }
        });
    }
}
```

**Interview Tip:** Explain @Transactional provides declarative transaction management - automatically begins, commits, or rolls back. Mention default is REQUIRED propagation and READ_COMMITTED isolation. Common pitfall: calling @Transactional method from same class doesn't work (no proxy). Example: "For money transfer, I use @Transactional so both debit and credit happen atomically - if one fails, both rollback."

---

## Q17: Spring Boot Actuator {#q17-actuator}

### What is Actuator?

Production-ready features for monitoring and managing Spring Boot applications.

### Adding Actuator

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### Built-in Endpoints

```properties
# Expose all endpoints (development only!)
management.endpoints.web.exposure.include=*

# Production - expose only specific endpoints
management.endpoints.web.exposure.include=health,info,metrics

# Change base path (default: /actuator)
management.endpoints.web.base-path=/manage

# Change port
management.server.port=9090
```

### Common Endpoints

#### 1. Health Endpoint

```
GET /actuator/health

{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500GB,
        "free": 250GB
      }
    }
  }
}
```

Custom health indicator:

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        // Check external service
        boolean serviceUp = checkExternalService();
        
        if (serviceUp) {
            return Health.up()
                .withDetail("service", "Available")
                .build();
        } else {
            return Health.down()
                .withDetail("service", "Unavailable")
                .withDetail("error", "Connection timeout")
                .build();
        }
    }
    
    private boolean checkExternalService() {
        try {
            // Check external API
            restTemplate.getForObject("http://external-api/health", String.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

#### 2. Metrics Endpoint

```
GET /actuator/metrics

{
  "names": [
    "jvm.memory.used",
    "jvm.gc.pause",
    "http.server.requests",
    "system.cpu.usage",
    "process.uptime"
  ]
}

GET /actuator/metrics/http.server.requests

{
  "name": "http.server.requests",
  "measurements": [
    { "statistic": "COUNT", "value": 1523 },
    { "statistic": "TOTAL_TIME", "value": 45.2 }
  ],
  "availableTags": [
    { "tag": "uri", "values": ["/api/users", "/api/orders"] },
    { "tag": "status", "values": ["200", "404", "500"] }
  ]
}
```

Custom metrics:

```java
@Service
public class UserService {
    
    private final MeterRegistry meterRegistry;
    private final Counter userCreationCounter;
    private final Timer userCreationTimer;
    
    public UserService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Counter
        this.userCreationCounter = Counter.builder("users.created")
            .description("Number of users created")
            .register(meterRegistry);
        
        // Timer
        this.userCreationTimer = Timer.builder("users.creation.time")
            .description("Time to create user")
            .register(meterRegistry);
    }
    
    public User createUser(User user) {
        return userCreationTimer.record(() -> {
            User saved = userRepository.save(user);
            userCreationCounter.increment();
            return saved;
        });
    }
    
    // Gauge (for current values)
    @PostConstruct
    public void init() {
        Gauge.builder("users.active", userRepository, UserRepository::countActiveUsers)
            .description("Number of active users")
            .register(meterRegistry);
    }
}
```

#### 3. Info Endpoint

```
GET /actuator/info

{
  "app": {
    "name": "User Service",
    "version": "1.0.0",
    "description": "User management service"
  },
  "build": {
    "artifact": "user-service",
    "version": "1.0.0-SNAPSHOT",
    "time": "2025-01-15T10:30:00Z"
  }
}
```

Configuration:

```properties
# application.properties
info.app.name=User Service
info.app.version=1.0.0
info.app.description=User management service
```

Or via code:

```java
@Component
public class AppInfoContributor implements InfoContributor {
    
    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("custom", Map.of(
            "feature", "enabled",
            "database", "MySQL",
            "cache", "Redis"
        ));
    }
}
```

#### 4. Env Endpoint

```
GET /actuator/env

Shows all configuration properties:
- application.properties
- Environment variables
- System properties
- Command line arguments
```

#### 5. Beans Endpoint

```
GET /actuator/beans

Lists all Spring beans in the application context
```

#### 6. Loggers Endpoint

```
GET /actuator/loggers

Shows all loggers and their levels

GET /actuator/loggers/com.example.service

POST /actuator/loggers/com.example.service
{
  "configuredLevel": "DEBUG"
}

// Changes log level at runtime!
```

#### 7. Thread Dump

```
GET /actuator/threaddump

Shows thread dump for troubleshooting
```

#### 8. Heap Dump

```
GET /actuator/heapdump

Downloads heap dump for memory analysis
```

### Security

```java
@Configuration
public class ActuatorSecurityConfig {
    
    @Bean
    public SecurityFilterChain actuatorSecurity(HttpSecurity http) throws Exception {
        return http
            .requestMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(EndpointRequest.to("health", "info"))
                    .permitAll()
                .anyRequest()
                    .hasRole("ADMIN")
            )
            .httpBasic(Customizer.withDefaults())
            .build();
    }
}
```

### Integration with Monitoring Tools

#### Prometheus

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

```properties
management.endpoints.web.exposure.include=prometheus
management.metrics.export.prometheus.enabled=true
```

```
GET /actuator/prometheus

# Prometheus format metrics
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap"} 123456
```

#### Grafana Dashboard

Connect to Prometheus data source and visualize:
- JVM memory usage
- HTTP request rates
- Database connection pools
- Custom application metrics

### Custom Endpoints

```java
@Component
@Endpoint(id = "custom")
public class CustomEndpoint {
    
    @ReadOperation
    public Map<String, Object> customInfo() {
        return Map.of(
            "status", "OK",
            "customMetric", calculateMetric()
        );
    }
    
    @WriteOperation
    public void updateConfig(@Selector String key, String value) {
        // Update configuration
    }
}

// Access: GET /actuator/custom
```

### Production Best Practices

```properties
# 1. Secure endpoints
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized

# 2. Use different port
management.server.port=9090
management.server.address=127.0.0.1

# 3. Change base path
management.endpoints.web.base-path=/manage

# 4. Enable HTTPS
management.server.ssl.enabled=true

# 5. Add authentication
# Via SecurityFilterChain (shown above)
```

**Interview Tip:** Explain Actuator provides production-ready features for monitoring. Common endpoints: health (liveness/readiness), metrics (JVM, HTTP), info (app details). In production, expose only necessary endpoints and secure with authentication. Example: "I use /health for Kubernetes liveness probes, /metrics for Prometheus monitoring, and custom metrics to track business KPIs like order creation rate."

---

## Summary: Interview Talking Points

### Q9: Core Features
"Spring's core is DI/IoC for loose coupling, plus AOP for cross-cutting concerns, MVC for web, Data for persistence, Security for authentication, and Boot for rapid development."

### Q10: DI & IoC
"IoC inverts control - Spring creates dependencies, not my code. I prefer constructor injection for immutability and testability. Makes swapping implementations easy."

### Q11: Starters
"Starters are one-stop dependencies. Auto-configuration uses @Conditional annotations to configure based on classpath. Just add starter and configure properties."

### Q12: Stereotypes
"All are @Component specializations. @Service for business logic, @Repository for data access with exception translation, @RestController for REST APIs."

### Q13: Lifecycle
"Bean instantiated, dependencies injected, @PostConstruct called, ready to use, @PreDestroy on shutdown. Singleton is default and stateless. Prototype creates new each time."

### Q14: AOP
"AOP separates cross-cutting concerns. I use it for logging execution time, auditing, caching, retry logic. @Around is most powerful - wraps method execution."

### Q15: Exceptions
"@ControllerAdvice provides global exception handling. Custom exceptions for different scenarios. Return meaningful errors with proper HTTP status codes."

### Q16: @Transactional
"Declarative transaction management. Automatically begins, commits, or rolls back. Default is REQUIRED propagation. Pitfall: calling from same class doesn't work."

### Q17: Actuator
"Production-ready features. /health for liveness, /metrics for monitoring, /info for app details. Integrate with Prometheus/Grafana. Secure in production."

---

**Best of luck with your LTIMindtree interview! 🚀**
