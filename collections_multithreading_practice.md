# Collections & Multithreading - Practice Problems

## Table of Contents
1. [Collections - Easy Problems](#collections---easy-problems)
2. [Collections - Medium Problems](#collections---medium-problems)
3. [Collections - Hard Problems](#collections---hard-problems)
4. [Multithreading - Easy Problems](#multithreading---easy-problems)
5. [Multithreading - Medium Problems](#multithreading---medium-problems)
6. [Multithreading - Hard Problems](#multithreading---hard-problems)

---

## Collections - Easy Problems

### Problem 1: Remove Duplicates from ArrayList

**Question:** Write a method to remove duplicates from an ArrayList while preserving order.

```java
Input: [1, 2, 3, 2, 4, 1, 5]
Output: [1, 2, 3, 4, 5]
```

**Solution:**

```java
public class RemoveDuplicates {
    
    // Solution 1: Using LinkedHashSet (preserves order)
    public static <T> List<T> removeDuplicates1(List<T> list) {
        return new ArrayList<>(new LinkedHashSet<>(list));
    }
    
    // Solution 2: Using Stream (Java 8+)
    public static <T> List<T> removeDuplicates2(List<T> list) {
        return list.stream()
                .distinct()
                .collect(Collectors.toList());
    }
    
    // Solution 3: Manual approach
    public static <T> List<T> removeDuplicates3(List<T> list) {
        List<T> result = new ArrayList<>();
        Set<T> seen = new HashSet<>();
        
        for (T item : list) {
            if (seen.add(item)) {  // add() returns false if already present
                result.add(item);
            }
        }
        
        return result;
    }
    
    public static void main(String[] args) {
        List<Integer> list = Arrays.asList(1, 2, 3, 2, 4, 1, 5);
        System.out.println(removeDuplicates1(list));  // [1, 2, 3, 4, 5]
    }
}
```

**Time Complexity:** O(n)  
**Space Complexity:** O(n)

---

### Problem 2: Find First Non-Repeating Character

**Question:** Given a string, find the first non-repeating character.

```java
Input: "leetcode"
Output: 'l'

Input: "loveleetcode"
Output: 'v'
```

**Solution:**

```java
public class FirstNonRepeating {
    
    public static Character firstNonRepeating(String str) {
        Map<Character, Integer> charCount = new LinkedHashMap<>();
        
        // Count occurrences
        for (char c : str.toCharArray()) {
            charCount.put(c, charCount.getOrDefault(c, 0) + 1);
        }
        
        // Find first with count = 1
        for (Map.Entry<Character, Integer> entry : charCount.entrySet()) {
            if (entry.getValue() == 1) {
                return entry.getKey();
            }
        }
        
        return null;  // No non-repeating character
    }
    
    public static void main(String[] args) {
        System.out.println(firstNonRepeating("leetcode"));      // 'l'
        System.out.println(firstNonRepeating("loveleetcode"));  // 'v'
        System.out.println(firstNonRepeating("aabb"));          // null
    }
}
```

---

### Problem 3: Group Anagrams

**Question:** Group strings that are anagrams of each other.

```java
Input: ["eat", "tea", "tan", "ate", "nat", "bat"]
Output: [["eat","tea","ate"], ["tan","nat"], ["bat"]]
```

**Solution:**

```java
public class GroupAnagrams {
    
    public static List<List<String>> groupAnagrams(String[] strs) {
        Map<String, List<String>> map = new HashMap<>();
        
        for (String str : strs) {
            // Sort characters to create key
            char[] chars = str.toCharArray();
            Arrays.sort(chars);
            String key = new String(chars);
            
            // Add to group
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(str);
        }
        
        return new ArrayList<>(map.values());
    }
    
    public static void main(String[] args) {
        String[] strs = {"eat", "tea", "tan", "ate", "nat", "bat"};
        System.out.println(groupAnagrams(strs));
        // [[eat, tea, ate], [tan, nat], [bat]]
    }
}
```

**Time Complexity:** O(n * k log k) where k is max string length  
**Space Complexity:** O(n * k)

---

## Collections - Medium Problems

### Problem 4: LRU Cache Implementation

**Question:** Implement an LRU (Least Recently Used) cache with get() and put() operations in O(1).

```java
LRUCache cache = new LRUCache(2);  // capacity = 2
cache.put(1, 1);
cache.put(2, 2);
cache.get(1);       // returns 1
cache.put(3, 3);    // evicts key 2
cache.get(2);       // returns -1 (not found)
```

**Solution:**

```java
import java.util.*;

public class LRUCache {
    
    private final int capacity;
    private final LinkedHashMap<Integer, Integer> cache;
    
    // Solution 1: Using LinkedHashMap
    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new LinkedHashMap<Integer, Integer>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
                return size() > capacity;
            }
        };
    }
    
    public int get(int key) {
        return cache.getOrDefault(key, -1);
    }
    
    public void put(int key, int value) {
        cache.put(key, value);
    }
    
    // Solution 2: Manual implementation with HashMap + DoublyLinkedList
    static class Node {
        int key, value;
        Node prev, next;
        
        Node(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }
    
    static class LRUCacheManual {
        private final int capacity;
        private final Map<Integer, Node> cache;
        private final Node head, tail;
        
        public LRUCacheManual(int capacity) {
            this.capacity = capacity;
            this.cache = new HashMap<>();
            
            // Dummy head and tail
            head = new Node(0, 0);
            tail = new Node(0, 0);
            head.next = tail;
            tail.prev = head;
        }
        
        public int get(int key) {
            Node node = cache.get(key);
            if (node == null) return -1;
            
            // Move to front (most recently used)
            moveToHead(node);
            return node.value;
        }
        
        public void put(int key, int value) {
            Node node = cache.get(key);
            
            if (node != null) {
                node.value = value;
                moveToHead(node);
            } else {
                Node newNode = new Node(key, value);
                cache.put(key, newNode);
                addToHead(newNode);
                
                if (cache.size() > capacity) {
                    // Remove least recently used (tail)
                    Node lru = tail.prev;
                    removeNode(lru);
                    cache.remove(lru.key);
                }
            }
        }
        
        private void addToHead(Node node) {
            node.next = head.next;
            node.prev = head;
            head.next.prev = node;
            head.next = node;
        }
        
        private void removeNode(Node node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
        
        private void moveToHead(Node node) {
            removeNode(node);
            addToHead(node);
        }
    }
    
    public static void main(String[] args) {
        LRUCache cache = new LRUCache(2);
        cache.put(1, 1);
        cache.put(2, 2);
        System.out.println(cache.get(1));  // 1
        cache.put(3, 3);                   // evicts key 2
        System.out.println(cache.get(2));  // -1
        cache.put(4, 4);                   // evicts key 1
        System.out.println(cache.get(1));  // -1
        System.out.println(cache.get(3));  // 3
        System.out.println(cache.get(4));  // 4
    }
}
```

---

### Problem 5: Top K Frequent Elements

**Question:** Find the k most frequent elements in an array.

```java
Input: nums = [1,1,1,2,2,3], k = 2
Output: [1,2]
```

**Solution:**

```java
public class TopKFrequent {
    
    // Solution 1: Using HashMap + PriorityQueue
    public static List<Integer> topKFrequent1(int[] nums, int k) {
        // Count frequencies
        Map<Integer, Integer> freqMap = new HashMap<>();
        for (int num : nums) {
            freqMap.put(num, freqMap.getOrDefault(num, 0) + 1);
        }
        
        // Min heap of size k
        PriorityQueue<Map.Entry<Integer, Integer>> pq = new PriorityQueue<>(
            (a, b) -> a.getValue() - b.getValue()
        );
        
        for (Map.Entry<Integer, Integer> entry : freqMap.entrySet()) {
            pq.offer(entry);
            if (pq.size() > k) {
                pq.poll();  // Remove least frequent
            }
        }
        
        List<Integer> result = new ArrayList<>();
        while (!pq.isEmpty()) {
            result.add(pq.poll().getKey());
        }
        
        return result;
    }
    
    // Solution 2: Using Bucket Sort (O(n) time)
    public static List<Integer> topKFrequent2(int[] nums, int k) {
        Map<Integer, Integer> freqMap = new HashMap<>();
        for (int num : nums) {
            freqMap.put(num, freqMap.getOrDefault(num, 0) + 1);
        }
        
        // Bucket: index = frequency, value = list of numbers with that frequency
        List<Integer>[] buckets = new List[nums.length + 1];
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new ArrayList<>();
        }
        
        for (Map.Entry<Integer, Integer> entry : freqMap.entrySet()) {
            int freq = entry.getValue();
            buckets[freq].add(entry.getKey());
        }
        
        // Collect top k from highest frequency buckets
        List<Integer> result = new ArrayList<>();
        for (int i = buckets.length - 1; i >= 0 && result.size() < k; i--) {
            result.addAll(buckets[i]);
        }
        
        return result.subList(0, k);
    }
    
    public static void main(String[] args) {
        int[] nums = {1, 1, 1, 2, 2, 3};
        System.out.println(topKFrequent1(nums, 2));  // [1, 2]
        System.out.println(topKFrequent2(nums, 2));  // [1, 2]
    }
}
```

**Time Complexity:**
- Solution 1: O(n log k)
- Solution 2: O(n)

---

### Problem 6: Find All Pairs with Given Sum

**Question:** Find all pairs in an array that sum to a target value.

```java
Input: arr = [1, 5, 7, -1, 5], target = 6
Output: [[1, 5], [7, -1]]
```

**Solution:**

```java
public class FindPairs {
    
    public static List<List<Integer>> findPairs(int[] arr, int target) {
        Set<Integer> seen = new HashSet<>();
        Set<List<Integer>> result = new HashSet<>();
        
        for (int num : arr) {
            int complement = target - num;
            
            if (seen.contains(complement)) {
                List<Integer> pair = Arrays.asList(
                    Math.min(num, complement),
                    Math.max(num, complement)
                );
                result.add(pair);
            }
            
            seen.add(num);
        }
        
        return new ArrayList<>(result);
    }
    
    public static void main(String[] args) {
        int[] arr = {1, 5, 7, -1, 5};
        System.out.println(findPairs(arr, 6));  // [[1, 5], [-1, 7]]
    }
}
```

---

## Collections - Hard Problems

### Problem 7: Implement HashMap from Scratch

**Question:** Implement a basic HashMap with put(), get(), and remove() operations.

**Solution:**

```java
public class MyHashMap<K, V> {
    
    private static class Entry<K, V> {
        K key;
        V value;
        Entry<K, V> next;
        
        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    
    private Entry<K, V>[] buckets;
    private int capacity = 16;
    private int size = 0;
    private static final double LOAD_FACTOR = 0.75;
    
    @SuppressWarnings("unchecked")
    public MyHashMap() {
        buckets = new Entry[capacity];
    }
    
    private int hash(K key) {
        return key == null ? 0 : Math.abs(key.hashCode() % capacity);
    }
    
    public void put(K key, V value) {
        if (size >= capacity * LOAD_FACTOR) {
            resize();
        }
        
        int index = hash(key);
        Entry<K, V> entry = buckets[index];
        
        // Check if key already exists
        while (entry != null) {
            if ((key == null && entry.key == null) || 
                (key != null && key.equals(entry.key))) {
                entry.value = value;  // Update existing
                return;
            }
            entry = entry.next;
        }
        
        // Add new entry at beginning of chain
        Entry<K, V> newEntry = new Entry<>(key, value);
        newEntry.next = buckets[index];
        buckets[index] = newEntry;
        size++;
    }
    
    public V get(K key) {
        int index = hash(key);
        Entry<K, V> entry = buckets[index];
        
        while (entry != null) {
            if ((key == null && entry.key == null) || 
                (key != null && key.equals(entry.key))) {
                return entry.value;
            }
            entry = entry.next;
        }
        
        return null;
    }
    
    public V remove(K key) {
        int index = hash(key);
        Entry<K, V> entry = buckets[index];
        Entry<K, V> prev = null;
        
        while (entry != null) {
            if ((key == null && entry.key == null) || 
                (key != null && key.equals(entry.key))) {
                
                if (prev == null) {
                    buckets[index] = entry.next;
                } else {
                    prev.next = entry.next;
                }
                
                size--;
                return entry.value;
            }
            prev = entry;
            entry = entry.next;
        }
        
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private void resize() {
        int newCapacity = capacity * 2;
        Entry<K, V>[] oldBuckets = buckets;
        buckets = new Entry[newCapacity];
        capacity = newCapacity;
        size = 0;
        
        // Rehash all entries
        for (Entry<K, V> entry : oldBuckets) {
            while (entry != null) {
                put(entry.key, entry.value);
                entry = entry.next;
            }
        }
    }
    
    public int size() {
        return size;
    }
    
    public static void main(String[] args) {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);
        
        System.out.println(map.get("two"));     // 2
        System.out.println(map.remove("two"));  // 2
        System.out.println(map.get("two"));     // null
        System.out.println(map.size());         // 2
    }
}
```

---

## Multithreading - Easy Problems

### Problem 8: Print Numbers Sequentially by Three Threads

**Question:** Three threads print numbers 1-30 in sequence. Thread 1 prints 1, 4, 7..., Thread 2 prints 2, 5, 8..., Thread 3 prints 3, 6, 9...

**Solution:**

```java
public class PrintSequentially {
    private int current = 1;
    private static final int MAX = 30;
    
    public synchronized void printNumber(int threadId) {
        while (current <= MAX) {
            // Wait if not this thread's turn
            while (current % 3 != threadId && current <= MAX) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            if (current <= MAX) {
                System.out.println("Thread " + threadId + ": " + current);
                current++;
                notifyAll();  // Wake up all waiting threads
            }
        }
    }
    
    public static void main(String[] args) {
        PrintSequentially printer = new PrintSequentially();
        
        Thread t1 = new Thread(() -> printer.printNumber(1));
        Thread t2 = new Thread(() -> printer.printNumber(2));
        Thread t3 = new Thread(() -> printer.printNumber(0));  // 0 for numbers divisible by 3
        
        t1.start();
        t2.start();
        t3.start();
    }
}
```

---

### Problem 9: Implement Thread-Safe Counter

**Question:** Implement a counter that can be safely incremented by multiple threads.

**Solution:**

```java
// Solution 1: Using synchronized
class SynchronizedCounter {
    private int count = 0;
    
    public synchronized void increment() {
        count++;
    }
    
    public synchronized int getCount() {
        return count;
    }
}

// Solution 2: Using AtomicInteger
class AtomicCounter {
    private AtomicInteger count = new AtomicInteger(0);
    
    public void increment() {
        count.incrementAndGet();
    }
    
    public int getCount() {
        return count.get();
    }
}

// Solution 3: Using ReentrantLock
class LockCounter {
    private int count = 0;
    private final Lock lock = new ReentrantLock();
    
    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }
    
    public int getCount() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }
}

// Test
public class CounterTest {
    public static void main(String[] args) throws InterruptedException {
        AtomicCounter counter = new AtomicCounter();
        
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    counter.increment();
                }
            });
            threads[i].start();
        }
        
        for (Thread t : threads) {
            t.join();
        }
        
        System.out.println("Final count: " + counter.getCount());  // 10000
    }
}
```

---

### Problem 10: Producer-Consumer with BlockingQueue

**Question:** Implement producer-consumer pattern using BlockingQueue.

**Solution:**

```java
import java.util.concurrent.*;

public class ProducerConsumer {
    
    static class Producer implements Runnable {
        private BlockingQueue<Integer> queue;
        
        public Producer(BlockingQueue<Integer> queue) {
            this.queue = queue;
        }
        
        @Override
        public void run() {
            try {
                for (int i = 1; i <= 10; i++) {
                    System.out.println("Producing: " + i);
                    queue.put(i);  // Blocks if queue is full
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    static class Consumer implements Runnable {
        private BlockingQueue<Integer> queue;
        
        public Consumer(BlockingQueue<Integer> queue) {
            this.queue = queue;
        }
        
        @Override
        public void run() {
            try {
                while (true) {
                    Integer item = queue.take();  // Blocks if queue is empty
                    System.out.println("Consuming: " + item);
                    Thread.sleep(300);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public static void main(String[] args) {
        BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(5);
        
        Thread producer = new Thread(new Producer(queue));
        Thread consumer = new Thread(new Consumer(queue));
        
        producer.start();
        consumer.start();
    }
}
```

---

## Multithreading - Medium Problems

### Problem 11: Dining Philosophers Problem

**Question:** Five philosophers sit at a table with five forks. Each needs two forks to eat. Prevent deadlock.

**Solution:**

```java
import java.util.concurrent.locks.*;

public class DiningPhilosophers {
    
    static class Fork {
        private final Lock lock = new ReentrantLock();
        
        public boolean pickUp() {
            return lock.tryLock();
        }
        
        public void putDown() {
            lock.unlock();
        }
    }
    
    static class Philosopher implements Runnable {
        private final int id;
        private final Fork leftFork;
        private final Fork rightFork;
        
        public Philosopher(int id, Fork left, Fork right) {
            this.id = id;
            this.leftFork = left;
            this.rightFork = right;
        }
        
        @Override
        public void run() {
            try {
                while (true) {
                    think();
                    eat();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        private void think() throws InterruptedException {
            System.out.println("Philosopher " + id + " is thinking");
            Thread.sleep(1000);
        }
        
        private void eat() throws InterruptedException {
            // Try to pick up both forks
            while (true) {
                if (leftFork.pickUp()) {
                    try {
                        if (rightFork.pickUp()) {
                            try {
                                // Both forks acquired - eat
                                System.out.println("Philosopher " + id + " is eating");
                                Thread.sleep(1000);
                                return;
                            } finally {
                                rightFork.putDown();
                            }
                        }
                    } finally {
                        leftFork.putDown();
                    }
                }
                
                // Couldn't get both forks, try again
                Thread.sleep(10);
            }
        }
    }
    
    public static void main(String[] args) {
        int numPhilosophers = 5;
        Fork[] forks = new Fork[numPhilosophers];
        
        for (int i = 0; i < numPhilosophers; i++) {
            forks[i] = new Fork();
        }
        
        for (int i = 0; i < numPhilosophers; i++) {
            Fork left = forks[i];
            Fork right = forks[(i + 1) % numPhilosophers];
            
            new Thread(new Philosopher(i, left, right)).start();
        }
    }
}
```

---

### Problem 12: Implement Thread Pool

**Question:** Implement a basic thread pool that can execute tasks.

**Solution:**

```java
import java.util.concurrent.*;

public class SimpleThreadPool {
    
    private final BlockingQueue<Runnable> taskQueue;
    private final Thread[] workers;
    private volatile boolean isShutdown = false;
    
    public SimpleThreadPool(int numThreads, int queueCapacity) {
        taskQueue = new LinkedBlockingQueue<>(queueCapacity);
        workers = new Thread[numThreads];
        
        for (int i = 0; i < numThreads; i++) {
            workers[i] = new Thread(new Worker(), "Worker-" + i);
            workers[i].start();
        }
    }
    
    public void submit(Runnable task) {
        if (isShutdown) {
            throw new IllegalStateException("Thread pool is shutdown");
        }
        
        try {
            taskQueue.put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void shutdown() {
        isShutdown = true;
        for (Thread worker : workers) {
            worker.interrupt();
        }
    }
    
    private class Worker implements Runnable {
        @Override
        public void run() {
            while (!isShutdown) {
                try {
                    Runnable task = taskQueue.take();
                    task.run();
                } catch (InterruptedException e) {
                    if (isShutdown) {
                        break;
                    }
                }
            }
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        SimpleThreadPool pool = new SimpleThreadPool(3, 10);
        
        for (int i = 0; i < 20; i++) {
            int taskId = i;
            pool.submit(() -> {
                System.out.println("Executing task " + taskId + 
                    " on " + Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        Thread.sleep(25000);
        pool.shutdown();
    }
}
```

---

### Problem 13: Rate Limiter

**Question:** Implement a rate limiter that allows max N requests per second.

**Solution:**

```java
import java.util.concurrent.*;

public class RateLimiter {
    
    // Solution 1: Token Bucket
    static class TokenBucketRateLimiter {
        private final int maxTokens;
        private final long refillInterval;
        private int availableTokens;
        private long lastRefillTime;
        
        public TokenBucketRateLimiter(int maxRequests, long timeWindowMs) {
            this.maxTokens = maxRequests;
            this.availableTokens = maxRequests;
            this.refillInterval = timeWindowMs / maxRequests;
            this.lastRefillTime = System.currentTimeMillis();
        }
        
        public synchronized boolean allowRequest() {
            refill();
            
            if (availableTokens > 0) {
                availableTokens--;
                return true;
            }
            
            return false;
        }
        
        private void refill() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTime;
            int tokensToAdd = (int) (timePassed / refillInterval);
            
            if (tokensToAdd > 0) {
                availableTokens = Math.min(maxTokens, availableTokens + tokensToAdd);
                lastRefillTime = now;
            }
        }
    }
    
    // Solution 2: Sliding Window Log
    static class SlidingWindowRateLimiter {
        private final int maxRequests;
        private final long windowMs;
        private final Queue<Long> requestLog;
        
        public SlidingWindowRateLimiter(int maxRequests, long windowMs) {
            this.maxRequests = maxRequests;
            this.windowMs = windowMs;
            this.requestLog = new ConcurrentLinkedQueue<>();
        }
        
        public synchronized boolean allowRequest() {
            long now = System.currentTimeMillis();
            long windowStart = now - windowMs;
            
            // Remove old requests
            while (!requestLog.isEmpty() && requestLog.peek() < windowStart) {
                requestLog.poll();
            }
            
            if (requestLog.size() < maxRequests) {
                requestLog.offer(now);
                return true;
            }
            
            return false;
        }
    }
    
    // Solution 3: Using Semaphore
    static class SemaphoreRateLimiter {
        private final Semaphore semaphore;
        private final ScheduledExecutorService scheduler;
        private final int maxRequests;
        private final long timeWindowMs;
        
        public SemaphoreRateLimiter(int maxRequests, long timeWindowMs) {
            this.maxRequests = maxRequests;
            this.timeWindowMs = timeWindowMs;
            this.semaphore = new Semaphore(maxRequests);
            this.scheduler = Executors.newScheduledThreadPool(1);
            
            // Refill permits periodically
            scheduler.scheduleAtFixedRate(() -> {
                int used = maxRequests - semaphore.availablePermits();
                semaphore.release(used);
            }, timeWindowMs, timeWindowMs, TimeUnit.MILLISECONDS);
        }
        
        public boolean allowRequest() {
            return semaphore.tryAcquire();
        }
        
        public void shutdown() {
            scheduler.shutdown();
        }
    }
    
    public static void main(String[] args) throws InterruptedException {
        RateLimiter.TokenBucketRateLimiter limiter = 
            new RateLimiter.TokenBucketRateLimiter(5, 1000);
        
        for (int i = 0; i < 10; i++) {
            if (limiter.allowRequest()) {
                System.out.println("Request " + i + " allowed");
            } else {
                System.out.println("Request " + i + " rejected");
            }
            Thread.sleep(100);
        }
    }
}
```

---

## Multithreading - Hard Problems

### Problem 14: Parallel Merge Sort

**Question:** Implement merge sort using multiple threads.

**Solution:**

```java
import java.util.concurrent.*;

public class ParallelMergeSort {
    
    private static final int THRESHOLD = 1000;  // Use sequential below this
    
    public static void parallelMergeSort(int[] arr) {
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(new MergeSortTask(arr, 0, arr.length - 1));
    }
    
    static class MergeSortTask extends RecursiveAction {
        private int[] arr;
        private int left;
        private int right;
        
        public MergeSortTask(int[] arr, int left, int right) {
            this.arr = arr;
            this.left = left;
            this.right = right;
        }
        
        @Override
        protected void compute() {
            if (right - left < THRESHOLD) {
                // Sequential sort for small arrays
                sequentialMergeSort(arr, left, right);
            } else {
                int mid = left + (right - left) / 2;
                
                // Fork two subtasks
                MergeSortTask leftTask = new MergeSortTask(arr, left, mid);
                MergeSortTask rightTask = new MergeSortTask(arr, mid + 1, right);
                
                invokeAll(leftTask, rightTask);
                
                // Merge results
                merge(arr, left, mid, right);
            }
        }
        
        private void sequentialMergeSort(int[] arr, int left, int right) {
            if (left < right) {
                int mid = left + (right - left) / 2;
                sequentialMergeSort(arr, left, mid);
                sequentialMergeSort(arr, mid + 1, right);
                merge(arr, left, mid, right);
            }
        }
        
        private void merge(int[] arr, int left, int mid, int right) {
            int[] temp = new int[right - left + 1];
            int i = left, j = mid + 1, k = 0;
            
            while (i <= mid && j <= right) {
                if (arr[i] <= arr[j]) {
                    temp[k++] = arr[i++];
                } else {
                    temp[k++] = arr[j++];
                }
            }
            
            while (i <= mid) {
                temp[k++] = arr[i++];
            }
            
            while (j <= right) {
                temp[k++] = arr[j++];
            }
            
            System.arraycopy(temp, 0, arr, left, temp.length);
        }
    }
    
    public static void main(String[] args) {
        int[] arr = {5, 2, 8, 1, 9, 3, 7, 4, 6};
        parallelMergeSort(arr);
        System.out.println(Arrays.toString(arr));
    }
}
```

---

### Problem 15: Implement ReadWrite Lock

**Question:** Implement a custom ReadWriteLock from scratch.

**Solution:**

```java
public class CustomReadWriteLock {
    
    private int readers = 0;
    private int writers = 0;
    private int writeRequests = 0;
    
    public synchronized void lockRead() throws InterruptedException {
        // Wait if there's a writer or pending write requests
        while (writers > 0 || writeRequests > 0) {
            wait();
        }
        readers++;
    }
    
    public synchronized void unlockRead() {
        readers--;
        notifyAll();
    }
    
    public synchronized void lockWrite() throws InterruptedException {
        writeRequests++;
        
        // Wait until no readers or writers
        while (readers > 0 || writers > 0) {
            wait();
        }
        
        writeRequests--;
        writers++;
    }
    
    public synchronized void unlockWrite() {
        writers--;
        notifyAll();
    }
    
    // Test
    public static void main(String[] args) {
        CustomReadWriteLock rwLock = new CustomReadWriteLock();
        SharedResource resource = new SharedResource();
        
        // Multiple readers
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                try {
                    rwLock.lockRead();
                    try {
                        System.out.println(Thread.currentThread().getName() + " reading: " + resource.read());
                        Thread.sleep(1000);
                    } finally {
                        rwLock.unlockRead();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Reader-" + i).start();
        }
        
        // One writer
        new Thread(() -> {
            try {
                rwLock.lockWrite();
                try {
                    System.out.println(Thread.currentThread().getName() + " writing");
                    resource.write(42);
                    Thread.sleep(2000);
                } finally {
                    rwLock.unlockWrite();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Writer").start();
    }
    
    static class SharedResource {
        private int value = 0;
        
        public int read() {
            return value;
        }
        
        public void write(int value) {
            this.value = value;
        }
    }
}
```

---

## Summary & Practice Tips

### Key Topics to Master:

**Collections:**
1. HashMap internals (hashing, collision handling, load factor)
2. ArrayList vs LinkedList trade-offs
3. HashSet vs TreeSet vs LinkedHashSet
4. PriorityQueue and heap operations
5. ConcurrentHashMap for thread safety

**Multithreading:**
1. Thread lifecycle and states
2. Synchronization (synchronized, Lock, volatile)
3. wait() / notify() / notifyAll()
4. Thread pools and ExecutorService
5. CountDownLatch, CyclicBarrier, Semaphore
6. Deadlock, livelock, starvation
7. Atomic classes
8. CompletableFuture for async programming

### Practice Schedule:

**Day 1-2:** Easy problems (1-3, 8-10)
**Day 3-4:** Medium problems (4-6, 11-13)
**Day 5-6:** Hard problems (7, 14-15)
**Day 7:** Mock interview with random problems

### Interview Tips:

1. **Always discuss trade-offs** (time vs space, simple vs optimal)
2. **Start with brute force** then optimize
3. **Test with edge cases** (empty, single element, duplicates)
4. **Explain your thought process** out loud
5. **For threading: discuss deadlock prevention**
6. **Dry run your code** with an example

Good luck with your practice! 🚀
