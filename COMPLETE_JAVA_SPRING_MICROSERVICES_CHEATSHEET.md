# 🎯 COMPLETE JAVA, SPRING & MICROSERVICES CHEAT SHEET
## All-in-One Interview Reference Guide - 27 Topics

---

# 📚 COMPLETE TABLE OF CONTENTS

## ☕ PART A: CORE JAVA (8 Topics)
1. [Collections Framework](#j1) - List, Set, Map comparisons
2. [HashMap Internals](#j2) - Hashing, collisions, resize
3. [Streams API](#j3) - Functional operations, collectors
4. [Multithreading](#j4) - synchronized, volatile, ExecutorService
5. [JVM Architecture](#j5) - Memory areas, class loading
6. [Garbage Collection](#j6) - GC algorithms, G1GC, ZGC
7. [Exception Handling](#j7) - Checked vs unchecked, best practices
8. [String Internals](#j8) - String pool, immutability

## 🍃 PART B: SPRING FRAMEWORK (5 Topics)
9. [Dependency Injection & IoC](#s1) - Constructor vs setter injection
10. [Bean Lifecycle](#s2) - Creation, initialization, destruction
11. [Bean Scopes](#s3) - Singleton, prototype, request, session
12. [AOP](#s4) - Aspects, pointcuts, advice types
13. [Spring MVC](#s5) - DispatcherServlet, request flow

## 🚀 PART C: SPRING BOOT (5 Topics)
14. [Auto-Configuration](#sb1) - How it works, @Conditional
15. [Starters](#sb2) - Common starters, dependencies
16. [Profiles & Properties](#sb3) - Environment-specific config
17. [Actuator](#sb4) - Monitoring endpoints, health checks
18. [Exception Handling](#sb5) - @ControllerAdvice, @ExceptionHandler

## 🔧 PART D: MICROSERVICES (6 Topics)
19. [Architecture Patterns](#m1) - Monolith vs microservices
20. [API Gateway](#m2) - Routing, load balancing, security
21. [Service Discovery](#m3) - Eureka, Consul
22. [Circuit Breaker](#m4) - Resilience4j, fallback
23. [Distributed Tracing](#m5) - Sleuth, Zipkin
24. [Event-Driven](#m6) - Kafka, RabbitMQ, async communication

## 🌐 PART E: REST API (5 Topics)
25. [REST Principles](#r1) - Stateless, resource-based
26. [HTTP Methods](#r2) - GET, POST, PUT, DELETE, PATCH
27. [Status Codes](#r3) - 2xx, 4xx, 5xx meanings
28. [API Versioning](#r4) - URI, header, content negotiation
29. [API Security](#r5) - JWT, OAuth2, API keys

---

# ☕ PART A: CORE JAVA

<a name="j1"></a>
## 1️⃣ Collections Framework

**⚡ 30-sec Answer:** *List (ordered, duplicates) - ArrayList O(1) get, LinkedList O(1) add at ends. Set (unique) - HashSet O(1), TreeSet O(log n) sorted. Map (key-value) - HashMap O(1), TreeMap O(log n) sorted. ConcurrentHashMap for thread-safety.*

### Key Interfaces
```
Collection
├── List (ordered, duplicates OK)
│   ├── ArrayList (array-backed, fast random access)
│   └── LinkedList (doubly-linked, fast insertion)
├── Set (no duplicates)
│   ├── HashSet (unordered, O(1))
│   └── TreeSet (sorted, O(log n))
└── Queue
    └── PriorityQueue (heap-based)

Map (key-value pairs)
├── HashMap (unordered, O(1))
├── TreeMap (sorted keys, O(log n))
└── ConcurrentHashMap (thread-safe)
```

### Quick Comparison
| Collection | Ordered | Duplicates | Null | Sorted | Speed |
|------------|---------|------------|------|--------|-------|
| ArrayList | ✅ | ✅ | ✅ | ❌ | get O(1), add O(1) |
| LinkedList | ✅ | ✅ | ✅ | ❌ | get O(n), add O(1) |
| HashSet | ❌ | ❌ | ✅ one | ❌ | O(1) |
| TreeSet | ✅ | ❌ | ❌ | ✅ | O(log n) |
| HashMap | ❌ | keys: ❌ | ✅ one key | ❌ | O(1) |

**🎓 Interview Points:** "ArrayList for random access, LinkedList for frequent insertions at ends. HashSet for fast lookup, TreeSet when need sorted. HashMap general purpose, ConcurrentHashMap for concurrency."

---

<a name="j2"></a>
## 2️⃣ HashMap Internals

**⚡ 30-sec Answer:** *HashMap uses array of buckets. hash(key) % array.length gives index. Collisions handled with linked lists, converted to trees when >8 entries (Java 8+). Load factor 0.75 triggers resize - doubles array size, rehashes all entries. put/get O(1) average, O(log n) worst case.*

### Structure
```
HashMap<K,V>
├── Node<K,V>[] table (array of buckets, initial size 16)
├── size (number of entries)
├── threshold (capacity * loadFactor = 12)
└── loadFactor (0.75)

Bucket collision handling:
0-8 entries: Linked list
8+ entries: Red-Black tree (Java 8+)
```

### Operations
```java
// Put: hash → index → insert/update
map.put("key", value);
// 1. hash = hash("key")
// 2. index = hash & (length-1)
// 3. Check bucket[index], insert or update

// Get: hash → index → search
map.get("key");
// 1. Same hash calculation
// 2. Traverse linked list/tree
// 3. Compare keys with equals()
```

**🎓 Interview Points:** "Initial capacity 16, doubles on resize. Load factor 0.75 means resize when 75% full. Trees for >8 collisions improve worst case from O(n) to O(log n). NOT thread-safe - use ConcurrentHashMap."

---

<a name="j3"></a>
## 3️⃣ Streams API

**⚡ 30-sec Answer:** *Stream pipeline: source → intermediate ops (lazy: filter, map, sorted) → terminal op (eager: collect, forEach). Immutable, don't modify source. Parallel streams split data across threads. Common: stream().filter().map().collect(Collectors.toList())*

### Pipeline Structure
```
Source → Intermediate → Intermediate → Terminal → Result
         (Lazy, chainable)          (Triggers execution)

list.stream()
    .filter(x -> x > 5)     // Intermediate
    .map(x -> x * 2)        // Intermediate  
    .collect(Collectors.toList())  // Terminal
```

### Common Operations
```java
// Filter
list.stream().filter(x -> x % 2 == 0)  // Even numbers

// Map (transform)
list.stream().map(String::toUpperCase)

// Reduce (aggregate)
list.stream().reduce(0, (a, b) -> a + b)  // Sum

// Collect
list.stream().collect(Collectors.toList())
list.stream().collect(Collectors.toSet())
list.stream().collect(Collectors.groupingBy(Person::getAge))

// Parallel
list.parallelStream().map(x -> heavy(x))  // Use ForkJoinPool
```

**🎓 Interview Points:** "Intermediate operations lazy - not executed until terminal. Streams single-use. Parallel good for large data + CPU-bound tasks. Avoid for small data or I/O operations."

---

<a name="j4"></a>
## 4️⃣ Multithreading & Concurrency

**⚡ 30-sec Answer:** *Create threads via Thread/Runnable/ExecutorService. synchronized for mutual exclusion - one thread enters. volatile ensures visibility. Race condition: concurrent modification without sync. Deadlock: circular wait. Solutions: synchronized, ReentrantLock, ConcurrentHashMap, AtomicInteger, CompletableFuture for async.*

### Thread Creation
```java
// Runnable (preferred)
new Thread(() -> System.out.println("Running")).start();

// ExecutorService (best for production)
ExecutorService executor = Executors.newFixedThreadPool(10);
executor.submit(() -> doWork());
executor.shutdown();
```

### Synchronization
```java
// synchronized method
public synchronized void increment() {
    count++;  // Thread-safe
}

// synchronized block
synchronized(this) {
    count++;
}

// volatile (visibility only)
private volatile boolean flag = false;  // Changes visible to all threads

// Atomic (lock-free)
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet();  // Thread-safe
```

### Common Problems
```java
// Race Condition
count++;  // NOT atomic: read, increment, write

// Deadlock
synchronized(lock1) {
    synchronized(lock2) { }  // T1 waits for lock2
}
synchronized(lock2) {
    synchronized(lock1) { }  // T2 waits for lock1
}
// Solution: Lock ordering - always lock1 then lock2
```

**🎓 Interview Points:** "synchronized provides mutual exclusion + visibility. volatile only visibility. ExecutorService manages thread pool - better than creating threads manually. CompletableFuture for async programming with thenApply/thenAccept."

---

<a name="j5"></a>
## 5️⃣ JVM Architecture

**⚡ 30-sec Answer:** *JVM: Class Loader → Runtime Data Areas (heap, stack, method area) → Execution Engine (interpreter + JIT). Heap stores objects (shared, GC'd), Stack stores method frames (per thread). Method Area stores class metadata. JIT compiles hot code to native. StackOverflowError: deep recursion, OutOfMemoryError: heap full.*

### Memory Areas
```
Heap (shared, GC'd)
├── Young Gen (new objects)
│   ├── Eden
│   └── Survivor (S0, S1)
└── Old Gen (long-lived objects)

Stack (per thread, not GC'd)
├── Local variables
├── Method calls (frames)
└── Return addresses

Method Area (shared)
├── Class metadata
├── Method bytecode
└── Static variables
```

### JVM Flags
```bash
-Xms2g          # Initial heap: 2GB
-Xmx4g          # Max heap: 4GB
-Xss1m          # Stack per thread: 1MB
-XX:+UseG1GC    # Use G1 garbage collector
```

**🎓 Interview Points:** "Heap shared across threads, GC'd. Stack per thread, holds method frames. Young Gen for new objects (frequent minor GC), Old Gen for long-lived (infrequent major GC). JIT compiles frequently-executed bytecode to native code for performance."

---

<a name="j6"></a>
## 6️⃣ Garbage Collection

**⚡ 30-sec Answer:** *GC frees unused objects. Mark-and-Sweep: mark reachable from GC roots (stack, static fields), sweep unreachable. Generational GC: Young (frequent minor GC), Old (infrequent major GC). G1GC default - predictable pauses. ZGC ultra-low latency <10ms. finalize() deprecated - use try-with-resources.*

### GC Process
```
1. Mark: Start from GC roots, mark all reachable objects
   GC Roots: Stack variables, static fields, active threads
   
2. Sweep: Delete unmarked (unreachable) objects

3. Compact: Eliminate fragmentation (optional)
```

### Collectors
```
Serial GC: Single thread, small heap
Parallel GC: Multiple threads, high throughput
G1GC: Default, predictable pauses (~200ms), large heaps
ZGC: Ultra-low latency (<10ms), huge heaps
```

### Configuration
```bash
-XX:+UseG1GC                    # G1 collector
-XX:MaxGCPauseMillis=200        # Target pause: 200ms
-XX:+UseZGC                     # ZGC collector
```

**🎓 Interview Points:** "G1GC divides heap into regions, predicts pause times. ZGC for ultra-low latency. GC roots: local variables, static fields, active threads. Minor GC fast (Young Gen), Major GC slow (Old Gen). Don't call System.gc() - let JVM decide."

---

<a name="j7"></a>
## 7️⃣ Exception Handling

**⚡ 30-sec Answer:** *Checked exceptions must be caught (IOException, SQLException). Unchecked exceptions can propagate (RuntimeException, NullPointerException). try-catch-finally for handling, try-with-resources for AutoCloseable. throw for throwing, throws for declaring. Custom exceptions extend Exception (checked) or RuntimeException (unchecked).*

### Exception Hierarchy
```
Throwable
├── Error (OutOfMemoryError, StackOverflowError - don't catch)
└── Exception
    ├── IOException (checked - must handle)
    ├── SQLException (checked)
    └── RuntimeException (unchecked - optional)
        ├── NullPointerException
        ├── IllegalArgumentException
        └── ArrayIndexOutOfBoundsException
```

### Handling
```java
// try-catch-finally
try {
    riskyOperation();
} catch (IOException e) {
    log.error("IO error", e);
} catch (Exception e) {
    log.error("General error", e);
} finally {
    cleanup();  // Always executes
}

// try-with-resources (AutoCloseable)
try (FileInputStream fis = new FileInputStream("file.txt")) {
    // Use resource
}  // Auto-closed, no finally needed

// Multi-catch (Java 7+)
try {
    operation();
} catch (IOException | SQLException e) {
    handle(e);
}
```

### Custom Exceptions
```java
// Checked exception
public class BusinessException extends Exception {
    public BusinessException(String message) {
        super(message);
    }
}

// Unchecked exception
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
```

**🎓 Interview Points:** "Checked: compiler enforces handling (IOException). Unchecked: runtime failures (NullPointerException). Use try-with-resources for AutoCloseable. Don't catch Error. Create custom exceptions extending Exception (checked) or RuntimeException (unchecked)."

---

<a name="j8"></a>
## 8️⃣ String, StringBuffer, StringBuilder

**⚡ 30-sec Answer:** *String immutable - creates new object on modification. String pool stores literals for reuse. StringBuffer thread-safe (synchronized), StringBuilder not thread-safe but faster. Use String for immutable, StringBuilder for concatenation in loops, StringBuffer for thread-safe operations.*

### Comparison
| Feature | String | StringBuffer | StringBuilder |
|---------|--------|--------------|---------------|
| **Mutability** | Immutable | Mutable | Mutable |
| **Thread-safe** | ✅ | ✅ synchronized | ❌ |
| **Performance** | Slow (new objects) | Slower (locking) | ✅ Fastest |
| **Use case** | Immutable data | Thread-safe concat | Single-thread concat |

### String Pool
```java
String s1 = "Hello";           // In string pool
String s2 = "Hello";           // Reuses from pool
s1 == s2;                      // true (same reference)

String s3 = new String("Hello");  // In heap (not pool)
s1 == s3;                      // false (different reference)
s1.equals(s3);                 // true (same content)

s3.intern();                   // Adds to pool
```

### When to Use
```java
// ✅ String: Immutable data
String name = "John";

// ❌ String: Loop concatenation (creates many objects)
String result = "";
for (int i = 0; i < 1000; i++) {
    result += i;  // Creates 1000 String objects!
}

// ✅ StringBuilder: Loop concatenation
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) {
    sb.append(i);  // Modifies same object
}
String result = sb.toString();

// ✅ StringBuffer: Thread-safe concatenation
StringBuffer buffer = new StringBuffer();
// Multiple threads can safely append
```

**🎓 Interview Points:** "String immutable - thread-safe, can be pooled. StringBuilder mutable, fast, NOT thread-safe - use for concatenation. StringBuffer synchronized - slower but thread-safe. String pool saves memory by reusing literals."

---

# 🍃 PART B: SPRING FRAMEWORK

<a name="s1"></a>
## 9️⃣ Dependency Injection & IoC

**⚡ 30-sec Answer:** *IoC: Spring manages object creation and dependencies. DI: Dependencies injected by framework, not created manually. Constructor injection (immutable, required), setter injection (optional, mutable), field injection (not recommended). @Autowired for injection, @Component for auto-detection, @Configuration for Java config.*

### Types of DI
```java
// Constructor Injection (Recommended)
@Service
public class UserService {
    private final UserRepository repo;
    
    @Autowired  // Optional in single constructor
    public UserService(UserRepository repo) {
        this.repo = repo;  // Immutable, testable
    }
}

// Setter Injection (Optional dependencies)
@Service
public class EmailService {
    private TemplateEngine template;
    
    @Autowired(required = false)
    public void setTemplate(TemplateEngine template) {
        this.template = template;
    }
}

// Field Injection (Not recommended - hard to test)
@Service
public class OrderService {
    @Autowired
    private OrderRepository repo;  // Can't be final, hard to mock
}
```

### Configuration
```java
// Annotation-based
@Configuration
@ComponentScan("com.example")
public class AppConfig {
    
    @Bean
    public DataSource dataSource() {
        return new HikariDataSource();
    }
}

// Component scanning
@Component
@Service
@Repository
@Controller
```

**🎓 Interview Points:** "IoC: Spring manages lifecycle, DI: injects dependencies. Constructor injection for required immutable dependencies. Setter for optional. Field injection discouraged - hard to test. @Autowired by type, @Qualifier by name."

---

<a name="s2"></a>
## 🔟 Bean Lifecycle

**⚡ 30-sec Answer:** *Lifecycle: Instantiate → Populate properties → setBeanName → BeanPostProcessor.before → @PostConstruct/InitializingBean → BeanPostProcessor.after → Bean ready → @PreDestroy/DisposableBean → Destroy. Use @PostConstruct for initialization, @PreDestroy for cleanup.*

### Lifecycle Phases
```
1. Instantiation (Constructor called)
2. Populate properties (@Autowired)
3. BeanNameAware.setBeanName()
4. BeanFactoryAware.setBeanFactory()
5. ApplicationContextAware.setApplicationContext()
6. BeanPostProcessor.postProcessBeforeInitialization()
7. @PostConstruct / InitializingBean.afterPropertiesSet()
8. Custom init-method
9. BeanPostProcessor.postProcessAfterInitialization()
10. Bean ready for use
11. @PreDestroy / DisposableBean.destroy()
12. Custom destroy-method
```

### Lifecycle Hooks
```java
@Component
public class MyBean {
    
    @PostConstruct
    public void init() {
        // Initialization logic (after dependencies injected)
    }
    
    @PreDestroy
    public void cleanup() {
        // Cleanup logic (before bean destroyed)
    }
}

// Or implement interfaces
public class MyBean implements InitializingBean, DisposableBean {
    
    @Override
    public void afterPropertiesSet() {
        // Initialization
    }
    
    @Override
    public void destroy() {
        // Cleanup
    }
}

// Or XML/Java config
@Bean(initMethod = "init", destroyMethod = "cleanup")
public MyBean myBean() {
    return new MyBean();
}
```

**🎓 Interview Points:** "@PostConstruct runs after dependencies injected - use for initialization. @PreDestroy for cleanup before destruction. BeanPostProcessor can modify all beans before/after initialization - used for AOP, proxies."

---

<a name="s3"></a>
## 1️⃣1️⃣ Bean Scopes

**⚡ 30-sec Answer:** *Singleton (default, one per container), Prototype (new per request), Request (per HTTP request), Session (per HTTP session), Application (per ServletContext). Singleton shared across app, prototype creates new each time. Use singleton for stateless, prototype for stateful.*

### Scopes
```java
// Singleton (default) - One instance per Spring container
@Component
@Scope("singleton")  // Default, can omit
public class SingletonBean {
    // Shared across entire application
}

// Prototype - New instance each time
@Component
@Scope("prototype")
public class PrototypeBean {
    // New instance on each getBean() or @Autowired
}

// Request - One per HTTP request
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestBean {
    // New instance per HTTP request
}

// Session - One per HTTP session
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionBean {
    // Same instance throughout user session
}
```

### Comparison
| Scope | Instances | Lifecycle | Use Case |
|-------|-----------|-----------|----------|
| **singleton** | 1 per container | App startup → shutdown | Stateless services, DAOs |
| **prototype** | New each request | On demand | Stateful objects, different config |
| **request** | 1 per HTTP request | Request start → end | Request-specific data |
| **session** | 1 per HTTP session | Session create → timeout | User session data |

### Prototype Injection Issue
```java
// Problem: Singleton holds Prototype
@Component
public class SingletonBean {
    @Autowired
    private PrototypeBean proto;  // Same instance always!
}

// Solution 1: Method injection
@Component
public class SingletonBean {
    @Lookup
    public PrototypeBean getPrototype() {
        return null;  // Spring overrides this
    }
}

// Solution 2: ObjectFactory
@Autowired
private ObjectFactory<PrototypeBean> prototypeFactory;

public void doSomething() {
    PrototypeBean proto = prototypeFactory.getObject();  // New instance
}
```

**🎓 Interview Points:** "Singleton default - one per container, shared. Prototype new each time. Request/Session for web apps. Singleton for stateless beans, prototype for stateful. Use @Lookup or ObjectFactory to inject prototype into singleton."

---

<a name="s4"></a>
## 1️⃣2️⃣ AOP (Aspect-Oriented Programming)

**⚡ 30-sec Answer:** *AOP separates cross-cutting concerns (logging, security, transactions) from business logic. Aspect contains advice (what to do), pointcut (where). Advice types: @Before, @After, @AfterReturning, @AfterThrowing, @Around. Join point is method execution. @Transactional uses AOP - Spring creates proxy, wraps method with transaction logic.*

### Key Concepts
```
Aspect = Advice + Pointcut
Advice = What to do (logging, security)
Pointcut = Where to do it (which methods)
Join Point = Method execution point
```

### Example
```java
@Aspect
@Component
public class LoggingAspect {
    
    // Before advice - runs before method
    @Before("execution(* com.example.service.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("Calling: " + joinPoint.getSignature());
    }
    
    // After returning - runs after successful return
    @AfterReturning(pointcut = "execution(* com.example.service.*.*(..))", 
                    returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        System.out.println("Returned: " + result);
    }
    
    // After throwing - runs after exception
    @AfterThrowing(pointcut = "execution(* com.example.service.*.*(..))", 
                   throwing = "error")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable error) {
        System.out.println("Exception: " + error);
    }
    
    // Around - wraps method execution
    @Around("execution(* com.example.service.*.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();  // Call actual method
        long time = System.currentTimeMillis() - start;
        System.out.println("Execution time: " + time + "ms");
        return result;
    }
}
```

### Pointcut Expressions
```java
// All methods in service package
@Before("execution(* com.example.service.*.*(..))")

// Specific method
@Before("execution(public void com.example.UserService.save(..))")

// Methods with @Transactional
@Before("@annotation(org.springframework.transaction.annotation.Transactional)")

// Reusable pointcut
@Pointcut("execution(* com.example.service.*.*(..))")
public void serviceMethods() {}

@Before("serviceMethods()")
public void log() { }
```

**🎓 Interview Points:** "@Transactional uses AOP - creates proxy wrapping method with begin/commit/rollback. @Before runs before method, @Around wraps (can prevent execution). Pointcut defines which methods. Spring AOP uses dynamic proxies (interface) or CGLIB (class)."

---

<a name="s5"></a>
## 1️⃣3️⃣ Spring MVC Architecture

**⚡ 30-sec Answer:** *DispatcherServlet is front controller. Flow: Request → DispatcherServlet → HandlerMapping (finds controller) → Controller → ViewResolver → View → Response. @Controller for web, @RestController for REST. @RequestMapping maps URLs. ModelAndView returns view name + data, @ResponseBody returns JSON/XML.*

### Request Flow
```
1. Client sends request to DispatcherServlet
2. DispatcherServlet asks HandlerMapping: "Which controller?"
3. HandlerMapping returns Controller method
4. DispatcherServlet calls Controller
5. Controller processes, returns ModelAndView
6. DispatcherServlet asks ViewResolver: "Which view?"
7. ViewResolver returns View (JSP, Thymeleaf)
8. View renders with Model data
9. DispatcherServlet sends response to client
```

### Components
```java
// Controller
@Controller
public class UserController {
    
    @GetMapping("/users/{id}")
    public String getUser(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "userView";  // View name
    }
}

// REST Controller (no ViewResolver)
@RestController
@RequestMapping("/api/users")
public class UserRestController {
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);  // Returns JSON
    }
    
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }
}

// Exception Handling
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponse(ex.getMessage()));
    }
}
```

**🎓 Interview Points:** "DispatcherServlet front controller - routes all requests. HandlerMapping finds controller method. @Controller returns view name, @RestController returns data (JSON). @RequestBody deserializes JSON to object, @ResponseBody serializes object to JSON."

---

# 🚀 PART C: SPRING BOOT

<a name="sb1"></a>
## 1️⃣4️⃣ Auto-Configuration

**⚡ 30-sec Answer:** *Auto-configuration automatically configures beans based on classpath. @EnableAutoConfiguration scans META-INF/spring.factories. @Conditional annotations control when configs apply. Example: spring-boot-starter-data-jpa on classpath → auto-configures DataSource, EntityManager. Disable with exclude or properties.*

### How It Works
```java
@SpringBootApplication  // Contains @EnableAutoConfiguration
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// Spring Boot scans META-INF/spring.factories
// Finds auto-configuration classes like:
// - DataSourceAutoConfiguration (if H2/MySQL on classpath)
// - JpaRepositoriesAutoConfiguration (if Spring Data JPA)
// - WebMvcAutoConfiguration (if Spring MVC)
```

### Conditional Beans
```java
@Configuration
@ConditionalOnClass(DataSource.class)  // Only if DataSource class exists
@ConditionalOnMissingBean(DataSource.class)  // Only if no DataSource bean
public class MyDataSourceConfig {
    
    @Bean
    public DataSource dataSource() {
        return new HikariDataSource();
    }
}

// Common conditionals:
@ConditionalOnProperty(name = "feature.enabled", havingValue = "true")
@ConditionalOnWebApplication  // Only for web apps
@ConditionalOnBean(EntityManagerFactory.class)
```

### Disable Auto-Config
```java
// Exclude specific configs
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})

// Or in properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

**🎓 Interview Points:** "Auto-configuration configures beans based on classpath. @ConditionalOnClass checks if class exists. @ConditionalOnMissingBean only creates if user didn't define. META-INF/spring.factories lists all auto-configs. Disable with exclude or properties."

---

<a name="sb2"></a>
## 1️⃣5️⃣ Spring Boot Starters

**⚡ 30-sec Answer:** *Starters are dependency descriptors bundling related libraries. spring-boot-starter-web includes Spring MVC, Tomcat, Jackson. spring-boot-starter-data-jpa includes Hibernate, JDBC. Starter-parent provides dependency management - versions pre-configured. Custom starters for company libraries.*

### Common Starters
```xml
<!-- Web applications (REST APIs, MVC) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<!-- Includes: Spring MVC, Tomcat, Jackson, Validation -->

<!-- JPA with Hibernate -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<!-- Includes: Hibernate, Spring Data JPA, JDBC -->

<!-- Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<!-- Includes: JUnit, Mockito, AssertJ, Hamcrest -->
```

### Starter Parent
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>

<!-- Benefits:
- Dependency versions managed (no need to specify)
- Plugin configurations
- Java version defaults
- Encoding defaults
-->
```

**🎓 Interview Points:** "Starters bundle related dependencies. spring-boot-starter-web for REST/MVC apps includes Spring MVC, embedded Tomcat, Jackson. Starter-parent manages versions - don't need to specify. View dependencies: mvn dependency:tree"

---

<a name="sb3"></a>
## 1️⃣6️⃣ Profiles & Properties

**⚡ 30-sec Answer:** *Profiles for environment-specific config. application.properties (default), application-dev.properties (dev profile), application-prod.properties (prod). Activate with spring.profiles.active=dev. @Value injects properties, @ConfigurationProperties for type-safe binding. Property sources: files, environment variables, command-line.*

### Profiles
```properties
# application.properties (default/common)
app.name=MyApp

# application-dev.properties
spring.datasource.url=jdbc:h2:mem:testdb
logging.level.root=DEBUG

# application-prod.properties
spring.datasource.url=jdbc:mysql://prod-server/db
logging.level.root=WARN
```

### Activation
```bash
# Command line
java -jar app.jar --spring.profiles.active=prod

# Environment variable
export SPRING_PROFILES_ACTIVE=prod

# application.properties
spring.profiles.active=dev

# Programmatically
SpringApplication app = new SpringApplication(Application.class);
app.setAdditionalProfiles("dev");
```

### Property Injection
```java
// @Value for simple properties
@Component
public class MyComponent {
    
    @Value("${app.name}")
    private String appName;
    
    @Value("${app.timeout:30}")  // Default 30
    private int timeout;
}

// @ConfigurationProperties for complex (type-safe)
@ConfigurationProperties(prefix = "app")
@Component
public class AppProperties {
    private String name;
    private int timeout;
    private Database database;
    
    public static class Database {
        private String url;
        private String username;
        // getters/setters
    }
    // getters/setters
}
```

### Property Sources Priority (high to low)
```
1. Command-line arguments
2. SPRING_APPLICATION_JSON
3. ServletConfig init parameters
4. ServletContext init parameters
5. JNDI attributes
6. Java System properties
7. OS environment variables
8. application-{profile}.properties outside jar
9. application-{profile}.properties inside jar
10. application.properties outside jar
11. application.properties inside jar
12. @PropertySource
13. Default properties
```

**🎓 Interview Points:** "Profiles for environment config - dev, prod. application-{profile}.properties for profile-specific. Activate with spring.profiles.active. @Value for simple injection, @ConfigurationProperties for type-safe complex properties. Command-line overrides file properties."

---

<a name="sb4"></a>
## 1️⃣7️⃣ Spring Boot Actuator

**⚡ 30-sec Answer:** *Actuator provides production-ready features - monitoring, metrics, health checks. Endpoints: /actuator/health (app health), /actuator/metrics (JVM, HTTP metrics), /actuator/info (app info). Configure exposure in properties. Custom health indicators with HealthIndicator. Secure with Spring Security.*

### Common Endpoints
```
/actuator/health       - Health status
/actuator/metrics      - Metrics (memory, CPU, HTTP)
/actuator/info         - Application info
/actuator/env          - Environment properties
/actuator/loggers      - Logger levels (can modify runtime)
/actuator/threaddump   - Thread dump
/actuator/heapdump     - Heap dump
/actuator/beans        - All Spring beans
/actuator/mappings     - All @RequestMapping URLs
```

### Configuration
```properties
# Enable endpoints
management.endpoints.web.exposure.include=health,metrics,info
management.endpoints.web.exposure.exclude=env,beans

# Health details
management.endpoint.health.show-details=always

# Custom info
info.app.name=MyApp
info.app.version=1.0.0
```

### Custom Health Indicator
```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        boolean databaseUp = checkDatabase();
        
        if (databaseUp) {
            return Health.up()
                .withDetail("database", "Available")
                .build();
        } else {
            return Health.down()
                .withDetail("database", "Unavailable")
                .build();
        }
    }
}
```

### Security
```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health").permitAll()
            .requestMatchers("/actuator/**").hasRole("ADMIN")
        );
        return http.build();
    }
}
```

**🎓 Interview Points:** "/actuator/health for health checks by load balancer. /actuator/metrics for Prometheus/Grafana. Expose only needed endpoints - sensitive data in /env. Custom HealthIndicator for database, external service checks. Secure with Spring Security - ADMIN role for management endpoints."

---

<a name="sb5"></a>
## 1️⃣8️⃣ Exception Handling in Spring Boot

**⚡ 30-sec Answer:** *@ControllerAdvice for global exception handling. @ExceptionHandler maps exceptions to methods. @ResponseStatus sets HTTP status. ResponseEntityExceptionHandler for Spring MVC exceptions. Custom ErrorResponse DTOs. ProblemDetail (Spring 6+) for RFC 7807 compliant errors.*

### Global Exception Handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // Handle specific exception
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFound(UserNotFoundException ex) {
        return new ErrorResponse(
            "USER_NOT_FOUND",
            ex.getMessage(),
            LocalDateTime.now()
        );
    }
    
    // Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return errors;
    }
    
    // Catch-all
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception ex) {
        return new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            LocalDateTime.now()
        );
    }
}
```

### Error Response DTO
```java
public class ErrorResponse {
    private String errorCode;
    private String message;
    private LocalDateTime timestamp;
    
    // constructors, getters, setters
}
```

### Controller-Specific
```java
@RestController
public class UserController {
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
```

**🎓 Interview Points:** "@ControllerAdvice global for all controllers. @ExceptionHandler maps exception to handler method. @ResponseStatus sets HTTP status code. Return ResponseEntity for fine control. ValidationException return field-level errors. Catch-all Exception handler for unexpected errors."

---

# 🔧 PART D: MICROSERVICES

<a name="m1"></a>
## 1️⃣9️⃣ Microservices Architecture

**⚡ 30-sec Answer:** *Microservices: independent deployable services, each owns data, communicate via API. vs Monolith (single deployable, shared DB). Benefits: independent scaling, tech diversity, fault isolation. Challenges: distributed complexity, data consistency, testing. Patterns: API Gateway, Service Discovery, Circuit Breaker, Event-Driven.*

### Monolith vs Microservices
```
Monolith:
┌──────────────────────────────┐
│   Single Application         │
│  ┌────────┐  ┌────────────┐  │
│  │  User  │  │  Product   │  │
│  │ Module │  │  Module    │  │
│  └────────┘  └────────────┘  │
│       ↓            ↓          │
│  ┌────────────────────────┐  │
│  │   Shared Database      │  │
│  └────────────────────────┘  │
└──────────────────────────────┘
Deploy all or nothing

Microservices:
┌─────────────┐    ┌──────────────┐
│User Service │    │Product Service│
│  ┌────────┐ │    │  ┌─────────┐ │
│  │User DB │ │    │  │Product DB│ │
│  └────────┘ │    │  └─────────┘ │
└──────┬──────┘    └───────┬──────┘
       │ API calls         │
       └───────────────────┘
Independent deployment
```

### When to Use Microservices
```
✅ Use when:
- Large team (>10 developers)
- Need independent scaling
- Different tech stacks per service
- Clear domain boundaries
- Mature DevOps (CI/CD, monitoring)

❌ Avoid when:
- Small team (<5 developers)
- Simple domain
- No clear boundaries
- Limited DevOps capability
```

**🎓 Interview Points:** "Microservices: independent services, own databases, API communication. Benefits: independent scaling/deployment, tech diversity, fault isolation. Challenges: distributed tracing, data consistency (eventual), complex testing. Use for large teams with clear domains, avoid for simple apps."

---

<a name="m2"></a>
## 2️⃣0️⃣ API Gateway Pattern

**⚡ 30-sec Answer:** *API Gateway single entry point for clients. Handles routing, load balancing, authentication, rate limiting, response aggregation. Spring Cloud Gateway uses reactive programming. Routes requests to services, transforms requests/responses. Benefits: centralized cross-cutting, client simplification. Tools: Spring Cloud Gateway, Netflix Zuul (deprecated), Kong.*

### Architecture
```
Client → API Gateway → Microservices

┌────────┐
│ Mobile │─┐
└────────┘ │
           │    ┌──────────────────┐
┌────────┐ │    │   API Gateway    │
│  Web   │─┼───→│  - Routing       │
└────────┘ │    │  - Auth          │
           │    │  - Rate limiting │
┌────────┐ │    │  - Aggregation   │
│  API   │─┘    └─────────┬────────┘
└────────┘                 │
              ┌────────────┼────────────┐
              ↓            ↓            ↓
        ┌──────────┐ ┌──────────┐ ┌──────────┐
        │  User    │ │ Product  │ │  Order   │
        │ Service  │ │ Service  │ │ Service  │
        └──────────┘ └──────────┘ └──────────┘
```

### Spring Cloud Gateway
```java
@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
            // Route to user service
            .route("user-service", r -> r
                .path("/api/users/**")
                .filters(f -> f
                    .stripPrefix(1)  // Remove /api
                    .addRequestHeader("X-Gateway", "true")
                )
                .uri("lb://USER-SERVICE")  // Load balanced
            )
            // Route to product service
            .route("product-service", r -> r
                .path("/api/products/**")
                .filters(f -> f.stripPrefix(1))
                .uri("lb://PRODUCT-SERVICE")
            )
            .build();
    }
}

// Global filters
@Component
public class AuthenticationFilter implements GlobalFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        
        if (token == null || !isValid(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        return chain.filter(exchange);
    }
}
```

### Features
```
Routing: /api/users → User Service
Load Balancing: Distribute across instances
Authentication: Validate JWT tokens
Rate Limiting: Throttle requests per client
Request/Response Transformation: Add headers, modify body
Response Aggregation: Combine multiple service calls
Circuit Breaking: Fail fast when service down
```

**🎓 Interview Points:** "API Gateway single entry point - routes to services. Handles cross-cutting: auth, rate limiting, logging. Spring Cloud Gateway reactive, integrates with Eureka for load balancing. Benefits: centralized logic, client simplification. Drawback: single point of failure - make highly available."

---

<a name="m3"></a>
## 2️⃣1️⃣ Service Discovery

**⚡ 30-sec Answer:** *Service Discovery dynamically finds service instances. Client-side (Eureka): client queries registry, chooses instance. Server-side (Nginx): load balancer queries registry. Netflix Eureka: services register on startup, send heartbeats. EurekaClient fetches registry, load balances with Ribbon. Benefits: dynamic scaling, no hardcoded URLs.*

### Architecture
```
┌─────────────────────┐
│  Eureka Server      │
│  (Service Registry) │
│                     │
│  User Service:      │
│  - 192.168.1.10     │
│  - 192.168.1.11     │
│  Product Service:   │
│  - 192.168.1.20     │
└──────────┬──────────┘
           │
     Register & Heartbeat
           │
    ┌──────┴───────┐
    ↓              ↓
┌─────────┐   ┌──────────┐
│  User   │   │ Product  │
│ Service │   │ Service  │
└─────────┘   └──────────┘
```

### Eureka Server
```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

```properties
server.port=8761
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

### Eureka Client
```java
@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

```properties
spring.application.name=USER-SERVICE
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

### Service Communication
```java
@Service
public class OrderService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public User getUserById(Long userId) {
        // USER-SERVICE resolved by Eureka
        return restTemplate.getForObject(
            "http://USER-SERVICE/users/" + userId,
            User.class
        );
    }
}

@Configuration
public class Config {
    @Bean
    @LoadBalanced  // Enable client-side load balancing
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

**🎓 Interview Points:** "Service Discovery avoids hardcoded IPs. Services register with Eureka on startup, send heartbeats (30s default). Clients query registry, cache locally, load balance. @LoadBalanced RestTemplate resolves service names to IPs. Eureka Server highly available - run multiple instances."

---

<a name="m4"></a>
## 2️⃣2️⃣ Circuit Breaker Pattern

**⚡ 30-sec Answer:** *Circuit Breaker prevents cascading failures. States: Closed (normal), Open (failing, reject requests), Half-Open (test recovery). Resilience4j implements pattern. After threshold failures, opens circuit, returns fallback. After timeout, half-opens to test. Benefits: fail fast, prevent resource exhaustion, graceful degradation.*

### States
```
CLOSED (Normal)
- Requests pass through
- Count failures
- Threshold reached → OPEN

OPEN (Failing)
- Reject requests immediately
- Return fallback
- After timeout → HALF_OPEN

HALF_OPEN (Testing)
- Allow limited requests
- All succeed → CLOSED
- Any fail → OPEN
```

### Resilience4j Implementation
```java
@Service
public class UserService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    @Retry(name = "userService")
    @RateLimiter(name = "userService")
    public User getUser(Long id) {
        return restTemplate.getForObject(
            "http://USER-SERVICE/users/" + id,
            User.class
        );
    }
    
    // Fallback method (same signature + Throwable)
    private User getUserFallback(Long id, Throwable t) {
        return new User(id, "Default User", "default@email.com");
    }
}
```

### Configuration
```properties
# Circuit Breaker
resilience4j.circuitbreaker.instances.userService.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.userService.wait-duration-in-open-state=60s
resilience4j.circuitbreaker.instances.userService.sliding-window-size=10

# Retry
resilience4j.retry.instances.userService.max-attempts=3
resilience4j.retry.instances.userService.wait-duration=500ms

# Rate Limiter
resilience4j.ratelimiter.instances.userService.limit-for-period=10
resilience4j.ratelimiter.instances.userService.limit-refresh-period=1s
```

### Monitoring
```java
@RestController
public class CircuitBreakerController {
    
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    
    @GetMapping("/actuator/circuit-breakers")
    public Map<String, String> getCircuitBreakers() {
        return circuitBreakerRegistry.getAllCircuitBreakers()
            .map(cb -> Map.entry(cb.getName(), cb.getState().toString()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
```

**🎓 Interview Points:** "Circuit Breaker prevents cascading failures. Closed (normal), Open (failing, reject fast), Half-Open (testing recovery). Resilience4j provides @CircuitBreaker, @Retry, @RateLimiter. Fallback method for degraded service. Failure threshold (50%), timeout (60s), sliding window (10 requests)."

---

<a name="m5"></a>
## 2️⃣3️⃣ Distributed Tracing

**⚡ 30-sec Answer:** *Distributed tracing tracks requests across services. Trace spans multiple services, each service creates span. Trace ID propagated via headers. Sleuth adds trace/span IDs to logs. Zipkin collects, visualizes traces. Helps debug latency, failures. Format: [app-name,trace-id,span-id,exportable].*

### Architecture
```
Request Flow:
Client → Gateway → User Service → Order Service → DB

Trace ID: abc123 (same across all)
├─ Span 1: Gateway (abc123, span-1)
├─ Span 2: User Service (abc123, span-2)
└─ Span 3: Order Service (abc123, span-3)

Each span:
- Service name
- Start/End time
- Tags (http.method, http.status)
```

### Spring Cloud Sleuth
```properties
# Auto-added to logs
spring.application.name=user-service

# Log pattern includes trace info
logging.pattern.console=%d{HH:mm:ss.SSS} [%X{traceId}/%X{spanId}] %-5level %logger{36} - %msg%n
```

```
Output:
10:15:30.123 [abc123/span-1] INFO  c.e.UserController - Getting user 1
10:15:30.456 [abc123/span-2] INFO  c.e.UserService - Fetching from DB
```

### Zipkin Integration
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

```properties
management.tracing.sampling.probability=1.0  # 100% sampling (dev only)
management.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans
```

### Viewing Traces
```
Zipkin UI (http://localhost:9411):
- Search by Trace ID
- See all spans in trace
- View latency breakdown
- Identify slow services
```

**🎓 Interview Points:** "Distributed tracing tracks request across services. Trace ID unique per request, propagated via headers. Sleuth auto-adds trace/span IDs to logs. Zipkin collects, visualizes traces. Helps identify bottlenecks, failures. Sampling rate (1.0 = 100%, production use 0.1 = 10%)."

---

<a name="m6"></a>
## 2️⃣4️⃣ Event-Driven Architecture

**⚡ 30-sec Answer:** *Event-driven: services communicate via events (async). Producer publishes events, consumers subscribe. Benefits: loose coupling, scalability, resilience. Kafka for high throughput streams. RabbitMQ for traditional messaging. Event types: domain events (OrderCreated), integration events (cross-service). Patterns: Saga, CQRS, Event Sourcing.*

### Synchronous vs Asynchronous
```
Synchronous (REST):
Service A → calls → Service B (waits for response)
- Tight coupling
- Cascade failures
- Lower throughput

Asynchronous (Events):
Service A → publishes event → Message Broker → Service B subscribes
- Loose coupling
- Resilient to failures
- Higher throughput
```

### Kafka Example
```java
// Producer
@Service
public class OrderService {
    
    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;
    
    public void createOrder(Order order) {
        orderRepository.save(order);
        
        // Publish event
        OrderEvent event = new OrderEvent(order.getId(), order.getTotal());
        kafkaTemplate.send("order-created", event);
    }
}

// Consumer
@Service
public class InventoryService {
    
    @KafkaListener(topics = "order-created", groupId = "inventory-group")
    public void handleOrderCreated(OrderEvent event) {
        // Reduce inventory
        inventoryRepository.reduceStock(event.getOrderId());
        
        // Publish new event
        kafkaTemplate.send("inventory-updated", new InventoryEvent(...));
    }
}
```

### RabbitMQ Example
```java
// Configuration
@Configuration
public class RabbitConfig {
    
    @Bean
    public Queue orderQueue() {
        return new Queue("order-queue");
    }
    
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("order-exchange");
    }
    
    @Bean
    public Binding binding(Queue orderQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderQueue)
            .to(exchange)
            .with("order.created");
    }
}

// Producer
@Service
public class OrderService {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void createOrder(Order order) {
        orderRepository.save(order);
        rabbitTemplate.convertAndSend("order-exchange", "order.created", order);
    }
}

// Consumer
@Service
public class EmailService {
    
    @RabbitListener(queues = "order-queue")
    public void handleOrderCreated(Order order) {
        sendEmail(order.getUserEmail(), "Order confirmed");
    }
}
```

### Event-Driven Patterns

**Saga Pattern (Choreography)**
```
Order Service → OrderCreated event
Inventory Service → Consumes, reduces stock → InventoryReserved event
Payment Service → Consumes, charges → PaymentProcessed event
Shipping Service → Consumes, ships → OrderShipped event

If Payment fails:
Payment Service → PaymentFailed event
Inventory Service → Consumes, restores stock (compensating action)
```

**CQRS (Command Query Responsibility Segregation)**
```
Write side: Handle commands, publish events
Read side: Consume events, build optimized read models
```

**🎓 Interview Points:** "Event-driven: async communication via events. Benefits: loose coupling, scalability, resilience. Kafka for high-throughput streams, RabbitMQ for traditional messaging. Saga pattern for distributed transactions with compensating actions. CQRS separates read/write models. Event Sourcing stores events as source of truth."

---

# 🌐 PART E: REST API

<a name="r1"></a>
## 2️⃣5️⃣ REST Principles

**⚡ 30-sec Answer:** *REST: Representational State Transfer. Principles: Stateless (no server session), Resource-based URLs (/users/1), HTTP methods (GET, POST, PUT, DELETE), Representations (JSON, XML), HATEOAS (links in response). RESTful: /users vs /getUsers. Use nouns not verbs in URLs.*

### REST Constraints
```
1. Client-Server: Separation of concerns
2. Stateless: No session on server, token in request
3. Cacheable: Responses marked cacheable
4. Uniform Interface: Standard HTTP methods
5. Layered System: Client doesn't know intermediaries
6. Code on Demand: Optional (JavaScript from server)
```

### Resource Naming
```
✅ Good (Nouns):
GET    /users           - List users
GET    /users/1         - Get user 1
POST   /users           - Create user
PUT    /users/1         - Update user 1
DELETE /users/1         - Delete user 1
GET    /users/1/orders  - User 1's orders

❌ Bad (Verbs):
GET    /getUsers
POST   /createUser
GET    /getUserOrders/1
```

### Stateless
```java
// ❌ BAD: Stateful (server session)
@GetMapping("/cart/add/{productId}")
public void addToCart(@PathVariable Long productId, HttpSession session) {
    List<Product> cart = (List) session.getAttribute("cart");
    cart.add(productService.findById(productId));
}

// ✅ GOOD: Stateless (client sends token)
@GetMapping("/carts/{cartId}/items")
public Cart getCart(@PathVariable String cartId, @RequestHeader("Authorization") String token) {
    validateToken(token);
    return cartService.findById(cartId);
}
```

### HATEOAS
```json
{
  "id": 1,
  "name": "John",
  "email": "john@example.com",
  "_links": {
    "self": {"href": "/users/1"},
    "orders": {"href": "/users/1/orders"},
    "address": {"href": "/users/1/address"}
  }
}
```

**🎓 Interview Points:** "REST stateless - no server session, client sends auth token. Resource-based URLs with nouns (/users not /getUsers). HTTP methods map to CRUD: GET (read), POST (create), PUT (update), DELETE (delete). HATEOAS provides navigation links. Idempotent: GET, PUT, DELETE can be repeated safely."

---

<a name="r2"></a>
## 2️⃣6️⃣ HTTP Methods

**⚡ 30-sec Answer:** *GET (retrieve, idempotent, cacheable), POST (create, not idempotent), PUT (update/replace, idempotent), PATCH (partial update), DELETE (remove, idempotent). Safe: GET (no side effects). Idempotent: GET, PUT, DELETE (repeat = same result). POST not idempotent (creates multiple).*

### Methods Comparison
| Method | Purpose | Safe | Idempotent | Request Body | Response Body |
|--------|---------|------|------------|--------------|---------------|
| GET | Retrieve | ✅ | ✅ | ❌ | ✅ |
| POST | Create | ❌ | ❌ | ✅ | ✅ |
| PUT | Update/Replace | ❌ | ✅ | ✅ | ✅ |
| PATCH | Partial Update | ❌ | ❌ | ✅ | ✅ |
| DELETE | Remove | ❌ | ✅ | ❌ | ✅/❌ |
| HEAD | Headers only | ✅ | ✅ | ❌ | ❌ |
| OPTIONS | Supported methods | ✅ | ✅ | ❌ | ✅ |

### Usage Examples
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    // GET - Retrieve (list)
    @GetMapping
    public List<User> getUsers() {
        return userService.findAll();
    }
    
    // GET - Retrieve (single)
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
    
    // POST - Create
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)  // 201
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }
    
    // PUT - Update (full replacement)
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return userService.update(user);
    }
    
    // PATCH - Partial update
    @PatchMapping("/{id}")
    public User partialUpdate(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        return userService.partialUpdate(id, updates);
    }
    
    // DELETE - Remove
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)  // 204
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }
}
```

### PUT vs PATCH
```java
// PUT - Replace entire resource
PUT /users/1
{
  "name": "John Updated",
  "email": "john@updated.com",
  "age": 31
}
// All fields required, missing fields set to null

// PATCH - Update specific fields
PATCH /users/1
{
  "email": "john@updated.com"
}
// Only updates email, other fields unchanged
```

### Idempotency
```
Idempotent = Same result when repeated

GET /users/1     → Same user each time ✅
PUT /users/1     → Same update each time ✅
DELETE /users/1  → First deletes, subsequent return 404 (same state) ✅

POST /users      → Creates new user each time ❌ NOT idempotent
```

**🎓 Interview Points:** "GET safe (no side effects) and idempotent (repeatable). POST creates - not idempotent (creates multiple). PUT replaces entire resource - idempotent. PATCH partial update. DELETE idempotent (same state after repeat). Safe methods: GET, HEAD, OPTIONS."

---

<a name="r3"></a>
## 2️⃣7️⃣ HTTP Status Codes

**⚡ 30-sec Answer:** *2xx success (200 OK, 201 Created, 204 No Content), 3xx redirect (301 Moved Permanently, 302 Found), 4xx client error (400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found), 5xx server error (500 Internal Server Error, 503 Service Unavailable). Return appropriate codes for API clarity.*

### Common Status Codes
```
2xx Success:
200 OK              - GET successful, general success
201 Created         - POST created resource (return location header)
204 No Content      - DELETE successful, no body returned
202 Accepted        - Async processing started

3xx Redirection:
301 Moved Permanently - Resource moved (SEO)
302 Found           - Temporary redirect
304 Not Modified    - Cached version still valid

4xx Client Error:
400 Bad Request     - Invalid input, validation failure
401 Unauthorized    - Missing/invalid authentication
403 Forbidden       - Authenticated but no permission
404 Not Found       - Resource doesn't exist
405 Method Not Allowed - Wrong HTTP method
409 Conflict        - Resource state conflict
422 Unprocessable Entity - Validation error
429 Too Many Requests - Rate limit exceeded

5xx Server Error:
500 Internal Server Error - Unexpected server error
502 Bad Gateway     - Invalid response from upstream
503 Service Unavailable - Server overloaded/down
504 Gateway Timeout - Upstream didn't respond
```

### Spring Boot Examples
```java
@RestController
public class UserController {
    
    // 200 OK
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id);
    }
    
    // 201 Created
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User saved = userService.save(user);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header("Location", "/users/" + saved.getId())
            .body(saved);
    }
    
    // 204 No Content
    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }
    
    // 400 Bad Request
    @PostMapping("/users")
    public User createUser(@Valid @RequestBody User user, BindingResult result) {
        if (result.hasErrors()) {
            throw new BadRequestException(result.getAllErrors());
        }
        return userService.save(user);
    }
    
    // 404 Not Found
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }
}

// Exception handlers
@RestControllerAdvice
public class ExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(UserNotFoundException ex) {
        return new ErrorResponse(404, ex.getMessage());
    }
    
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(BadRequestException ex) {
        return new ErrorResponse(400, ex.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception ex) {
        return new ErrorResponse(500, "Internal server error");
    }
}
```

**🎓 Interview Points:** "2xx success - 200 OK general, 201 Created (return Location header), 204 No Content. 4xx client errors - 400 validation, 401 auth missing, 403 no permission, 404 not found. 5xx server errors - 500 unexpected, 503 unavailable. Return appropriate codes for API clarity and client handling."

---

<a name="r4"></a>
## 2️⃣8️⃣ API Versioning

**⚡ 30-sec Answer:** *API versioning handles breaking changes. Methods: URI versioning (/api/v1/users), Header versioning (Accept: application/vnd.api.v1+json), Query parameter (/users?version=1), Content negotiation. URI most common - simple, visible. Semantic versioning: major.minor.patch. Breaking change = new major version.*

### Versioning Strategies

**1. URI Versioning (Most Common)**
```java
@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 {
    @GetMapping
    public List<UserV1> getUsers() {
        // Old format
    }
}

@RestController
@RequestMapping("/api/v2/users")
public class UserControllerV2 {
    @GetMapping
    public List<UserV2> getUsers() {
        // New format with additional fields
    }
}

URLs:
GET /api/v1/users
GET /api/v2/users
```

**2. Header Versioning**
```java
@RestController
public class UserController {
    
    @GetMapping(value = "/users", headers = "API-Version=1")
    public List<UserV1> getUsersV1() {
        // Version 1
    }
    
    @GetMapping(value = "/users", headers = "API-Version=2")
    public List<UserV2> getUsersV2() {
        // Version 2
    }
}

Request:
GET /users
API-Version: 2
```

**3. Query Parameter**
```java
@RestController
public class UserController {
    
    @GetMapping("/users")
    public List<User> getUsers(@RequestParam(defaultValue = "1") int version) {
        if (version == 2) {
            return getUsersV2();
        }
        return getUsersV1();
    }
}

URLs:
GET /users?version=1
GET /users?version=2
```

**4. Content Negotiation**
```java
@RestController
public class UserController {
    
    @GetMapping(value = "/users", produces = "application/vnd.api.v1+json")
    public List<UserV1> getUsersV1() {
        // Version 1
    }
    
    @GetMapping(value = "/users", produces = "application/vnd.api.v2+json")
    public List<UserV2> getUsersV2() {
        // Version 2
    }
}

Request:
GET /users
Accept: application/vnd.api.v2+json
```

### Comparison
| Method | Pros | Cons | Use When |
|--------|------|------|----------|
| **URI** | Simple, visible, cacheable | URL changes | Most cases |
| **Header** | Clean URLs | Not visible, harder to test | Advanced APIs |
| **Query Param** | Simple | Pollutes query string | Internal APIs |
| **Content Negotiation** | RESTful | Complex, not obvious | Mature APIs |

### Semantic Versioning
```
v1.2.3
│ │ │
│ │ └─ Patch: Bug fixes (backward compatible)
│ └─── Minor: New features (backward compatible)
└───── Major: Breaking changes (NOT backward compatible)

Examples:
v1.0.0 → v1.0.1: Bug fix
v1.0.1 → v1.1.0: New field added
v1.1.0 → v2.0.0: Field removed (breaking!)
```

**🎓 Interview Points:** "URI versioning (/api/v1) most common - simple, visible. Header versioning cleaner URLs but harder to test. Breaking changes require new major version. Deprecate old versions gracefully with warnings. Support 2-3 versions max. Document version differences clearly."

---

<a name="r5"></a>
## 2️⃣9️⃣ API Security

**⚡ 30-sec Answer:** *API security: Authentication (who you are), Authorization (what you can do). JWT (stateless token, claims-based), OAuth2 (delegated authorization, Google/Facebook login), API Keys (simple, less secure). HTTPS mandatory. Rate limiting prevents abuse. CORS for browser security. Input validation prevents injection.*

### Authentication Methods

**1. JWT (JSON Web Token)**
```java
@RestController
public class AuthController {
    
    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        // Validate credentials
        User user = authService.authenticate(request.getUsername(), request.getPassword());
        
        // Generate JWT
        String token = jwtService.generateToken(user);
        return new TokenResponse(token);
    }
}

// JWT Service
@Service
public class JwtService {
    
    private static final String SECRET = "your-secret-key";
    
    public String generateToken(User user) {
        return Jwts.builder()
            .setSubject(user.getUsername())
            .claim("userId", user.getId())
            .claim("roles", user.getRoles())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
            .signWith(SignatureAlgorithm.HS256, SECRET)
            .compact();
    }
    
    public Claims validateToken(String token) {
        return Jwts.parser()
            .setSigningKey(SECRET)
            .parseClaimsJws(token)
            .getBody();
    }
}

// Security Filter
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain) throws ServletException, IOException {
        String token = extractToken(request);
        
        if (token != null && jwtService.validateToken(token) != null) {
            // Set authentication in context
            Authentication auth = getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        chain.doFilter(request, response);
    }
}
```

**2. OAuth2**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .oauth2Login()  // Enable OAuth2 login
                .defaultSuccessUrl("/home")
            .and()
            .authorizeHttpRequests()
                .requestMatchers("/public/**").permitAll()
                .anyRequest().authenticated();
        return http.build();
    }
}

// application.properties
spring.security.oauth2.client.registration.google.client-id=your-client-id
spring.security.oauth2.client.registration.google.client-secret=your-secret
spring.security.oauth2.client.registration.google.scope=profile,email
```

**3. API Keys**
```java
@RestController
public class ProductController {
    
    @GetMapping("/products")
    public List<Product> getProducts(@RequestHeader("X-API-Key") String apiKey) {
        if (!apiKeyService.isValid(apiKey)) {
            throw new UnauthorizedException("Invalid API key");
        }
        return productService.findAll();
    }
}

// API Key Filter
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain) throws ServletException, IOException {
        String apiKey = request.getHeader("X-API-Key");
        
        if (apiKey == null || !apiKeyService.isValid(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        chain.doFilter(request, response);
    }
}
```

### Authorization
```java
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")  // Only admins
    public List<User> getAllUsers() {
        return userService.findAll();
    }
    
    @PostMapping("/users")
    @PreAuthorize("hasAuthority('USER_CREATE')")  // Specific permission
    public User createUser(@RequestBody User user) {
        return userService.save(user);
    }
}
```

### Security Best Practices
```java
// 1. HTTPS only
server.ssl.enabled=true

// 2. CORS configuration
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("https://trusted-domain.com");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}

// 3. Rate limiting
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private Map<String, Integer> requestCounts = new ConcurrentHashMap<>();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain) throws ServletException, IOException {
        String clientId = getClientId(request);
        int count = requestCounts.getOrDefault(clientId, 0);
        
        if (count > 100) {  // 100 requests per minute
            response.setStatus(429);  // Too Many Requests
            return;
        }
        
        requestCounts.put(clientId, count + 1);
        chain.doFilter(request, response);
    }
}

// 4. Input validation
@PostMapping("/users")
public User createUser(@Valid @RequestBody User user) {
    // @Valid triggers validation
    return userService.save(user);
}

public class User {
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;
    
    @Email
    private String email;
    
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$")
    private String password;
}
```

### Comparison
| Method | Pros | Cons | Use Case |
|--------|------|------|----------|
| **JWT** | Stateless, scalable, claims | Token size, no revocation | Microservices, mobile apps |
| **OAuth2** | Delegated auth, trusted | Complex | Social login, 3rd party apps |
| **API Keys** | Simple | Less secure, hard to rotate | Internal APIs, server-to-server |

**🎓 Interview Points:** "JWT stateless token with claims (user info) - good for microservices. OAuth2 for delegated authorization (Google/Facebook login). API keys simple but less secure. Always HTTPS. Rate limiting prevents abuse (100 req/min). CORS allows trusted origins. Validate all inputs to prevent injection."

---

# 📖 QUICK REFERENCE SUMMARY

## Java Core
- **Collections**: ArrayList O(1) get, HashMap O(1), TreeSet O(log n) sorted
- **HashMap**: Buckets, collisions → linked list/tree, resize at 0.75 load
- **Streams**: filter/map (lazy) → collect (eager), parallelStream for large data
- **Threads**: synchronized (mutex), volatile (visibility), ExecutorService (pool)
- **JVM**: Heap (objects, GC'd), Stack (frames, per thread), Method Area (metadata)
- **GC**: Mark-and-Sweep, G1GC (default, 200ms pauses), ZGC (<10ms)
- **Exceptions**: Checked (must handle), Unchecked (RuntimeException), try-with-resources
- **String**: Immutable (thread-safe), StringBuilder (fast, not thread-safe), String pool

## Spring Framework
- **DI/IoC**: Constructor injection (recommended), @Autowired, @Configuration
- **Bean Lifecycle**: Instantiate → @Autowired → @PostConstruct → ready → @PreDestroy
- **Scopes**: Singleton (default), Prototype (new each time), Request/Session
- **AOP**: @Before, @After, @Around, @Transactional uses AOP
- **Spring MVC**: DispatcherServlet → HandlerMapping → Controller → ViewResolver

## Spring Boot
- **Auto-Config**: @EnableAutoConfiguration, @ConditionalOnClass, META-INF/spring.factories
- **Starters**: spring-boot-starter-web, starter-data-jpa (bundle dependencies)
- **Profiles**: application-{profile}.properties, spring.profiles.active=dev
- **Actuator**: /actuator/health, /metrics, /info, custom HealthIndicator
- **Exceptions**: @ControllerAdvice, @ExceptionHandler, ResponseEntityExceptionHandler

## Microservices
- **Architecture**: Independent services, own DB, API communication
- **API Gateway**: Single entry, routing, auth, rate limiting (Spring Cloud Gateway)
- **Discovery**: Eureka - register/heartbeat, @LoadBalanced RestTemplate
- **Circuit Breaker**: Resilience4j - CLOSED/OPEN/HALF_OPEN, fallback
- **Tracing**: Sleuth adds trace IDs, Zipkin visualizes
- **Events**: Kafka (streams), RabbitMQ (messaging), Saga pattern

## REST API
- **Principles**: Stateless, resource URLs (/users), HTTP methods
- **Methods**: GET (retrieve), POST (create), PUT (replace), PATCH (partial), DELETE
- **Status**: 200 OK, 201 Created, 400 Bad Request, 401 Unauthorized, 404 Not Found, 500 Error
- **Versioning**: URI (/api/v1), Header, Query param - URI most common
- **Security**: JWT (stateless), OAuth2 (delegated), API Keys, HTTPS, CORS, rate limiting

---

**✅ COMPLETE - All 29 Topics Covered!**
Ready for interviews! 🎯🚀
