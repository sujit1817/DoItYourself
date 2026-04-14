# ☕ Java & Spring Boot — Interview Prep Notes

> **Topics Covered:** Spring Boot Optimization · Microservices · JPA · Concurrency · REST · JVM · RabbitMQ · Maven · and more.

---

## Table of Contents

1. [Optimize Spring Boot Startup & Performance](#1-optimize-spring-boot-startup--performance)
2. [Prevent Memory Heap Issues in Java](#2-prevent-memory-heap-issues-in-java)
3. [Handle Failures in Microservices](#3-handle-failures-in-microservices)
4. [REST API Versioning](#4-rest-api-versioning)
5. [REST API Best Practices](#5-rest-api-best-practices)
6. [Detect Slow Microservice](#6-detect-slow-microservice)
7. [Track Exceptions in Microservices](#7-track-exceptions-in-microservices)
8. [Entity Type vs Value Type (JPA)](#8-entity-type-vs-value-type-jpa)
9. [Scheduling Tasks in Spring Boot](#9-scheduling-tasks-in-spring-boot)
10. [Marker Interface](#10-marker-interface)
11. [equals() and hashCode() Real-World Use Cases](#11-equals-and-hashcode-real-world-use-cases)
12. [Varargs in Java](#12-varargs-in-java)
13. [Idempotent Methods in REST](#13-idempotent-methods-in-rest)
14. [Why Avoid public static final for Singleton](#14-why-avoid-public-static-final-for-singleton)
15. [Spring Cloud](#15-spring-cloud)
16. [Secure Communication Between Services](#16-secure-communication-between-services)
17. [Sealed Classes/Interfaces (Java 17)](#17-sealed-classesinterfaces-java-17)
18. [DELETE vs TRUNCATE](#18-delete-vs-truncate)
19. [Custom Actuator Endpoint](#19-custom-actuator-endpoint)
20. [Multiple Databases in Spring Boot](#20-multiple-databases-in-spring-boot)
21. [Class.forName vs ClassLoader.loadClass](#21-classforname-vs-classloaderloadclass)
22. [Parallel Programming in Spring](#22-parallel-programming-in-spring)
23. [Future in Java](#23-future-in-java)
24. [CompletableFuture in Java](#24-completablefuture-in-java)
25. [Garbage Collector — Java 8 Improvements](#25-garbage-collector--java-8-improvements)
26. [Custom Scope in Spring](#26-custom-scope-in-spring)
27. [ArrayList vs LinkedList — Element Access](#27-arraylist-vs-linkedlist--element-access)
28. [finally Block Return Value](#28-finally-block-return-value)
29. [RestTemplate Methods](#29-resttemplate-methods)
30. [volatile vs Atomic](#30-volatile-vs-atomic)
31. [== vs equals()](#31--vs-equals)
32. [Jenkins + Kubernetes Flow](#32-jenkins--kubernetes-flow)
33. [RabbitMQ Implementation](#33-rabbitmq-implementation)
34. [@Component with Private Constructor](#34-component-with-private-constructor)
35. [Skip Fields in Serialization Without transient](#35-skip-fields-in-serialization-without-transient)
36. [JPA Inheritance Strategies](#36-jpa-inheritance-strategies)
37. [SQL — Experience & Join Date Query](#37-sql--experience--join-date-query)
38. [Pagination in REST Endpoint](#38-pagination-in-rest-endpoint)
39. [Maven Overview](#39-maven-overview)

---

## 1. Optimize Spring Boot Startup & Performance

### 🚀 Reduce Startup Time

```properties
# Lazy bean initialization — only initialize when needed
spring.main.lazy-initialization=true

# Activate specific profile to avoid loading unused configs
spring.profiles.active=prod

# Reduce log noise at startup
logging.level.root=warn
```

- **Exclude unused auto-configurations** via `@SpringBootApplication(exclude = {...})`

### ⚡ Increase Runtime Performance

| Technique | Details |
|-----------|---------|
| **Lazy Initialization** | `spring.main.lazy-initialization=true` |
| **Caching** | Use `@Cacheable` for frequently accessed data |
| **Async Processing** | `@Async` + `CompletableFuture` for non-blocking calls |
| **Profile-Based Config** | Separate `dev` / `prod` properties |
| **Monitoring** | Micrometer, Prometheus, Grafana |
| **GC Tuning** | Use G1GC or ZGC for low-latency apps |
| **Efficient Data Access** | Tune JPA/Hibernate, avoid N+1 queries, use pagination |
| **Connection Pooling** | HikariCP (default) for DB connection reuse |

> 💡 **Avoid N+1 in JPA:** Use `fetch join` or `@EntityGraph`. Enable SQL logging only in dev.

---

## 2. Prevent Memory Heap Issues in Java

### 🔍 What is a Memory Leak?
> Objects that are **no longer needed** but remain in memory (heap/metaspace), and the **GC cannot remove** them.

### Common Causes & Fixes

```java
// ❌ Bad — static fields live as long as the class
public static List<User> userList = new ArrayList<>();

// ✅ Fix — clear when done
list.clear();
map = null;
```

| Cause | Fix |
|-------|-----|
| Static collections | Clear or nullify after use |
| Growing collections | Use `list.clear()` when done |
| Wrong data structure | `ArrayList` for index access; `ConcurrentHashMap` for thread safety |
| Inner/anonymous classes | They hold implicit refs to outer class — use with caution |

> 🛠️ **Tools:** VisualVM, JProfiler, Eclipse MAT

---

## 3. Handle Failures in Microservices

Use **Resilience4j** for circuit breaking, retries, and fallbacks.

```java
@CircuitBreaker(name = "productService", fallbackMethod = "getDefaultProduct")
@Retry(name = "productService")
public String getProductDetails(String productId) {
    return restTemplate.getForObject("http://localhost:8081/products/" + productId, String.class);
}

public String getDefaultProduct(String productId, Throwable t) {
    return "Fallback: No data available for productId=" + productId;
}
```

```properties
# Retry config
resilience4j.retry.instances.productService.max-attempts=3
resilience4j.retry.instances.productService.wait-duration=2s
```

### How It Works
```
Request → OrderService → ProductService fails
              ↓
         Retry (3x, 2s apart)
              ↓
         Circuit Breaker OPENS
              ↓
         Fallback method called
```

> ⚠️ `@Hystrix` is now deprecated. Prefer **Resilience4j**.

---

## 4. REST API Versioning

> Managing API changes without breaking existing clients.

### Strategies

| Strategy | Example |
|----------|---------|
| **URI Versioning** | `/api/v1/users` |
| **Query Parameter** | `/api/users?version=1` |
| **Header Versioning** | `X-API-Version: 1` |
| **Content Negotiation** | `Accept: application/vnd.myapp.v1+json` |

### Why Version?
- Add new features safely
- Fix bugs/behavior without breaking old clients
- Maintain backward compatibility

---

## 5. REST API Best Practices

1. **Richardson Maturity Model** — Evaluate and improve REST design level by level
2. **Versioning** — As described above
3. **Documentation** — Use **Swagger / OpenAPI**
4. **Correct HTTP Status Codes** — `200`, `201`, `400`, `404`, `500`, etc.
5. **Content Negotiation** — Support `JSON`, `XML` where needed
6. **Error Handling** — Return structured error responses with message, code, timestamp
7. **Pagination** — For list endpoints like `/getAllBooks?page=0&size=10`

---

## 6. Detect Slow Microservice

> Services A → B → C → D (C is slow, A appears slow)

**Solution: Use [Zipkin](https://zipkin.io/)**

- Distributed tracing tool
- Tracks request timing across each microservice hop
- Helps pinpoint which service/endpoint is the bottleneck
- Integrated with **Spring Cloud Sleuth**

---

## 7. Track Exceptions in Microservices

1. Every request gets a unique **Trace ID** (via Sleuth/Zipkin)
2. On exception, grab the **traceId** from logs
3. Use **Kibana** or **Zipkin** to search by traceId
4. Navigate to the failing service and resolve the issue

---

## 8. Entity Type vs Value Type (JPA)

| | Entity Type | Value Type |
|--|-------------|------------|
| **Annotation** | `@Entity` | `@Embeddable` / `@Embedded` |
| **Identity** | Has `@Id` | No `@Id` |
| **Storage** | Separate table | Same table as owning entity |
| **Lifecycle** | Independent | Tied to parent entity |

```java
@Entity
public class Employee {
    @Id Long id;

    @Embedded
    private Address address; // Value Type — no separate table
}

@Embeddable
public class Address {
    String city;
    String zip;
}
```

---

## 9. Scheduling Tasks in Spring Boot

### Setup

```java
@SpringBootApplication
@EnableScheduling
public class MyApp { ... }
```

```java
@Component
public class MyScheduledTask {

    @Scheduled(fixedRate = 5000)
    public void runEveryFiveSeconds() {
        System.out.println("Task at: " + LocalDateTime.now());
    }
}
```

### Scheduling Options

| Annotation | Behavior |
|-----------|----------|
| `@Scheduled(fixedRate = 5000)` | Every 5s **from last START** |
| `@Scheduled(fixedDelay = 5000)` | Every 5s **after last COMPLETION** |
| `@Scheduled(initialDelay = 10000, fixedRate = 5000)` | Start after 10s, then every 5s |
| `@Scheduled(cron = "0 0/1 * * * ?")` | Cron expression (every 1 min) |

> 💡 **fixedRate** can overlap if task takes longer than interval. Use **fixedDelay** if you need a gap between runs.

---

## 10. Marker Interface

> An interface with **no methods or fields** — used to tag a class for special behavior.

**Built-in examples:** `Serializable`, `Cloneable`

```java
public interface Auditable { } // Marker

public class User implements Auditable { ... }
public class Product { ... } // Not auditable

public class AuditLogger {
    public void logChange(Object obj) {
        if (obj instanceof Auditable) {
            System.out.println("Auditing: " + obj.getClass().getSimpleName());
        }
    }
}
```

> 🔄 Modern alternative: Use **annotations** (e.g., `@Auditable`) for the same purpose with more flexibility.

---

## 11. equals() and hashCode() Real-World Use Cases

### 1. Collections — HashSet / HashMap

> These use `hashCode()` to find the bucket and `equals()` to confirm equality.

Without override → two objects with same data are treated as **different**.

### 2. JPA/Hibernate Entity Comparisons

> Override `equals()` to compare by entity ID for correct caching and identity.

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User)) return false;
    User user = (User) o;
    return Objects.equals(id, user.id);
}

@Override
public int hashCode() {
    return Objects.hash(id);
}
```

---

## 12. Varargs in Java

> Accept **zero or more** arguments of the same type — no overloading needed.

```java
public void printAll(String... names) {
    for (String name : names) System.out.println(name);
}

printAll("Sai", "Ravi", "Anita"); // Works!
```

### Rules

```java
void method(String... names, int... ages); // ❌ Only one vararg allowed
void method(String prefix, String... names); // ✅ Must be last parameter
```

### JDK Example
```java
String.format("Name: %s, Age: %d", "Sai", 25); // uses Object... args
```

---

## 13. Idempotent Methods in REST

> Calling the same request **multiple times** produces the **same result**.

| Method | Idempotent? | Why |
|--------|------------|-----|
| `GET` | ✅ Yes | Only reads data |
| `PUT` | ✅ Yes | Same input = same state |
| `DELETE` | ✅ Yes | Deleting twice = resource still gone |
| `POST` | ❌ No | Creates a new resource each time |

### Why It Matters
- Safe to **retry** on network failures
- Critical in **distributed systems** with retry logic

---

## 14. Why Avoid public static final for Singleton

```java
public class MySingleton {
    public static final MySingleton INSTANCE = new MySingleton(); // Eager
    private MySingleton() {}
}
```

| Issue | Explanation |
|-------|-------------|
| **No lazy loading** | Instance created at class load — wastes memory if unused |
| **Hard to test** | Can't mock or replace easily |
| **No control** | Can't add logic (e.g., logging, pooling) later |

> ✅ **Prefer:** `enum` singleton or `@Component` with `@Scope("singleton")` (Spring default).

---

## 15. Spring Cloud

> A toolkit for building **resilient, scalable distributed microservices** on top of Spring.

| Feature | Tool |
|---------|------|
| Service Discovery | Eureka Server |
| Config Management | Spring Cloud Config Server |
| Load Balancing | Spring Cloud LoadBalancer |
| Circuit Breaker | Resilience4j / Hystrix |
| API Gateway | Spring Cloud Gateway |
| Tracing & Logging | Sleuth + Zipkin + ELK Stack |

---

## 16. Secure Communication Between Services

| Strategy | Description |
|----------|-------------|
| 🔒 HTTPS (TLS/SSL) | Encrypt all inter-service traffic |
| 🔐 OAuth2 / JWT | Authenticate requests between services |
| 🤝 Mutual TLS (mTLS) | Both sides verify identity via certificates |
| 🧰 API Gateway | Centralized auth + routing |
| 🧾 Spring Security | Role/scope-based access control |
| 🔁 Token Propagation | Forward JWT downstream in `Authorization` header |

### JWT Flow
```
Service A → gets JWT from Auth Server
         → sends JWT in Authorization: Bearer <token>
         → Service B validates token ✅
```

---

## 17. Sealed Classes/Interfaces (Java 17)

> Restrict which classes can **extend or implement** a type.

```java
public sealed interface Payment permits CreditCardPayment, UpiPayment, WalletPayment {
    void pay(double amount);
}

public final class CreditCardPayment implements Payment {
    public void pay(double amount) {
        System.out.println("Paid ₹" + amount + " via Credit Card");
    }
}

public non-sealed class FlexiblePayment implements Payment {
    // Can be extended by anyone
    public void pay(double amount) { ... }
}
```

| Keyword | Meaning |
|---------|---------|
| `sealed` | Restricts which classes can extend |
| `permits` | Lists the allowed subclasses |
| `final` | Subclass cannot be extended further |
| `non-sealed` | Lifts the restriction — open for extension |

### Benefits
- Explicit control over subclassing
- Enables exhaustive **pattern matching** in `switch` expressions (compiler knows all subtypes)

---

## 18. DELETE vs TRUNCATE

| Feature | `DELETE` | `TRUNCATE` |
|---------|----------|------------|
| Removes all rows? | ✅ (without WHERE) | ✅ Always |
| Conditional deletion? | ✅ (`WHERE` clause) | ❌ |
| Rollback possible? | ✅ Yes | ⚠️ DB-dependent |
| Fires triggers? | ✅ Yes | ❌ No |
| Speed | Slower | Faster |
| Resets auto-increment? | ❌ No | ✅ Yes (most DBs) |
| Transaction log | Fully logged | Minimally logged |
| Frees storage? | ❌ Marks as reusable | ✅ Deallocates immediately |

---

## 19. Custom Actuator Endpoint

```java
@Component
@Endpoint(id = "customstatus")
public class CustomStatusEndpoint {

    @ReadOperation
    public String customHealth() {
        return "Application is running fine!";
    }
}
```

```properties
management.endpoints.web.exposure.include=health,info,customstatus
```

```
GET http://localhost:8080/actuator/customstatus
```

| Annotation | HTTP Method | Purpose |
|-----------|------------|---------|
| `@ReadOperation` | GET | Read/fetch data |
| `@WriteOperation` | POST | Modify/update data |
| `@DeleteOperation` | DELETE | Delete/reset data |

---

## 20. Multiple Databases in Spring Boot

```properties
# Primary DB
spring.datasource.primary.url=jdbc:mysql://localhost:3306/primary_db
spring.datasource.primary.username=root
spring.datasource.primary.password=root

# Secondary DB
spring.datasource.secondary.url=jdbc:mysql://localhost:3306/secondary_db
spring.datasource.secondary.username=root
spring.datasource.secondary.password=root
```

> ⚠️ Auto-configuration applies **only to the primary** datasource. Secondary must be manually configured.

```java
@Configuration
@EnableJpaRepositories(
    basePackages = "com.example.secondary.repository",
    entityManagerFactoryRef = "secondaryEntityManagerFactory",
    transactionManagerRef = "secondaryTransactionManager"
)
public class SecondaryDBConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.secondary")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(secondaryDataSource())
                .packages("com.example.secondary.entity")
                .persistenceUnit("secondary")
                .build();
    }

    @Bean
    public PlatformTransactionManager secondaryTransactionManager(
            @Qualifier("secondaryEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
```

---

## 21. Class.forName vs ClassLoader.loadClass

| Method | Loads Class | Initializes (Static Blocks) |
|--------|------------|----------------------------|
| `Class.forName("ClassName")` | ✅ | ✅ Yes |
| `ClassLoader.loadClass("ClassName")` | ✅ | ❌ No |

> Use `Class.forName()` for JDBC drivers and similar where initialization matters.
> Use `ClassLoader.loadClass()` when you want deferred initialization.

---

## 22. Parallel Programming in Spring

### Using @Async

```java
@SpringBootApplication
@EnableAsync
public class MyApp { ... }

@Async
public CompletableFuture<String> processData() {
    // long-running task
    return CompletableFuture.completedFuture("done");
}
```

### Parallel Tasks with CompletableFuture

```java
CompletableFuture<String> task1 = service.task1();
CompletableFuture<String> task2 = service.task2();
CompletableFuture.allOf(task1, task2).join(); // wait for both
```

**Use Cases:** Email sending, PDF generation, file uploads, API fan-out calls

---

## 23. Future in Java

> Represents the **result of an async task** — available *later*.

```java
ExecutorService executor = Executors.newSingleThreadExecutor();

Future<String> future = executor.submit(() -> {
    Thread.sleep(2000);
    return "Task Completed";
});

System.out.println("Doing other work...");
String result = future.get(); // blocks until done
executor.shutdown();
```

### Key Methods

| Method | Description |
|--------|-------------|
| `get()` | Blocks until result is ready |
| `get(timeout, unit)` | Blocks with timeout |
| `isDone()` | `true` if completed |
| `isCancelled()` | `true` if cancelled |
| `cancel(bool)` | Tries to stop the task |

### ❌ Limitations of Future
- No chaining (no `thenApply`)
- Blocking only
- No exception handling pipeline

> ✅ **Java 8+:** Use `CompletableFuture` instead.

---

## 24. CompletableFuture in Java

> Non-blocking, composable async programming introduced in **Java 8**.

```java
// Parallel API calls with timeout and fallback
CompletableFuture<String> userFuture = CompletableFuture
    .supplyAsync(() -> getUserDetails(), executor)
    .orTimeout(3, TimeUnit.SECONDS)
    .exceptionally(ex -> "User API failed: " + ex.getMessage());

CompletableFuture<String> orderFuture = CompletableFuture
    .supplyAsync(() -> getOrderDetails(), executor)
    .orTimeout(4, TimeUnit.SECONDS)
    .exceptionally(ex -> "Order API failed: " + ex.getMessage());

// Combine both
userFuture.thenCombine(orderFuture, (user, order) ->
    "Result:\n" + user + "\n" + order
).thenAccept(System.out::println).join();
```

### Key Operations

| Method | Purpose |
|--------|---------|
| `supplyAsync()` | Run async task returning a value |
| `thenApply()` | Transform the result |
| `thenAccept()` | Consume the result (no return) |
| `thenCombine()` | Merge two futures |
| `allOf()` | Wait for all futures |
| `orTimeout()` | Complete exceptionally on timeout |
| `exceptionally()` | Fallback on error |

---

## 25. Garbage Collector — Java 8 Improvements

### 1. G1 Garbage Collector
- Available in Java 8, **default from Java 9**
- Designed for **large heap applications** (multi-GB)
- Divides heap into **regions** for predictable, low-pause GC

### 2. Metaspace Replaces PermGen

| | PermGen (≤ Java 7) | Metaspace (Java 8+) |
|--|---------------------|----------------------|
| Location | Part of heap | Native memory (off-heap) |
| Size | Fixed, limited | Grows dynamically |
| Error | `OutOfMemoryError: PermGen space` | Much rarer |

> 💡 **Pause Time** = duration the app is paused during GC. Lower = better responsiveness.

---

## 26. Custom Scope in Spring

```java
// 1. Implement Scope interface
public class MyCustomScope implements Scope {
    private final Map<String, Object> scopedObjects = new HashMap<>();

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        return scopedObjects.computeIfAbsent(name, k -> objectFactory.getObject());
    }

    @Override
    public Object remove(String name) {
        return scopedObjects.remove(name);
    }
    // ... other methods
}

// 2. Register it
@Configuration
public class ScopeConfig {
    @Bean
    public static CustomScopeConfigurer customScopeConfigurer() {
        CustomScopeConfigurer configurer = new CustomScopeConfigurer();
        configurer.setScopes(Map.of("myScope", new MyCustomScope()));
        return configurer;
    }
}

// 3. Use it
@Component
@Scope("myScope")
public class CustomScopedBean { ... }
```

---

## 27. ArrayList vs LinkedList — Element Access

```java
ArrayList<Integer> arrayList = new ArrayList<>(List.of(10, 20, 30, 40, 50));
Integer third = arrayList.get(2); // O(1) — direct memory access ✅

LinkedList<Integer> linkedList = new LinkedList<>(List.of(10, 20, 30, 40, 50));
Integer third = linkedList.get(2); // O(n) — traverses from head ❌
```

| | ArrayList | LinkedList |
|--|-----------|------------|
| Random access | **O(1)** ✅ | O(n) ❌ |
| Insert/delete (middle) | O(n) | **O(1)** ✅ |
| Insert/delete (end) | O(1) amortized | O(1) |
| Memory | Less (contiguous array) | More (node + pointers) |

> 💡 **Rule of thumb:** Prefer `ArrayList` for reads; `LinkedList` for frequent insert/delete at arbitrary positions.

---

## 28. finally Block Return Value

```java
public static String call() {
    try {
        return "one";
    } catch (Exception e) {
        return "two";
    } finally {
        return "three"; // Always wins!
    }
}
```

| Scenario | Return Value |
|----------|-------------|
| No exception | `"three"` |
| Exception thrown | `"three"` |

> ⚠️ **`finally` always overrides** `try` and `catch` return values. Avoid returning from `finally` in production code.

---

## 29. RestTemplate Methods

| HTTP Verb | Method | Description |
|-----------|--------|-------------|
| GET | `getForObject()` | Returns response body as object |
| GET | `getForEntity()` | Returns full `ResponseEntity` |
| POST | `postForObject()` | Sends body, returns response |
| POST | `postForEntity()` | Sends body, returns `ResponseEntity` |
| POST | `postForLocation()` | Returns URI of new resource |
| PUT | `put()` | Updates resource (no return) |
| DELETE | `delete()` | Deletes resource |
| ANY | `exchange()` | Full control — method, headers, body |
| ANY | `execute()` | Lower-level customization |
| UTILS | `headForHeaders()` | Gets only headers |
| UTILS | `optionsForAllow()` | Gets allowed HTTP methods |

### exchange() Example

```java
HttpHeaders headers = new HttpHeaders();
headers.set("Accept", "application/json");
HttpEntity<String> entity = new HttpEntity<>(headers);

ResponseEntity<Post> response = restTemplate.exchange(
    "https://api.example.com/posts/1",
    HttpMethod.GET,
    entity,
    Post.class
);
System.out.println(response.getStatusCode());
System.out.println(response.getBody().getTitle());
```

---

## 30. volatile vs Atomic

> Every thread normally **caches variables locally** for performance.

### volatile
- Ensures reads/writes go to **main memory** (visibility)
- Does **NOT** guarantee atomicity (e.g., `count++` is not atomic)

### Atomic Classes (AtomicInteger, etc.)
- From `java.util.concurrent.atomic`
- Guarantees **both visibility AND atomicity**
- Uses **CAS (Compare-And-Swap)** under the hood

```java
static volatile int volatileCounter = 0;
static AtomicInteger atomicCounter = new AtomicInteger(0);

// In 10 threads, each incrementing 1000 times:
volatileCounter++;              // ❌ Not atomic → ~8390
atomicCounter.incrementAndGet(); // ✅ Atomic   → 10000
```

| | `volatile` | `AtomicInteger` |
|--|-----------|-----------------|
| Visibility | ✅ | ✅ |
| Atomicity | ❌ | ✅ |
| Use case | Flags, on/off switches | Counters, accumulators |
| CAS | ❌ | ✅ |

---

## 31. == vs equals()

| | `==` | `.equals()` |
|--|------|------------|
| Compares | **Reference** (memory address) | **Content** (value) |
| Primitives | Value comparison | N/A |
| Objects | Same instance? | Same data? (if overridden) |

```java
String a = new String("hello");
String b = new String("hello");

a == b        // false — different objects
a.equals(b)   // true  — same content
```

---

## 32. Jenkins + Kubernetes Flow

```
Developer pushes code to GitHub
        ↓
Jenkins Pipeline triggers:
  1. mvn clean install
  2. Run tests
  3. Build Docker image
  4. Push image to registry
        ↓
Jenkins: kubectl apply / Helm upgrade
        ↓
Kubernetes:
  - Pulls latest image
  - Rolling update (replaces old pods)
  - Load balancing, scaling, self-healing
```

| Jenkins | Kubernetes |
|---------|------------|
| Automates build & deploy | Automates container runtime |
| Triggers deployments | Executes and scales deployments |
| Focus: delivery pipeline | Focus: running at scale |

---

## 33. RabbitMQ Implementation

### 1. Dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

### 2. Configuration

```java
@Configuration
public class RabbitMQConfig {
    public static final String QUEUE = "my-queue";
    public static final String EXCHANGE = "my-exchange";
    public static final String ROUTING_KEY = "my-routing-key";

    @Bean public Queue queue() { return new Queue(QUEUE, true); }
    @Bean public TopicExchange exchange() { return new TopicExchange(EXCHANGE); }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }
}
```

### 3. Producer

```java
@Service
public class MessageProducer {
    @Autowired private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, message);
    }
}
```

### 4. Consumer

```java
@Component
public class MessageConsumer {
    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void receiveMessage(String message) {
        System.out.println("Received: " + message);
    }
}
```

---

## 34. @Component with Private Constructor

> ❌ **No**, Spring will **NOT** create a bean for a class with a private constructor, even with `@Component`.

Spring uses reflection to instantiate beans — it needs an accessible constructor. A private constructor blocks that.

> ✅ To enforce singleton and still use Spring, rely on the default **singleton scope** and a public/package-private constructor.

---

## 35. Skip Fields in Serialization Without transient

### Option 1: Custom writeObject / readObject

```java
private void writeObject(ObjectOutputStream oos) throws IOException {
    oos.defaultWriteObject(); // serialize default fields
    // skip sensitive fields by not writing them
}

private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    ois.defaultReadObject();
}
```

### Option 2: Implement Externalizable

```java
public class User implements Externalizable {
    private String name;
    private String password; // skip this

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(name); // only write name
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        name = (String) in.readObject();
    }
}
```

---

## 36. JPA Inheritance Strategies

| Strategy | Table Structure | Best For |
|----------|----------------|----------|
| `SINGLE_TABLE` | One table, all subclasses | Performance, shared fields |
| `JOINED` | Parent + child in separate tables | Normalized schema |
| `TABLE_PER_CLASS` | Each class = own table | Independent subclasses |

```java
// TABLE_PER_CLASS example
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
class Payment { protected double amount; }

@Entity
class CreditCardPayment extends Payment { private String cardNumber; }

@Entity
class CashPayment extends Payment { private String currency; }
```

---

## 37. SQL — Experience & Join Date Query

```sql
-- Employees with more than 3 years of experience
SELECT *
FROM employee
WHERE DATEDIFF(SYSDATE(), join_date) > (365 * 3);
```

### Date-Only Extraction

```sql
-- MySQL
SELECT DATE(join_date) AS only_date FROM employee;

-- Oracle
SELECT TRUNC(join_date) AS only_date FROM employee;
```

---

## 38. Pagination in REST Endpoint

### Controller

```java
@GetMapping("/getAllEmployees")
public ResponseEntity<?> getAllEmployees(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

    Page<Employee> result = service.getAllEmployees(PageRequest.of(page, size));
    return result.hasContent()
        ? ResponseEntity.ok(result)
        : ResponseEntity.status(HttpStatus.NO_CONTENT).build();
}
```

### Service

```java
public Page<Employee> getAllEmployees(Pageable pageable) {
    return employeeRepository.findAll(pageable);
}
```

### Sample Request

```
GET /getAllEmployees?page=1&size=5
```

Returns page 2 (0-indexed) with 5 records per page.

> ✅ Repository must extend `JpaRepository` or `PagingAndSortingRepository`.

---

## 39. Maven Overview

### What Maven Does
- Manages **dependencies** via `pom.xml`
- Downloads from **Maven Central** and caches in `~/.m2/repository`
- Standardizes build lifecycle

### Common Commands

| Command | Action |
|---------|--------|
| `mvn compile` | Compiles source code |
| `mvn test` | Runs unit tests |
| `mvn package` | Creates JAR/WAR |
| `mvn install` | Installs artifact to local repo |
| `mvn clean install` | Clean build + install |
| `mvn spring-boot:run` | Runs Spring Boot app directly |

---

## 📌 Quick Reference Cheat Sheet

| Topic | Key Takeaway |
|-------|-------------|
| Startup Optimization | `lazy-initialization=true`, exclude unused configs |
| Memory Leaks | Avoid static collections, clear maps/lists after use |
| Microservice Failures | Resilience4j: CircuitBreaker + Retry + Fallback |
| Idempotency | GET/PUT/DELETE = idempotent; POST = not |
| finally return | Always overrides try/catch returns |
| volatile | Visibility only — NOT atomic |
| AtomicInteger | Visibility + atomicity via CAS |
| Sealed Classes | Restrict subclassing; great for pattern matching |
| CompletableFuture | Prefer over Future for chaining, timeout, fallback |
| G1GC / Metaspace | Java 8+ GC improvements — lower pauses, no PermGen |

---

*Notes compiled from Java & Spring Boot interview preparation. Last updated: 2025.*
