# Microservices Architecture - Complete Interview Guide (Q26-37)

## Table of Contents
1. [Microservices vs Monolithic Architecture](#q26-microservices-vs-monolithic-architecture)
2. [Key Principles of Microservices Design](#q27-key-principles-of-microservices-design)
3. [Inter-Service Communication](#q28-inter-service-communication)
4. [Service Discovery Patterns](#q29-service-discovery-patterns)
5. [API Gateway Pattern](#q30-api-gateway-pattern)
6. [Distributed Transactions](#q31-distributed-transactions)
7. [Circuit Breaker Pattern](#q32-circuit-breaker-pattern)
8. [Saga Pattern](#q33-saga-pattern)
9. [Data Consistency Across Microservices](#q34-data-consistency)
10. [12-Factor App Methodology](#q35-12-factor-app-methodology)
11. [API Versioning](#q36-api-versioning)
12. [Deployment Strategies](#q37-deployment-strategies)

---

## Q26: Microservices vs Monolithic Architecture

### Monolithic Architecture:

```
┌─────────────────────────────────────────────┐
│         MONOLITHIC APPLICATION              │
│                                             │
│  ┌──────────────────────────────────────┐  │
│  │         User Interface               │  │
│  └──────────────────────────────────────┘  │
│                                             │
│  ┌──────────────────────────────────────┐  │
│  │         Business Logic               │  │
│  │  - User Management                   │  │
│  │  - Order Processing                  │  │
│  │  - Payment Processing                │  │
│  │  - Inventory Management              │  │
│  │  - Notification Service              │  │
│  └──────────────────────────────────────┘  │
│                                             │
│  ┌──────────────────────────────────────┐  │
│  │         Data Access Layer            │  │
│  └──────────────────────────────────────┘  │
│                                             │
│  ┌──────────────────────────────────────┐  │
│  │       Single Database                │  │
│  └──────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
     Single deployment unit
     Shared memory & resources
```

### Microservices Architecture:

```
                    ┌─────────────┐
                    │API Gateway  │
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ┌────▼────┐      ┌─────▼──┐        ┌─────▼────┐
   │  User   │      │ Order  │        │ Payment  │
   │ Service │      │Service │        │ Service  │
   └────┬────┘      └────┬───┘        └────┬─────┘
        │                │                  │
   ┌────▼────┐      ┌────▼───┐        ┌────▼─────┐
   │ User DB │      │Order DB│        │Payment DB│
   └─────────┘      └────────┘        └──────────┘
   
   Each service:
   - Independent deployment
   - Own database
   - Different tech stack possible
   - Scales independently
```

### Detailed Comparison:

| Aspect | Monolithic | Microservices |
|--------|-----------|---------------|
| **Architecture** | Single unified unit | Multiple independent services |
| **Deployment** | Deploy entire app | Deploy services independently |
| **Scaling** | Scale entire application | Scale individual services |
| **Technology** | Single tech stack | Polyglot (multiple technologies) |
| **Database** | Single shared database | Database per service |
| **Development** | Simple initially | Complex coordination |
| **Testing** | Easier (everything together) | Harder (integration testing) |
| **Debugging** | Easier (single codebase) | Harder (distributed tracing) |
| **Performance** | Fast (in-process calls) | Network overhead |
| **Reliability** | Single point of failure | Fault isolation |
| **Team Structure** | Larger unified team | Small autonomous teams |
| **Deployment Time** | Slower (redeploy all) | Faster (deploy changed service) |

### When to Use Monolithic:

```
✅ Good for:
- Small applications
- Simple domains
- Small teams (< 10 people)
- Startups/MVPs
- Limited complexity
- Tight deadlines

Example: Blog platform, Small e-commerce site
```

### When to Use Microservices:

```
✅ Good for:
- Large, complex applications
- Multiple teams
- Need for independent scaling
- Polyglot requirements
- Frequent deployments
- Need for resilience

Example: Netflix, Amazon, Uber
```

### Code Example - Monolithic:

```java
// Monolithic Spring Boot Application
@SpringBootApplication
public class MonolithicEcommerceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MonolithicEcommerceApplication.class, args);
    }
}

// All services in one application
@RestController
@RequestMapping("/api")
public class EcommerceController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private InventoryService inventoryService;
    
    @PostMapping("/orders")
    public Order createOrder(@RequestBody OrderRequest request) {
        // All in-process calls (fast but tightly coupled)
        User user = userService.getUser(request.getUserId());
        inventoryService.reserveItems(request.getItems());
        Payment payment = paymentService.processPayment(request);
        return orderService.createOrder(request);
    }
}

// Single database
@Entity
public class Order {
    @Id
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;  // Direct foreign key relationship
    
    // All data in same database
}
```

### Code Example - Microservices:

```java
// 1. User Service (separate application)
@SpringBootApplication
@EnableEurekaClient
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }
}

// 2. Order Service (separate application)
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @Autowired
    private UserServiceClient userClient;  // Feign client
    
    @Autowired
    private PaymentServiceClient paymentClient;
    
    @PostMapping
    public Order createOrder(@RequestBody OrderRequest request) {
        // Network calls to other services
        User user = userClient.getUser(request.getUserId());
        Payment payment = paymentClient.processPayment(request);
        // Create order
        return orderService.createOrder(request);
    }
}

// Feign client for inter-service communication
@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/api/users/{id}")
    User getUser(@PathVariable Long id);
}

// Each service has its own database
@Entity
public class Order {
    @Id
    private Long id;
    
    private Long userId;  // No foreign key - stores only ID
    // User data fetched via API call
}
```

### Migration Path: Monolith to Microservices

```
Step 1: Identify Bounded Contexts
┌─────────────────────────────┐
│      MONOLITH               │
│  ┌─────┐ ┌─────┐ ┌─────┐   │
│  │Users│ │Order│ │Pay  │   │
│  └─────┘ └─────┘ └─────┘   │
└─────────────────────────────┘

Step 2: Extract One Service (Strangler Pattern)
┌──────────────────┐    ┌─────────┐
│   MONOLITH       │───▶│Payment  │
│  ┌─────┐ ┌─────┐│    │Service  │
│  │Users│ │Order││    └─────────┘
│  └─────┘ └─────┘│
└──────────────────┘

Step 3: Continue Extraction
┌──────────┐  ┌──────┐  ┌─────────┐
│  User    │  │Order │  │Payment  │
│ Service  │  │Service│ │Service  │
└──────────┘  └──────┘  └─────────┘
```

---

## Q27: Key Principles of Microservices Design

### 1. Single Responsibility Principle

```
Each microservice should do ONE thing and do it well

❌ Bad:
┌──────────────────────────┐
│  E-commerce Service      │
│  - User Management       │
│  - Order Processing      │
│  - Payment Processing    │
│  - Inventory Management  │
└──────────────────────────┘

✅ Good:
┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐
│   User   │  │  Order   │  │ Payment  │  │Inventory │
│ Service  │  │ Service  │  │ Service  │  │ Service  │
└──────────┘  └──────────┘  └──────────┘  └──────────┘
```

### 2. Autonomy & Independence

```java
// Each service is autonomous
@SpringBootApplication
public class OrderService {
    // Own database
    @Autowired
    private OrderRepository orderRepository;
    
    // Own business logic
    @Service
    public class OrderBusinessLogic {
        // Independent decision making
    }
    
    // Own deployment
    // Own scaling
    // Own technology choices
}
```

### 3. Domain-Driven Design (DDD)

```
Bounded Contexts:

E-commerce System:
┌─────────────────────────────────────────────────┐
│  User Context          Order Context            │
│  ┌──────────┐         ┌──────────┐             │
│  │  User    │         │  Order   │             │
│  │- id      │         │- orderId │             │
│  │- name    │         │- userId  │ ← Reference │
│  │- email   │         │- items   │             │
│  └──────────┘         └──────────┘             │
│                                                 │
│  Payment Context       Inventory Context       │
│  ┌──────────┐         ┌──────────┐             │
│  │ Payment  │         │Inventory │             │
│  │- paymentId│        │- sku     │             │
│  │- orderId │         │- quantity│             │
│  └──────────┘         └──────────┘             │
└─────────────────────────────────────────────────┘
```

### 4. Decentralized Data Management

```java
// ❌ WRONG: Shared Database
@Entity
public class Order {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;  // Direct join across services
}

// ✅ CORRECT: Database per Service
@Entity
public class Order {
    private Long userId;  // Store only reference
    
    // Fetch user data via API when needed
    public User getUser() {
        return userServiceClient.getUser(userId);
    }
}

// Each service owns its data
// Order Service → Order Database
// User Service → User Database
// Payment Service → Payment Database
```

### 5. Smart Endpoints, Dumb Pipes

```
Traditional ESB (Enterprise Service Bus):
Services ──▶ ┌─────────────────┐ ──▶ Services
             │  Smart ESB      │
             │- Transformation │
             │- Routing Logic  │
             │- Orchestration  │
             └─────────────────┘

Microservices Approach:
Service A ──REST/gRPC──▶ Service B
     ▲                        │
     │                        ▼
 (Smart)              (Simple Protocol)
- Business Logic      - HTTP/gRPC
- Data Transformation - Message Queue
- Validation
```

```java
// Smart endpoints
@RestController
public class OrderController {
    
    @PostMapping("/orders")
    public Order createOrder(@RequestBody OrderRequest request) {
        // Service contains all logic
        validateRequest(request);
        transformData(request);
        Order order = processOrder(request);
        publishEvent(order);
        return order;
    }
}

// Dumb pipe - just HTTP
RestTemplate restTemplate = new RestTemplate();
User user = restTemplate.getForObject(
    "http://user-service/users/{id}",
    User.class,
    userId
);
```

### 6. Design for Failure

```java
// Implement resilience patterns
@Service
public class OrderService {
    
    @Autowired
    private UserServiceClient userClient;
    
    // Circuit Breaker
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    public User getUser(Long id) {
        return userClient.getUser(id);
    }
    
    // Fallback method
    public User getUserFallback(Long id, Exception e) {
        log.error("User service unavailable, using cached data", e);
        return getCachedUser(id);
    }
    
    // Retry
    @Retry(name = "userService", maxAttempts = 3)
    public User getUserWithRetry(Long id) {
        return userClient.getUser(id);
    }
    
    // Timeout
    @TimeLimiter(name = "userService")
    public CompletableFuture<User> getUserWithTimeout(Long id) {
        return CompletableFuture.supplyAsync(() -> userClient.getUser(id));
    }
}
```

### 7. Infrastructure Automation

```yaml
# Docker Compose for local development
version: '3.8'
services:
  user-service:
    build: ./user-service
    ports:
      - "8081:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      
  order-service:
    build: ./order-service
    ports:
      - "8082:8080"
    depends_on:
      - user-service
      
  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    depends_on:
      - user-service
      - order-service
```

```yaml
# Kubernetes deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: order-service
  template:
    metadata:
      labels:
        app: order-service
    spec:
      containers:
      - name: order-service
        image: order-service:1.0
        ports:
        - containerPort: 8080
```

### 8. Decentralized Governance

```
Each team owns their service completely:

Team A:          Team B:          Team C:
┌──────────┐    ┌──────────┐    ┌──────────┐
│  User    │    │  Order   │    │ Payment  │
│ Service  │    │ Service  │    │ Service  │
│          │    │          │    │          │
│ - Java   │    │ - Node.js│    │ - Python │
│ - MySQL  │    │ - MongoDB│    │ - Postgres│
│ - Docker │    │ - Docker │    │ - Docker │
└──────────┘    └──────────┘    └──────────┘

Teams choose:
- Programming language
- Database
- Deployment strategy
- Testing approach
```

---

## Q28: Inter-Service Communication

### Communication Patterns:

```
1. Synchronous (Request-Response)
   Client ──request──▶ Service ──response──▶ Client
   - REST (HTTP)
   - gRPC
   - GraphQL

2. Asynchronous (Event-Driven)
   Service A ──event──▶ Message Broker ──event──▶ Service B
   - Message Queues (RabbitMQ)
   - Event Streaming (Kafka)
   - Pub/Sub (Redis, SNS/SQS)
```

### 1. REST Communication

```java
// Using RestTemplate (older approach)
@Service
public class OrderService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public User getUser(Long userId) {
        String url = "http://user-service/api/users/" + userId;
        return restTemplate.getForObject(url, User.class);
    }
    
    public Payment processPayment(PaymentRequest request) {
        String url = "http://payment-service/api/payments";
        return restTemplate.postForObject(url, request, Payment.class);
    }
}

// Configuration
@Configuration
public class RestTemplateConfig {
    
    @Bean
    @LoadBalanced  // Client-side load balancing
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

```java
// Using WebClient (modern, reactive)
@Service
public class OrderService {
    
    @Autowired
    private WebClient.Builder webClientBuilder;
    
    public Mono<User> getUser(Long userId) {
        return webClientBuilder.build()
            .get()
            .uri("http://user-service/api/users/{id}", userId)
            .retrieve()
            .bodyToMono(User.class);
    }
    
    public Mono<Payment> processPayment(PaymentRequest request) {
        return webClientBuilder.build()
            .post()
            .uri("http://payment-service/api/payments")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Payment.class);
    }
}
```

```java
// Using OpenFeign (declarative)
@FeignClient(name = "user-service")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{id}")
    User getUser(@PathVariable("id") Long id);
    
    @PostMapping("/api/users")
    User createUser(@RequestBody UserRequest request);
}

@FeignClient(name = "payment-service")
public interface PaymentServiceClient {
    
    @PostMapping("/api/payments")
    Payment processPayment(@RequestBody PaymentRequest request);
}

// Usage
@Service
public class OrderService {
    
    @Autowired
    private UserServiceClient userClient;
    
    @Autowired
    private PaymentServiceClient paymentClient;
    
    public Order createOrder(OrderRequest request) {
        User user = userClient.getUser(request.getUserId());
        Payment payment = paymentClient.processPayment(
            new PaymentRequest(request)
        );
        // Create order
        return saveOrder(request, user, payment);
    }
}
```

### 2. gRPC Communication

```protobuf
// user.proto
syntax = "proto3";

package user;

service UserService {
    rpc GetUser(GetUserRequest) returns (UserResponse);
    rpc CreateUser(CreateUserRequest) returns (UserResponse);
}

message GetUserRequest {
    int64 id = 1;
}

message UserResponse {
    int64 id = 1;
    string name = 2;
    string email = 3;
}
```

```java
// Server side
@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {
    
    @Autowired
    private UserService userService;
    
    @Override
    public void getUser(GetUserRequest request, 
                       StreamObserver<UserResponse> responseObserver) {
        User user = userService.getUser(request.getId());
        
        UserResponse response = UserResponse.newBuilder()
            .setId(user.getId())
            .setName(user.getName())
            .setEmail(user.getEmail())
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

// Client side
@Service
public class OrderService {
    
    @GrpcClient("user-service")
    private UserServiceBlockingStub userServiceStub;
    
    public User getUser(Long userId) {
        GetUserRequest request = GetUserRequest.newBuilder()
            .setId(userId)
            .build();
        
        UserResponse response = userServiceStub.getUser(request);
        
        return new User(
            response.getId(),
            response.getName(),
            response.getEmail()
        );
    }
}
```

### 3. Message Queue Communication (RabbitMQ)

```java
// Producer (Order Service)
@Service
public class OrderService {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public Order createOrder(OrderRequest request) {
        Order order = saveOrder(request);
        
        // Publish event asynchronously
        OrderCreatedEvent event = new OrderCreatedEvent(
            order.getId(),
            order.getUserId(),
            order.getTotalAmount()
        );
        
        rabbitTemplate.convertAndSend(
            "order-exchange",
            "order.created",
            event
        );
        
        return order;
    }
}

// Consumer (Notification Service)
@Service
public class NotificationService {
    
    @RabbitListener(queues = "notification-queue")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Send email notification
        emailService.sendOrderConfirmation(
            event.getUserId(),
            event.getOrderId()
        );
    }
}

// Consumer (Inventory Service)
@Service
public class InventoryService {
    
    @RabbitListener(queues = "inventory-queue")
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Update inventory
        inventoryRepository.reduceStock(event.getOrderId());
    }
}

// Configuration
@Configuration
public class RabbitMQConfig {
    
    @Bean
    public Queue notificationQueue() {
        return new Queue("notification-queue");
    }
    
    @Bean
    public Queue inventoryQueue() {
        return new Queue("inventory-queue");
    }
    
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange("order-exchange");
    }
    
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
            .bind(notificationQueue())
            .to(orderExchange())
            .with("order.created");
    }
    
    @Bean
    public Binding inventoryBinding() {
        return BindingBuilder
            .bind(inventoryQueue())
            .to(orderExchange())
            .with("order.created");
    }
}
```

### 4. Event Streaming with Kafka

```java
// Producer
@Service
public class OrderService {
    
    @Autowired
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    
    public Order createOrder(OrderRequest request) {
        Order order = saveOrder(request);
        
        OrderCreatedEvent event = new OrderCreatedEvent(order);
        
        kafkaTemplate.send("order-events", order.getId().toString(), event);
        
        return order;
    }
}

// Consumer
@Service
public class EmailService {
    
    @KafkaListener(topics = "order-events", groupId = "email-service")
    public void handleOrderCreated(OrderCreatedEvent event) {
        sendOrderConfirmation(event);
    }
}

// Configuration
@Configuration
public class KafkaConfig {
    
    @Bean
    public ProducerFactory<String, OrderCreatedEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }
    
    @Bean
    public KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

### Comparison: Sync vs Async

| Aspect | Synchronous (REST/gRPC) | Asynchronous (Messaging) |
|--------|-------------------------|--------------------------|
| **Response** | Immediate | Eventual |
| **Coupling** | Tight | Loose |
| **Availability** | Both services must be up | Services can be down temporarily |
| **Use Case** | Query operations | Event notifications |
| **Examples** | Get user details | Order placed notification |
| **Latency** | Low | Higher |
| **Complexity** | Simple | More complex |

---

## Q29: Service Discovery Patterns

### The Problem:

```
Without Service Discovery:
Order Service needs to call User Service

Where is User Service?
- IP: 192.168.1.10:8080 (hardcoded) ❌
- What if it moves?
- What if there are multiple instances?
- What if an instance fails?
```

### Service Discovery Pattern:

```
┌──────────────┐
│Order Service │
└──────┬───────┘
       │ 1. Where is user-service?
       ▼
┌─────────────────┐
│Service Registry │ ← 2. Returns: 192.168.1.10:8080
│  (Eureka)       │              192.168.1.11:8080
└─────────▲───────┘
          │
          │ 3. Register/Heartbeat
          │
    ┌─────┴──────┐
    │User Service│
    │ Instances  │
    └────────────┘
```

### Types of Service Discovery:

#### 1. Client-Side Discovery (Netflix Eureka)

```
┌──────────────┐
│Order Service │
└──────┬───────┘
       │ 1. Get service locations
       │
┌──────▼───────┐
│   Eureka     │
│   Server     │
└──────────────┘
       ▲
       │ 2. Register
       │
┌──────┴───────┐
│User Service  │
└──────────────┘

Order Service:
- Queries Eureka for user-service locations
- Client chooses which instance to call
- Client-side load balancing (Ribbon)
```

#### 2. Server-Side Discovery (Consul, Kubernetes)

```
┌──────────────┐
│Order Service │
└──────┬───────┘
       │ 1. Call load balancer
       │
┌──────▼────────┐
│Load Balancer  │
│  + Service    │
│   Discovery   │
└──────┬────────┘
       │ 2. Query & route
       │
┌──────▼────────┐
│ User Service  │
│  Instances    │
└───────────────┘

Load Balancer:
- Queries service registry
- Routes to healthy instance
- Server-side load balancing
```

### Netflix Eureka Implementation:

```java
// 1. Eureka Server
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

```yaml
# application.yml (Eureka Server)
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
  server:
    enable-self-preservation: false
```

```java
// 2. Service Registration (User Service)
@SpringBootApplication
@EnableEurekaClient
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

```yaml
# application.yml (User Service)
server:
  port: 8081

spring:
  application:
    name: user-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
```

```java
// 3. Service Discovery (Order Service)
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}

// Feign client automatically discovers user-service
@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/api/users/{id}")
    User getUser(@PathVariable Long id);
}

// Or using RestTemplate with @LoadBalanced
@Configuration
public class RestConfig {
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

@Service
public class OrderService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public User getUser(Long id) {
        // Service name instead of URL
        return restTemplate.getForObject(
            "http://user-service/api/users/" + id,
            User.class
        );
    }
}
```

### Consul Implementation:

```java
// 1. Add dependency
// pom.xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-consul-discovery</artifactId>
</dependency>
```

```yaml
# application.yml
spring:
  application:
    name: user-service
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        enabled: true
        register: true
        health-check-path: /actuator/health
        health-check-interval: 10s
        instance-id: ${spring.application.name}:${random.value}
```

```java
// Service automatically registers with Consul
@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

### Kubernetes Service Discovery:

```yaml
# Kubernetes automatically provides service discovery
apiVersion: v1
kind: Service
metadata:
  name: user-service
spec:
  selector:
    app: user-service
  ports:
  - port: 80
    targetPort: 8080
  type: ClusterIP

---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
      - name: user-service
        image: user-service:1.0
        ports:
        - containerPort: 8080
```

```java
// In Kubernetes, just use service name
@Service
public class OrderService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public User getUser(Long id) {
        // Kubernetes DNS resolves user-service
        return restTemplate.getForObject(
            "http://user-service/api/users/" + id,
            User.class
        );
    }
}
```

### Health Checks:

```java
// Spring Boot Actuator
@SpringBootApplication
public class UserServiceApplication {
    // Actuator automatically exposes /actuator/health
}

// Custom health indicator
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        try {
            Connection connection = dataSource.getConnection();
            connection.close();
            return Health.up().build();
        } catch (Exception e) {
            return Health.down().withException(e).build();
        }
    }
}
```

---

## Q30: API Gateway Pattern

### Architecture:

```
Without API Gateway (Client calls services directly):
┌────────┐
│ Client │
└───┬────┘
    ├──────────▶ User Service (auth?)
    ├──────────▶ Order Service (auth?)
    ├──────────▶ Payment Service (auth?)
    └──────────▶ Product Service (auth?)
    
Problems:
- Client needs to know all service endpoints
- Cross-cutting concerns duplicated
- Multiple network calls
- Different protocols

With API Gateway:
┌────────┐
│ Client │
└───┬────┘
    │
    ▼
┌─────────────────────┐
│   API Gateway       │
│ - Authentication    │
│ - Rate Limiting     │
│ - Load Balancing    │
│ - Caching           │
│ - Request Routing   │
│ - Protocol Translation │
└────────┬────────────┘
         │
    ┌────┼────┬──────┐
    ▼    ▼    ▼      ▼
  User Order Pay  Product
Service Service Service Service
```

### Benefits:

```
✅ Single entry point for clients
✅ Centralized cross-cutting concerns
✅ Protocol translation (REST to gRPC)
✅ Request aggregation (combine multiple service calls)
✅ Authentication & authorization
✅ Rate limiting & throttling
✅ Caching
✅ Load balancing
✅ Circuit breaking
✅ Request/response transformation
✅ API versioning
```

### Spring Cloud Gateway Implementation:

```java
// 1. Gateway Application
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

```yaml
# application.yml - Route Configuration
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        # User Service Routes
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - RewritePath=/api/users/(?<segment>.*), /${segment}
            
        # Order Service Routes
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - RewritePath=/api/orders/(?<segment>.*), /${segment}
            - AddRequestHeader=X-Request-Source, API-Gateway
            
        # Payment Service Routes
        - id: payment-service
          uri: lb://payment-service
          predicates:
            - Path=/api/payments/**
          filters:
            - name: CircuitBreaker
              args:
                name: paymentCircuitBreaker
                fallbackUri: forward:/fallback/payment

server:
  port: 8080

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Custom Filters:

```java
// Global Filter - Applied to all routes
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Check for auth token
        if (!request.getHeaders().containsKey("Authorization")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        String token = request.getHeaders().getFirst("Authorization");
        
        if (!validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }
        
        // Add user info to request
        ServerHttpRequest modifiedRequest = request.mutate()
            .header("X-User-Id", extractUserId(token))
            .build();
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
    
    @Override
    public int getOrder() {
        return -1;  // High priority
    }
    
    private boolean validateToken(String token) {
        // JWT validation logic
        return true;
    }
    
    private String extractUserId(String token) {
        // Extract user ID from JWT
        return "user123";
    }
}

// Request Logging Filter
@Component
@Slf4j
public class LoggingFilter implements GlobalFilter, Ordered {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Request: {} {}", 
            exchange.getRequest().getMethod(),
            exchange.getRequest().getURI()
        );
        
        long startTime = System.currentTimeMillis();
        
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            log.info("Response: {} - Duration: {}ms",
                exchange.getResponse().getStatusCode(),
                duration
            );
        }));
    }
    
    @Override
    public int getOrder() {
        return 0;
    }
}

