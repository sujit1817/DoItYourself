# System Design Concepts - Interview Guide (Q78-83)

## Table of Contents
1. [System Design Approach](#system-design-approach)
2. [CAP Theorem](#cap-theorem)
3. [Event-Driven Architecture](#event-driven-architecture)
4. [High Availability & Fault Tolerance](#high-availability--fault-tolerance)
5. [Load Balancing Strategies](#load-balancing-strategies)
6. [Eventual Consistency](#eventual-consistency)

---

## System Design Approach

### Step-by-Step Framework for System Design Interviews:

```
1. REQUIREMENTS (5-10 min)
   ├─ Functional Requirements
   ├─ Non-Functional Requirements
   ├─ Scale/Capacity Estimation
   └─ Constraints & Assumptions

2. HIGH-LEVEL DESIGN (10-15 min)
   ├─ Major Components
   ├─ Data Flow
   ├─ APIs/Interfaces
   └─ Database Schema

3. DEEP DIVE (15-20 min)
   ├─ Bottlenecks
   ├─ Scalability
   ├─ Trade-offs
   └─ Optimization

4. WRAP-UP (5 min)
   ├─ Review Design
   ├─ Failure Scenarios
   └─ Monitoring & Metrics
```

### Example: URL Shortener System Design

#### Step 1: Requirements

**Functional Requirements:**
- Generate short URL from long URL
- Redirect short URL to original URL
- Optional: Custom aliases
- Optional: URL expiration

**Non-Functional Requirements:**
- High availability (99.99%)
- Low latency (<100ms)
- Scalable (millions of URLs/day)
- Durable (no data loss)

**Capacity Estimation:**
```
Traffic:
- 100M URLs created per month
- Read/Write ratio: 100:1 (100M writes, 10B reads/month)
- URLs per second: 100M / (30 * 24 * 3600) = ~40 URLs/sec
- Reads per second: 40 * 100 = 4000 reads/sec

Storage:
- Each URL: ~500 bytes
- 100M URLs/month * 500 bytes = 50 GB/month
- 5 years: 50 GB * 12 * 5 = 3 TB

Bandwidth:
- Write: 40 URLs/sec * 500 bytes = 20 KB/sec
- Read: 4000 * 500 bytes = 2 MB/sec

Cache:
- 20% of daily traffic (hot URLs): 10B * 0.2 = 2B URLs
- Memory: 2B * 500 bytes = 1 TB cache needed
```

#### Step 2: High-Level Design

```
┌──────────┐
│  Client  │
└────┬─────┘
     │
     ▼
┌─────────────────┐
│  Load Balancer  │
└────┬────────────┘
     │
     ▼
┌──────────────────────────┐
│   Application Servers    │
│   (Stateless)            │
└────┬────────────┬────────┘
     │            │
     ▼            ▼
┌─────────┐  ┌──────────┐
│  Cache  │  │ Database │
│ (Redis) │  │ (NoSQL)  │
└─────────┘  └──────────┘
```

**API Design:**
```java
// Create short URL
POST /api/v1/shorten
Request: {
    "longUrl": "https://example.com/very/long/url",
    "customAlias": "mylink",  // optional
    "expirationDate": "2025-12-31"  // optional
}
Response: {
    "shortUrl": "https://short.ly/abc123",
    "createdAt": "2025-03-01T10:00:00Z"
}

// Redirect
GET /{shortCode}
Response: 302 Redirect to long URL
```

**Database Schema:**
```sql
CREATE TABLE urls (
    id BIGINT PRIMARY KEY,
    short_code VARCHAR(10) UNIQUE NOT NULL,
    long_url TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP,
    click_count INT DEFAULT 0,
    user_id BIGINT,
    INDEX idx_short_code (short_code),
    INDEX idx_user_id (user_id)
);
```

**Short Code Generation:**
```java
public class ShortCodeGenerator {
    private static final String CHARS = 
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = CHARS.length(); // 62
    
    // Convert ID to short code (base62 encoding)
    public static String encode(long id) {
        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(CHARS.charAt((int)(id % BASE)));
            id /= BASE;
        }
        return sb.reverse().toString();
    }
    
    // 7 characters = 62^7 = 3.5 trillion combinations
    
    public static long decode(String shortCode) {
        long id = 0;
        for (char c : shortCode.toCharArray()) {
            id = id * BASE + CHARS.indexOf(c);
        }
        return id;
    }
}
```

#### Step 3: Deep Dive - Scalability

**Distributed ID Generation:**
```
┌─────────────────────────────────────┐
│     Snowflake ID (64 bits)          │
├──────┬──────┬──────────┬───────────┤
│ 1 bit│ 41   │ 10 bits  │ 12 bits   │
│(sign)│(time)│(machine) │(sequence) │
└──────┴──────┴──────────┴───────────┘

- 41 bits timestamp: 69 years
- 10 bits machine ID: 1024 machines
- 12 bits sequence: 4096 IDs per millisecond per machine
```

```java
public class SnowflakeIdGenerator {
    private final long machineId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;
    
    private static final long EPOCH = 1420070400000L; // 2015-01-01
    private static final long MACHINE_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;
    
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards!");
        }
        
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }
        
        lastTimestamp = timestamp;
        
        return ((timestamp - EPOCH) << (MACHINE_ID_BITS + SEQUENCE_BITS))
             | (machineId << SEQUENCE_BITS)
             | sequence;
    }
    
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
```

**Caching Strategy:**
```java
@Service
public class UrlService {
    @Autowired
    private RedisTemplate<String, String> redis;
    
    @Autowired
    private UrlRepository repository;
    
    public String getLongUrl(String shortCode) {
        // Try cache first
        String longUrl = redis.opsForValue().get("url:" + shortCode);
        
        if (longUrl != null) {
            return longUrl;
        }
        
        // Cache miss - query database
        Url url = repository.findByShortCode(shortCode);
        if (url != null) {
            // Update cache with TTL
            redis.opsForValue().set(
                "url:" + shortCode, 
                url.getLongUrl(),
                24, TimeUnit.HOURS
            );
            return url.getLongUrl();
        }
        
        return null;
    }
}
```

**Rate Limiting:**
```java
@Component
public class RateLimiter {
    @Autowired
    private RedisTemplate<String, String> redis;
    
    public boolean allowRequest(String userId) {
        String key = "rate_limit:" + userId;
        Long count = redis.opsForValue().increment(key);
        
        if (count == 1) {
            redis.expire(key, 1, TimeUnit.HOURS);
        }
        
        return count <= 1000; // 1000 requests per hour
    }
}
```

---

## CAP Theorem

### The CAP Triangle:

```
              Consistency
                   /\
                  /  \
                 /    \
                /  CA  \
               /        \
              /          \
             /____________\
       Partition          Availability
       Tolerance

You can only guarantee 2 out of 3!
```

### Definitions:

**Consistency (C):**
- All nodes see the same data at the same time
- Every read receives the most recent write
- Example: Bank balance must be consistent across all ATMs

**Availability (A):**
- Every request receives a response (success or failure)
- System remains operational even if some nodes fail
- Example: Website should respond even during database issues

**Partition Tolerance (P):**
- System continues to function despite network partitions
- Messages between nodes may be lost or delayed
- Example: Datacenter connectivity loss shouldn't break system

### CAP Trade-offs:

```
┌────────────────┬─────────────────┬──────────────────┐
│  CP System     │  AP System      │  CA System       │
│  (Consistency  │  (Availability  │  (Not realistic  │
│   + Partition) │   + Partition)  │   in distributed)│
├────────────────┼─────────────────┼──────────────────┤
│ - MongoDB      │ - Cassandra     │ - Traditional    │
│ - HBase        │ - DynamoDB      │   RDBMS (single) │
│ - Redis        │ - Riak          │ - PostgreSQL     │
│ - Zookeeper    │ - CouchDB       │   (single node)  │
│                │                 │                  │
│ During         │ During          │ Assumes no       │
│ partition:     │ partition:      │ network          │
│ - Block writes │ - Allow writes  │ partitions       │
│ - Return error │ - May return    │                  │
│ - Wait for     │   stale data    │                  │
│   consensus    │                 │                  │
└────────────────┴─────────────────┴──────────────────┘
```

### Real-World Examples:

#### CP System (Banking):

```java
// Strong consistency required
@Service
public class BankingService {
    
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void transferMoney(String fromAccount, String toAccount, BigDecimal amount) {
        // Lock both accounts
        Account from = accountRepo.findByIdForUpdate(fromAccount);
        Account to = accountRepo.findByIdForUpdate(toAccount);
        
        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
        
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        
        accountRepo.save(from);
        accountRepo.save(to);
        
        // Both updates succeed or both fail (ACID)
        // During network partition: Operation blocks or fails
        // Consistency > Availability
    }
}
```

#### AP System (Social Media Feed):

```java
// Availability over consistency
@Service
public class FeedService {
    
    @Autowired
    private List<DataSource> dataSourceReplicas;
    
    public List<Post> getUserFeed(String userId) {
        // Try all replicas until one responds
        for (DataSource replica : dataSourceReplicas) {
            try {
                return replica.getFeed(userId);
                // May return slightly stale data
                // But always available
            } catch (Exception e) {
                // Try next replica
                continue;
            }
        }
        
        // Return cached/default feed if all fail
        return getCachedFeed(userId);
        // Availability > Consistency
    }
}
```

### PACELC Extension:

```
If there is a Partition (P):
    Choose between Availability (A) and Consistency (C)
Else (E):
    Choose between Latency (L) and Consistency (C)

Examples:
- PA/EL: Cassandra (Available during partition, Low latency normally)
- PC/EC: HBase (Consistent during partition, Consistent normally)
- PA/EC: MongoDB (Available during partition, Consistent normally)
```

### Interview Question: "Design a globally distributed system"

```java
// Multi-region architecture
public class GlobalSystemDesign {
    
    /*
    Design Choices:
    
    1. Strong Consistency (CP):
       - Single master region
       - Replicate synchronously
       - Higher latency
       - Example: Financial transactions
    
    2. Eventual Consistency (AP):
       - Multi-master (active-active)
       - Replicate asynchronously
       - Lower latency
       - Conflict resolution needed
       - Example: Social media posts
    
    Architecture:
    
    US-East ─────┐
                 │
    US-West ─────┼───── Global Load Balancer
                 │
    EU-West ─────┤
                 │
    Asia-Pacific ┘
    
    Each region:
    - Load balancer
    - App servers
    - Cache (Redis)
    - Database replica
    
    Replication Strategy:
    - Writes go to nearest region
    - Async replication to other regions
    - Conflict resolution: Last-Write-Wins or Vector Clocks
    */
    
    @Service
    public class MultiRegionService {
        
        @Autowired
        private RegionRouter regionRouter;
        
        public void writeData(String key, String value) {
            // Write to local region
            Region localRegion = regionRouter.getLocalRegion();
            localRegion.write(key, value, System.currentTimeMillis());
            
            // Async replicate to other regions
            regionRouter.getRemoteRegions().forEach(region -> {
                CompletableFuture.runAsync(() -> {
                    region.replicate(key, value, System.currentTimeMillis());
                });
            });
        }
        
        public String readData(String key) {
            // Read from local region (may be stale)
            return regionRouter.getLocalRegion().read(key);
        }
        
        // Conflict resolution (if concurrent writes in different regions)
        public void resolveConflict(String key, List<WriteRecord> writes) {
            // Strategy 1: Last-Write-Wins
            WriteRecord latest = writes.stream()
                .max(Comparator.comparing(WriteRecord::getTimestamp))
                .orElseThrow();
            
            // Strategy 2: Vector Clocks (more complex, but better)
            // Strategy 3: Application-specific (e.g., merge lists)
        }
    }
}
```

---

## Event-Driven Architecture

### Core Concepts:

```
Traditional (Request-Response):
Service A ──request──▶ Service B ──response──▶ Service A
(Synchronous, tightly coupled)

Event-Driven:
Service A ──event──▶ Event Bus ──event──▶ Service B
                                ──event──▶ Service C
                                ──event──▶ Service D
(Asynchronous, loosely coupled)
```

### Architecture Diagram:

```
┌──────────────┐
│  Publishers  │
│ (Producers)  │
└──────┬───────┘
       │ Publish events
       ▼
┌─────────────────────────────┐
│      Event Bus/Broker       │
│   (Kafka, RabbitMQ, SNS)    │
└─────────────┬───────────────┘
              │ Subscribe to events
       ┌──────┴──────┬──────────────┐
       ▼             ▼              ▼
┌──────────┐  ┌──────────┐  ┌──────────┐
│Consumer 1│  │Consumer 2│  │Consumer 3│
└──────────┘  └──────────┘  └──────────┘
```

### Event Types:

**1. Domain Events:**
```java
// Something that happened in the domain
public class OrderPlacedEvent {
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private LocalDateTime timestamp;
    
    // Event should be immutable
    // Event represents past tense (OrderPlaced, not PlaceOrder)
}
```

**2. Integration Events:**
```java
// Cross-service communication
public class UserRegisteredEvent {
    private String userId;
    private String email;
    private LocalDateTime registeredAt;
    
    // Published to message bus
    // Consumed by multiple services
}
```

**3. Command Events:**
```java
// Request to do something
public class SendEmailCommand {
    private String recipient;
    private String subject;
    private String body;
    
    // Not strictly "event-driven" but used in CQRS
}
```

### Implementation with Kafka:

```java
// Producer (Publisher)
@Service
public class OrderService {
    
    @Autowired
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    
    @Transactional
    public Order placeOrder(OrderRequest request) {
        // 1. Process order
        Order order = createOrder(request);
        orderRepository.save(order);
        
        // 2. Publish event
        OrderPlacedEvent event = new OrderPlacedEvent(
            order.getId(),
            order.getUserId(),
            order.getTotalAmount()
        );
        
        kafkaTemplate.send("order-events", event);
        
        return order;
    }
}

// Consumer (Subscriber)
@Service
public class NotificationService {
    
    @KafkaListener(topics = "order-events", groupId = "notification-service")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        // Send order confirmation email
        emailService.sendOrderConfirmation(
            event.getUserId(),
            event.getOrderId()
        );
    }
}

@Service
public class InventoryService {
    
    @KafkaListener(topics = "order-events", groupId = "inventory-service")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        // Reduce inventory
        inventoryRepository.reduceStock(event.getOrderId());
    }
}

@Service
public class AnalyticsService {
    
    @KafkaListener(topics = "order-events", groupId = "analytics-service")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        // Update analytics
        analyticsRepository.recordSale(event);
    }
}
```

### Event Sourcing:

```java
// Store all changes as events instead of current state
public class EventSourcingExample {
    
    // Traditional approach
    class BankAccount {
        private BigDecimal balance = BigDecimal.ZERO;
        
        public void deposit(BigDecimal amount) {
            balance = balance.add(amount);
            // Only current state stored
        }
    }
    
    // Event Sourcing approach
    class BankAccountEventSourced {
        private List<Event> events = new ArrayList<>();
        
        public void deposit(BigDecimal amount) {
            MoneyDepositedEvent event = new MoneyDepositedEvent(amount);
            events.add(event);
            apply(event);
            // All events stored - full audit trail
        }
        
        public BigDecimal getBalance() {
            // Reconstruct state from events
            return events.stream()
                .map(this::apply)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        private BigDecimal apply(Event event) {
            if (event instanceof MoneyDepositedEvent) {
                return ((MoneyDepositedEvent) event).getAmount();
            } else if (event instanceof MoneyWithdrawnEvent) {
                return ((MoneyWithdrawnEvent) event).getAmount().negate();
            }
            return BigDecimal.ZERO;
        }
    }
}
```

### CQRS (Command Query Responsibility Segregation):

```
Write Side (Commands):          Read Side (Queries):
┌──────────────┐               ┌──────────────┐
│Command Model │               │ Query Model  │
│  (Normalized)│               │ (Denormalized)│
└──────┬───────┘               └───────▲──────┘
       │                               │
       │ Events                        │
       │                               │
       └───────────────┬───────────────┘
                       │
                ┌──────▼──────┐
                │ Event Store │
                └─────────────┘

Benefits:
- Optimized writes and reads separately
- Scale independently
- Different data models for different use cases
```

```java
// Command side
@Service
public class OrderCommandService {
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private EventPublisher eventPublisher;
    
    public void createOrder(CreateOrderCommand command) {
        Order order = new Order(command);
        orderRepository.save(order);
        
        // Publish event
        eventPublisher.publish(new OrderCreatedEvent(order));
    }
}

// Query side
@Service
public class OrderQueryService {
    @Autowired
    private OrderReadRepository readRepository;
    
    public OrderView getOrder(String orderId) {
        // Query from read-optimized model
        return readRepository.findById(orderId);
    }
    
    @EventHandler
    public void on(OrderCreatedEvent event) {
        // Update read model
        OrderView view = new OrderView(event);
        readRepository.save(view);
    }
}
```

### Benefits of Event-Driven Architecture:

```
✅ Loose Coupling
   - Services don't know about each other
   - Easy to add new consumers

✅ Scalability
   - Async processing
   - Can scale consumers independently

✅ Resilience
   - Failure in one service doesn't affect others
   - Can replay events

✅ Audit Trail
   - Complete history of what happened
   - Easy to debug

❌ Challenges:
   - Eventual consistency
   - Debugging distributed flows
   - Event schema evolution
   - Duplicate/out-of-order events
```

---

## High Availability & Fault Tolerance

### Availability Metrics:

```
Availability = Uptime / (Uptime + Downtime)

┌───────────┬────────────┬─────────────────┐
│Availability│ Downtime/yr│  Common Name   │
├───────────┼────────────┼─────────────────┤
│  90%      │ 36.5 days  │  One nine       │
│  99%      │  3.65 days │  Two nines      │
│  99.9%    │  8.76 hours│  Three nines    │
│  99.99%   │  52.6 min  │  Four nines     │
│  99.999%  │  5.26 min  │  Five nines     │
│  99.9999% │  31.5 sec  │  Six nines      │
└───────────┴────────────┴─────────────────┘
```

### High Availability Strategies:

#### 1. Redundancy:

```
Single Point of Failure (SPOF):     Redundant:
┌──────────┐                        ┌──────────┐  ┌──────────┐
│  Server  │ ✗ If fails,           │ Server 1 │  │ Server 2 │
│          │   system down         │          │  │          │
└──────────┘                        └────┬─────┘  └────┬─────┘
                                         │             │
                                         └──────┬──────┘
                                                │
                                         ┌──────▼──────┐
                                         │Load Balancer│
                                         └─────────────┘
                                    ✓ If one fails, other handles requests
```

```java
// Implementation: Health checks
@RestController
public class HealthController {
    
    @Autowired
    private DatabaseHealthIndicator dbHealth;
    
    @Autowired
    private CacheHealthIndicator cacheHealth;
    
    @GetMapping("/health")
    public ResponseEntity<HealthStatus> health() {
        boolean isHealthy = dbHealth.isHealthy() && cacheHealth.isHealthy();
        
        if (isHealthy) {
            return ResponseEntity.ok(new HealthStatus("UP"));
        }
        
        // Load balancer removes unhealthy instances
        return ResponseEntity.status(503).body(new HealthStatus("DOWN"));
    }
}
```

#### 2. Replication:

```
Master-Slave:                     Master-Master:
┌────────┐                       ┌─────────┐──sync──┌─────────┐
│ Master │──writes              │ Master1 │◄──────▶│ Master2 │
└───┬────┘                       └────┬────┘        └────┬────┘
    │replicates                       │writes           │writes
    ▼                                 ▼                 ▼
┌────────┐  ┌────────┐           All writes to both, conflict resolution needed
│ Slave1 │  │ Slave2 │
└────────┘  └────────┘
  reads        reads
```

```java
// Master-Slave with Spring Boot
@Configuration
public class DatabaseConfig {
    
    @Bean
    @Primary
    public DataSource masterDataSource() {
        return DataSourceBuilder.create()
            .url("jdbc:mysql://master-db:3306/mydb")
            .build();
    }
    
    @Bean
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create()
            .url("jdbc:mysql://slave-db:3306/mydb")
            .build();
    }
    
    @Bean
    public DataSource routingDataSource() {
        RoutingDataSource routing = new RoutingDataSource();
        
        Map<Object, Object> sources = new HashMap<>();
        sources.put("master", masterDataSource());
        sources.put("slave", slaveDataSource());
        
        routing.setTargetDataSources(sources);
        routing.setDefaultTargetDataSource(masterDataSource());
        
        return routing;
    }
}

// Route reads to slave
@Transactional(readOnly = true)
public User getUser(Long id) {
    // Goes to slave
    return userRepository.findById(id);
}

@Transactional
public void updateUser(User user) {
    // Goes to master
    userRepository.save(user);
}
```

#### 3. Failover:

```
Active-Passive:
┌────────┐         ┌────────┐
│ Active │ running │Passive │ standby
└────┬───┘         └────────┘
     │
     ✗ fails
     │
┌────▼───┐         ┌────────┐
│ Active │ offline │ Active │ takes over
└────────┘         └────┬───┘
                        │ running
                        ▼
```

```java
// Implement with Spring Cloud Netflix Eureka
@EnableEurekaClient
@SpringBootApplication
public class ServiceApplication {
    
    // Service registers with Eureka
    // If instance fails, Eureka marks it down
    // Load balancer routes to healthy instances
}

// Client-side load balancing
@LoadBalanced
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}

// Automatic failover
@Autowired
private RestTemplate restTemplate;

public String callService() {
    // Ribbon + Eureka handle failover automatically
    return restTemplate.getForObject(
        "http://user-service/users/123",
        String.class
    );
}
```

#### 4. Circuit Breaker:

```java
// Prevent cascading failures
@Service
public class UserService {
    
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserFallback")
    public User getUser(Long id) {
        // Call to external service
        return externalUserService.getUser(id);
    }
    
    public User getUserFallback(Long id, Exception e) {
        // Return cached data or default user
        return cachedUserRepository.findById(id)
            .orElse(User.getDefaultUser());
    }
}

// Circuit states:
// CLOSED: Normal operation
// OPEN: Too many failures, block requests
// HALF_OPEN: Try limited requests to test recovery
```

#### 5. Graceful Degradation:

```java
@Service
public class RecommendationService {
    
    public List<Product> getRecommendations(String userId) {
        try {
            // Try ML-based recommendations
            return mlService.getPersonalizedRecommendations(userId);
        } catch (Exception e) {
            log.warn("ML service unavailable, falling back to popular items");
            
            // Fallback to popular items
            return productService.getPopularProducts();
        }
    }
}
```

### Disaster Recovery:

```
Recovery Metrics:
- RTO (Recovery Time Objective): How long to recover
- RPO (Recovery Point Objective): How much data loss acceptable

Strategies:
┌────────────┬──────────┬──────────┬──────────┐
│  Strategy  │   RTO    │   RPO    │   Cost   │
├────────────┼──────────┼──────────┼──────────┤
│ Backup     │ Hours    │ Hours    │   Low    │
│ Pilot Light│ Minutes  │ Minutes  │  Medium  │
│ Warm Standby│ Seconds │ Seconds  │   High   │
│Multi-Region│Near-zero │Near-zero │Very High │
└────────────┴──────────┴──────────┴──────────┘
```

---

## Load Balancing Strategies

### Load Balancer Types:

```
Layer 4 (Transport Layer):          Layer 7 (Application Layer):
- Routes based on IP/Port           - Routes based on HTTP headers, URL
- Faster (no packet inspection)     - Slower (inspects content)
- TCP/UDP                           - HTTP/HTTPS
- Examples: AWS NLB, HAProxy        - Examples: AWS ALB, Nginx
```

### Load Balancing Algorithms:

#### 1. Round Robin:

```
Requests distributed equally in order
┌────────┐
│ Req 1  │──▶ Server 1
├────────┤
│ Req 2  │──▶ Server 2
├────────┤
│ Req 3  │──▶ Server 3
├────────┤
│ Req 4  │──▶ Server 1 (back to start)
└────────┘

Pros: Simple, fair distribution
Cons: Ignores server capacity/load
```

```java
public class RoundRobinLoadBalancer {
    private List<Server> servers;
    private AtomicInteger currentIndex = new AtomicInteger(0);
    
    public Server selectServer() {
        int index = currentIndex.getAndIncrement() % servers.size();
        return servers.get(index);
    }
}
```

#### 2. Weighted Round Robin:

```
Servers with different capacities get different weights

Server 1 (weight=3): ███
Server 2 (weight=2): ██
Server 3 (weight=1): █

Distribution:
Req 1 → Server 1
Req 2 → Server 1
Req 3 → Server 1
Req 4 → Server 2
Req 5 → Server 2
Req 6 → Server 3
(repeat)
```

```java
public class WeightedRoundRobinLoadBalancer {
    private List<ServerWeight> servers;
    private int currentIndex = 0;
    private int currentWeight = 0;
    private int maxWeight;
    private int gcd; // Greatest common divisor of all weights
    
    public Server selectServer() {
        while (true) {
            currentIndex = (currentIndex + 1) % servers.size();
            
            if (currentIndex == 0) {
                currentWeight = currentWeight - gcd;
                if (currentWeight <= 0) {
                    currentWeight = maxWeight;
                }
            }
            
            if (servers.get(currentIndex).getWeight() >= currentWeight) {
                return servers.get(currentIndex).getServer();
            }
        }
    }
}
```

#### 3. Least Connections:

```
Route to server with fewest active connections

Server 1: 5 connections
Server 2: 3 connections ← Choose this
Server 3: 7 connections

Best for: Long-lived connections (WebSocket, DB)
```

```java
public class LeastConnectionsLoadBalancer {
    private List<ServerWithConnections> servers;
    
    public Server selectServer() {
        return servers.stream()
            .min(Comparator.comparing(ServerWithConnections::getActiveConnections))
            .map(ServerWithConnections::getServer)
            .orElseThrow();
    }
    
    public void incrementConnections(Server server) {
        servers.stream()
            .filter(s -> s.getServer().equals(server))
            .findFirst()
            .ifPresent(ServerWithConnections::incrementConnections);
    }
    
    public void decrementConnections(Server server) {
        servers.stream()
            .filter(s -> s.getServer().equals(server))
            .findFirst()
            .ifPresent(ServerWithConnections::decrementConnections);
    }
}
```

#### 4. IP Hash (Consistent Hashing):

```
Same client IP always goes to same server (session affinity)

hash(client_ip) % num_servers = server_index

Client 192.168.1.1 → always Server 2
Client 192.168.1.2 → always Server 1
Client 192.168.1.3 → always Server 3

Pros: Session persistence without sticky sessions
Cons: Uneven distribution if client IPs not random
```

```java
public class IPHashLoadBalancer {
    private List<Server> servers;
    
    public Server selectServer(String clientIP) {
        int hash = clientIP.hashCode();
        int index = Math.abs(hash) % servers.size();
        return servers.get(index);
    }
}

// Consistent Hashing (better for dynamic server lists)
public class ConsistentHashLoadBalancer {
    private TreeMap<Integer, Server> ring = new TreeMap<>();
    private int numberOfReplicas = 150; // Virtual nodes
    
    public void addServer(Server server) {
        for (int i = 0; i < numberOfReplicas; i++) {
            int hash = hash(server.getId() + ":" + i);
            ring.put(hash, server);
        }
    }
    
    public Server selectServer(String key) {
        int hash = hash(key);
        
        // Find first server clockwise on ring
        Map.Entry<Integer, Server> entry = ring.ceilingEntry(hash);
        
        if (entry == null) {
            entry = ring.firstEntry(); // Wrap around
        }
        
        return entry.getValue();
    }
    
    private int hash(String key) {
        return key.hashCode();
    }
}
```

#### 5. Least Response Time:

```
Route to server with lowest response time + least connections

Server 1: Avg response time 100ms, 5 connections
Server 2: Avg response time  50ms, 3 connections ← Choose
Server 3: Avg response time 150ms, 2 connections

Score = response_time * active_connections
```

```java
public class LeastResponseTimeLoadBalancer {
    private Map<Server, Metrics> serverMetrics = new ConcurrentHashMap<>();
    
    public Server selectServer() {
        return serverMetrics.entrySet().stream()
            .min(Comparator.comparing(entry -> {
                Metrics m = entry.getValue();
                return m.getAverageResponseTime() * m.getActiveConnections();
            }))
            .map(Map.Entry::getKey)
            .orElseThrow();
    }
    
    public void recordResponse(Server server, long responseTime) {
        serverMetrics.get(server).recordResponse(responseTime);
    }
}
```

#### 6. Random:

```java
public class RandomLoadBalancer {
    private List<Server> servers;
    private Random random = new Random();
    
    public Server selectServer() {
        int index = random.nextInt(servers.size());
        return servers.get(index);
    }
}
```

### Health Checks:

```java
@Component
public class HealthCheckScheduler {
    
    @Autowired
    private LoadBalancer loadBalancer;
    
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void checkHealth() {
        for (Server server : loadBalancer.getAllServers()) {
            try {
                HttpResponse response = HttpClient.newHttpClient()
                    .send(HttpRequest.newBuilder()
                        .uri(URI.create(server.getUrl() + "/health"))
                        .timeout(Duration.ofSeconds(2))
                        .build(),
                        HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    loadBalancer.markHealthy(server);
                } else {
                    loadBalancer.markUnhealthy(server);
                }
            } catch (Exception e) {
                loadBalancer.markUnhealthy(server);
            }
        }
    }
}
```

---

## Eventual Consistency

### Consistency Models:

```
Strong Consistency:
┌────────┐ write(x=5) ┌────────┐
│Client 1│────────────▶│Database│
└────────┘             └───┬────┘
                           │ replicate
┌────────┐                 │
│Client 2│ read(x)         ▼
└────┬───┘          ┌────────────┐
     │◄─────────────│  Replicas  │
     │ returns 5    └────────────┘
     
Always returns latest value (high latency)

Eventual Consistency:
┌────────┐ write(x=5) ┌────────┐
│Client 1│────────────▶│Database│
└────────┘             └───┬────┘
                           │ async replicate
┌────────┐                 │
│Client 2│ read(x)         ▼
└────┬───┘          ┌────────────┐
     │◄─────────────│  Replicas  │
     │ returns 3    └────────────┘
    (stale)               ▼
                    Eventually returns 5

May return stale data temporarily (low latency)
```

### BASE vs ACID:

```
ACID (Traditional Databases):
- Atomicity: All or nothing
- Consistency: Valid state always
- Isolation: Transactions isolated
- Durability: Committed data persists

BASE (NoSQL/Distributed):
- Basically Available: System available even during failures
- Soft state: State may change without input (due to eventual consistency)
- Eventually consistent: System becomes consistent over time
```

### Eventual Consistency Patterns:

#### 1. Read-Your-Writes Consistency:

```java
// User always sees their own writes
@Service
public class ReadYourWritesService {
    
    @Autowired
    private PrimaryDatabase primary;
    
    @Autowired
    private List<ReplicaDatabase> replicas;
    
    public void write(String userId, String data) {
        // Write to primary
        primary.write(userId, data);
        
        // Store write timestamp for user
        userWriteTimestamps.put(userId, System.currentTimeMillis());
    }
    
    public String read(String userId) {
        long userLastWrite = userWriteTimestamps.getOrDefault(userId, 0L);
        
        // If recent write, read from primary
        if (System.currentTimeMillis() - userLastWrite < 5000) {
            return primary.read(userId);
        }
        
        // Otherwise, read from replica
        return replicas.get(random.nextInt(replicas.size())).read(userId);
    }
}
```

#### 2. Monotonic Reads:

```java
// User never sees older data after seeing newer data
@Service
public class MonotonicReadsService {
    
    // Track which replica each user reads from
    private Map<String, ReplicaDatabase> userReplicas = new ConcurrentHashMap<>();
    
    public String read(String userId) {
        // Always read from same replica for this user
        ReplicaDatabase replica = userReplicas.computeIfAbsent(
            userId,
            k -> selectReplica()
        );
        
        return replica.read(userId);
    }
}
```

#### 3. Conflict Resolution:

```java
// Last-Write-Wins (LWW)
public class LastWriteWins {
    
    public void resolveConflict(List<Write> concurrentWrites) {
        Write latest = concurrentWrites.stream()
            .max(Comparator.comparing(Write::getTimestamp))
            .orElseThrow();
        
        // Keep the latest write, discard others
        database.write(latest.getKey(), latest.getValue());
    }
}

// Vector Clocks (more sophisticated)
public class VectorClockConflictResolver {
    
    public void resolveConflict(List<VersionedValue> versions) {
        // Compare vector clocks
        List<VersionedValue> concurrent = findConcurrentVersions(versions);
        
        if (concurrent.size() == 1) {
            // Clear winner, no conflict
            database.write(concurrent.get(0));
        } else {
            // True conflict, need application-specific resolution
            VersionedValue merged = applicationMerge(concurrent);
            database.write(merged);
        }
    }
    
    private VersionedValue applicationMerge(List<VersionedValue> versions) {
        // Example: Shopping cart merge
        Set<Item> allItems = new HashSet<>();
        for (VersionedValue v : versions) {
            allItems.addAll(v.getItems());
        }
        return new VersionedValue(allItems);
    }
}
```

### Compensating Transactions (Saga):

```java
// Handle eventual consistency in distributed transactions
@Service
public class OrderSaga {
    
    public void placeOrder(Order order) {
        try {
            // Step 1: Reserve inventory
            inventoryService.reserve(order.getItems());
            
            // Step 2: Process payment
            paymentService.charge(order.getAmount());
            
            // Step 3: Create shipment
            shippingService.createShipment(order);
            
        } catch (InventoryException e) {
            // No compensation needed (first step failed)
            throw e;
            
        } catch (PaymentException e) {
            // Compensate: Release inventory
            inventoryService.release(order.getItems());
            throw e;
            
        } catch (ShippingException e) {
            // Compensate: Refund payment and release inventory
            paymentService.refund(order.getAmount());
            inventoryService.release(order.getItems());
            throw e;
        }
    }
}

// Event-driven Saga (better for microservices)
@Service
public class OrderSagaOrchestrator {
    
    @EventHandler
    public void on(OrderPlacedEvent event) {
        // Start saga
        inventoryService.reserveInventory(event.getOrderId());
    }
    
    @EventHandler
    public void on(InventoryReservedEvent event) {
        paymentService.processPayment(event.getOrderId());
    }
    
    @EventHandler
    public void on(PaymentProcessedEvent event) {
        shippingService.createShipment(event.getOrderId());
    }
    
    @EventHandler
    public void on(PaymentFailedEvent event) {
        // Compensate
        inventoryService.releaseInventory(event.getOrderId());
    }
}
```

### Anti-Entropy and Gossip Protocol:

```java
// Replicas periodically synchronize to resolve conflicts
@Scheduled(fixedRate = 60000) // Every minute
public void antiEntropy() {
    for (ReplicaNode replica : replicas) {
        // Exchange merkle trees to find differences
        MerkleTree localTree = buildMerkleTree(localData);
        MerkleTree remoteTree = replica.getMerkleTree();
        
        List<String> differences = compareTrees(localTree, remoteTree);
        
        // Sync only different keys
        for (String key : differences) {
            syncKey(key, replica);
        }
    }
}
```

---

## Interview Tips

### Key Points to Remember:

1. **Always clarify requirements** before designing
2. **Start with high-level** design, then dive deep
3. **Discuss trade-offs** (there's no perfect solution)
4. **Consider scale** from the beginning
5. **Think about failure scenarios**
6. **Use numbers** (back-of-envelope calculations)

### Common Follow-up Questions:

- "How would you handle 10x more traffic?"
- "What happens if database goes down?"
- "How do you ensure data consistency?"
- "How would you monitor this system?"
- "What are the bottlenecks?"
- "How would you deploy updates with zero downtime?"

### Practice Problems:

1. Design URL shortener
2. Design Twitter feed
3. Design Uber/ride-sharing
4. Design Netflix
5. Design payment system
6. Design distributed cache
7. Design rate limiter
8. Design notification system

Remember: **There's no single correct answer in system design. Focus on demonstrating your thought process, trade-offs, and justifying your decisions!**
