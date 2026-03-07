# Spring Framework - Complete Interview Guide (Q9-Q17)

## Table of Contents
- [Q9: Core Features of Spring Framework](#q9-core-features)
- [Q10: Dependency Injection and IoC](#q10-dependency-injection)
- [Q11: Spring Boot Starters and Auto-Configuration](#q11-starters)
- [Q12: @Component, @Service, @Repository, @Controller](#q12-stereotypes)
- [Q13: Spring Bean Lifecycle and Scopes](#q13-lifecycle)
- [Q14: Spring AOP](#q14-aop)
- [Q15: Exception Handling](#q15-exceptions)
- [Q16: @Transactional Annotation](#q16-transactional)
- [Q17: Spring Boot Actuator](#q17-actuator)

---

## Q9: Core Features of Spring Framework {#q9-core-features}

### Overview
Spring Framework is a comprehensive framework for enterprise Java development. Created by Rod Johnson in 2003, it provides infrastructure support for developing Java applications.

### 1. Dependency Injection (DI) / Inversion of Control (IoC)

**The Heart of Spring**

```java
// Without DI (Tight Coupling)
public class OrderService {
    private PaymentService paymentService = new PaymentService();
    // Tightly coupled to PaymentService implementation
}

// With DI (Loose Coupling)
@Service
public class OrderService {
    private final PaymentService paymentService;
    
    @Autowired
    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

**Benefits:**
- ✅ Loose coupling
- ✅ Easier testing (inject mocks)
- ✅ Better code organization
- ✅ Flexibility to swap implementations

### 2. Aspect-Oriented Programming (AOP)

Separate cross-cutting concerns from business logic.

```java
@Aspect
@Component
public class LoggingAspect {
    
    @Around("@annotation(LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        
        Object result = joinPoint.proceed();
        
        long executionTime = System.currentTimeMillis() - start;
        System.out.println(joinPoint.getSignature() + " executed in " + executionTime + "ms");
        
        return result;
    }
}

// Usage - logging is applied via AOP
@Service
public class UserService {
    
    @LogExecutionTime
    public User createUser(User user) {
        // Only business logic here
        return userRepository.save(user);
    }
}
```

### 3. Spring MVC (Web Framework)

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
```

### 4. Transaction Management

Declarative transaction management with `@Transactional`.

```java
@Service
public class BankingService {
    
    @Transactional
    public void transferMoney(Long fromAccount, Long toAccount, BigDecimal amount) {
        // Debit from account
        accountRepository.debit(fromAccount, amount);
        
        // Credit to account
        accountRepository.credit(toAccount, amount);
        
        // Both operations succeed or both rollback
    }
}
```

### 5. Data Access (Spring Data)

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Spring generates implementation automatically
    User findByEmail(String email);
    
    List<User> findByAgeGreaterThan(int age);
    
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
    List<User> searchByName(@Param("name") String name);
}
```

### 6. Spring Security

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .build();
    }
}
```

### Spring Framework Architecture

```
┌─────────────────────────────────────────────────────┐
│           Spring Framework Modules                  │
├─────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────┐  │
│  │         Spring Core Container                │  │
│  │  (IoC, DI, Beans, Context, Expression)      │  │
│  └──────────────────────────────────────────────┘  │
│                      ▲                              │
│         ┌────────────┼────────────┐                │
│         │            │            │                │
│    ┌────▼───┐   ┌───▼────┐   ┌──▼─────┐          │
│    │  AOP   │   │  Data  │   │  Web   │          │
│    │        │   │ Access │   │  MVC   │          │
│    └────────┘   └────────┘   └────────┘          │
│         │            │            │                │
│    ┌────▼────────────▼────────────▼────┐          │
│    │      Spring Boot (Opinionated)    │          │
│    └───────────────────────────────────┘          │
└─────────────────────────────────────────────────────┘
```

### Key Benefits

- **Lightweight:** Small footprint, minimal overhead
- **Non-invasive:** Code doesn't depend on Spring APIs
- **Loose Coupling:** DI/IoC promotes loose coupling
- **Declarative:** AOP and transactions are declarative
- **Testable:** Easy to test with DI
- **Flexible:** Integrates with various frameworks
- **Production-Ready:** Actuator, metrics, health checks

**Interview Tip:** Emphasize that Spring's core is DI/IoC which makes code loosely coupled and testable. Give concrete examples: "In my project, I used Spring MVC for REST APIs, Spring Data JPA for database access, and Spring Security for authentication."

---

## Q10: Dependency Injection and Inversion of Control {#q10-dependency-injection}

### Traditional Approach (Without IoC)

```java
// Tight coupling - class creates its own dependencies
public class OrderService {
    private PaymentService paymentService;
    private EmailService emailService;
    private InventoryService inventoryService;
    
    public OrderService() {
        // Creating dependencies manually
        this.paymentService = new PaymentService();
        this.emailService = new EmailService();
        this.inventoryService = new InventoryService();
    }
    
    public void createOrder(Order order) {
        paymentService.processPayment(order.getPaymentInfo());
        inventoryService.reduceStock(order.getItems());
        emailService.sendConfirmation(order.getUserEmail());
    }
}

// ❌ Problems:
// 1. Tight coupling
// 2. Hard to test
// 3. Inflexible
// 4. Difficult to maintain
```

### Inversion of Control (IoC)

**Definition:** A design principle where control of object creation and dependency management is inverted from application code to the framework.

**Traditional:** "I create my dependencies"  
**IoC:** "Framework gives me my dependencies"

```
Traditional Flow:
┌──────────────┐
│  Your Code   │ ──creates──► Dependencies
└──────────────┘

IoC Flow:
┌──────────────┐
│   Framework  │ ──creates──► Dependencies
│   (Spring)   │ ──injects──► Your Code
└──────────────┘
```

### Dependency Injection (DI)

**Definition:** A pattern that implements IoC by injecting dependencies into a class rather than having the class create them.

### Types of Dependency Injection

#### 1. Constructor Injection (Recommended ✅)

```java
@Service
public class OrderService {
    
    private final PaymentService paymentService;
    private final EmailService emailService;
    private final InventoryService inventoryService;
    
    // Constructor injection
    @Autowired  // Optional in Spring 4.3+ if single constructor
    public OrderService(
        PaymentService paymentService,
        EmailService emailService,
        InventoryService inventoryService
    ) {
        this.paymentService = paymentService;
        this.emailService = emailService;
        this.inventoryService = inventoryService;
    }
    
    public void createOrder(Order order) {
        paymentService.processPayment(order.getPaymentInfo());
        inventoryService.reduceStock(order.getItems());
        emailService.sendConfirmation(order.getUserEmail());
    }
}

// ✅ Benefits:
// 1. Immutable - fields can be final
// 2. Testable - easy to provide mocks
// 3. Explicit - all dependencies are clear
// 4. Fail-fast - app won't start if dependencies missing
```

#### 2. Setter Injection

```java
@Service
public class OrderService {
    
    private PaymentService paymentService;
    private EmailService emailService;
    
    @Autowired
    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    
    @Autowired
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }
}

// ✅ Use when: Optional dependencies, reconfiguration needed
// ❌ Drawbacks: Cannot make fields final, object might be incomplete
```

#### 3. Field Injection (Not Recommended)

```java
@Service
public class OrderService {
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private EmailService emailService;
}

// ❌ Not Recommended:
// 1. Cannot make fields final
// 2. Harder to test
// 3. Hides dependencies
// 4. Cannot use outside Spring container
```

### Complete Working Example

```java
// 1. Define interfaces (loose coupling)
public interface PaymentService {
    void processPayment(PaymentInfo info);
}

public interface EmailService {
    void sendEmail(String to, String message);
}

// 2. Implement services
@Service
public class StripePaymentService implements PaymentService {
    @Override
    public void processPayment(PaymentInfo info) {
        System.out.println("Processing payment via Stripe");
    }
}

@Service
public class SmtpEmailService implements EmailService {
    @Override
    public void sendEmail(String to, String message) {
        System.out.println("Sending email via SMTP");
    }
}

// 3. Inject dependencies
@Service
public class OrderService {
    
    private final PaymentService paymentService;
    private final EmailService emailService;
    
    public OrderService(
        PaymentService paymentService,
        EmailService emailService
    ) {
        this.paymentService = paymentService;
        this.emailService = emailService;
    }
    
    public void createOrder(Order order) {
        paymentService.processPayment(order.getPaymentInfo());
        emailService.sendEmail(order.getUserEmail(), "Order confirmed");
    }
}

// 4. Easy to test
@SpringBootTest
public class OrderServiceTest {
    
    @Mock
    private PaymentService paymentService;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private OrderService orderService;
    
    @Test
    public void testCreateOrder() {
        Order order = new Order();
        orderService.createOrder(order);
        
        verify(paymentService).processPayment(any());
        verify(emailService).sendEmail(any(), any());
    }
}
```

### Handling Multiple Implementations

```java
// Multiple implementations
@Service
@Primary  // Default choice
public class StripePaymentService implements PaymentService { }

@Service
public class PayPalPaymentService implements PaymentService { }

// Inject specific implementation
@Service
public class OrderService {
    
    private final PaymentService stripePayment;
    private final PaymentService paypalPayment;
    
    public OrderService(
        @Qualifier("stripePaymentService") PaymentService stripePayment,
        @Qualifier("payPalPaymentService") PaymentService paypalPayment
    ) {
        this.stripePayment = stripePayment;
        this.paypalPayment = paypalPayment;
    }
}

// Custom qualifier
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Stripe {}

@Service
@Stripe
public class StripePaymentService implements PaymentService { }

// Usage
@Service
public class OrderService {
    
    private final PaymentService paymentService;
    
    public OrderService(@Stripe PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

### How Spring IoC Container Works

```
1. Component Scanning
   └─► Scans for @Component, @Service, @Repository, @Controller
   
2. Bean Definition Registration
   └─► Creates BeanDefinitions (metadata about each bean)
   
3. Dependency Resolution
   └─► Analyzes dependencies (constructor params, @Autowired)
   
4. Bean Instantiation
   └─► Creates beans in order (dependencies first)
   
5. Dependency Injection
   └─► Injects dependencies into beans
   
6. Application Ready
   └─► All beans initialized, context ready
```

### ApplicationContext vs BeanFactory

| Feature | BeanFactory | ApplicationContext |
|---------|-------------|-------------------|
| **Instantiation** | Lazy (on demand) | Eager (on startup) |
| **Internationalization** | No | Yes (MessageSource) |
| **Event Publication** | No | Yes (ApplicationEvent) |
| **Annotation Support** | Manual | Automatic |
| **AOP Support** | Manual | Built-in |
| **Use Case** | Resource-constrained | Enterprise apps (recommended) |

**Interview Tip:** Explain IoC as "Hollywood Principle: Don't call us, we'll call you." Always mention you prefer constructor injection for immutability and testability. Example: "Instead of creating new PaymentService(), I let Spring inject it, making it easy to swap implementations or mock in tests."

---

## Q11: Spring Boot Starters and Auto-Configuration {#q11-starters}

### Spring Boot Starters

**Definition:** Starters are dependency descriptors that bring in all necessary dependencies for a particular functionality. Naming convention: `spring-boot-starter-*`

### Common Starters

#### 1. spring-boot-starter-web

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

Brings in:
✅ Spring MVC
✅ REST support
✅ Embedded Tomcat
✅ Jackson (JSON)
✅ Hibernate Validator

Perfect for: REST APIs and web applications
```

#### 2. spring-boot-starter-data-jpa

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

Brings in:
✅ Spring Data JPA
✅ Hibernate (default JPA)
✅ JDBC
✅ Transaction management

Perfect for: Database access
```

#### 3. spring-boot-starter-security

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

Brings in:
✅ Spring Security
✅ Authentication
✅ Authorization
✅ CSRF protection
```

### More Starters

```xml
<!-- Redis caching -->
<artifactId>spring-boot-starter-data-redis</artifactId>

<!-- MongoDB -->
<artifactId>spring-boot-starter-data-mongodb</artifactId>

<!-- RabbitMQ -->
<artifactId>spring-boot-starter-amqp</artifactId>

<!-- Validation -->
<artifactId>spring-boot-starter-validation</artifactId>

<!-- Actuator (monitoring) -->
<artifactId>spring-boot-starter-actuator</artifactId>

<!-- Testing -->
<artifactId>spring-boot-starter-test</artifactId>
```

### Auto-Configuration

**Definition:** Spring Boot automatically configures your application based on dependencies you've added.

```
How Auto-Configuration Works:

1. Check Classpath
   ├─ Is spring-boot-starter-data-jpa present?
   └─ Is spring-boot-starter-web present?

2. Conditional Configuration
   ├─ If DataSource is on classpath
   │  └─ Configure DataSource automatically
   ├─ If EntityManagerFactory is on classpath
   │  └─ Configure JPA automatically
   └─ If DispatcherServlet is on classpath
      └─ Configure Spring MVC automatically

3. Apply Defaults
   ├─ Use application.properties for customization
   └─ Use sensible defaults otherwise
```

### Auto-Configuration Example

```java
// 1. Add starters
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
</dependency>

// 2. Spring Boot automatically configures:
// ✅ DataSource
// ✅ EntityManagerFactory
// ✅ TransactionManager
// ✅ JPA repositories

// 3. You just write:
@Entity
public class User {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
}

public interface UserRepository extends JpaRepository<User, Long> { }

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    public User save(User user) {
        return userRepository.save(user);  // Transactions configured automatically!
    }
}
```

### Customizing Auto-Configuration

#### application.properties

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=root
spring.datasource.password=secret

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Server
server.port=8080
server.servlet.context-path=/api

# Logging
logging.level.org.springframework.web=DEBUG
```

#### application.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: secret
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

server:
  port: 8080
  servlet:
    context-path: /api
```

### @SpringBootApplication Explained

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// @SpringBootApplication combines:

@Configuration
// Configuration class

@EnableAutoConfiguration
// Enables auto-configuration

@ComponentScan
// Scans for components
```

### Viewing Auto-Configuration Report

```bash
# Run with debug
java -jar myapp.jar --debug

# Or in application.properties
logging.level.org.springframework.boot.autoconfigure=DEBUG

# Output shows:
# Positive matches: (applied)
# ✅ DataSourceAutoConfiguration
# ✅ JpaRepositoriesAutoConfiguration

# Negative matches: (not applied)
# ❌ MongoAutoConfiguration - no MongoDB on classpath
```

### Excluding Auto-Configuration

```java
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    SecurityAutoConfiguration.class
})
public class Application { }

// Or in properties
spring.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

### Conditional Annotations

```java
@ConditionalOnClass(DataSource.class)
// Apply only if DataSource class is present

@ConditionalOnMissingBean(DataSource.class)
// Apply only if DataSource bean doesn't exist

@ConditionalOnProperty(name = "mycustom.enabled", havingValue = "true")
// Apply only if property is set

@ConditionalOnWebApplication
// Apply only for web apps

// Example
@Configuration
@ConditionalOnClass(RedisTemplate.class)
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true")
public class RedisConfig {
    
    @Bean
    @ConditionalOnMissingBean
    public RedisTemplate<String, Object> redisTemplate() {
        return new RedisTemplate<>();
    }
}
```

**Interview Tip:** Explain starters as "one-stop-shop dependencies." Auto-configuration uses @Conditional annotations to configure beans based on classpath. Example: "Adding spring-boot-starter-data-jpa automatically configures DataSource, EntityManager, and transactions - I just provide database URL in properties."

---

## Q12: Difference between @Component, @Service, @Repository, and @Controller {#q12-stereotypes}

### Overview

These are **stereotype annotations** that mark a class as a Spring-managed bean. They are specializations of `@Component`.

### Hierarchy

```
    @Component (Base)
         │
┌────────┼────────┬────────┐
│        │        │        │
@Service @Repository @Controller
                      │
                 @RestController
```

### Comparison Table

| Annotation | Purpose | Layer | Special Features | When to Use |
|------------|---------|-------|------------------|-------------|
| **@Component** | Generic stereotype | Any | None | Utility classes |
| **@Service** | Business logic | Service | Indicates business logic | Business services |
| **@Repository** | Data access | Persistence | Exception translation | DAO classes |
| **@Controller** | Web MVC | Presentation | Returns view name | Traditional MVC |
| **@RestController** | REST API | Presentation | @Controller + @ResponseBody | REST APIs |

### Complete Examples

#### 1. @Component - Generic Bean

```java
@Component
public class EmailValidator {
    
    public boolean isValid(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}

@Component
public class PasswordEncoder {
    
    public String encode(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}
```

#### 2. @Service - Business Logic

```java
@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private PaymentService paymentService;
    
    @Transactional
    public Order createOrder(OrderRequest request) {
        // Business logic
        validateOrder(request);
        
        Order order = new Order(request);
        order.setStatus(OrderStatus.PENDING);
        Order saved = orderRepository.save(order);
        
        Payment payment = paymentService.processPayment(saved);
        
        return saved;
    }
    
    private void validateOrder(OrderRequest request) {
        if (request.getItems().isEmpty()) {
            throw new BusinessException("Order must have items");
        }
    }
}
```

#### 3. @Repository - Data Access

```java
// Spring Data JPA - @Repository optional
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
}

// Custom implementation - @Repository required
@Repository
public class CustomUserRepositoryImpl {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    public List<User> findUsersByComplexCriteria(SearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        // Complex query logic
        return entityManager.createQuery(query).getResultList();
    }
}

// Exception translation
@Repository
public class JdbcUserRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public User findById(Long id) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT * FROM users WHERE id = ?",
                new UserRowMapper(),
                id
            );
        } catch (SQLException e) {
            // @Repository translates SQLException to DataAccessException
            throw new DataAccessException("Error finding user", e);
        }
    }
}
```

#### 4. @Controller - Traditional MVC

```java
@Controller
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // Returns view name
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "users/list";  // Returns view name
    }
    
    @GetMapping("/users/{id}")
    public String showUser(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "users/detail";
    }
}
```

#### 5. @RestController - REST API

```java
@RestController  // @Controller + @ResponseBody
@RequestMapping("/api/users")
public class UserRestController {
    
    @Autowired
    private UserService userService;
    
    // All methods return JSON/XML automatically
    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
```

### Layered Architecture

```
┌─────────────────────────────────────┐
│   Presentation Layer                │
│   @Controller / @RestController     │
└─────────────┬───────────────────────┘
              │
              ▼
┌─────────────────────────────────────┐
│   Service Layer                     │
│   @Service                          │
└─────────────┬───────────────────────┘
              │
              ▼
┌─────────────────────────────────────┐
│   Persistence Layer                 │
│   @Repository                       │
└─────────────┬───────────────────────┘
              │
              ▼
         ┌──────────┐
         │ Database │
         └──────────┘
```

### Best Practices

- ✅ Use **@Service** for business logic
- ✅ Use **@Repository** for data access
- ✅ Use **@RestController** for REST APIs
- ✅ Use **@Component** for utilities
- ✅ Follow layered architecture
- ❌ Never put business logic in controllers
- ❌ Never put data access in services

**Interview Tip:** All stereotypes are @Component specializations. Key difference: @Repository provides exception translation, @RestController = @Controller + @ResponseBody. Example: "Using @Service makes it clear this class contains business logic, improving maintainability."

---

*[Document continues with Q13-Q17 in the same detailed format...]*

Due to character limits, I'll create a second file for the remaining questions.