// Rate Limiting Filter
@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {
    
    private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientId = getClientId(exchange.getRequest());
        
        RateLimiter rateLimiter = rateLimiters.computeIfAbsent(
            clientId,
            k -> RateLimiter.create(100.0)  // 100 requests per second
        );
        
        if (!rateLimiter.tryAcquire()) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }
        
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
    
    private String getClientId(ServerHttpRequest request) {
        return request.getHeaders().getFirst("X-Client-Id");
    }
}
```

### Request Aggregation (BFF Pattern):

```java
// Backend for Frontend (BFF) - Aggregate multiple service calls
@RestController
@RequestMapping("/api/bff")
public class BFFController {
    
    @Autowired
    private WebClient.Builder webClientBuilder;
    
    @GetMapping("/user-dashboard/{userId}")
    public Mono<UserDashboard> getUserDashboard(@PathVariable Long userId) {
        // Parallel calls to multiple services
        Mono<User> userMono = getUserInfo(userId);
        Mono<List<Order>> ordersMono = getUserOrders(userId);
        Mono<PaymentInfo> paymentMono = getPaymentInfo(userId);
        
        // Combine results
        return Mono.zip(userMono, ordersMono, paymentMono)
            .map(tuple -> new UserDashboard(
                tuple.getT1(),  // User
                tuple.getT2(),  // Orders
                tuple.getT3()   // Payment Info
            ));
    }
    
