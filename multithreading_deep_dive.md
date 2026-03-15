# Complete Multithreading Deep Dive - Interview Guide

## Table of Contents
1. [Thread Basics & Lifecycle](#thread-basics--lifecycle)
2. [Synchronization Mechanisms](#synchronization-mechanisms)
3. [Thread Communication](#thread-communication)
4. [Concurrent Collections](#concurrent-collections)
5. [Executor Framework](#executor-framework)
6. [Advanced Concurrency Utilities](#advanced-concurrency-utilities)
7. [Common Threading Problems](#common-threading-problems)
8. [Best Practices & Patterns](#best-practices--patterns)
9. [Interview Scenarios](#interview-scenarios)

---

## Thread Basics & Lifecycle

### Complete Thread State Diagram:

```
                    start()
NEW ────────────────────────▶ RUNNABLE ◄──────────────┐
(created but              (ready to run or           │
not started)               currently running)         │
                                   │                   │
                                   │                   │
            ┌──────────────────────┼──────────────┐   │
            │                      │              │   │
    sleep()/join()            wait()         I/O or   │
    with timeout              on monitor     lock     │
            │                      │          wait    │
            ▼                      ▼              ▼   │
       TIMED_WAITING           WAITING        BLOCKED │
       (sleeping for        (waiting for     (waiting │
        specific time)       notification)    for lock)
            │                      │              │   │
            │                      │              │   │
            │   timeout expires/   │ notify()/    │   │ lock
            │   join() completes   │ notifyAll()  │   │ acquired
            └──────────────────────┴──────────────┴───┘
                                   │
                                   │ run() completes
                                   │ or exception
                                   ▼
                              TERMINATED
                           (thread finished)
```

### Creating and Starting Threads:

```java
// Method 1: Extend Thread class
class WorkerThread extends Thread {
    private String taskName;
    
    public WorkerThread(String taskName) {
        this.taskName = taskName;
    }
    
    @Override
    public void run() {
        System.out.println(taskName + " started by " + Thread.currentThread().getName());
        try {
            for (int i = 1; i <= 5; i++) {
                System.out.println(taskName + " - Step " + i);
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.out.println(taskName + " was interrupted");
            Thread.currentThread().interrupt(); // Restore interrupt status
        }
        System.out.println(taskName + " completed");
    }
}

// Method 2: Implement Runnable (Preferred - more flexible)
class Task implements Runnable {
    private String taskName;
    
    public Task(String taskName) {
        this.taskName = taskName;
    }
    
    @Override
    public void run() {
        System.out.println(taskName + " executing in " + Thread.currentThread().getName());
        // Task logic
    }
}

// Method 3: Lambda/Anonymous (Java 8+)
public class ThreadCreationDemo {
    public static void main(String[] args) {
        // Using Thread class
        WorkerThread worker = new WorkerThread("Task-1");
        worker.start(); // Don't call run() directly!
        
        // Using Runnable
        Thread thread1 = new Thread(new Task("Task-2"));
        thread1.start();
        
        // Using Lambda
        Thread thread2 = new Thread(() -> {
            System.out.println("Lambda task executing");
        });
        thread2.start();
        
        // Using method reference
        Thread thread3 = new Thread(ThreadCreationDemo::myTask);
        thread3.start();
    }
    
    public static void myTask() {
        System.out.println("Method reference task");
    }
}
```

### Thread Properties and Methods:

```java
public class ThreadPropertiesDemo {
    public static void main(String[] args) {
        Thread thread = new Thread(() -> {
            // Inside thread
            Thread current = Thread.currentThread();
            System.out.println("Thread Name: " + current.getName());
            System.out.println("Thread ID: " + current.getId());
            System.out.println("Thread Priority: " + current.getPriority());
            System.out.println("Thread State: " + current.getState());
            System.out.println("Is Daemon: " + current.isDaemon());
            System.out.println("Is Alive: " + current.isAlive());
        });
        
        // Set properties before starting
        thread.setName("MyCustomThread");
        thread.setPriority(Thread.MAX_PRIORITY); // 1-10, default is 5
        thread.setDaemon(true); // Daemon threads don't prevent JVM exit
        
        System.out.println("Before start: " + thread.getState()); // NEW
        thread.start();
        System.out.println("After start: " + thread.getState()); // RUNNABLE
        
        // Wait for thread to complete
        try {
            thread.join(); // Main thread waits for 'thread' to die
            System.out.println("After join: " + thread.getState()); // TERMINATED
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```

### Thread Priority:

```java
public class ThreadPriorityDemo {
    public static void main(String[] args) {
        Thread lowPriority = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                System.out.println("Low priority: " + i);
            }
        });
        
        Thread highPriority = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                System.out.println("HIGH PRIORITY: " + i);
            }
        });
        
        lowPriority.setPriority(Thread.MIN_PRIORITY);   // 1
        highPriority.setPriority(Thread.MAX_PRIORITY);  // 10
        // Thread.NORM_PRIORITY is 5 (default)
        
        lowPriority.start();
        highPriority.start();
        
        // Note: Priority is just a hint to scheduler
        // OS may or may not honor it
    }
}
```

---

## Synchronization Mechanisms

### 1. Synchronized Keyword

```java
public class SynchronizationDemo {
    private int counter = 0;
    
    // Synchronized method - locks on 'this'
    public synchronized void increment() {
        counter++;
    }
    
    // Equivalent synchronized block
    public void incrementBlock() {
        synchronized(this) {
            counter++;
        }
    }
    
    // Synchronized static method - locks on Class object
    private static int staticCounter = 0;
    
    public static synchronized void incrementStatic() {
        staticCounter++;
    }
    
    // Equivalent
    public static void incrementStaticBlock() {
        synchronized(SynchronizationDemo.class) {
            staticCounter++;
        }
    }
    
    // Fine-grained locking with custom lock objects
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();
    private int balance1 = 0;
    private int balance2 = 0;
    
    public void updateBalance1(int amount) {
        synchronized(lock1) {
            balance1 += amount;
        }
    }
    
    public void updateBalance2(int amount) {
        synchronized(lock2) {  // Different lock - can run concurrently
            balance2 += amount;
        }
    }
}
```

### 2. ReentrantLock (More Flexible)

```java
import java.util.concurrent.locks.*;
import java.util.concurrent.TimeUnit;

public class ReentrantLockDemo {
    private final ReentrantLock lock = new ReentrantLock();
    private int counter = 0;
    
    // Basic usage
    public void increment() {
        lock.lock();
        try {
            counter++;
        } finally {
            lock.unlock(); // Always unlock in finally!
        }
    }
    
    // Try lock - non-blocking attempt
    public boolean tryIncrement() {
        if (lock.tryLock()) {
            try {
                counter++;
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false; // Couldn't acquire lock
    }
    
    // Try lock with timeout
    public boolean tryIncrementTimeout() {
        try {
            if (lock.tryLock(2, TimeUnit.SECONDS)) {
                try {
                    counter++;
                    return true;
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
    
    // Check lock status
    public void checkLockStatus() {
        System.out.println("Is locked: " + lock.isLocked());
        System.out.println("Hold count: " + lock.getHoldCount());
        System.out.println("Queue length: " + lock.getQueueLength());
        System.out.println("Is fair: " + lock.isFair());
    }
    
    // Fair lock - threads acquire lock in order
    private final ReentrantLock fairLock = new ReentrantLock(true);
}
```

### 3. ReadWriteLock

```java
import java.util.concurrent.locks.*;

public class ReadWriteLockDemo {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();
    private String data = "";
    
    // Multiple threads can read simultaneously
    public String read() {
        readLock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " reading...");
            Thread.sleep(1000); // Simulate read operation
            return data;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            readLock.unlock();
        }
    }
    
    // Only one thread can write at a time
    public void write(String newData) {
        writeLock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + " writing...");
            Thread.sleep(1000); // Simulate write operation
            data = newData;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            writeLock.unlock();
        }
    }
    
    public static void main(String[] args) {
        ReadWriteLockDemo demo = new ReadWriteLockDemo();
        
        // Multiple readers - execute concurrently
        for (int i = 0; i < 3; i++) {
            new Thread(() -> demo.read(), "Reader-" + i).start();
        }
        
        // One writer - blocks all readers and writers
        new Thread(() -> demo.write("New Data"), "Writer").start();
    }
}
```

### 4. Volatile Keyword

```java
public class VolatileDemo {
    // Without volatile - thread may cache value
    private boolean running = false;
    
    // With volatile - always read from main memory
    private volatile boolean runningVolatile = false;
    
    // Visibility problem demonstration
    public void demonstrateVisibilityProblem() {
        new Thread(() -> {
            while (!running) {
                // Thread may never see running=true due to caching
            }
            System.out.println("Thread stopped");
        }).start();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        running = true; // Other thread might not see this change!
    }
    
    // Correct approach with volatile
    public void demonstrateVolatile() {
        new Thread(() -> {
            while (!runningVolatile) {
                // Will definitely see runningVolatile=true
            }
            System.out.println("Thread stopped correctly");
        }).start();
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        runningVolatile = true; // Immediately visible to all threads
    }
    
    // Double-checked locking requires volatile
    private volatile Singleton instance;
    
    public Singleton getSingleton() {
        if (instance == null) {
            synchronized(this) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
    
    // Important: volatile does NOT make compound operations atomic
    private volatile int counter = 0;
    
    public void increment() {
        counter++; // NOT atomic! Still needs synchronization
    }
    
    // For atomic operations, use AtomicInteger
    private AtomicInteger atomicCounter = new AtomicInteger(0);
    
    public void incrementAtomic() {
        atomicCounter.incrementAndGet(); // Atomic operation
    }
}
```

---

## Thread Communication

### 1. wait(), notify(), notifyAll()

```java
public class ProducerConsumerWaitNotify {
    private final Queue<Integer> queue = new LinkedList<>();
    private final int MAX_SIZE = 5;
    
    // Producer adds items
    public synchronized void produce(int item) throws InterruptedException {
        // Wait while queue is full
        while (queue.size() == MAX_SIZE) {
            System.out.println("Queue full, producer waiting...");
            wait(); // Releases lock and waits
        }
        
        queue.add(item);
        System.out.println("Produced: " + item + ", Queue size: " + queue.size());
        
        notifyAll(); // Wake up all waiting threads
    }
    
    // Consumer removes items
    public synchronized int consume() throws InterruptedException {
        // Wait while queue is empty
        while (queue.isEmpty()) {
            System.out.println("Queue empty, consumer waiting...");
            wait(); // Releases lock and waits
        }
        
        int item = queue.poll();
        System.out.println("Consumed: " + item + ", Queue size: " + queue.size());
        
        notifyAll(); // Wake up all waiting threads
        return item;
    }
    
    public static void main(String[] args) {
        ProducerConsumerWaitNotify pc = new ProducerConsumerWaitNotify();
        
        // Producer thread
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                try {
                    pc.produce(i);
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        
        // Consumer thread
        Thread consumer = new Thread(() -> {
            for (int i = 1; i <= 10; i++) {
                try {
                    pc.consume();
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        
        producer.start();
        consumer.start();
    }
}
```

### 2. Condition Variables (Better than wait/notify)

```java
import java.util.concurrent.locks.*;

public class ProducerConsumerCondition {
    private final Queue<Integer> queue = new LinkedList<>();
    private final int MAX_SIZE = 5;
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    
    public void produce(int item) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == MAX_SIZE) {
                notFull.await(); // Wait for space
            }
            
            queue.add(item);
            System.out.println("Produced: " + item);
            
            notEmpty.signal(); // Signal consumers
        } finally {
            lock.unlock();
        }
    }
    
    public int consume() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                notEmpty.await(); // Wait for item
            }
            
            int item = queue.poll();
            System.out.println("Consumed: " + item);
            
            notFull.signal(); // Signal producers
            return item;
        } finally {
            lock.unlock();
        }
    }
}
```

### 3. BlockingQueue (Best Approach)

```java
import java.util.concurrent.*;

public class ProducerConsumerBlockingQueue {
    private final BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(5);
    
    // Producer - automatically blocks when full
    public void produce(int item) throws InterruptedException {
        queue.put(item); // Blocks if queue is full
        System.out.println("Produced: " + item);
    }
    
    // Consumer - automatically blocks when empty
    public int consume() throws InterruptedException {
        int item = queue.take(); // Blocks if queue is empty
        System.out.println("Consumed: " + item);
        return item;
    }
    
    // Non-blocking alternatives
    public boolean tryProduce(int item) {
        return queue.offer(item); // Returns false if full
    }
    
    public Integer tryConsume() {
        return queue.poll(); // Returns null if empty
    }
    
    // With timeout
    public boolean tryProduceTimeout(int item) throws InterruptedException {
        return queue.offer(item, 2, TimeUnit.SECONDS);
    }
}
```

---

## Concurrent Collections

### ConcurrentHashMap Deep Dive:

```java
import java.util.concurrent.*;

public class ConcurrentHashMapDemo {
    public static void main(String[] args) {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        
        // 1. Thread-safe basic operations
        map.put("key1", 100);
        map.get("key1");
        map.remove("key1");
        
        // 2. Atomic operations
        map.putIfAbsent("key2", 200); // Add only if absent
        map.replace("key2", 200, 300); // Replace only if old value matches
        map.remove("key2", 300); // Remove only if value matches
        
        // 3. Compute operations (atomic)
        // Increment counter atomically
        map.compute("counter", (key, oldValue) -> {
            return oldValue == null ? 1 : oldValue + 1;
        });
        
        // Compute only if absent
        map.computeIfAbsent("key3", key -> {
            System.out.println("Computing value for " + key);
            return key.length(); // Expensive computation
        });
        
        // Compute only if present
        map.computeIfPresent("key3", (key, oldValue) -> {
            return oldValue * 2;
        });
        
        // 4. Merge operation (atomic)
        // Word frequency counter
        String[] words = {"apple", "banana", "apple", "cherry", "banana", "apple"};
        ConcurrentHashMap<String, Integer> wordCount = new ConcurrentHashMap<>();
        
        for (String word : words) {
            wordCount.merge(word, 1, Integer::sum);
            // If key exists: merge with function
            // If key doesn't exist: use provided value
        }
        
        System.out.println(wordCount);
        // Output: {apple=3, banana=2, cherry=1}
        
        // 5. Bulk operations with parallelism threshold
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        
        // forEach with parallelism (1 = parallel if size > 1)
        map.forEach(1, (key, value) -> {
            System.out.println(key + " = " + value);
        });
        
        // search - returns first non-null result
        String result = map.search(1, (key, value) -> {
            return value > 2 ? key : null;
        });
        
        // reduce - parallel reduction
        Integer sum = map.reduce(1,
            (key, value) -> value,           // Transformer
            (v1, v2) -> v1 + v2              // Reducer
        );
        
        // 6. Concurrent iteration (weakly consistent)
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            // Safe to iterate while other threads modify
            // May or may not see concurrent modifications
        }
    }
}
```

### Other Concurrent Collections:

```java
import java.util.concurrent.*;

public class ConcurrentCollectionsOverview {
    
    // 1. CopyOnWriteArrayList - snapshot iteration
    public void demonstrateCopyOnWriteArrayList() {
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        
        list.add("Item 1");
        list.add("Item 2");
        
        // Iteration uses snapshot - won't see concurrent changes
        for (String item : list) {
            System.out.println(item);
            list.add("Item 3"); // Safe, but won't appear in this iteration
        }
        
        // Good for: Many reads, few writes
        // Bad for: Frequent writes (expensive copy)
    }
    
    // 2. ConcurrentLinkedQueue - non-blocking queue
    public void demonstrateConcurrentLinkedQueue() {
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        
        queue.offer("Item 1"); // Add to tail
        queue.offer("Item 2");
        
        String head = queue.poll(); // Remove from head
        String peek = queue.peek(); // Look at head without removing
        
        // Non-blocking, lock-free
        // Good for: High-throughput scenarios
    }
    
    // 3. ConcurrentLinkedDeque - double-ended queue
    public void demonstrateConcurrentLinkedDeque() {
        ConcurrentLinkedDeque<String> deque = new ConcurrentLinkedDeque<>();
        
        deque.offerFirst("Front");
        deque.offerLast("Back");
        
        String first = deque.pollFirst();
        String last = deque.pollLast();
    }
    
    // 4. ConcurrentSkipListMap - sorted concurrent map
    public void demonstrateConcurrentSkipListMap() {
        ConcurrentSkipListMap<String, Integer> sortedMap = new ConcurrentSkipListMap<>();
        
        sortedMap.put("zebra", 3);
        sortedMap.put("apple", 1);
        sortedMap.put("banana", 2);
        
        // Keys are sorted
        System.out.println(sortedMap); // {apple=1, banana=2, zebra=3}
        
        // Range queries
        SortedMap<String, Integer> subMap = sortedMap.subMap("apple", "zebra");
    }
    
    // 5. ConcurrentSkipListSet - sorted concurrent set
    public void demonstrateConcurrentSkipListSet() {
        ConcurrentSkipListSet<Integer> set = new ConcurrentSkipListSet<>();
        
        set.add(5);
        set.add(2);
        set.add(8);
        
        System.out.println(set); // [2, 5, 8] - sorted
    }
    
    // 6. BlockingQueue implementations
    public void demonstrateBlockingQueues() {
        // LinkedBlockingQueue - optionally bounded
        BlockingQueue<String> linkedQueue = new LinkedBlockingQueue<>(10);
        
        // ArrayBlockingQueue - bounded, uses array
        BlockingQueue<String> arrayQueue = new ArrayBlockingQueue<>(10);
        
        // PriorityBlockingQueue - unbounded, priority-based
        BlockingQueue<Integer> priorityQueue = new PriorityBlockingQueue<>();
        
        // SynchronousQueue - no capacity, direct handoff
        BlockingQueue<String> syncQueue = new SynchronousQueue<>();
        
        // DelayQueue - elements become available after delay
        BlockingQueue<Delayed> delayQueue = new DelayQueue<>();
    }
}
```

---

## Executor Framework

### ExecutorService Types:

```java
import java.util.concurrent.*;
import java.util.*;

public class ExecutorFrameworkDemo {
    
    public static void main(String[] args) {
        // 1. Single Thread Executor
        ExecutorService singleExecutor = Executors.newSingleThreadExecutor();
        singleExecutor.submit(() -> System.out.println("Task 1"));
        singleExecutor.submit(() -> System.out.println("Task 2"));
        // Tasks execute sequentially
        
        // 2. Fixed Thread Pool
        ExecutorService fixedPool = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 10; i++) {
            int taskId = i;
            fixedPool.submit(() -> {
                System.out.println("Task " + taskId + " by " + 
                    Thread.currentThread().getName());
            });
        }
        // 3 threads handle 10 tasks
        
        // 3. Cached Thread Pool
        ExecutorService cachedPool = Executors.newCachedThreadPool();
        for (int i = 0; i < 100; i++) {
            cachedPool.submit(() -> {
                // Creates new thread if needed, reuses idle threads
                // Threads die after 60 seconds of inactivity
            });
        }
        
        // 4. Scheduled Thread Pool
        ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(2);
        
        // Execute once after delay
        scheduledPool.schedule(() -> {
            System.out.println("Executed after 5 seconds");
        }, 5, TimeUnit.SECONDS);
        
        // Execute repeatedly at fixed rate
        scheduledPool.scheduleAtFixedRate(() -> {
            System.out.println("Executed every 2 seconds");
        }, 0, 2, TimeUnit.SECONDS);
        
        // Execute repeatedly with fixed delay between executions
        scheduledPool.scheduleWithFixedDelay(() -> {
            System.out.println("Executed 2 seconds after previous completes");
        }, 0, 2, TimeUnit.SECONDS);
        
        // 5. Work Stealing Pool (Java 8+)
        ExecutorService workStealingPool = Executors.newWorkStealingPool();
        // Uses ForkJoinPool, steals tasks from busy threads
        // Good for recursive tasks
        
        // Proper shutdown
        fixedPool.shutdown(); // No new tasks accepted
        try {
            if (!fixedPool.awaitTermination(60, TimeUnit.SECONDS)) {
                fixedPool.shutdownNow(); // Force shutdown
            }
        } catch (InterruptedException e) {
            fixedPool.shutdownNow();
        }
    }
}
```

### Future and Callable:

```java
import java.util.concurrent.*;
import java.util.*;

public class FutureCallableDemo {
    
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        // 1. Callable - returns result
        Callable<Integer> task = () -> {
            Thread.sleep(2000);
            return 42;
        };
        
        Future<Integer> future = executor.submit(task);
        
        // Do other work while task executes
        System.out.println("Task submitted, doing other work...");
        
        // Get result (blocks until complete)
        Integer result = future.get(); // Blocks for 2 seconds
        System.out.println("Result: " + result);
        
        // 2. Future with timeout
        Future<String> futureWithTimeout = executor.submit(() -> {
            Thread.sleep(5000);
            return "Completed";
        });
        
        try {
            String resultTimeout = futureWithTimeout.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            System.out.println("Task timed out");
            futureWithTimeout.cancel(true); // Interrupt if running
        }
        
        // 3. Check Future status
        Future<String> futureStatus = executor.submit(() -> "Done");
        
        System.out.println("Is done: " + futureStatus.isDone());
        System.out.println("Is cancelled: " + futureStatus.isCancelled());
        
        // 4. Cancel Future
        Future<String> futureCancel = executor.submit(() -> {
            Thread.sleep(10000);
            return "Never happens";
        });
        
        futureCancel.cancel(true); // true = interrupt if running
        
        // 5. Submit multiple tasks
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int taskId = i;
            tasks.add(() -> {
                Thread.sleep(1000);
                return taskId * taskId;
            });
        }
        
        // invokeAll - wait for all to complete
        List<Future<Integer>> futures = executor.invokeAll(tasks);
        for (Future<Integer> f : futures) {
            System.out.println("Result: " + f.get());
        }
        
        // invokeAny - return first successful result
        Integer firstResult = executor.invokeAny(tasks);
        System.out.println("First result: " + firstResult);
        
        executor.shutdown();
    }
}
```

### CompletableFuture (Advanced):

```java
import java.util.concurrent.*;

public class CompletableFutureDemo {
    
    public static void main(String[] args) throws Exception {
        // 1. Create CompletableFuture
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Hello";
        });
        
        // 2. Chain operations
        CompletableFuture<String> future2 = future1
            .thenApply(s -> s + " World")        // Transform result
            .thenApply(String::toUpperCase);     // Another transformation
        
        System.out.println(future2.get()); // HELLO WORLD
        
        // 3. Combine multiple futures
        CompletableFuture<String> futureA = CompletableFuture.supplyAsync(() -> "Hello");
        CompletableFuture<String> futureB = CompletableFuture.supplyAsync(() -> "World");
        
        CompletableFuture<String> combined = futureA.thenCombine(futureB, (a, b) -> a + " " + b);
        System.out.println(combined.get()); // Hello World
        
        // 4. Handle both success and failure
        CompletableFuture<Integer> futureError = CompletableFuture.supplyAsync(() -> {
            if (Math.random() > 0.5) {
                throw new RuntimeException("Error!");
            }
            return 42;
        }).handle((result, exception) -> {
            if (exception != null) {
                System.out.println("Error: " + exception.getMessage());
                return 0;
            }
            return result;
        });
        
        // 5. Async callbacks
        CompletableFuture.supplyAsync(() -> "Data")
            .thenAcceptAsync(data -> {
                System.out.println("Async processing: " + data);
            })
            .thenRun(() -> {
                System.out.println("Cleanup complete");
            });
        
        // 6. Wait for multiple futures
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> "Task1");
        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> "Task2");
        CompletableFuture<String> f3 = CompletableFuture.supplyAsync(() -> "Task3");
        
        CompletableFuture<Void> allOf = CompletableFuture.allOf(f1, f2, f3);
        allOf.join(); // Wait for all
        
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(f1, f2, f3);
        System.out.println("First completed: " + anyOf.get());
        
        // 7. Exception handling
        CompletableFuture<Integer> futureWithException = CompletableFuture.supplyAsync(() -> {
            return 10 / 0; // ArithmeticException
        }).exceptionally(ex -> {
            System.out.println("Exception: " + ex.getMessage());
            return 0;
        });
    }
}
```

---

## Advanced Concurrency Utilities

### 1. CountDownLatch:

```java
import java.util.concurrent.*;

public class CountDownLatchDemo {
    
    public static void main(String[] args) throws InterruptedException {
        int numWorkers = 3;
        CountDownLatch startSignal = new CountDownLatch(1); // Start gate
        CountDownLatch doneSignal = new CountDownLatch(numWorkers); // Completion gate
        
        for (int i = 0; i < numWorkers; i++) {
            int workerId = i;
            new Thread(() -> {
                try {
                    System.out.println("Worker " + workerId + " waiting to start...");
                    startSignal.await(); // Wait for start signal
                    
                    System.out.println("Worker " + workerId + " working...");
                    Thread.sleep(2000); // Simulate work
                    
                    System.out.println("Worker " + workerId + " done");
                    doneSignal.countDown(); // Signal completion
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
        
        System.out.println("Preparing to start all workers...");
        Thread.sleep(1000);
        startSignal.countDown(); // Release all workers
        
        System.out.println("Waiting for workers to complete...");
        doneSignal.await(); // Wait for all to finish
        System.out.println("All workers completed!");
    }
    
    // Real-world example: Service initialization
    public void serviceInitialization() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        
        // Database initialization
        new Thread(() -> {
            System.out.println("Initializing database...");
            // Initialize DB
            latch.countDown();
        }).start();
        
        // Cache initialization
        new Thread(() -> {
            System.out.println("Initializing cache...");
            // Initialize cache
            latch.countDown();
        }).start();
        
        // Config loading
        new Thread(() -> {
            System.out.println("Loading configuration...");
            // Load config
            latch.countDown();
        }).start();
        
        latch.await(); // Wait for all services to initialize
        System.out.println("All services ready, starting application...");
    }
}
```

### 2. CyclicBarrier:

```java
import java.util.concurrent.*;

public class CyclicBarrierDemo {
    
    public static void main(String[] args) {
        int numThreads = 3;
        
        // Barrier with action to run when all threads reach barrier
        CyclicBarrier barrier = new CyclicBarrier(numThreads, () -> {
            System.out.println("All threads reached barrier, proceeding...");
        });
        
        for (int i = 0; i < numThreads; i++) {
            int threadId = i;
            new Thread(() -> {
                try {
                    System.out.println("Thread " + threadId + " - Phase 1");
                    Thread.sleep(1000);
                    barrier.await(); // Wait for others
                    
                    System.out.println("Thread " + threadId + " - Phase 2");
                    Thread.sleep(1000);
                    barrier.await(); // Barrier can be reused!
                    
                    System.out.println("Thread " + threadId + " - Phase 3");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    // Real-world example: Matrix computation
    public void matrixMultiplication() {
        int numThreads = 4;
        int[][] matrix = new int[1000][1000];
        CyclicBarrier barrier = new CyclicBarrier(numThreads);
        
        for (int i = 0; i < numThreads; i++) {
            int start = i * 250;
            int end = (i + 1) * 250;
            
            new Thread(() -> {
                try {
                    // Each thread processes rows start to end
                    for (int row = start; row < end; row++) {
                        // Process row
                    }
                    
                    barrier.await(); // Wait for all threads to finish
                    
                    // All threads can now proceed to next phase
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
```

### 3. Semaphore:

```java
import java.util.concurrent.*;

public class SemaphoreDemo {
    
    // Connection pool using semaphore
    static class ConnectionPool {
        private final Semaphore semaphore;
        private final Connection[] connections;
        
        public ConnectionPool(int poolSize) {
            this.semaphore = new Semaphore(poolSize);
            this.connections = new Connection[poolSize];
            for (int i = 0; i < poolSize; i++) {
                connections[i] = new Connection("Connection-" + i);
            }
        }
        
        public Connection getConnection() throws InterruptedException {
            semaphore.acquire(); // Wait for available permit
            return getNextAvailableConnection();
        }
        
        public void releaseConnection(Connection conn) {
            if (markAsUnused(conn)) {
                semaphore.release(); // Release permit
            }
        }
        
        private Connection getNextAvailableConnection() {
            for (Connection conn : connections) {
                if (conn.tryAcquire()) {
                    return conn;
                }
            }
            return null;
        }
        
        private boolean markAsUnused(Connection conn) {
            return conn.release();
        }
    }
    
    static class Connection {
        private String name;
        private boolean inUse;
        
        public Connection(String name) {
            this.name = name;
        }
        
        public synchronized boolean tryAcquire() {
            if (!inUse) {
                inUse = true;
                return true;
            }
            return false;
        }
        
        public synchronized boolean release() {
            inUse = false;
            return true;
        }
    }
    
    public static void main(String[] args) {
        ConnectionPool pool = new ConnectionPool(3); // Max 3 connections
        
        // 10 threads try to get connections
        for (int i = 0; i < 10; i++) {
            int threadId = i;
            new Thread(() -> {
                try {
                    System.out.println("Thread " + threadId + " requesting connection...");
                    Connection conn = pool.getConnection();
                    System.out.println("Thread " + threadId + " got connection");
                    
                    Thread.sleep(2000); // Use connection
                    
                    pool.releaseConnection(conn);
                    System.out.println("Thread " + threadId + " released connection");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
    
    // Rate limiting example
    public void rateLimiting() {
        Semaphore rateLimiter = new Semaphore(5); // Max 5 requests per time window
        
        for (int i = 0; i < 20; i++) {
            int requestId = i;
            new Thread(() -> {
                try {
                    rateLimiter.acquire();
                    System.out.println("Processing request " + requestId);
                    Thread.sleep(1000);
                    rateLimiter.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
}
```

### 4. Exchanger:

```java
import java.util.concurrent.*;

public class ExchangerDemo {
    
    public static void main(String[] args) {
        Exchanger<String> exchanger = new Exchanger<>();
        
        // Producer thread
        new Thread(() -> {
            try {
                String data = "Data from Producer";
                System.out.println("Producer has: " + data);
                
                // Exchange data with consumer
                String received = exchanger.exchange(data);
                System.out.println("Producer received: " + received);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        // Consumer thread
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                String data = "Data from Consumer";
                System.out.println("Consumer has: " + data);
                
                // Exchange data with producer
                String received = exchanger.exchange(data);
                System.out.println("Consumer received: " + received);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    // Real-world: Buffer swapping
    public void bufferSwapping() {
        Exchanger<Buffer> exchanger = new Exchanger<>();
        
        // Producer fills buffer
        new Thread(() -> {
            Buffer currentBuffer = new Buffer();
            try {
                while (true) {
                    currentBuffer.fill(); // Fill buffer
                    currentBuffer = exchanger.exchange(currentBuffer); // Swap
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        // Consumer processes buffer
        new Thread(() -> {
            Buffer currentBuffer = new Buffer();
            try {
                while (true) {
                    currentBuffer = exchanger.exchange(currentBuffer); // Swap
                    currentBuffer.process(); // Process buffer
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    static class Buffer {
        public void fill() {
            // Fill buffer with data
        }
        
        public void process() {
            // Process buffer data
        }
    }
}
```

### 5. Phaser (Advanced CyclicBarrier):

```java
import java.util.concurrent.*;

public class PhaserDemo {
    
    public static void main(String[] args) {
        Phaser phaser = new Phaser(3); // 3 parties registered
        
        for (int i = 0; i < 3; i++) {
            int threadId = i;
            new Thread(() -> {
                System.out.println("Thread " + threadId + " - Phase 0");
                phaser.arriveAndAwaitAdvance(); // Wait for phase 0
                
                System.out.println("Thread " + threadId + " - Phase 1");
                phaser.arriveAndAwaitAdvance(); // Wait for phase 1
                
                System.out.println("Thread " + threadId + " - Phase 2");
                phaser.arriveAndDeregister(); // Done, deregister
            }).start();
        }
    }
    
    // Dynamic registration
    public void dynamicParties() {
        Phaser phaser = new Phaser(1); // Main thread
        
        for (int i = 0; i < 5; i++) {
            phaser.register(); // Dynamically register party
            new Thread(() -> {
                // Do work
                phaser.arriveAndAwaitAdvance();
            }).start();
        }
        
        phaser.arriveAndDeregister(); // Main thread done
    }
}
```

---

## Common Threading Problems

### 1. Deadlock:

```java
public class DeadlockDemo {
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();
    
    // Creates deadlock
    public void method1() {
        synchronized (lock1) {
            System.out.println("Thread 1: Holding lock 1...");
            try { Thread.sleep(10); } catch (InterruptedException e) {}
            System.out.println("Thread 1: Waiting for lock 2...");
            
            synchronized (lock2) {
                System.out.println("Thread 1: Holding lock 1 & 2");
            }
        }
    }
    
    public void method2() {
        synchronized (lock2) {
            System.out.println("Thread 2: Holding lock 2...");
            try { Thread.sleep(10); } catch (InterruptedException e) {}
            System.out.println("Thread 2: Waiting for lock 1...");
            
            synchronized (lock1) {
                System.out.println("Thread 2: Holding lock 1 & 2");
            }
        }
    }
    
    // Solution 1: Always acquire locks in same order
    public void method1Fixed() {
        synchronized (lock1) {
            synchronized (lock2) {
                // Work
            }
        }
    }
    
    public void method2Fixed() {
        synchronized (lock1) { // Same order!
            synchronized (lock2) {
                // Work
            }
        }
    }
    
    // Solution 2: Use tryLock with timeout
    ReentrantLock lockA = new ReentrantLock();
    ReentrantLock lockB = new ReentrantLock();
    
    public void method1WithTryLock() {
        try {
            if (lockA.tryLock(1, TimeUnit.SECONDS)) {
                try {
                    if (lockB.tryLock(1, TimeUnit.SECONDS)) {
                        try {
                            // Work
                        } finally {
                            lockB.unlock();
                        }
                    }
                } finally {
                    lockA.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### 2. Race Condition:

```java
public class RaceConditionDemo {
    private int counter = 0;
    
    // Race condition
    public void increment() {
        counter++; // Not atomic! (read, modify, write)
    }
    
    // Solution 1: Synchronized
    public synchronized void incrementSafe1() {
        counter++;
    }
    
    // Solution 2: AtomicInteger
    private AtomicInteger atomicCounter = new AtomicInteger(0);
    
    public void incrementSafe2() {
        atomicCounter.incrementAndGet();
    }
    
    // Demonstration
    public static void main(String[] args) throws InterruptedException {
        RaceConditionDemo demo = new RaceConditionDemo();
        
        Thread[] threads = new Thread[1000];
        for (int i = 0; i < 1000; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    demo.increment(); // Race condition!
                }
            });
            threads[i].start();
        }
        
        for (Thread t : threads) {
            t.join();
        }
        
        System.out.println("Expected: 1000000");
        System.out.println("Actual: " + demo.counter); // Less than 1000000!
    }
}
```

### 3. Livelock:

```java
public class LivelockDemo {
    // Two threads keep responding to each other, but make no progress
    
    static class Spoon {
        private Diner owner;
        
        public Spoon(Diner owner) {
            this.owner = owner;
        }
        
        public Diner getOwner() {
            return owner;
        }
        
        public synchronized void setOwner(Diner owner) {
            this.owner = owner;
        }
        
        public synchronized void use() {
            System.out.println(owner.name + " is eating");
        }
    }
    
    static class Diner {
        private String name;
        private boolean isHungry;
        
        public Diner(String name) {
            this.name = name;
            this.isHungry = true;
        }
        
        public void eatWith(Spoon spoon, Diner spouse) {
            while (isHungry) {
                // Don't have spoon, so wait
                if (spoon.owner != this) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        continue;
                    }
                    continue;
                }
                
                // If spouse is hungry, give them the spoon
                if (spouse.isHungry) {
                    System.out.println(name + ": You eat first " + spouse.name);
                    spoon.setOwner(spouse);
                    continue; // Livelock! Both keep giving spoon to other
                }
                
                // Eat
                spoon.use();
                isHungry = false;
                spoon.setOwner(spouse);
            }
        }
    }
    
    public static void main(String[] args) {
        Diner husband = new Diner("Husband");
        Diner wife = new Diner("Wife");
        Spoon spoon = new Spoon(husband);
        
        new Thread(() -> husband.eatWith(spoon, wife)).start();
        new Thread(() -> wife.eatWith(spoon, husband)).start();
        
        // Both threads keep giving spoon to each other - livelock!
    }
}
```

### 4. Starvation:

```java
public class StarvationDemo {
    // Low priority threads may never get CPU time
    
    public static void main(String[] args) {
        Thread highPriority = new Thread(() -> {
            while (true) {
                System.out.println("High priority running");
            }
        });
        
        Thread lowPriority = new Thread(() -> {
            int count = 0;
            while (true) {
                System.out.println("Low priority running: " + count++);
                // May never get to run!
            }
        });
        
        highPriority.setPriority(Thread.MAX_PRIORITY);
        lowPriority.setPriority(Thread.MIN_PRIORITY);
        
        lowPriority.start();
        highPriority.start();
    }
    
    // Solution: Use fair locks
    ReentrantLock fairLock = new ReentrantLock(true); // Fair lock
}
```

---

## Best Practices & Patterns

### Thread-Safe Singleton:

```java
// 1. Eager initialization (thread-safe)
public class EagerSingleton {
    private static final EagerSingleton INSTANCE = new EagerSingleton();
    
    private EagerSingleton() {}
    
    public static EagerSingleton getInstance() {
        return INSTANCE;
    }
}

// 2. Lazy initialization with double-checked locking
public class LazySingleton {
    private static volatile LazySingleton instance;
    
    private LazySingleton() {}
    
    public static LazySingleton getInstance() {
        if (instance == null) {
            synchronized (LazySingleton.class) {
                if (instance == null) {
                    instance = new LazySingleton();
                }
            }
        }
        return instance;
    }
}

// 3. Bill Pugh Singleton (best)
public class BillPughSingleton {
    private BillPughSingleton() {}
    
    private static class SingletonHelper {
        private static final BillPughSingleton INSTANCE = new BillPughSingleton();
    }
    
    public static BillPughSingleton getInstance() {
        return SingletonHelper.INSTANCE;
    }
}

// 4. Enum Singleton (simplest, recommended)
public enum EnumSingleton {
    INSTANCE;
    
    public void doSomething() {
        // Business logic
    }
}
```

### Thread-Local Pattern:

```java
public class ThreadLocalPattern {
    // Each thread has its own copy
    private static ThreadLocal<SimpleDateFormat> dateFormat = 
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
    
    public String formatDate(Date date) {
        return dateFormat.get().format(date);
        // No synchronization needed!
    }
    
    // Real-world: Request context
    static class RequestContext {
        private static ThreadLocal<String> userId = new ThreadLocal<>();
        private static ThreadLocal<String> requestId = new ThreadLocal<>();
        
        public static void setUserId(String id) {
            userId.set(id);
        }
        
        public static String getUserId() {
            return userId.get();
        }
        
        public static void clear() {
            userId.remove(); // Important in thread pools!
            requestId.remove();
        }
    }
}
```

---

## Interview Scenarios

### Scenario 1: Implement Thread-Safe Cache

```java
import java.util.concurrent.*;

public class ThreadSafeCache<K, V> {
    private final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<>();
    private final int maxSize;
    
    public ThreadSafeCache(int maxSize) {
        this.maxSize = maxSize;
    }
    
    public V get(K key) {
        return cache.get(key);
    }
    
    public void put(K key, V value) {
        if (cache.size() >= maxSize) {
            // Evict oldest entry (simplified)
            K oldestKey = cache.keys().nextElement();
            cache.remove(oldestKey);
        }
        cache.put(key, value);
    }
    
    public V computeIfAbsent(K key, Function<K, V> mappingFunction) {
        return cache.computeIfAbsent(key, mappingFunction);
    }
}
```

### Scenario 2: Implement Rate Limiter

```java
import java.util.concurrent.*;

public class RateLimiter {
    private final Semaphore semaphore;
    private final int maxPermits;
    private final long timeWindow;
    
    public RateLimiter(int maxRequests, long timeWindowMs) {
        this.semaphore = new Semaphore(maxRequests);
        this.maxPermits = maxRequests;
        this.timeWindow = timeWindowMs;
    }
    
    public boolean tryAcquire() {
        if (semaphore.tryAcquire()) {
            // Release permit after time window
            CompletableFuture.delayedExecutor(timeWindow, TimeUnit.MILLISECONDS)
                .execute(semaphore::release);
            return true;
        }
        return false;
    }
}
```

### Scenario 3: Parallel Processing Pipeline

```java
public class ParallelPipeline<T, R> {
    private final ExecutorService executor;
    
    public ParallelPipeline(int threads) {
        this.executor = Executors.newFixedThreadPool(threads);
    }
    
    public List<R> process(List<T> items, Function<T, R> processor) {
        List<CompletableFuture<R>> futures = items.stream()
            .map(item -> CompletableFuture.supplyAsync(() -> processor.apply(item), executor))
            .collect(Collectors.toList());
        
        return futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}
```

---

## Key Takeaways

### When to Use What:

| Scenario | Best Choice |
|----------|-------------|
| Simple mutual exclusion | `synchronized` |
| Need timeout/tryLock | `ReentrantLock` |
| Multiple readers, single writer | `ReadWriteLock` |
| Simple flag sharing | `volatile` |
| Counter/accumulator | `AtomicInteger` |
| Producer-Consumer | `BlockingQueue` |
| Thread pool | `ExecutorService` |
| Future with callbacks | `CompletableFuture` |
| Wait for multiple threads | `CountDownLatch` |
| Reusable synchronization point | `CyclicBarrier` |
| Resource limiting | `Semaphore` |

### Common Interview Questions:

1. **"How does synchronized work?"** - Acquires intrinsic lock, ensures mutual exclusion
2. **"Difference between wait() and sleep()?"** - wait() releases lock, sleep() doesn't
3. **"Why use ThreadPool?"** - Reuses threads, controls concurrency, better resource management
4. **"Explain deadlock conditions"** - Mutual exclusion, hold and wait, no preemption, circular wait
5. **"volatile vs synchronized?"** - volatile for visibility only, synchronized for atomicity too
6. **"How to stop a thread?"** - Use interrupt(), check flag, don't use stop()
7. **"What is happens-before?"** - Memory visibility guarantee between operations

This completes the deep dive! Practice these concepts with the coding problems in the next file.
