# 🎯 SDE II Interview Preparation Guide
### Sujit Mali — Java | Spring Boot | Kafka | Microservices | RAG

> **How to use this guide:** Each question has a full verbal answer you can rehearse. Scenario-based questions include a STAR-format breakdown. Read every answer aloud at least once — it trains your brain to recall under pressure.

---

## Table of Contents

1. [Java & Spring Boot](#1-java--spring-boot)
2. [System Design & Microservices](#2-system-design--microservices)
3. [Kafka & Event-Driven Architecture](#3-kafka--event-driven-architecture)
4. [Projects — RAG Chatbot & Reinsurance Platform](#4-projects--rag-chatbot--reinsurance-platform)
5. [Data Integrity in AI RAG Chatbot](#5-data-integrity-in-ai-rag-chatbot)

---

## 1. Java & Spring Boot

---

### Q1. What is the difference between `HashMap` and `ConcurrentHashMap`? When would you use one over the other?

**Answer:**

`HashMap` is not thread-safe. If two threads simultaneously write to it, you can get data corruption, infinite loops (in Java 7 due to bucket-linked-list cycles), or missed entries. It is fine only in single-threaded contexts or when confined to a single thread (e.g., within a method scope).

`ConcurrentHashMap` is designed for concurrent access. In Java 8+, it uses a **bucket-level locking** strategy (CAS + synchronized on individual bins), meaning multiple threads can read and write to *different* buckets simultaneously without blocking each other. This is far more efficient than wrapping a `HashMap` in `Collections.synchronizedMap()`, which locks the *entire map* on every operation.

**Key differences at a glance:**

| Feature | HashMap | ConcurrentHashMap |
|---|---|---|
| Thread safety | ❌ Not thread-safe | ✅ Thread-safe |
| Null keys/values | ✅ One null key allowed | ❌ Null keys/values not allowed |
| Performance (concurrent) | Poor — needs external sync | High — segment/bin-level locking |
| Iteration | Fail-fast (throws ConcurrentModificationException) | Weakly consistent (doesn't throw) |

**Real-world example from your work:**  
In the Reinsurance platform, if you cache contract metadata in a shared `Map` that multiple threads read during claim processing, you'd use `ConcurrentHashMap`. A `HashMap` there would be a race condition waiting to happen.

```java
// Wrong — race condition in a shared service bean
private Map<String, ContractMetadata> cache = new HashMap<>();

// Correct — thread-safe shared cache
private Map<String, ContractMetadata> cache = new ConcurrentHashMap<>();
```

---

### Q2. Explain how the JVM handles memory — heap vs. metaspace vs. stack. How did you use this when you reduced memory by 25%?

**Answer:**

The JVM divides memory into several regions:

**Heap** — This is where all object instances live. The heap is further divided into:
- **Young Generation** (Eden + Survivor spaces): Newly created objects go here. Minor GC runs frequently here.
- **Old Generation** (Tenured): Objects that survive multiple GCs get promoted here. Major/Full GC runs here and is expensive.

**Metaspace** (Java 8+ replacement for PermGen) — Stores class metadata — the bytecode definition of classes, method signatures, annotations. Unlike PermGen, Metaspace grows dynamically using native memory, so it doesn't cause `OutOfMemoryError: PermGen space` anymore. But it can still cause `OutOfMemoryError: Metaspace` if you load too many classes (e.g., dynamic class generation).

**Stack** — Each thread has its own stack. It stores method call frames, local primitive variables, and references (not objects). Stack is LIFO. `StackOverflowError` happens when recursion is too deep.

**How you applied this for the 25% memory reduction:**

The problem was that Hibernate entities were being loaded with all associations eagerly — meaning a single `Contract` entity load would also pull in all related `Claims`, `Participants`, and `Documents` from the database into heap memory, even if only the contract header was needed.

Steps you took:
1. **Hibernate lazy loading** — Changed associations from `FetchType.EAGER` to `FetchType.LAZY`. Objects are only loaded when explicitly accessed.
2. **Entity fetch limitations** — Used projections (DTO queries) via `@Query` with JPQL to fetch only required columns instead of full entity graphs.
3. **Caching** — Used Spring's `@Cacheable` with a second-level cache (e.g., Caffeine or EhCache) to keep frequently accessed contract metadata in memory across requests rather than hitting the DB repeatedly.

```java
// Before: loads entire Claim graph into heap
@OneToMany(fetch = FetchType.EAGER)
private List<ClaimDocument> documents;

// After: loads only when accessed
@OneToMany(fetch = FetchType.LAZY)
private List<ClaimDocument> documents;
```

---

### Q3. What are the different types of Garbage Collectors in Java? How would you choose one for claims processing?

**Answer:**

Java offers several GC algorithms, each with different trade-offs between throughput, latency, and memory:

**Serial GC** — Single-threaded. Stops the world (STW) for both minor and major GC. Only suitable for small, single-core applications. Not relevant for your use case.

**Parallel GC (Throughput Collector)** — Multiple threads for GC, but still stops the world. Maximizes throughput at the cost of longer pause times. Good for batch jobs where you don't mind occasional pauses.

**G1GC (Garbage First — default since Java 9)** — Divides heap into equal-sized regions. Incrementally collects, targeting a configurable max pause time (`-XX:MaxGCPauseMillis`). Balances throughput and latency. Best general-purpose choice.

**ZGC (Java 15+ production-ready)** — Mostly concurrent, sub-millisecond pauses regardless of heap size. Best for latency-sensitive applications.

**Shenandoah** — Similar to ZGC, concurrent compaction. Good for large heaps.

**For your claims processing service:**

Claims are latency-sensitive (user-facing approval APIs) but also involve heavy batch processing. The recommendation is:
- **G1GC with tuned pause time** for API-serving pods (`-XX:MaxGCPauseMillis=200`)
- **Parallel GC** for batch processing pods where throughput matters more than pauses

```bash
# For API-serving claims service
-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Xms512m -Xmx2g

# For batch processing job
-XX:+UseParallelGC -Xms1g -Xmx4g
```

---

### Q4. How does `CompletableFuture` work? How is it different from `Future`?

**Answer:**

`Future` (Java 5) represents the result of an async computation, but it is limited — you can only block on `future.get()` to retrieve the result. You cannot chain operations, handle exceptions gracefully, or combine multiple futures without complex, error-prone code.

`CompletableFuture` (Java 8) is a major improvement. It implements both `Future` and `CompletionStage`, allowing you to:
- **Chain transformations** non-blockingly using `thenApply`, `thenCompose`, `thenAccept`
- **Combine futures** using `thenCombine`, `allOf`, `anyOf`
- **Handle exceptions** using `exceptionally`, `handle`
- **Run callbacks** on completion

**Example — parallel claim validation:**

```java
// Run two validations in parallel, combine results
CompletableFuture<Boolean> policyCheck =
    CompletableFuture.supplyAsync(() -> validatePolicy(claimId));

CompletableFuture<Boolean> fraudCheck =
    CompletableFuture.supplyAsync(() -> checkFraudScore(claimId));

CompletableFuture<Void> combined = CompletableFuture
    .allOf(policyCheck, fraudCheck)
    .thenRun(() -> {
        if (policyCheck.join() && fraudCheck.join()) {
            approveClaim(claimId);
        }
    })
    .exceptionally(ex -> {
        log.error("Validation failed", ex);
        return null;
    });
```

With `Future`, you'd have to call `get()` on each one — blocking the thread and making the two calls effectively sequential in practice.

---

### Q5. What is `@Transactional(propagation = REQUIRES_NEW)` vs `REQUIRED`?

**Answer:**

Transaction propagation controls what happens when a transactional method calls another transactional method.

**REQUIRED (default):** If a transaction already exists, join it. If none exists, create one. Both methods share the same transaction — if either fails, everything rolls back.

**REQUIRES_NEW:** Always suspend the current transaction and create a brand new one. The new transaction commits or rolls back independently of the outer one.

**Real-world example from reinsurance:**

Imagine a `processClaimApproval()` method that:
1. Updates the claim status in the database
2. Logs an audit entry

If the audit log write fails, you don't want the claim status update to roll back — that update was valid. So the audit logging method should use `REQUIRES_NEW`:

```java
@Transactional(propagation = Propagation.REQUIRED)
public void processClaimApproval(Long claimId) {
    claimRepository.updateStatus(claimId, Status.APPROVED);
    auditService.log(claimId, "APPROVED"); // inner call
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void log(Long claimId, String action) {
    // This runs in its own transaction
    // Even if this fails, claim status update is already committed
    auditRepository.save(new AuditEntry(claimId, action));
}
```

**Important gotcha:** `REQUIRES_NEW` only works when the inner method is called through a Spring proxy (i.e., from a different bean). Self-invocation (`this.log(...)`) bypasses the proxy and propagation is ignored.

---

### Q6. What is Hibernate's N+1 problem and how did you use lazy loading to address it?

**Answer:**

The N+1 problem occurs when you fetch a list of N entities and Hibernate fires an additional query for each entity to load a related association. Instead of 1 query (join), you get N+1 queries, which can cripple performance.

**Example:**

```java
// Fetch all contracts — 1 query
List<Contract> contracts = contractRepository.findAll();

// For each contract, access claims — triggers N additional queries!
for (Contract c : contracts) {
    System.out.println(c.getClaims().size()); // LAZY proxy fires query here
}
// Total: 1 + N queries
```

**Solutions:**

1. **JOIN FETCH in JPQL** — Most efficient for read-heavy operations:
```java
@Query("SELECT c FROM Contract c JOIN FETCH c.claims WHERE c.status = 'ACTIVE'")
List<Contract> findActiveContractsWithClaims();
```

2. **`@EntityGraph`** — Declarative way to define fetch paths:
```java
@EntityGraph(attributePaths = {"claims", "participants"})
List<Contract> findByStatus(String status);
```

3. **Lazy loading + careful access patterns** — Keep associations lazy and only load them when you genuinely need them, within an open session.

**The risk of lazy loading:** If you access a lazy association outside of a Hibernate session (e.g., after the `@Transactional` method returns), you get `LazyInitializationException`. The fix is either using OpenSessionInView (not recommended for APIs), DTO projections, or ensuring the access happens within the transaction boundary.

---

### Q7. How does `@Async` work under the hood? How do you configure a custom ThreadPool for it?

**Answer:**

When you annotate a method with `@Async`, Spring wraps the method call through a proxy. Instead of executing on the calling thread, it submits the task to an `Executor` (thread pool) and returns immediately. The actual method runs on a separate thread from the pool.

By default, Spring uses a `SimpleAsyncTaskExecutor` which creates a **new thread for every call** — no pooling, no reuse. This is dangerous under load.

**Configuring a proper ThreadPoolTaskExecutor:**

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "claimsExecutor")
    public Executor claimsTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);       // Always-alive threads
        executor.setMaxPoolSize(20);       // Max threads under load
        executor.setQueueCapacity(100);    // Queue before new threads spawn
        executor.setThreadNamePrefix("claims-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}

// Usage
@Async("claimsExecutor")
public CompletableFuture<Void> sendClaimNotification(Long claimId) {
    notificationService.send(claimId);
    return CompletableFuture.completedFuture(null);
}
```

**CallerRunsPolicy** means if the queue and max threads are both full, the calling thread executes the task itself — providing natural back-pressure instead of throwing `RejectedExecutionException`.

---

### Q8. Explain `@Cacheable`, `@CacheEvict`, and `@CachePut`

**Answer:**

These three annotations are the backbone of Spring's declarative caching.

**`@Cacheable`** — On the first call, executes the method and stores the result in cache. On subsequent calls with the same key, returns from cache without executing the method.

```java
@Cacheable(value = "contracts", key = "#contractId")
public ContractMetadata getContractMetadata(Long contractId) {
    return contractRepository.findById(contractId); // Only called on cache miss
}
```

**`@CacheEvict`** — Removes an entry (or all entries) from cache. Use when data is updated or deleted.

```java
@CacheEvict(value = "contracts", key = "#contractId")
public void updateContractTerms(Long contractId, ContractTerms terms) {
    contractRepository.updateTerms(contractId, terms);
}
```

**`@CachePut`** — Always executes the method AND updates the cache. Unlike `@Cacheable`, it never skips execution. Use after a save/update when you want the cache to reflect the new value immediately.

```java
@CachePut(value = "contracts", key = "#result.id")
public ContractMetadata saveContract(Contract contract) {
    return contractRepository.save(contract);
}
```

**Eviction strategy for contract metadata:**

Since contract terms rarely change but are read thousands of times per day, the right strategy is:
- Cache with a TTL (time-to-live) of e.g., 1 hour using Caffeine
- Explicitly evict on contract update events
- Use Kafka to broadcast cache invalidation events across service instances so all pods clear their local caches

---

### 🔶 Scenario: API is slow under load — how do you profile and fix it end-to-end?

**STAR Answer:**

**Situation:** A claims module API endpoint was responding in 4+ seconds under load, causing frontend timeouts.

**Task:** Identify the bottleneck and reduce response time without changing the API contract.

**Action:**

Step 1 — **Application-level profiling**: Added `@Timed` (Micrometer) on the slow endpoint and checked Actuator metrics. Found the service layer took 3.5s of the 4s total.

Step 2 — **SQL profiling**: Enabled Hibernate `show_sql` and `format_sql` in staging. Discovered an N+1 pattern — the API was fetching contracts and then firing one query per contract to load associated participants.

Step 3 — **Fix N+1**: Rewrote the query using `JOIN FETCH` to load contracts and participants in a single query.

Step 4 — **Index verification**: Used `EXPLAIN ANALYZE` on PostgreSQL to check query execution plan. Found a missing index on `contract_status` column. Added the index.

Step 5 — **Caching**: Contract metadata (relatively static) was being fetched on every call. Added `@Cacheable` with Caffeine in-memory cache, TTL 30 minutes.

Step 6 — **Pagination**: The endpoint was returning unbounded result sets. Added `Pageable` to limit results per page.

**Result:** Response time dropped from 4.2s to ~350ms under the same load. Memory usage also improved because we stopped loading full entity graphs.

---

### 🔶 Scenario: `LazyInitializationException` in production

**Answer:**

This exception means Hibernate tried to load a lazy association but there was no active session/transaction to do so.

**Root cause:** A `@Transactional` method fetched an entity with a lazy collection, returned it to the controller, and the controller (outside the transaction boundary) then tried to iterate over the lazy collection.

**Fix options:**

1. **Use DTO projection** — Never expose entities outside the service layer. Map to DTOs inside the transaction:
```java
@Transactional(readOnly = true)
public ContractDTO getContract(Long id) {
    Contract c = contractRepository.findById(id).orElseThrow();
    return new ContractDTO(c.getId(), c.getClaims().size()); // safe — inside tx
}
```

2. **JOIN FETCH** — Eagerly load what you need for this specific use case.

3. **`@Transactional(readOnly = true)`** — Use read-only transactions for GET operations. This also gives a performance hint to Hibernate to skip dirty checking.

---

## 2. System Design & Microservices

---

### Q1. You used the Strangler Fig pattern — explain it in detail

**Answer:**

The Strangler Fig pattern (named after the strangler fig tree that grows around an existing tree and eventually replaces it) is a migration strategy to incrementally replace a monolith with microservices, without a risky big-bang rewrite.

**How it works — 3 phases:**

**Phase 1 — Intercept:** Place an API Gateway or routing layer in front of the monolith. All traffic still goes to the monolith, but you now have a seam to redirect individual routes.

**Phase 2 — Extract & Route:** Identify a bounded context (e.g., the Activity module). Build it as an independent microservice with its own database. Update the routing layer to send Activity-related requests to the new service while all other requests still go to the monolith.

**Phase 3 — Strangle:** Over time, more and more routes are redirected to new microservices. The monolith handles less and less. Eventually, you decommission it.

**Challenges you'd face and how to handle them:**

- **Dual-write period:** If both the monolith and the new Activity service need the same data during transition, you need a temporary synchronization mechanism — either DB-level replication or event-based sync via Kafka.
- **Data ownership:** Once the Activity service has its own DB, you must never let the monolith write directly to it. All writes go through the Activity service's API or Kafka events.
- **Backward compatibility:** The new service must honour the same API contracts as the monolith did during the transition period.

---

### Q2. How do you handle distributed transactions? Saga pattern — choreography vs orchestration?

**Answer:**

In microservices, a single business operation (like processing a claim) can span multiple services — Claims, Contract, Accounting. Traditional 2-phase commit (2PC) is impractical because it locks resources across services and creates tight coupling. Instead, you use the **Saga pattern**.

A Saga is a sequence of local transactions, where each step publishes an event or message to trigger the next step. If a step fails, compensating transactions undo the previous steps.

**Choreography-based Saga:**
Each service listens for events and reacts independently. No central coordinator.

```
ClaimService → publishes ClaimSubmitted
ContractService listens → validates coverage → publishes CoverageValidated
AccountingService listens → reserves funds → publishes FundsReserved
NotificationService listens → sends approval notification
```

If funds reservation fails, AccountingService publishes `FundsReservationFailed`, ContractService listens and reverses coverage validation, etc.

**Pros:** Loose coupling, each service is autonomous.  
**Cons:** Hard to track the overall state, distributed debugging is painful.

**Orchestration-based Saga:**
A central Saga Orchestrator (its own service) coordinates the flow by explicitly calling each service in sequence.

```
SagaOrchestrator:
1. Call ClaimService.submit()   → success
2. Call ContractService.validate() → success
3. Call AccountingService.reserve() → FAIL
4. Compensate: Call ContractService.revert()
5. Compensate: Call ClaimService.cancel()
```

**Pros:** Single place to track state, easier debugging, clear failure handling.  
**Cons:** Orchestrator can become a bottleneck or single point of failure.

**When to choose which:**
- Use **choreography** for simple, 2-3 step flows with well-defined events.
- Use **orchestration** for complex, multi-step business workflows like your multi-level claim approval.

---

### Q3. How does Resilience4j Circuit Breaker work?

**Answer:**

A Circuit Breaker prevents cascading failures by monitoring calls to an external service and "opening" (blocking calls) when failures exceed a threshold.

**Three states:**

**Closed (normal operation):** Calls pass through. Failures are counted. If the failure rate exceeds the threshold (e.g., 50% in the last 10 calls), the circuit transitions to **Open**.

**Open (blocking):** All calls immediately fail with a `CallNotPermittedException` — no actual call is made to the failing service. After a configured wait duration (e.g., 30 seconds), it moves to **Half-Open**.

**Half-Open (probing):** Allows a limited number of test calls. If they succeed, the circuit transitions back to **Closed**. If they fail, it goes back to **Open**.

```java
@Bean
public CircuitBreakerConfig circuitBreakerConfig() {
    return CircuitBreakerConfig.custom()
        .failureRateThreshold(50)           // open if 50% calls fail
        .waitDurationInOpenState(Duration.ofSeconds(30))
        .slidingWindowSize(10)              // count last 10 calls
        .permittedNumberOfCallsInHalfOpenState(3)
        .build();
}

@CircuitBreaker(name = "contractService", fallbackMethod = "fallbackGetContract")
public ContractDTO getContractDetails(Long id) {
    return contractServiceClient.getContract(id);
}

public ContractDTO fallbackGetContract(Long id, Exception ex) {
    log.warn("Circuit open, returning cached contract for {}", id);
    return contractCache.get(id); // serve from cache during outage
}
```

---

### Q4. How do you design idempotent APIs?

**Answer:**

An idempotent API is one where making the same request multiple times produces the same result as making it once. This is critical for claims submission — if a network retry causes a claim to be submitted twice, you must not create two claims.

**Strategy 1 — Idempotency Key:**
The client generates a unique key (UUID) per logical request and sends it as a header (`Idempotency-Key`). The server stores the key + response in a fast store (Redis). On receiving a request, check if the key already exists. If yes, return the stored response. If no, process and store.

```java
@PostMapping("/claims")
public ResponseEntity<ClaimResponse> submitClaim(
    @RequestHeader("Idempotency-Key") String idempotencyKey,
    @RequestBody ClaimRequest request) {

    // Check cache first
    ClaimResponse existing = idempotencyStore.get(idempotencyKey);
    if (existing != null) return ResponseEntity.ok(existing);

    // Process claim
    ClaimResponse response = claimService.process(request);

    // Store result
    idempotencyStore.put(idempotencyKey, response, Duration.ofHours(24));
    return ResponseEntity.ok(response);
}
```

**Strategy 2 — Natural idempotency via unique constraint:**
For claim submission, store a composite unique constraint on `(policy_id, incident_date, claim_type)`. A duplicate submission simply triggers a DB constraint violation, which you handle gracefully.

**Strategy 3 — Optimistic locking:**
Use `@Version` on entities to prevent concurrent updates from overwriting each other.

---

### 🔶 Scenario: Duplicate Kafka events processing same activity twice

**Answer:**

**Root cause:** The consumer committed the offset before the processing completed (or the pod crashed after processing but before committing). On restart, Kafka delivered the same message again.

**Fix:**

Step 1 — Switch to **manual offset commit** in Spring Kafka:
```yaml
spring.kafka.consumer.enable-auto-commit: false
spring.kafka.listener.ack-mode: MANUAL_IMMEDIATE
```

Step 2 — Implement **idempotent consumer** using a processed-message store:
```java
@KafkaListener(topics = "activity-events")
public void handleActivity(ActivityEvent event, Acknowledgment ack) {
    String messageId = event.getEventId();

    if (processedEventStore.exists(messageId)) {
        log.info("Duplicate event {}, skipping", messageId);
        ack.acknowledge(); // still commit offset
        return;
    }

    activityService.process(event);
    processedEventStore.mark(messageId, Duration.ofDays(7));
    ack.acknowledge(); // commit only after successful processing
}
```

The `processedEventStore` can be a Redis SET or a DB table with a unique constraint on `event_id`.

---

### 🔶 Scenario: Designing a multi-level claim approval workflow

**Answer:**

See the accompanying HTML visual file `kafka_approval_flow.html` for the full flow diagram.

**Services involved:**
- **Claims Service** — Accepts submission, creates claim record
- **Approval Orchestrator** — Manages state machine for approval levels
- **Rule Engine Service** — Determines required approval level based on claim amount
- **Notification Service** — Sends emails/push at each stage
- **Accounting Service** — Releases payment after final approval

**Kafka topics:**
- `claim.submitted` — Claims Service → Approval Orchestrator
- `approval.requested.level1` — Level 1 approver queue
- `approval.requested.level2` — Level 2 approver queue (claims > ₹5 lakh)
- `claim.approved` — Final approval → Accounting Service
- `claim.rejected` — Final rejection → Notification Service
- `approval.dlq` — Dead letter queue for failed events

**State management:**
The Approval Orchestrator maintains a `claim_approval_state` table tracking current state for each claim. This ensures that even if the orchestrator restarts, it can resume from the correct state.

---

## 3. Kafka & Event-Driven Architecture

---

### Q1. How does Kafka guarantee message ordering?

**Answer:**

Kafka guarantees ordering **within a partition**. A topic can have multiple partitions, and Kafka only ensures that messages within a single partition are consumed in the order they were produced.

**Implication for your claims workflow:**

If all events for a specific claim must be processed in order (submitted → validated → approved), all events for the same claim must land in the same partition. You achieve this by using the **claim ID as the message key**:

```java
kafkaTemplate.send("claim-events", claimId.toString(), claimEvent);
```

Kafka hashes the key and assigns it to a partition. All messages with the same claim ID will always go to the same partition, ensuring order.

**What breaks ordering:**
- Using a null key — messages are round-robin distributed across partitions
- Having more consumers in a group than partitions — some consumers sit idle
- Consumer failures causing rebalance — temporary ordering disruption during rebalance

---

### Q2. Explain consumer groups and what happens during a rebalance

**Answer:**

A **consumer group** is a set of consumers that collectively consume a topic. Kafka ensures each partition is consumed by exactly one consumer within a group. This enables horizontal scaling — adding consumers increases parallelism up to the number of partitions.

**Example:** Topic `claim-events` has 6 partitions. A consumer group with 3 consumers will have each consumer handling 2 partitions.

**Rebalance** occurs when the group membership changes — a consumer joins, crashes, or a new partition is added. During rebalance:
1. All consumers stop consuming (this is the "stop the world" phase)
2. The Group Coordinator (a Kafka broker) assigns partitions to consumers using a partition assignment strategy
3. Consumers resume from their last committed offset

**Problems during rebalance:**
- Processing gaps during the stop phase
- Duplicate processing if a consumer processed messages but hadn't committed offsets before crashing

**Mitigation — `CooperativeStickyAssignor`:**

In Spring Kafka, configure the cooperative sticky assignor which minimizes the number of partitions that move between consumers during a rebalance, reducing disruption:

```yaml
spring.kafka.consumer.partition-assignment-strategy: 
  org.apache.kafka.clients.consumer.CooperativeStickyAssignor
```

---

### Q3. At-least-once vs at-most-once vs exactly-once delivery

**Answer:**

**At-most-once:** Message is delivered zero or one time. The producer fires and forgets. Offset is committed before processing. If processing fails, the message is lost. Use for non-critical logs where loss is acceptable.

**At-least-once (most common):** Message is delivered one or more times. Offset is committed only after successful processing. If the consumer crashes after processing but before committing, it re-processes on restart. Can cause duplicates. Requires idempotent consumers.

**Exactly-once:** Message is processed exactly once end-to-end. Achievable in Kafka with:
- **Idempotent producer** (`enable.idempotence=true`) — eliminates producer-side duplicates
- **Transactional producer** — wraps produce + offset commit in a single atomic transaction

```yaml
spring.kafka.producer.properties:
  enable.idempotence: true
  transactional.id: claim-producer-1
  acks: all
```

**In your reinsurance platform:** You used at-least-once with idempotent consumers (event ID deduplication) — this is the right pragmatic choice. True exactly-once has performance overhead and is rarely worth it unless the business absolutely cannot tolerate duplicates.

---

### Q4. What is a Dead Letter Topic (DLT) and how do you configure it in Spring Kafka?

**Answer:**

A Dead Letter Topic (DLT) is a separate Kafka topic where messages that fail processing after all retry attempts are sent. It prevents bad messages from blocking the main consumer indefinitely while preserving them for later analysis and reprocessing.

```java
@Bean
public DefaultErrorHandler errorHandler(KafkaTemplate<?, ?> kafkaTemplate) {
    // Retry 3 times with 1s, 2s, 4s backoff
    ExponentialBackOffWithMaxRetries backoff = new ExponentialBackOffWithMaxRetries(3);
    backoff.setInitialInterval(1000L);
    backoff.setMultiplier(2.0);

    DeadLetterPublishingRecoverer recoverer =
        new DeadLetterPublishingRecoverer(kafkaTemplate,
            (record, ex) -> new TopicPartition(record.topic() + ".DLT",
                                               record.partition()));

    return new DefaultErrorHandler(recoverer, backoff);
}
```

Messages in the DLT have extra headers added by Spring — original topic, original offset, exception class, and exception message — making debugging straightforward.

---

### Q5. Key producer configurations — `acks`, `retries`, `linger.ms`, `batch.size`

**Answer:**

**`acks`** — Controls when the producer considers a send successful:
- `acks=0` — Fire and forget. No confirmation. Fastest, but data loss possible.
- `acks=1` — Leader broker acknowledges. Good balance. Data loss if leader crashes before replication.
- `acks=all` (or `-1`) — All in-sync replicas (ISR) acknowledge. Safest. Use for financial/claims data.

**`retries`** — How many times the producer retries on transient failures. Set to a large number (e.g., `Integer.MAX_VALUE`) when using idempotent producers — Kafka manages deduplication.

**`linger.ms`** — How long the producer waits before sending a batch, hoping more messages arrive to batch together. `linger.ms=0` sends immediately (low latency). `linger.ms=10` waits 10ms for more messages (higher throughput).

**`batch.size`** — Maximum size (bytes) of a message batch. Larger batches mean fewer network calls but higher memory usage and latency.

**Tuning for your claim events (reliability-first):**

```yaml
spring.kafka.producer:
  acks: all
  retries: 2147483647
  properties:
    enable.idempotence: true
    max.in.flight.requests.per.connection: 5
    linger.ms: 5
    batch.size: 16384
```

---

### 🔶 Scenario: One Kafka partition receiving 80% of all messages

**Answer:**

**Root cause:** The message key has low cardinality or a hot key. For example, if you're keying by `status` (e.g., "PENDING"), all messages with status PENDING land on the same partition.

**Diagnosis:**

```java
// Check partition offset lag using Kafka consumer group describe
kafka-consumer-groups.sh --describe --group claims-group --bootstrap-server localhost:9092
```

**Fix options:**

1. **Change partitioning key** — Use a high-cardinality key like `claimId` (UUID) instead of `status`.

2. **Custom partitioner** — If you need to key by a low-cardinality field for ordering, create a custom partitioner that hashes `claimId` but also considers the target partition load.

```java
public class StickyLoadPartitioner implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] keyBytes,
                         Object value, byte[] valueBytes, Cluster cluster) {
        int numPartitions = cluster.partitionCountForTopic(topic);
        // Salt the key with a time bucket to spread load
        String saltedKey = key + "-" + (System.currentTimeMillis() / 60000);
        return Math.abs(saltedKey.hashCode() % numPartitions);
    }
}
```

3. **Increase partition count** — But note: you cannot decrease partitions once created, and increasing partitions breaks ordering guarantees for existing keys.

---

## 4. Projects — RAG Chatbot & Reinsurance Platform

---

### Q1. Explain the RAG architecture end-to-end

**Answer:**

See `rag_pipeline_flow.html` for the visual representation.

RAG (Retrieval Augmented Generation) combines information retrieval with generative AI. Instead of relying on the LLM's training data alone, RAG retrieves relevant information from your private knowledge base at query time and provides it as context to the LLM.

**Ingestion Pipeline (offline):**

```
PDF/Text Documents
    ↓
Document Parser (Apache PDFBox / Tika)
    ↓
Text Chunker (split into ~500 token chunks with overlap)
    ↓
Embedding Model (OpenAI text-embedding-ada-002)
    ↓
Vector Database (pgvector / Pinecone / Weaviate)
```

**Query Pipeline (online):**

```
User Question
    ↓
Embed the question using the same embedding model
    ↓
Similarity Search in Vector DB (cosine similarity, top-K results)
    ↓
Retrieved Chunks (the most relevant document sections)
    ↓
Prompt Assembly: System Prompt + Retrieved Chunks + User Question
    ↓
LLM (GPT-4 / Claude) generates grounded answer
    ↓
Advisor Chain validation (safety, logging)
    ↓
Response to User
```

**Chunking strategy matters:** If chunks are too large, they contain noise. If too small, they lose context. A good default is ~500 tokens with a 50-token overlap so a clause split across chunk boundaries is still captured.

---

### Q2. What is an embedding model and how does similarity search work?

**Answer:**

An embedding model converts text into a dense vector of floating-point numbers (e.g., 1536 dimensions for OpenAI's `text-embedding-ada-002`). This vector captures the semantic meaning of the text — similar meanings produce vectors that are geometrically close in the high-dimensional space.

**Example:**
- "The policyholder must notify within 30 days" → `[0.023, -0.451, 0.892, ...]`
- "Claim must be filed within 30 days of incident" → `[0.019, -0.443, 0.901, ...]` (close in vector space)
- "The premium is due quarterly" → `[0.412, 0.123, -0.234, ...]` (far in vector space)

**Similarity search using cosine similarity:**

Cosine similarity measures the angle between two vectors. A value of 1 means identical direction (semantically similar), 0 means orthogonal (unrelated), -1 means opposite.

```sql
-- pgvector similarity search
SELECT content, (embedding <=> query_embedding) AS distance
FROM document_chunks
ORDER BY embedding <=> query_embedding
LIMIT 5;
```

The `<=>` operator in pgvector computes cosine distance. You retrieve the top-K closest chunks and pass them to the LLM.

---

### Q3. Walk me through the Claims module — how does a claim move from submission to approval via Kafka?

**Answer:**

**Step 1 — Submission:**
Client calls `POST /claims/submit`. Claims Service validates the request, saves the claim with status `DRAFT`, and publishes a `ClaimSubmitted` event to the `claim.submitted` Kafka topic.

**Step 2 — Rule Evaluation:**
Rule Engine Service consumes `ClaimSubmitted`. It evaluates business rules:
- Claim ≤ ₹1 lakh → auto-approve
- ₹1 lakh to ₹5 lakh → Level 1 approval needed
- > ₹5 lakh → Level 1 + Level 2 approval needed

Publishes `ApprovalLevelDetermined` event.

**Step 3 — Approval Routing:**
Approval Orchestrator consumes the event, creates approval tasks in the `claim_approval` table, and publishes to `approval.requested.level1`.

**Step 4 — Approver Action:**
When an approver acts (approve/reject), it publishes `ApprovalDecision` event.

**Step 5 — Final Decision:**
Approval Orchestrator checks if all required levels are approved. If yes, publishes `ClaimApproved` to `claim.approved`. Accounting Service consumes this and initiates payment.

**State tracking:**

```java
public enum ClaimStatus {
    DRAFT, SUBMITTED, UNDER_REVIEW, LEVEL1_APPROVED,
    LEVEL2_REQUIRED, LEVEL2_APPROVED, APPROVED, REJECTED, PAID
}
```

---

### Q4. How did you implement caching for contract metadata?

**Answer:**

Contract metadata (terms, limits, participant details) is read on almost every claim operation but changes infrequently (only during contract amendments, which happen perhaps monthly).

**Implementation:**

```java
@Service
public class ContractMetadataService {

    @Cacheable(value = "contractMetadata", key = "#contractId",
               condition = "#contractId != null")
    public ContractMetadata getMetadata(Long contractId) {
        return contractRepository.findMetadataById(contractId);
    }

    @CacheEvict(value = "contractMetadata", key = "#contractId")
    public void onContractAmended(Long contractId) {
        // Cache entry removed; next read will fetch fresh from DB
    }
}
```

**Caffeine cache config:**

```java
@Bean
public CacheManager cacheManager() {
    CaffeineCacheManager manager = new CaffeineCacheManager("contractMetadata");
    manager.setCaffeine(Caffeine.newBuilder()
        .maximumSize(1000)           // max 1000 contracts cached
        .expireAfterWrite(1, TimeUnit.HOURS)
        .recordStats());             // expose hit/miss rate to Actuator
    return manager;
}
```

**Multi-instance cache invalidation via Kafka:**

In a microservices deployment with multiple pods, a `@CacheEvict` in one pod doesn't clear the cache in other pods. The solution:
1. When a contract is amended, publish a `ContractAmended` Kafka event.
2. All pods subscribe to this event and evict from their local cache.

---

## 5. Data Integrity in AI RAG Chatbot

---

### Overview

Data integrity in a RAG chatbot is not a single check — it spans the entire pipeline across 5 layers. Understanding all 5 layers is what distinguishes a senior engineer's answer from a junior one.

See `data_integrity_rag.html` for the visual layer diagram.

---

### Layer 1: Ingestion Integrity

**What can go wrong:**
- Corrupt or partial PDF parse (especially scanned documents)
- Incorrect chunking splitting a clause mid-sentence
- Silent embedding failures (zero-vector stored)
- Duplicate documents re-ingested without deduplication

**How to handle it:**

Maintain a **document registry** — a metadata table with document ID, content hash (SHA-256), version, ingestion timestamp, and status.

```java
// Idempotent ingestion
public void ingestDocument(File document) {
    String contentHash = sha256(document);

    if (documentRegistry.existsByHash(contentHash)) {
        log.info("Document already ingested, skipping: {}", document.getName());
        return;
    }

    List<String> chunks = chunker.split(document, 500, 50); // size, overlap
    for (String chunk : chunks) {
        float[] embedding = embeddingModel.embed(chunk);
        vectorStore.save(new VectorEntry(documentId, chunk, embedding));
    }

    documentRegistry.save(new DocumentRecord(documentId, contentHash, NOW));
}
```

---

### Layer 2: Stale Embedding Integrity

**What can go wrong:**
When a policy document is updated (e.g., claim limit changes), the old chunks and embeddings remain in the vector store. The chatbot continues to serve stale information with full confidence.

**This is the most dangerous integrity issue** — no error is thrown, the system just silently returns wrong answers.

**How to handle it:**

Delete-and-re-ingest strategy:

```java
public void updateDocument(Long documentId, File updatedDocument) {
    // Step 1: Delete all existing embeddings for this document
    vectorStore.deleteByDocumentId(documentId);

    // Step 2: Re-ingest fresh
    ingestDocument(updatedDocument);

    // Step 3: Update registry
    documentRegistry.updateVersion(documentId);
}
```

This can be triggered manually, or automatically via a document management webhook when a policy is amended.

---

### Layer 3: Retrieval Integrity

**What can go wrong:**
- Threshold too low → irrelevant chunks retrieved → LLM hallucinates using wrong context
- Threshold too high → relevant chunks filtered out → LLM says "I don't know"
- Top-K too small → important context missed
- Top-K too large → noise dominates the context window

**How to calibrate:**

Build an evaluation dataset of 50–100 known question-answer pairs. Measure Precision@K (what fraction of retrieved chunks are relevant) and Recall@K (what fraction of relevant chunks were retrieved) at different threshold values. Plot the precision-recall curve and pick the threshold that best meets your use case.

```java
// Retrieval with configurable threshold
List<VectorSearchResult> results = vectorStore.similaritySearch(
    queryEmbedding,
    topK = 5,
    minSimilarityScore = 0.78   // tuned via offline evaluation
);
```

---

### Layer 4: Response Grounding Integrity

**What can go wrong:**
The LLM generates an answer that goes beyond the retrieved context — mixing retrieved facts with hallucinated details.

**How to handle it — system prompt engineering:**

```
You are an insurance knowledge assistant.
Answer ONLY based on the provided context sections below.
If the answer is not found in the context, say exactly:
"I don't have enough information in the knowledge base to answer this question."
Do NOT use any outside knowledge. Do NOT speculate.
```

The safety advisor in your advisor chain can post-process the response and flag or block responses that contain phrases indicating speculation ("I believe", "it's likely that") rather than factual retrieval.

---

### Layer 5: Conversational Context Integrity

**What can go wrong:**
In a multi-turn conversation, early user statements can drift into later answers. For example:
- User: "Assume my deductible is ₹500."
- 3 turns later, the LLM includes ₹500 in its calculation blending it with retrieved data.

**How to handle it:**

Explicitly structure the conversation memory so the system prompt always overrides user-stated assumptions:

```
System: [Knowledge base context always takes precedence]
Memory: [Prior conversation turns]
Current Query: [User's latest message]

Rule: If user-stated information conflicts with the knowledge base, always use the knowledge base.
```

---

### Interview Summary: One-line framing to open with

> *"Data integrity in a RAG system is not a single concern — it spans five layers: ingestion correctness, vector store freshness, retrieval relevance, response grounding, and conversational context consistency. I addressed each of these in the design of our Insurance Knowledge Assistant."*

---

*End of Interview Preparation Guide — Sujit Mali*

*Generated for SDE II Interview Preparation | All examples are grounded in real project experience*