    private Mono<User> getUserInfo(Long userId) {
        return webClientBuilder.build()
            .get()
            .uri("http://user-service/api/users/{id}", userId)
            .retrieve()
            .bodyToMono(User.class);
    }
    
    private Mono<List<Order>> getUserOrders(Long userId) {
        return webClientBuilder.build()
            .get()
            .uri("http://order-service/api/orders?userId={id}", userId)
            .retrieve()
            .bodyToFlux(Order.class)
            .collectList();
    }
    
    private Mono<PaymentInfo> getPaymentInfo(Long userId) {
        return webClientBuilder.build()
            .get()
            .uri("http://payment-service/api/payments/user/{id}", userId)
            .retrieve()
            .bodyToMono(PaymentInfo.class);
    }
}
```

### Zuul vs Spring Cloud Gateway:

| Feature | Zuul 1.x | Zuul 2.x | Spring Cloud Gateway |
|---------|----------|----------|----------------------|
| **Model** | Blocking I/O | Non-blocking | Non-blocking (Reactive) |
| **Framework** | Servlet | Netty | Spring WebFlux |
| **Performance** | Good | Better | Best |
| **Spring Integration** | Good | Good | Excellent |
| **Status** | Maintenance mode | Active | Recommended |

---

## Q31: Distributed Transactions

### The Problem:

```
Traditional Transaction (ACID):
BEGIN TRANSACTION
    UPDATE accounts SET balance = balance - 100 WHERE id = 1;
    UPDATE accounts SET balance = balance + 100 WHERE id = 2;
COMMIT;

Both succeed or both fail (Atomicity)

Microservices:
Order Service  → Create Order (its own DB)
Payment Service → Process Payment (its own DB)
Inventory Service → Reduce Stock (its own DB)

How to ensure all succeed or all rollback?
```

### Solution Patterns:

#### 1. Two-Phase Commit (2PC) - Not Recommended

```
Phase 1: Prepare
Coordinator → Service A: Can you commit?
Coordinator → Service B: Can you commit?
Coordinator → Service C: Can you commit?

All respond: YES

Phase 2: Commit
Coordinator → Service A: COMMIT
Coordinator → Service B: COMMIT
Coordinator → Service C: COMMIT

Problems:
❌ Blocking protocol
❌ Single point of failure (coordinator)
❌ Not suitable for microservices
❌ Poor performance
```

#### 2. Saga Pattern (Recommended)

See detailed explanation in Q33 below.

#### 3. Eventual Consistency with Events

```java
// Order Service
@Service
public class OrderService {
    
    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;
    
    @Transactional
    public Order createOrder(OrderRequest request) {
        // 1. Create order in local database
        Order order = new Order(request);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);
        
        // 2. Publish event
        OrderCreatedEvent event = new OrderCreatedEvent(order);
        kafkaTemplate.send("order-events", event);
        
        return order;
    }
}

// Payment Service - Listens to events
@Service
public class PaymentService {
    
    @KafkaListener(topics = "order-events")
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            // Process payment
            Payment payment = processPayment(event);
            paymentRepository.save(payment);
            
            // Publish success event
            PaymentCompletedEvent successEvent = 
                new PaymentCompletedEvent(event.getOrderId(), payment.getId());
            kafkaTemplate.send("payment-events", successEvent);
            
        } catch (PaymentException e) {
            // Publish failure event
            PaymentFailedEvent failureEvent = 
                new PaymentFailedEvent(event.getOrderId(), e.getMessage());
            kafkaTemplate.send("payment-events", failureEvent);
        }
    }
}

// Order Service - Listens to payment events
@Service
public class OrderEventHandler {
    
    @KafkaListener(topics = "payment-events")
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        Order order = orderRepository.findById(event.getOrderId());
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }
    
    @KafkaListener(topics = "payment-events")
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        Order order = orderRepository.findById(event.getOrderId());
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
```

#### 4. Outbox Pattern

```java
// Ensures message publication with database transaction
@Entity
public class Outbox {
    @Id
    private String id;
    private String aggregateType;  // "Order"
    private String aggregateId;    // Order ID
    private String eventType;      // "OrderCreated"
    private String payload;        // JSON event data
    private LocalDateTime createdAt;
    private boolean processed;
}

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OutboxRepository outboxRepository;
    
    @Transactional
    public Order createOrder(OrderRequest request) {
        // 1. Save order
        Order order = new Order(request);
        orderRepository.save(order);
        
        // 2. Save event to outbox (same transaction)
        OrderCreatedEvent event = new OrderCreatedEvent(order);
        Outbox outbox = new Outbox();
        outbox.setAggregateType("Order");
        outbox.setAggregateId(order.getId().toString());
        outbox.setEventType("OrderCreated");
        outbox.setPayload(toJson(event));
        outboxRepository.save(outbox);
        
        // Both saved atomically
        return order;
    }
}

// Separate process publishes events from outbox
@Component
public class OutboxPublisher {
    
    @Autowired
    private OutboxRepository outboxRepository;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishEvents() {
        List<Outbox> unpublished = outboxRepository.findByProcessedFalse();
        
        for (Outbox outbox : unpublished) {
            // Publish to Kafka
            kafkaTemplate.send(
                outbox.getAggregateType() + "-events",
                outbox.getAggregateId(),
                outbox.getPayload()
            );
            
            // Mark as processed
            outbox.setProcessed(true);
            outboxRepository.save(outbox);
        }
    }
}
```

---

## Q32: Circuit Breaker Pattern

### The Problem:

```
Service A ──calls──▶ Service B (down/slow)
                         ↓
                    Waits... waits...
                         ↓
                    Times out
                         ↓
              Service A also becomes slow
                         ↓
            Cascading failure across system!
```

### Circuit Breaker States:

```
┌─────────────────────────────────────────────┐
│              CLOSED (Normal)                │
│  ┌────────┐         Success                │
│  │Request │ ────────────────────▶ Service  │
│  └────────┘         (Count failures)       │
└──────────────┬──────────────────────────────┘
               │ Failure threshold reached
               ▼
┌─────────────────────────────────────────────┐
│              OPEN (Failing)                 │
│  ┌────────┐         Fail Fast              │
│  │Request │ ────▶ Return Error Immediately │
│  └────────┘         (Don't call service)   │
└──────────────┬──────────────────────────────┘
               │ After timeout period
               ▼
┌─────────────────────────────────────────────┐
│           HALF-OPEN (Testing)               │
│  ┌────────┐      Limited Requests           │
│  │Request │ ────────────────────▶ Service   │
│  └────────┘      If successful → CLOSED    │
│                  If failed → OPEN           │
└─────────────────────────────────────────────┘
```

### Resilience4j Implementation:

```java
// Add dependency
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot2</artifactId>
</dependency>
```

```yaml
# application.yml
resilience4j:
  circuitbreaker:
    instances:
      userService:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        permitted-number-of-calls-in-half-open-state: 3
        wait-duration-in-open-state: 10s
        failure-rate-threshold: 50
        slow-call-duration-threshold: 2s
        slow-call-rate-threshold: 50
        
      paymentService:
        register-health-indicator: true
        sliding-window-size: 100
        failure-rate-threshold: 60
        wait-duration-in-open-state: 30s
```

```java
@Service
public class OrderService {
    
    @Autowired
    private UserServiceClient userClient;
    
    @Autowired
    private PaymentServiceClient paymentClient;
    
    // Circuit Breaker
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    public User getUser(Long userId) {
        return userClient.getUser(userId);
    }
    
    // Fallback method
    public User getUserFallback(Long userId, Exception e) {
        log.error("User service unavailable, using fallback", e);
        
        // Return cached user or default
        return userCache.get(userId)
            .orElse(User.getDefaultUser());
    }
    
    // With Retry + Circuit Breaker
    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    @Retry(name = "paymentService")
    public Payment processPayment(PaymentRequest request) {
        return paymentClient.processPayment(request);
    }
    
    public Payment paymentFallback(PaymentRequest request, Exception e) {
        log.error("Payment service unavailable", e);
        
        // Queue for later processing
        paymentQueue.add(request);
        
        // Return pending payment
        return Payment.pending(request);
    }
}
```

### Manual Circuit Breaker Implementation:

```java
public class ManualCircuitBreaker {
    
    private enum State {
        CLOSED, OPEN, HALF_OPEN
    }
    
    private State state = State.CLOSED;
    private int failureCount = 0;
    private int successCount = 0;
    private final int failureThreshold = 5;
    private final int successThreshold = 2;
    private final long timeout = 60000;  // 1 minute
    private long lastFailureTime;
    
    public <T> T execute(Supplier<T> operation, Supplier<T> fallback) {
        if (state == State.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime >= timeout) {
                state = State.HALF_OPEN;
                successCount = 0;
            } else {
                return fallback.get();  // Fail fast
            }
        }
        
        try {
            T result = operation.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            return fallback.get();
        }
    }
    
    private synchronized void onSuccess() {
        if (state == State.HALF_OPEN) {
            successCount++;
            if (successCount >= successThreshold) {
                state = State.CLOSED;
                failureCount = 0;
            }
        } else {
            failureCount = 0;
        }
    }
    
    private synchronized void onFailure() {
        failureCount++;
        lastFailureTime = System.currentTimeMillis();
        
        if (failureCount >= failureThreshold) {
            state = State.OPEN;
        }
    }
}

// Usage
@Service
public class OrderService {
    
    private ManualCircuitBreaker userServiceCircuitBreaker = 
        new ManualCircuitBreaker();
    
    public User getUser(Long userId) {
        return userServiceCircuitBreaker.execute(
            () -> userClient.getUser(userId),  // Operation
            () -> getCachedUser(userId)        // Fallback
        );
    }
}
```

### Monitoring Circuit Breakers:

```java
@RestController
@RequestMapping("/actuator")
public class CircuitBreakerController {
    
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    
    @GetMapping("/circuit-breakers")
    public Map<String, Object> getCircuitBreakers() {
        return circuitBreakerRegistry.getAllCircuitBreakers()
            .stream()
            .collect(Collectors.toMap(
                CircuitBreaker::getName,
                cb -> Map.of(
                    "state", cb.getState(),
                    "metrics", cb.getMetrics()
                )
            ));
    }
}
```

---

## Q33: Saga Pattern

### What is Saga?

A saga is a sequence of local transactions where each transaction updates data within a single service. If one transaction fails, saga executes compensating transactions to undo changes.

### Types of Sagas:

#### 1. Choreography-Based Saga (Event-Driven)

```
No central coordinator, services react to events

Order Created ──▶ Reserve Inventory ──▶ Process Payment ──▶ Ship Order
     │                   │                    │                 │
     │                   │                    │                 │
   Fails               Fails               Fails              Done
     │                   │                    │
     ▼                   ▼                    ▼
   Cancel          Release Stock        Refund Payment
```

```java
// Order Service
@Service
public class OrderService {
    
    @Autowired
    private EventPublisher eventPublisher;
    
    @Transactional
    public Order createOrder(OrderRequest request) {
        Order order = new Order(request);
        order.setStatus(OrderStatus.PENDING);
        orderRepository.save(order);
        
        // Publish event
        eventPublisher.publish(new OrderCreatedEvent(order));
        
        return order;
    }
    
    // Listen to inventory events
    @EventHandler
    public void on(InventoryReservedEvent event) {
        Order order = orderRepository.findById(event.getOrderId());
        order.setStatus(OrderStatus.INVENTORY_RESERVED);
        orderRepository.save(order);
    }
    
    @EventHandler
    public void on(InventoryReservationFailedEvent event) {
        Order order = orderRepository.findById(event.getOrderId());
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}

// Inventory Service
@Service
public class InventoryService {
    
    @EventHandler
    @Transactional
    public void on(OrderCreatedEvent event) {
        try {
            reserveStock(event.getItems());
            eventPublisher.publish(new InventoryReservedEvent(event.getOrderId()));
        } catch (InsufficientStockException e) {
            eventPublisher.publish(
                new InventoryReservationFailedEvent(event.getOrderId())
            );
        }
    }
    
    // Compensating transaction
    @EventHandler
    @Transactional
    public void on(PaymentFailedEvent event) {
        releaseStock(event.getOrderId());
        eventPublisher.publish(new InventoryReleasedEvent(event.getOrderId()));
    }
}

// Payment Service
@Service
public class PaymentService {
    
    @EventHandler
    @Transactional
    public void on(InventoryReservedEvent event) {
        try {
            Payment payment = processPayment(event.getOrderId());
            eventPublisher.publish(new PaymentCompletedEvent(event.getOrderId()));
        } catch (PaymentException e) {
            eventPublisher.publish(new PaymentFailedEvent(event.getOrderId()));
        }
    }
}
```

#### 2. Orchestration-Based Saga (Centralized)

```
Saga Orchestrator controls the flow

                  ┌──────────────────┐
                  │ Saga Orchestrator│
                  └─────────┬────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
        ▼                   ▼                   ▼
  ┌─────────┐         ┌─────────┐        ┌─────────┐
  │ Inventory│         │ Payment │        │Shipping │
  │ Service  │         │ Service │        │ Service │
  └─────────┘         └─────────┘        └─────────┘
```

```java
// Saga Orchestrator
@Service
public class OrderSagaOrchestrator {
    
    @Autowired
    private InventoryServiceClient inventoryClient;
    
    @Autowired
    private PaymentServiceClient paymentClient;
    
    @Autowired
    private ShippingServiceClient shippingClient;
    
    @Autowired
    private OrderRepository orderRepository;
    
    public void executeOrderSaga(OrderRequest request) {
        Order order = null;
        InventoryReservation reservation = null;
        Payment payment = null;
        
        try {
            // Step 1: Create Order
            order = createOrder(request);
            
            // Step 2: Reserve Inventory
            reservation = inventoryClient.reserve(order.getItems());
            
            // Step 3: Process Payment
            payment = paymentClient.processPayment(order.getTotalAmount());
            
            // Step 4: Create Shipment
            shippingClient.createShipment(order);
            
            // Success - update order
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
            
        } catch (InventoryException e) {
            // Step 1 succeeded, Step 2 failed
            // Compensate: Cancel order
            if (order != null) {
                cancelOrder(order);
            }
            throw new SagaException("Inventory reservation failed", e);
            
        } catch (PaymentException e) {
            // Steps 1-2 succeeded, Step 3 failed
            // Compensate: Release inventory, Cancel order
            if (reservation != null) {
                inventoryClient.release(reservation.getId());
            }
            if (order != null) {
                cancelOrder(order);
            }
            throw new SagaException("Payment failed", e);
            
        } catch (ShippingException e) {
            // Steps 1-3 succeeded, Step 4 failed
            // Compensate: Refund payment, Release inventory, Cancel order
            if (payment != null) {
                paymentClient.refund(payment.getId());
            }
            if (reservation != null) {
                inventoryClient.release(reservation.getId());
            }
            if (order != null) {
                cancelOrder(order);
            }
            throw new SagaException("Shipping failed", e);
        }
    }
    
    private Order createOrder(OrderRequest request) {
        Order order = new Order(request);
        order.setStatus(OrderStatus.PENDING);
        return orderRepository.save(order);
    }
    
    private void cancelOrder(Order order) {
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
```

### State Machine for Saga:

```java
// Using Spring State Machine
@Configuration
@EnableStateMachine
public class OrderSagaStateMachine {
    
    public enum States {
        ORDER_CREATED,
        INVENTORY_RESERVED,
        PAYMENT_PROCESSED,
        SHIPPED,
        COMPLETED,
        CANCELLED
    }
    
    public enum Events {
        RESERVE_INVENTORY,
        INVENTORY_RESERVED,
        INVENTORY_FAILED,
        PROCESS_PAYMENT,
        PAYMENT_PROCESSED,
        PAYMENT_FAILED,
        SHIP_ORDER,
        SHIPPED,
        SHIPPING_FAILED
    }
    
    @Bean
    public StateMachineConfigurer<States, Events> configurer() {
        return new StateMachineConfigurerAdapter<States, Events>() {
            
            @Override
            public void configure(StateMachineStateConfigurer<States, Events> states)
                    throws Exception {
                states
                    .withStates()
                    .initial(States.ORDER_CREATED)
                    .state(States.INVENTORY_RESERVED)
                    .state(States.PAYMENT_PROCESSED)
                    .state(States.SHIPPED)
                    .end(States.COMPLETED)
                    .end(States.CANCELLED);
            }
            
            @Override
            public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
                    throws Exception {
                transitions
                    .withExternal()
                        .source(States.ORDER_CREATED)
                        .target(States.INVENTORY_RESERVED)
                        .event(Events.INVENTORY_RESERVED)
                    .and()
                    .withExternal()
                        .source(States.ORDER_CREATED)
                        .target(States.CANCELLED)
                        .event(Events.INVENTORY_FAILED)
                    .and()
                    .withExternal()
                        .source(States.INVENTORY_RESERVED)
                        .target(States.PAYMENT_PROCESSED)
                        .event(Events.PAYMENT_PROCESSED)
                    .and()
                    .withExternal()
                        .source(States.INVENTORY_RESERVED)
                        .target(States.CANCELLED)
                        .event(Events.PAYMENT_FAILED);
                    // ... more transitions
            }
        };
    }
}
```

### Choreography vs Orchestration:

| Aspect | Choreography | Orchestration |
|--------|-------------|---------------|
| **Coordinator** | None | Central orchestrator |
| **Coupling** | Loose | Tighter |
| **Complexity** | Distributed | Centralized |
| **Visibility** | Hard to track | Easy to track |
| **Failure Handling** | Distributed | Centralized |
| **Best For** | Simple flows | Complex workflows |

---

## Q34: Data Consistency Across Microservices

### Consistency Patterns:

#### 1. Strong Consistency (Not Practical)

```
All services see same data at same time
❌ Requires distributed transactions
❌ Poor performance
❌ Not suitable for microservices
```

#### 2. Eventual Consistency (Recommended)

```
Services eventually become consistent over time
✅ High availability
✅ Better performance
✅ Acceptable for most use cases
```

### Implementation Strategies:

#### 1. Event Sourcing

```java
// Store all changes as events
@Entity
public class OrderEvent {
    @Id
    private String id;
    private String orderId;
    private String eventType;  // OrderCreated, PaymentProcessed, etc.
    private String eventData;  // JSON
    private LocalDateTime timestamp;
}

@Service
public class OrderService {
    
    @Autowired
    private EventStore eventStore;
    
    public void createOrder(OrderRequest request) {
        // Store event
        OrderCreatedEvent event = new OrderCreatedEvent(request);
        eventStore.save(event);
        
        // Publish event
        eventPublisher.publish(event);
    }
    
    // Rebuild state from events
    public Order getOrder(String orderId) {
        List<OrderEvent> events = eventStore.findByOrderId(orderId);
        
        Order order = new Order();
        for (OrderEvent event : events) {
            order.apply(event);  // Apply each event
        }
        
        return order;
    }
}
```

#### 2. CQRS (Command Query Responsibility Segregation)

```java
// Write Model (Commands)
@Service
public class OrderCommandService {
    
    @Transactional
    public void createOrder(CreateOrderCommand command) {
        Order order = new Order(command);
        orderRepository.save(order);
        
        // Publish event for read model
        eventPublisher.publish(new OrderCreatedEvent(order));
    }
}

// Read Model (Queries)
@Service
public class OrderQueryService {
    
    @Autowired
    private OrderReadRepository readRepository;
    
    public OrderView getOrder(String orderId) {
        return readRepository.findById(orderId);
    }
    
    // Update read model when event occurs
    @EventHandler
    public void on(OrderCreatedEvent event) {
        OrderView view = new OrderView(event);
        readRepository.save(view);
    }
}

// Different databases for read and write
@Entity
@Table(name = "orders_write")
public class Order {
    // Normalized for writes
}

@Entity
@Table(name = "orders_read")
public class OrderView {
    // Denormalized for fast reads
    @Embedded
    private UserInfo userInfo;  // Cached user data
    
    @Embedded
    private PaymentInfo paymentInfo;  // Cached payment data
}
```

#### 3. Saga Pattern for Consistency

See Q33 above for detailed implementation.

#### 4. Two-Phase Commit (Avoid)

Not recommended for microservices due to blocking nature and complexity.

### Handling Conflicts:

```java
// Last-Write-Wins
@Service
public class ConflictResolver {
    
    public Order resolveConflict(List<Order> versions) {
        return versions.stream()
            .max(Comparator.comparing(Order::getUpdatedAt))
            .orElseThrow();
    }
}

// Version-based Optimistic Locking
@Entity
public class Order {
    @Id
    private String id;
    
    @Version
    private Long version;
    
    // JPA automatically handles version conflicts
}

// Application-specific Merge
@Service
public class OrderMergeService {
    
    public Order merge(Order local, Order remote) {
        Order merged = new Order();
        
        // Merge logic based on business rules
        merged.setItems(mergeItems(local.getItems(), remote.getItems()));
        merged.setStatus(resolveStatus(local.getStatus(), remote.getStatus()));
        
        return merged;
    }
    
    private List<OrderItem> mergeItems(List<OrderItem> local, List<OrderItem> remote) {
        // Combine items from both versions
        Set<OrderItem> merged = new HashSet<>(local);
        merged.addAll(remote);
        return new ArrayList<>(merged);
    }
}
```

---

## Q35: 12-Factor App Methodology

### Overview:

```
12-Factor App: Best practices for building cloud-native applications

┌────────────────────────────────────────────┐
│  1. Codebase      - One codebase in VCS   │
│  2. Dependencies  - Explicitly declare     │
│  3. Config        - Store in environment   │
│  4. Backing Services - Treat as resources │
│  5. Build/Release/Run - Separate stages   │
│  6. Processes     - Stateless processes    │
│  7. Port Binding  - Self-contained         │
│  8. Concurrency   - Scale via processes    │
│  9. Disposability - Fast startup/shutdown  │
│  10. Dev/Prod Parity - Keep similar        │
│  11. Logs         - Treat as streams       │
│  12. Admin        - Run as one-off tasks   │
└────────────────────────────────────────────┘
```

### Detailed Explanation:

#### 1. Codebase

```
One codebase tracked in version control, many deploys

✅ Good:
GitHub Repo: user-service
├─ dev deployment
├─ staging deployment
└─ production deployment

❌ Bad:
Multiple repos for same service
Different code for different environments
```

```java
// Same codebase for all environments
@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

#### 2. Dependencies

```yaml
# Explicitly declare dependencies (pom.xml)
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>2.7.0</version>
    </dependency>
</dependencies>

# Never rely on system-wide packages
✅ Packaged in JAR with all dependencies
❌ Assumes libraries on server
```

#### 3. Config

```java
// ❌ WRONG: Hardcoded config
@Service
public class DatabaseService {
    private String url = "jdbc:mysql://prod-db:3306/mydb";
    private String username = "admin";
    private String password = "secret123";
}

// ✅ CORRECT: Environment-based config
@Service
public class DatabaseService {
    
    @Value("${db.url}")
    private String url;
    
    @Value("${db.username}")
    private String username;
    
    @Value("${db.password}")
    private String password;
}
```

```yaml
# application.yml - defaults
db:
  url: jdbc:mysql://localhost:3306/mydb
  username: root

# Environment variables override
# DB_URL=jdbc:mysql://prod-db:3306/mydb
# DB_USERNAME=admin
# DB_PASSWORD=secret123
```

#### 4. Backing Services

```java
// Treat databases, caches, queues as attached resources
@Configuration
public class BackingServicesConfig {
    
    // Database - can be swapped via config
    @Bean
    public DataSource dataSource(
        @Value("${db.url}") String url,
        @Value("${db.username}") String username,
        @Value("${db.password}") String password
    ) {
        return DataSourceBuilder.create()
            .url(url)
            .username(username)
            .password(password)
            .build();
    }
    
    // Cache - can be swapped
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
        @Value("${redis.host}") String host,
        @Value("${redis.port}") int port
    ) {
        // Configuration
    }
}
```

#### 5. Build, Release, Run

```
Build: Code + Dependencies → Executable
Release: Executable + Config → Deployment artifact
Run: Execute the release

┌──────┐    ┌─────────┐    ┌──────┐
│Build │ ─▶ │ Release │ ─▶ │ Run  │
└──────┘    └─────────┘    └──────┘

// Build (CI)
mvn clean package

// Release (CD)
docker build -t user-service:v1.0 .
docker tag user-service:v1.0 registry/user-service:v1.0
docker push registry/user-service:v1.0

// Run (Deployment)
kubectl apply -f deployment.yml
```

#### 6. Processes

```java
// ❌ WRONG: Stateful (sticky sessions)
@RestController
public class UserController {
    private Map<String, User> sessionData = new HashMap<>();
    
    @GetMapping("/users/current")
    public User getCurrentUser() {
        return sessionData.get("current");  // Broken in multi-instance
    }
}

// ✅ CORRECT: Stateless
@RestController
public class UserController {
    
    @Autowired
    private RedisTemplate<String, User> redis;
    
    @GetMapping("/users/current")
    public User getCurrentUser(@RequestHeader("Authorization") String token) {
        String userId = extractUserId(token);
        return redis.opsForValue().get("user:" + userId);
    }
}
```

#### 7. Port Binding

```java
// Self-contained, exports services via port binding
@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}

// application.yml
server:
  port: ${PORT:8080}  // Configurable port

// No external web server (Tomcat embedded)
```

#### 8. Concurrency

```yaml
# Scale by adding more processes, not threads

# Kubernetes scaling
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  replicas: 5  # Run 5 instances
  
# Horizontal Pod Autoscaler
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: user-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: user-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

#### 9. Disposability

```java
// Fast startup
@SpringBootApplication
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
    // Starts in seconds
}

// Graceful shutdown
@Configuration
public class ShutdownConfig {
    
    @PreDestroy
    public void onShutdown() {
        log.info("Shutting down gracefully...");
        // Complete ongoing requests
        // Close connections
        // Release resources
    }
}
```

#### 10. Dev/Prod Parity

```yaml
# Docker Compose for local dev (mirrors production)
version: '3.8'
services:
  user-service:
    build: .
    environment:
      - DB_URL=jdbc:postgresql://db:5432/users
      - REDIS_HOST=redis
  
  db:
    image: postgres:14
  
  redis:
    image: redis:7
```

#### 11. Logs

```java
// ❌ WRONG: Write to files
log.info("User created: " + userId);
// Writes to /var/log/application.log

// ✅ CORRECT: Write to stdout/stderr
@Slf4j
@Service
public class UserService {
    public void createUser(User user) {
        log.info("Creating user: {}", user.getId());  // stdout
        // Let infrastructure handle log aggregation
    }
}
```

```yaml
# Kubernetes collects logs from stdout
kubectl logs -f pod/user-service-abc123

# Centralized logging (ELK, Splunk)
stdout → Fluentd → Elasticsearch → Kibana
```

#### 12. Admin Processes

```java
// Run admin tasks as one-off processes
@Component
public class DatabaseMigration implements CommandLineRunner {
    
    @Override
    public void run(String... args) {
        if (Arrays.asList(args).contains("--migrate")) {
            runMigrations();
        }
    }
}

// Run migration
java -jar app.jar --migrate

// Or use separate admin scripts
@SpringBootApplication
public class AdminTasks {
    public static void main(String[] args) {
        if (args[0].equals("create-user")) {
            createAdminUser();
        }
    }
}
```

---

## Q36: API Versioning

### Versioning Strategies:

#### 1. URI Versioning (Most Common)

```java
// Version in URL path
@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 {
    
    @GetMapping("/{id}")
    public UserV1 getUser(@PathVariable Long id) {
        return new UserV1(userService.getUser(id));
    }
}

@RestController
@RequestMapping("/api/v2/users")
public class UserControllerV2 {
    
    @GetMapping("/{id}")
    public UserV2 getUser(@PathVariable Long id) {
        return new UserV2(userService.getUser(id));
    }
}

// Different response structures
public class UserV1 {
    private Long id;
    private String name;
    // Old structure
}

public class UserV2 {
    private Long id;
    private String firstName;
    private String lastName;
    private Map<String, Object> metadata;
    // New structure with breaking changes
}

// Pros: Simple, explicit, easy to route
// Cons: URL pollution, hard to deprecate
```

#### 2. Header Versioning

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping(value = "/{id}", headers = "API-Version=1")
    public UserV1 getUserV1(@PathVariable Long id) {
        return new UserV1(userService.getUser(id));
    }
    
    @GetMapping(value = "/{id}", headers = "API-Version=2")
    public UserV2 getUserV2(@PathVariable Long id) {
        return new UserV2(userService.getUser(id));
    }
}

// Client request:
// GET /api/users/123
// API-Version: 2

// Pros: Clean URLs, flexible
// Cons: Not visible in URL, harder to test
```

#### 3. Accept Header Versioning (Content Negotiation)

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping(value = "/{id}", produces = "application/vnd.company.v1+json")
    public UserV1 getUserV1(@PathVariable Long id) {
        return new UserV1(userService.getUser(id));
    }
    
    @GetMapping(value = "/{id}", produces = "application/vnd.company.v2+json")
    public UserV2 getUserV2(@PathVariable Long id) {
        return new UserV2(userService.getUser(id));
    }
}

// Client request:
// GET /api/users/123
// Accept: application/vnd.company.v2+json

// Pros: RESTful, standard HTTP
// Cons: Complex, harder to understand
```

#### 4. Query Parameter Versioning

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public ResponseEntity<Object> getUser(
        @PathVariable Long id,
        @RequestParam(defaultValue = "1") int version
    ) {
        if (version == 2) {
            return ResponseEntity.ok(new UserV2(userService.getUser(id)));
        }
        return ResponseEntity.ok(new UserV1(userService.getUser(id)));
    }
}

// Client request:
// GET /api/users/123?version=2

// Pros: Simple, easy to test
// Cons: Not clean, version can be forgotten
```

### Best Practices:

```java
// 1. Version only when breaking changes
@RestController
@RequestMapping("/api/v2/users")
public class UserControllerV2 {
    
    // Breaking change: firstName/lastName instead of name
    @GetMapping("/{id}")
    public UserV2 getUser(@PathVariable Long id) {
        return new UserV2(userService.getUser(id));
    }
}

// 2. Support multiple versions temporarily
@Configuration
public class VersionSupport {
    private static final String V1_DEPRECATION = "2024-12-31";
    private static final String V2_RELEASE = "2024-06-01";
}

// 3. Add deprecation warnings
@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 {
    
    @GetMapping("/{id}")
    public ResponseEntity<UserV1> getUser(@PathVariable Long id) {
        return ResponseEntity.ok()
            .header("Warning", "299 - Deprecated API. Use v2. Sunset: 2024-12-31")
            .header("Sunset", "Wed, 31 Dec 2024 23:59:59 GMT")
            .body(new UserV1(userService.getUser(id)));
    }
}

// 4. Use API Gateway for version routing
spring:
  cloud:
    gateway:
      routes:
        - id: users-v1
          uri: lb://user-service
          predicates:
            - Path=/api/v1/users/**
          filters:
            - RewritePath=/api/v1/users/(?<segment>.*), /v1/${segment}
            
        - id: users-v2
          uri: lb://user-service
          predicates:
            - Path=/api/v2/users/**
          filters:
            - RewritePath=/api/v2/users/(?<segment>.*), /v2/${segment}
```

### Semantic Versioning:

```
Version Format: MAJOR.MINOR.PATCH

MAJOR: Breaking changes (v1 → v2)
MINOR: New features, backward compatible (v2.0 → v2.1)
PATCH: Bug fixes, backward compatible (v2.1.0 → v2.1.1)

Example:
v1.0.0 → Initial release
v1.1.0 → Add optional field "phoneNumber"
v1.1.1 → Fix bug in validation
v2.0.0 → Breaking change: split "name" to "firstName"/"lastName"
```

---

## Q37: Deployment Strategies

### 1. Recreate Deployment

```
Stop all old instances, then start new ones

Old Version (v1):
┌───┬───┬───┐
│ ✓ │ ✓ │ ✓ │
└───┴───┴───┘

Shutdown:
┌───┬───┬───┐
│ ✗ │ ✗ │ ✗ │ ← Downtime!
└───┴───┴───┘

New Version (v2):
┌───┬───┬───┐
│ ✓ │ ✓ │ ✓ │
└───┴───┴───┘

Pros: Simple, clean
Cons: Downtime
Use case: Development environments
```

```yaml
# Kubernetes
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  replicas: 3
  strategy:
    type: Recreate
  template:
    spec:
      containers:
      - name: user-service
        image: user-service:2.0
```

### 2. Rolling Update (Default)

```
Gradually replace instances one by one

Step 1:
┌───┬───┬───┐
│v1 │v1 │v1 │
└───┴───┴───┘

Step 2:
┌───┬───┬───┐
│v2 │v1 │v1 │ ← Replace one
└───┴───┴───┘

Step 3:
┌───┬───┬───┐
│v2 │v2 │v1 │
└───┴───┴───┘

Step 4:
┌───┬───┬───┐
│v2 │v2 │v2 │ ← Complete
└───┴───┴───┘

Pros: No downtime, gradual
Cons: Two versions running simultaneously
Use case: Most production deployments
```

```yaml
# Kubernetes
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1        # Max extra pods during update
      maxUnavailable: 1  # Max unavailable pods during update
  template:
    spec:
      containers:
      - name: user-service
        image: user-service:2.0
```

### 3. Blue-Green Deployment

```
Run two identical environments, switch traffic

Blue (v1) - Current:
┌───┬───┬───┐
│ ✓ │ ✓ │ ✓ │ ← 100% traffic
└───┴───┴───┘

Green (v2) - Staging:
┌───┬───┬───┐
│ ✓ │ ✓ │ ✓ │ ← 0% traffic (testing)
└───┴───┴───┘

Switch:
Blue (v1):
┌───┬───┬───┐
│ ✓ │ ✓ │ ✓ │ ← 0% traffic (kept for rollback)
└───┴───┴───┘

Green (v2):
┌───┬───┬───┐
│ ✓ │ ✓ │ ✓ │ ← 100% traffic
└───┴───┴───┘

Pros: Instant rollback, zero downtime
Cons: Expensive (double resources)
Use case: Critical applications
```

```yaml
# Kubernetes Service switches between deployments
apiVersion: v1
kind: Service
metadata:
  name: user-service
spec:
  selector:
    app: user-service
    version: v2  # Change from v1 to v2 for instant switch
  ports:
  - port: 80
    targetPort: 8080
```

### 4. Canary Deployment

```
Route small % of traffic to new version

Step 1: 95% v1, 5% v2
┌───┬───┬───┬───┬───┬───┬───┬───┬───┬───┐
│v1 │v1 │v1 │v1 │v1 │v1 │v1 │v1 │v1 │v2 │
└───┴───┴───┴───┴───┴───┴───┴───┴───┴───┘
 95%                                 5%

Step 2: Monitor metrics, if good → 50% v1, 50% v2
┌───┬───┬───┬───┬───┬───┬───┬───┬───┬───┐
│v1 │v1 │v1 │v1 │v1 │v2 │v2 │v2 │v2 │v2 │
└───┴───┴───┴───┴───┴───┴───┴───┴───┴───┘
 50%                 50%

Step 3: If metrics still good → 100% v2
┌───┬───┬───┬───┬───┬───┬───┬───┬───┬───┐
│v2 │v2 │v2 │v2 │v2 │v2 │v2 │v2 │v2 │v2 │
└───┴───┴───┴───┴───┴───┴───┴───┴───┴───┘
100%

Pros: Low risk, gradual validation
Cons: Complex, requires monitoring
Use case: High-traffic applications
```

```yaml
# Istio Virtual Service for traffic splitting
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: user-service
spec:
  hosts:
  - user-service
  http:
  - match:
    - headers:
        user-group:
          exact: beta-testers
    route:
    - destination:
        host: user-service
        subset: v2
  - route:
    - destination:
        host: user-service
        subset: v1
      weight: 90
    - destination:
        host: user-service
        subset: v2
      weight: 10
```

### 5. A/B Testing

```
Route traffic based on user attributes

Users from US:        Users from EU:
┌───┬───┬───┐        ┌───┬───┬───┐
│v1 │v1 │v1 │        │v2 │v2 │v2 │
└───┴───┴───┘        └───┴───┴───┘

Mobile users:         Desktop users:
┌───┬───┬───┐        ┌───┬───┬───┐
│v2 │v2 │v2 │        │v1 │v1 │v1 │
└───┴───┴───┘        └───┴───┴───┘

Pros: Test features with specific users
Cons: Complex routing logic
Use case: Feature testing
```

```java
// Spring Cloud Gateway with custom routing
@Component
public class ABTestingFilter implements GlobalFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
        
        if (userAgent != null && userAgent.contains("Mobile")) {
            // Route mobile users to v2
            exchange.getAttributes().put("version", "v2");
        } else {
            // Route desktop users to v1
            exchange.getAttributes().put("version", "v1");
        }
        
        return chain.filter(exchange);
    }
}
```

### Summary Table:

| Strategy | Downtime | Cost | Complexity | Risk | Use Case |
|----------|----------|------|------------|------|----------|
| **Recreate** | Yes | Low | Low | High | Dev/Test |
| **Rolling** | No | Low | Medium | Medium | Most prod |
| **Blue-Green** | No | High | Medium | Low | Critical apps |
| **Canary** | No | Medium | High | Low | High traffic |
| **A/B Test** | No | Medium | High | Low | Feature testing |

---

## Interview Tips & Key Takeaways

### Must-Know Points:

1. **Microservices vs Monolithic** - Know trade-offs, when to use each
2. **Service Communication** - REST, gRPC, messaging patterns
3. **Service Discovery** - Eureka, Consul, how it works
4. **API Gateway** - Single entry point, cross-cutting concerns
5. **Distributed Transactions** - Saga pattern (choreography vs orchestration)
6. **Circuit Breaker** - Prevent cascading failures
7. **Data Consistency** - Eventual consistency, CQRS, Event Sourcing
8. **12-Factor App** - Production-ready microservices checklist
9. **Versioning** - URI, header, semantic versioning
10. **Deployment** - Rolling, Blue-Green, Canary strategies

### Common Follow-up Questions:

- "How do you handle database changes in microservices?"
- "What happens when a service is down?"
- "How do you test microservices?"
- "How do you monitor microservices?"
- "How do you handle secrets and configuration?"
- "What are the challenges you faced with microservices?"

### Practice Scenarios:

1. Design an e-commerce system with microservices
2. Handle payment failure in order processing
3. Implement service-to-service authentication
4. Design a deployment pipeline for microservices
5. Handle data migration across services

Remember: There's no one-size-fits-all answer in microservices. Always discuss **trade-offs** and justify your choices! 🚀
