# 🎯 JAVA, SPRING & MICROSERVICES - INTERVIEW CHEAT SHEETS
## Complete Quick Reference Guide for Technical Interviews

---

# 📚 TABLE OF CONTENTS

## Part A: Core Java
- [J1: Collections Framework](#j1-collections)
- [J2: HashMap Internal Working](#j2-hashmap)
- [J3: Java 8 Streams API](#j3-streams)
- [J4: Multithreading & Concurrency](#j4-multithreading)
- [J5: JVM Architecture & Memory](#j5-jvm)
- [J6: Garbage Collection](#j6-gc)
- [J7: Exception Handling](#j7-exceptions)
- [J8: String, StringBuffer, StringBuilder](#j8-strings)

## Part B: Spring Framework
- [S1: Dependency Injection & IoC](#s1-di-ioc)
- [S2: Bean Lifecycle](#s2-bean-lifecycle)
- [S3: Bean Scopes](#s3-bean-scopes)
- [S4: AOP (Aspect-Oriented Programming)](#s4-aop)
- [S5: Spring MVC Architecture](#s5-spring-mvc)

## Part C: Spring Boot
- [SB1: Auto-Configuration](#sb1-auto-config)
- [SB2: Spring Boot Starters](#sb2-starters)
- [SB3: Application Properties & Profiles](#sb3-profiles)
- [SB4: Spring Boot Actuator](#sb4-actuator)
- [SB5: Exception Handling](#sb5-exceptions)

## Part D: Microservices
- [M1: Microservices Architecture](#m1-architecture)
- [M2: API Gateway Pattern](#m2-api-gateway)
- [M3: Service Discovery](#m3-service-discovery)
- [M4: Circuit Breaker Pattern](#m4-circuit-breaker)
- [M5: Distributed Tracing](#m5-tracing)
- [M6: Event-Driven Architecture](#m6-event-driven)

## Part E: REST API
- [R1: REST Principles](#r1-rest-principles)
- [R2: HTTP Methods](#r2-http-methods)
- [R3: HTTP Status Codes](#r3-status-codes)
- [R4: API Versioning](#r4-versioning)
- [R5: API Security](#r5-security)

---

# 🔷 PART A: CORE JAVA

<a name="j1-collections"></a>
## J1: 📦 Collections Framework

### ⚡ Quick Answer (30 seconds)
> *"Collections Framework provides data structures: List (ordered, duplicates allowed - ArrayList fast random access, LinkedList fast insertion/deletion), Set (unique elements - HashSet O(1) operations, TreeSet sorted), Map (key-value - HashMap O(1) lookup, TreeMap sorted keys). ArrayList backed by array, LinkedList by doubly-linked nodes. HashMap uses array of buckets, handles collisions with linked lists (Java 8+ trees for >8 entries). Time complexity: ArrayList get O(1) add O(1), LinkedList get O(n) add O(1), HashMap get/put O(1) average."*

### 📊 Collection Hierarchy

```
        Collection (I)
             |
    ┌────────┼────────┐
    |        |        |
  List(I)  Set(I)  Queue(I)
    |        |        |
    |        |        └─ PriorityQueue
    |        |        └─ ArrayDeque
    |        |
    |        ├─ HashSet
    |        ├─ LinkedHashSet
    |        └─ TreeSet (SortedSet)
    |
    ├─ ArrayList
    ├─ LinkedList
    └─ Vector (Legacy)
         └─ Stack (Legacy)

        Map (I) - Separate hierarchy
             |
    ┌────────┼────────┐
    |        |        |
 HashMap  TreeMap  LinkedHashMap
    |
    └─ LinkedHashMap
    
 Hashtable (Legacy)
```

### 📋 List Implementations

#### ArrayList vs LinkedList

```
ArrayList (Dynamic Array)
┌───┬───┬───┬───┬───┬───┬───┬───┐
│ 0 │ 1 │ 2 │ 3 │ 4 │ 5 │ 6 │ 7 │
└───┴───┴───┴───┴───┴───┴───┴───┘
 ↑ Direct access by index (Fast!)

LinkedList (Doubly-Linked List)
┌────┐    ┌────┐    ┌────┐    ┌────┐
│ 10 │ ←→ │ 20 │ ←→ │ 30 │ ←→ │ 40 │
└────┘    └────┘    └────┘    └────┘
 ↑ Must traverse (Slow!)
```

**Comparison Table:**

| Operation | ArrayList | LinkedList |
|-----------|-----------|------------|
| **get(index)** | O(1) ✅ | O(n) ❌ |
| **add(element)** | O(1)* amortized | O(1) ✅ |
| **add(index, element)** | O(n) | O(n) |
| **remove(index)** | O(n) | O(n) |
| **contains(element)** | O(n) | O(n) |
| **Memory** | Less (contiguous) | More (node overhead) |

**Code Examples:**

```java
// ArrayList - Best for random access
List<String> arrayList = new ArrayList<>();
arrayList.add("Java");        // O(1)
arrayList.get(0);             // O(1) ✅ Fast!
arrayList.add(0, "Python");   // O(n) - shifts elements

// LinkedList - Best for frequent insertions/deletions at ends
List<String> linkedList = new LinkedList<>();
linkedList.addFirst("Java");  // O(1) ✅
linkedList.addLast("Python"); // O(1) ✅
linkedList.get(0);            // O(n) ❌ Slow!
```

### 🔑 Set Implementations

#### HashSet vs TreeSet vs LinkedHashSet

```
HashSet (Unordered)
┌────────────────────────┐
│  "Java"  "C++"         │
│     "Python"           │
│         "Go"  "Rust"   │
└────────────────────────┘
No order, O(1) operations

TreeSet (Sorted - Red-Black Tree)
         "Go"
        /    \
    "C++"    "Python"
              /      \
          "Java"   "Rust"
Sorted order, O(log n) operations

LinkedHashSet (Insertion Order)
"Java" → "Python" → "C++" → "Go" → "Rust"
Insertion order maintained, O(1) operations
```

**Comparison Table:**

| Feature | HashSet | TreeSet | LinkedHashSet |
|---------|---------|---------|---------------|
| **Order** | None | Sorted (natural/comparator) | Insertion order |
| **add()** | O(1) ✅ | O(log n) | O(1) ✅ |
| **contains()** | O(1) ✅ | O(log n) | O(1) ✅ |
| **remove()** | O(1) ✅ | O(log n) | O(1) ✅ |
| **Null allowed** | Yes (one) | No ❌ | Yes (one) |
| **Use Case** | Fast lookup | Sorted data | Maintain order |

**Code Examples:**

```java
// HashSet - Fastest, no order
Set<String> hashSet = new HashSet<>();
hashSet.add("Java");
hashSet.add("Python");
hashSet.add("C++");
// Output order: unpredictable

// TreeSet - Sorted
Set<String> treeSet = new TreeSet<>();
treeSet.add("Java");
treeSet.add("Python");
treeSet.add("C++");
// Output: [C++, Java, Python] (sorted)

// LinkedHashSet - Insertion order
Set<String> linkedSet = new LinkedHashSet<>();
linkedSet.add("Java");
linkedSet.add("Python");
linkedSet.add("C++");
// Output: [Java, Python, C++] (insertion order)
```

### 🗺️ Map Implementations

#### HashMap vs TreeMap vs LinkedHashMap

**Comparison Table:**

| Feature | HashMap | TreeMap | LinkedHashMap |
|---------|---------|---------|---------------|
| **Order** | None | Sorted by key | Insertion/Access order |
| **put()** | O(1) ✅ | O(log n) | O(1) ✅ |
| **get()** | O(1) ✅ | O(log n) | O(1) ✅ |
| **Null key** | Yes (one) | No ❌ | Yes (one) |
| **Null values** | Yes | Yes | Yes |
| **Thread-safe** | No | No | No |
| **Use Case** | General purpose | Sorted keys | Cache (LRU) |

**Code Examples:**

```java
// HashMap - Fastest, no order
Map<String, Integer> hashMap = new HashMap<>();
hashMap.put("Java", 1);
hashMap.put("Python", 2);
hashMap.put("C++", 3);
// Order: unpredictable

// TreeMap - Sorted by keys
Map<String, Integer> treeMap = new TreeMap<>();
treeMap.put("Java", 1);
treeMap.put("Python", 2);
treeMap.put("C++", 3);
// Order: {C++=3, Java=1, Python=2} (sorted keys)

// LinkedHashMap - Insertion order
Map<String, Integer> linkedMap = new LinkedHashMap<>();
linkedMap.put("Java", 1);
linkedMap.put("Python", 2);
linkedMap.put("C++", 3);
// Order: {Java=1, Python=2, C++=3} (insertion order)

// LinkedHashMap - Access order (LRU Cache)
Map<String, Integer> lruCache = new LinkedHashMap<>(16, 0.75f, true);
lruCache.put("A", 1);
lruCache.put("B", 2);
lruCache.get("A");  // Moves "A" to end
// Order: {B=2, A=1} (most recently accessed at end)
```

### ⚠️ Thread-Safe Collections

```java
// ❌ NOT thread-safe
List<String> list = new ArrayList<>();
Map<String, Integer> map = new HashMap<>();

// ✅ Thread-safe wrappers (slow - full synchronization)
List<String> syncList = Collections.synchronizedList(new ArrayList<>());
Map<String, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());

// ✅ Concurrent collections (fast - lock striping)
ConcurrentHashMap<String, Integer> concMap = new ConcurrentHashMap<>();
CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>();

// ConcurrentHashMap: Lock striping for high concurrency
concMap.put("key", 1);      // Thread-safe
concMap.get("key");         // No locking for reads!
concMap.putIfAbsent("k", 1); // Atomic operation

// CopyOnWriteArrayList: Copy on write (expensive writes, fast reads)
cowList.add("item");        // Creates new array copy
cowList.get(0);             // No locking!
// Use for: Read-heavy scenarios (listeners, observers)
```

### 🎓 Interview Talking Points

✅ **Key Points:**
- "ArrayList backed by array - O(1) random access, O(n) insertion. LinkedList doubly-linked - O(1) add/remove at ends, O(n) random access"
- "HashMap O(1) average case using hash function and buckets. Collisions handled with linked lists (trees for >8 entries in Java 8+)"
- "TreeSet/TreeMap use Red-Black tree - O(log n) operations but maintain sorted order"
- "ConcurrentHashMap uses lock striping - splits map into segments, allows concurrent reads/writes"
- "CopyOnWriteArrayList copies array on write - expensive writes but lock-free reads"

### 💡 When to Use What

```java
✅ ArrayList: Random access, rare insertions
    userList.get(100);  // Fast!

✅ LinkedList: Frequent insertions/deletions at ends
    queue.addFirst(item);
    queue.removeLast();

✅ HashSet: Fast lookup, no duplicates, no order needed
    uniqueEmails.contains("user@email.com");

✅ TreeSet: Need sorted unique elements
    sortedScores.first();  // Lowest score
    sortedScores.last();   // Highest score

✅ HashMap: Fast key-value lookup
    userCache.get(userId);

✅ TreeMap: Need sorted keys
    dateMap.firstKey();   // Earliest date
    dateMap.lastKey();    // Latest date

✅ LinkedHashMap: LRU cache
    new LinkedHashMap<>(16, 0.75f, true);  // Access order

✅ ConcurrentHashMap: High concurrency
    sharedCache.put(key, value);  // Thread-safe

✅ CopyOnWriteArrayList: Many reads, few writes
    listeners.forEach(l -> l.notify());  // Lock-free iteration
```

---

<a name="j2-hashmap"></a>
## J2: 🗺️ HashMap Internal Working

### ⚡ Quick Answer (30 seconds)
> *"HashMap stores key-value pairs using array of buckets. Hash function converts key to index: index = hash(key) % array.length. Collisions (two keys same index) handled with linked lists, converted to balanced trees when >8 entries (Java 8+). put() and get() are O(1) average. Load factor 0.75 triggers resize (rehashing) when 75% full - creates array 2x size, rehashes all entries. Initial capacity 16, grows to 32, 64, 128... Thread-unsafe - use ConcurrentHashMap for concurrency."*

### 🏗️ Internal Structure

```
HashMap Internal Structure:

┌─────────────────────────────────────────┐
│    HashMap                              │
│                                         │
│  Node<K,V>[] table = new Node[16];     │
│  size = 5                               │
│  threshold = 12  (capacity * 0.75)     │
│  loadFactor = 0.75                      │
└─────────────────────────────────────────┘
              ↓
    Array of Buckets (Initial: 16)
┌────┬────┬────┬────┬────┬────┬────┬────┐
│ 0  │ 1  │ 2  │ 3  │ 4  │ 5  │... │ 15 │
└────┴────┴────┴────┴────┴────┴────┴────┘
  │    │    │                  │
  ↓    ↓    ↓                  ↓
Node  Node  Node              Node
        │
        ↓ Collision!
      Node (Linked list)
        │
        ↓ >8 collisions
    TreeNode (Red-Black Tree)
```

### 🔢 Hash Function & Index Calculation

```java
// How HashMap calculates array index:

// Step 1: Get hashCode from key
String key = "Java";
int hashCode = key.hashCode();  // e.g., 2301506

// Step 2: Improve hash (reduce collisions)
int hash = hashCode ^ (hashCode >>> 16);

// Step 3: Calculate bucket index
int index = hash & (table.length - 1);
// Equivalent to: index = hash % table.length
// But bitwise AND is faster!

// Example:
// table.length = 16
// hash = 2301506
// index = 2301506 & 15 = 2
```

**Why XOR with right-shifted bits?**
```
Original hash: 10110101 11010011 00101110 10110010
Right shift 16: 00000000 00000000 10110101 11010011
XOR result:    10110101 11010011 10011011 01101001
                                  ↑ Better distribution
```

### 📝 Node Structure

```java
static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;      // Cached hash value
    final K key;
    V value;
    Node<K,V> next;      // For collision chain (linked list)
    
    Node(int hash, K key, V value, Node<K,V> next) {
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.next = next;
    }
}

// For tree nodes (when bucket has >8 entries)
static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
    TreeNode<K,V> parent;
    TreeNode<K,V> left;
    TreeNode<K,V> right;
    TreeNode<K,V> prev;
    boolean red;  // Red-Black tree color
}
```

### 🔄 put() Method Flow

```java
map.put("Java", 1);

// Internal flow:
┌─────────────────────────────────────────┐
│ 1. Calculate hash                       │
│    hash = hash(key)                     │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│ 2. Calculate bucket index               │
│    index = hash & (table.length - 1)    │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│ 3. Check if bucket is empty             │
│    if (table[index] == null)            │
│        → Insert new Node                │
│    else                                 │
│        → Handle collision               │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│ 4. Collision handling                   │
│    • Check if key exists (equals())     │
│      → If yes: Replace value            │
│    • If bucket size < 8:                │
│      → Add to linked list               │
│    • If bucket size >= 8:               │
│      → Convert to Red-Black tree        │
└────────────┬────────────────────────────┘
             ↓
┌─────────────────────────────────────────┐
│ 5. Check load factor                    │
│    if (size > threshold)                │
│        → Resize and rehash              │
└─────────────────────────────────────────┘
```

**Code Example:**

```java
Map<String, Integer> map = new HashMap<>();

// Put operation
map.put("Java", 1);
// 1. hash("Java") = 2301506
// 2. index = 2301506 & 15 = 2
// 3. table[2] = null
// 4. table[2] = new Node(hash, "Java", 1, null)

map.put("Python", 2);
// Different index, no collision

map.put("JavaScript", 3);
// Same index as "Java" - collision!
// table[2] = Node("Java", 1) → Node("JavaScript", 3)
// Linked list created

// Get operation
Integer value = map.get("Java");
// 1. hash("Java") = 2301506
// 2. index = 2
// 3. Traverse linked list at table[2]
// 4. Compare keys using equals()
// 5. Return value: 1
```

### 🔁 Resize & Rehashing

```java
// Happens when: size > capacity * loadFactor
// Initial: capacity = 16, threshold = 12
// After 12 puts → resize!

// Before resize (capacity = 16):
┌────┬────┬────┬────┐
│ 0  │ 1  │ 2  │ 3  │ ... (16 buckets)
└────┴────┴────┴────┘
  │
  ↓ Entry at index 2

// After resize (capacity = 32):
┌────┬────┬────┬────┬────┬────┬────┬────┐
│ 0  │ 1  │ 2  │ 3  │... │ 18 │... │ 31 │ (32 buckets)
└────┴────┴────┴────┴────┴────┴────┴────┘
  │                          │
  ↓                          ↓
Entry may move!          Or here!
(same hash % 32)
```

**Rehashing Process:**

```java
void resize() {
    // 1. Create new array (double size)
    Node<K,V>[] oldTable = table;
    Node<K,V>[] newTable = new Node[oldTable.length * 2];
    
    // 2. Rehash all entries
    for (Node<K,V> node : oldTable) {
        while (node != null) {
            int newIndex = node.hash & (newTable.length - 1);
            // Place in new bucket
            newTable[newIndex] = node;
            node = node.next;
        }
    }
    
    // 3. Update table reference
    table = newTable;
    threshold = (int)(newTable.length * loadFactor);
}

// Performance impact:
// Resizing: O(n) - must rehash all entries
// But happens rarely (only when exceeding threshold)
```

### 🌳 Tree vs Linked List (Java 8+)

```java
// Threshold: 8 entries in one bucket

Linked List (< 8 entries):
Node → Node → Node → Node → Node
O(n) traversal for lookup

Red-Black Tree (≥ 8 entries):
        Node
       /    \
    Node    Node
    / \      / \
 Node Node Node Node
O(log n) traversal for lookup ✅

// Converts back to linked list when < 6 entries
// Hysteresis prevents thrashing
```

### 💡 Time Complexity

| Operation | Average Case | Worst Case (before Java 8) | Worst Case (Java 8+) |
|-----------|--------------|---------------------------|---------------------|
| **get()** | O(1) | O(n) all in one bucket | O(log n) tree |
| **put()** | O(1) | O(n) | O(log n) |
| **remove()** | O(1) | O(n) | O(log n) |
| **containsKey()** | O(1) | O(n) | O(log n) |

### 🔒 Thread Safety Issues

```java
// ❌ HashMap is NOT thread-safe!

// Problem 1: Race condition on put
Thread 1: map.put("A", 1);  // Calculates index 2
Thread 2: map.put("B", 2);  // Same index 2
// Both try to write to same bucket - one may be lost!

// Problem 2: Infinite loop during resize (Java 7)
Thread 1: Resizing, moving entries
Thread 2: Also resizing simultaneously
// Can create circular linked list → infinite loop!

// ✅ Solutions:

// 1. Collections.synchronizedMap (slow)
Map<String, Integer> syncMap = 
    Collections.synchronizedMap(new HashMap<>());

// 2. ConcurrentHashMap (fast) ✅
Map<String, Integer> concMap = new ConcurrentHashMap<>();
// Uses lock striping - multiple locks for different segments
// Allows concurrent reads and writes
```

### 🎓 Interview Talking Points

✅ **Key Points:**
- "HashMap uses array of buckets. Hash function converts key to index modulo array size"
- "Collisions handled with linked lists, converted to Red-Black trees for >8 entries in Java 8+"
- "Load factor 0.75 means resize when 75% full. Resize creates 2x array and rehashes all entries - O(n) operation"
- "Initial capacity 16, doubles each resize: 16 → 32 → 64 → 128..."
- "get() and put() are O(1) average case, O(log n) worst case with trees"
- "NOT thread-safe - use ConcurrentHashMap for concurrent access"

### 💡 Best Practices

```java
// ✅ Pre-size if you know capacity
Map<String, Integer> map = new HashMap<>(1000);  // Avoids resizes

// ✅ Use immutable keys (String, Integer, etc.)
Map<String, Integer> goodMap = new HashMap<>();
goodMap.put("key", 1);  // String is immutable ✅

// ❌ Mutable keys can break HashMap
class MutableKey {
    int value;
    public int hashCode() { return value; }
}
MutableKey key = new MutableKey();
key.value = 1;
map.put(key, "data");
key.value = 2;  // hashCode changed!
map.get(key);   // Returns null! (looking in wrong bucket)

// ✅ Override both hashCode() and equals()
class Person {
    String name;
    
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Person)) return false;
        return Objects.equals(name, ((Person) obj).name);
    }
}
```

---

<a name="j3-streams"></a>
## J3: 🌊 Java 8 Streams API

### ⚡ Quick Answer (30 seconds)
> *"Streams provide functional-style operations on collections. Stream pipeline has source (collection), intermediate operations (filter, map, sorted - lazy, chainable), terminal operation (collect, forEach, reduce - triggers execution). Streams are immutable, don't modify source, evaluated lazily. Parallel streams split data across threads for concurrent processing. Common operations: filter (predicate), map (transform), reduce (aggregate), collect (to List/Set/Map). Example: list.stream().filter(x -> x > 5).map(x -> x * 2).collect(Collectors.toList())."*

### 🔄 Stream Pipeline

```
Stream Pipeline Structure:

Source → Intermediate Ops → Terminal Op → Result
         (Lazy)              (Eager)

┌──────────┐   ┌────────┐   ┌────────┐   ┌──────────┐   ┌────────┐
│Collection│ → │filter()│ → │ map()  │ → │sorted()  │ → │collect()│
└──────────┘   └────────┘   └────────┘   └──────────┘   └────────┘
   Source      Intermediate  Intermediate  Intermediate    Terminal
               Operations (Lazy - not executed until terminal)
```

### 📋 Stream Creation

```java
// From Collection
List<String> list = Arrays.asList("a", "b", "c");
Stream<String> stream1 = list.stream();

// From Array
String[] array = {"a", "b", "c"};
Stream<String> stream2 = Arrays.stream(array);

// From Values
Stream<String> stream3 = Stream.of("a", "b", "c");

// Infinite Stream
Stream<Integer> stream4 = Stream.iterate(0, n -> n + 1);  // 0,1,2,3...
Stream<Double> stream5 = Stream.generate(Math::random);   // Random numbers

// From Range
IntStream range = IntStream.range(1, 10);  // 1 to 9
IntStream rangeClosed = IntStream.rangeClosed(1, 10);  // 1 to 10

// From File
Stream<String> lines = Files.lines(Paths.get("file.txt"));
```

### 🔄 Intermediate Operations (Lazy)

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

// filter - Keep elements matching predicate
numbers.stream()
    .filter(n -> n % 2 == 0)  // [2, 4, 6, 8, 10]
    .collect(Collectors.toList());

// map - Transform each element
numbers.stream()
    .map(n -> n * 2)  // [2, 4, 6, 8, 10, 12, 14, 16, 18, 20]
    .collect(Collectors.toList());

// flatMap - Flatten nested structures
List<List<Integer>> nested = Arrays.asList(
    Arrays.asList(1, 2),
    Arrays.asList(3, 4),
    Arrays.asList(5, 6)
);
nested.stream()
    .flatMap(List::stream)  // [1, 2, 3, 4, 5, 6]
    .collect(Collectors.toList());

// distinct - Remove duplicates
Arrays.asList(1, 2, 2, 3, 3, 4).stream()
    .distinct()  // [1, 2, 3, 4]
    .collect(Collectors.toList());

// sorted - Sort elements
numbers.stream()
    .sorted()  // Natural order
    .collect(Collectors.toList());

numbers.stream()
    .sorted(Comparator.reverseOrder())  // Reverse
    .collect(Collectors.toList());

// limit - Take first N elements
numbers.stream()
    .limit(5)  // [1, 2, 3, 4, 5]
    .collect(Collectors.toList());

// skip - Skip first N elements
numbers.stream()
    .skip(5)  // [6, 7, 8, 9, 10]
    .collect(Collectors.toList());

// peek - Perform action without modifying (debugging)
numbers.stream()
    .peek(n -> System.out.println("Processing: " + n))
    .map(n -> n * 2)
    .collect(Collectors.toList());
```

### 🎯 Terminal Operations (Eager)

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

// collect - Convert to collection
List<Integer> list = numbers.stream()
    .collect(Collectors.toList());

Set<Integer> set = numbers.stream()
    .collect(Collectors.toSet());

Map<Integer, String> map = numbers.stream()
    .collect(Collectors.toMap(
        n -> n,
        n -> "Number: " + n
    ));

// forEach - Perform action on each element
numbers.stream()
    .forEach(System.out::println);

// reduce - Aggregate to single value
int sum = numbers.stream()
    .reduce(0, (a, b) -> a + b);  // Sum: 15

int product = numbers.stream()
    .reduce(1, (a, b) -> a * b);  // Product: 120

Optional<Integer> max = numbers.stream()
    .reduce((a, b) -> a > b ? a : b);  // Max: 5

// count - Count elements
long count = numbers.stream().count();  // 5

// anyMatch - Check if any element matches
boolean hasEven = numbers.stream()
    .anyMatch(n -> n % 2 == 0);  // true

// allMatch - Check if all elements match
boolean allPositive = numbers.stream()
    .allMatch(n -> n > 0);  // true

// noneMatch - Check if no elements match
boolean noneNegative = numbers.stream()
    .noneMatch(n -> n < 0);  // true

// findFirst - Get first element
Optional<Integer> first = numbers.stream()
    .findFirst();  // Optional[1]

// findAny - Get any element (useful for parallel)
Optional<Integer> any = numbers.stream()
    .findAny();  // Optional[any element]

// min/max - Get minimum/maximum
Optional<Integer> min = numbers.stream()
    .min(Integer::compare);  // Optional[1]

Optional<Integer> max2 = numbers.stream()
    .max(Integer::compare);  // Optional[5]
```

### 🔗 Chaining Operations

```java
List<String> names = Arrays.asList(
    "John", "Jane", "Jack", "Jill", "Bob", "Alice"
);

// Complex pipeline
List<String> result = names.stream()
    .filter(name -> name.startsWith("J"))     // [John, Jane, Jack, Jill]
    .map(String::toUpperCase)                  // [JOHN, JANE, JACK, JILL]
    .sorted()                                  // [JACK, JANE, JILL, JOHN]
    .limit(3)                                  // [JACK, JANE, JILL]
    .collect(Collectors.toList());

System.out.println(result);  // [JACK, JANE, JILL]
```

### 📊 Collectors Examples

```java
List<Person> people = Arrays.asList(
    new Person("John", 25),
    new Person("Jane", 30),
    new Person("Jack", 25),
    new Person("Jill", 30)
);

// toList
List<String> names = people.stream()
    .map(Person::getName)
    .collect(Collectors.toList());

// toSet
Set<Integer> ages = people.stream()
    .map(Person::getAge)
    .collect(Collectors.toSet());  // [25, 30]

// joining
String namesCsv = people.stream()
    .map(Person::getName)
    .collect(Collectors.joining(", "));  // "John, Jane, Jack, Jill"

// groupingBy
Map<Integer, List<Person>> byAge = people.stream()
    .collect(Collectors.groupingBy(Person::getAge));
// {25=[John, Jack], 30=[Jane, Jill]}

// partitioningBy
Map<Boolean, List<Person>> youngOld = people.stream()
    .collect(Collectors.partitioningBy(p -> p.getAge() < 30));
// {false=[Jane, Jill], true=[John, Jack]}

// counting
Map<Integer, Long> ageCount = people.stream()
    .collect(Collectors.groupingBy(
        Person::getAge,
        Collectors.counting()
    ));
// {25=2, 30=2}

// summingInt
int totalAge = people.stream()
    .collect(Collectors.summingInt(Person::getAge));  // 110

// averagingInt
double avgAge = people.stream()
    .collect(Collectors.averagingInt(Person::getAge));  // 27.5

// maxBy/minBy
Optional<Person> oldest = people.stream()
    .collect(Collectors.maxBy(
        Comparator.comparing(Person::getAge)
    ));
```

### ⚡ Parallel Streams

```java
List<Integer> numbers = IntStream.rangeClosed(1, 1000000)
    .boxed()
    .collect(Collectors.toList());

// Sequential stream
long start = System.currentTimeMillis();
long sum1 = numbers.stream()
    .mapToInt(Integer::intValue)
    .sum();
System.out.println("Sequential: " + (System.currentTimeMillis() - start) + "ms");

// Parallel stream
start = System.currentTimeMillis();
long sum2 = numbers.parallelStream()
    .mapToInt(Integer::intValue)
    .sum();
System.out.println("Parallel: " + (System.currentTimeMillis() - start) + "ms");

// How parallel works:
┌──────────────────────────────────────────┐
│      Original Collection (1M items)      │
└────────────┬─────────────────────────────┘
             │ parallelStream()
       ┌─────┴──────┐
       ↓            ↓
  ┌─────────┐  ┌─────────┐
  │ Chunk 1 │  │ Chunk 2 │  ... (Split by ForkJoinPool)
  │250K items│  │250K items│
  └────┬────┘  └────┬────┘
       │            │
    Thread 1     Thread 2
       │            │
       └──────┬─────┘
              ↓
          Combine results
```

**When to use Parallel:**
```java
// ✅ Good for parallel:
// - Large datasets (>10,000 elements)
// - CPU-intensive operations
// - Independent operations (no shared state)
numbers.parallelStream()
    .map(n -> heavyComputation(n))  // CPU-bound
    .collect(Collectors.toList());

// ❌ Avoid parallel:
// - Small datasets (overhead > benefit)
// - I/O operations (thread waiting)
// - Operations with side effects
numbers.parallelStream()
    .forEach(n -> sharedList.add(n));  // Race condition!
```

### ⚠️ Common Pitfalls

```java
// ❌ WRONG: Stream reuse
Stream<Integer> stream = numbers.stream();
stream.filter(n -> n > 5).collect(Collectors.toList());
stream.map(n -> n * 2).collect(Collectors.toList());  // IllegalStateException!
// Streams can only be used once

// ✅ CORRECT: Create new stream
numbers.stream().filter(n -> n > 5).collect(Collectors.toList());
numbers.stream().map(n -> n * 2).collect(Collectors.toList());

// ❌ WRONG: Modifying source during stream
List<Integer> list = new ArrayList<>(Arrays.asList(1, 2, 3));
list.stream().forEach(n -> list.add(n * 2));  // ConcurrentModificationException!

// ✅ CORRECT: Create new collection
List<Integer> result = list.stream()
    .map(n -> n * 2)
    .collect(Collectors.toList());

// ❌ WRONG: Side effects in parallel
int[] sum = {0};
IntStream.range(1, 100).parallel()
    .forEach(n -> sum[0] += n);  // Race condition!
System.out.println(sum[0]);  // Wrong answer!

// ✅ CORRECT: Use reduce
int correctSum = IntStream.range(1, 100).parallel()
    .reduce(0, Integer::sum);
```

### 🎓 Interview Talking Points

✅ **Key Points:**
- "Streams have source, intermediate operations (lazy), terminal operation (eager)"
- "Intermediate: filter, map, flatMap, sorted - chainable, not executed until terminal"
- "Terminal: collect, forEach, reduce - triggers pipeline execution"
- "Streams are immutable, don't modify source collection"
- "Parallel streams use ForkJoinPool, split data across threads - good for large datasets and CPU-bound operations"
- "Avoid parallel for small data, I/O operations, or side effects"

### 💡 Real-World Examples

```java
// Example 1: Process orders
List<Order> orders = getOrders();

BigDecimal totalRevenue = orders.stream()
    .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
    .filter(o -> o.getDate().isAfter(LocalDate.now().minusDays(30)))
    .map(Order::getAmount)
    .reduce(BigDecimal.ZERO, BigDecimal::add);

// Example 2: Group employees by department
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment));

// Example 3: Find top 5 products by sales
List<Product> top5 = products.stream()
    .sorted(Comparator.comparing(Product::getSales).reversed())
    .limit(5)
    .collect(Collectors.toList());

// Example 4: Convert list of objects to map
Map<Long, User> userMap = users.stream()
    .collect(Collectors.toMap(User::getId, Function.identity()));
```

---

<a name="j4-multithreading"></a>
## J4: 🧵 Multithreading & Concurrency

### ⚡ Quick Answer (30 seconds)
> *"Thread is lightweight process. Create via Thread class or Runnable interface. synchronized keyword provides mutual exclusion - only one thread enters synchronized block. volatile ensures visibility across threads. ExecutorService manages thread pool - submit tasks, reuses threads. Common issues: race conditions (concurrent modification), deadlock (circular wait), thread starvation. Solutions: synchronized, locks (ReentrantLock), concurrent collections (ConcurrentHashMap), atomic variables (AtomicInteger). CompletableFuture for async programming."*

### 🧵 Creating Threads

```java
// Method 1: Extend Thread class
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Thread running: " + Thread.currentThread().getName());
    }
}

MyThread thread = new MyThread();
thread.start();  // Starts new thread, calls run()

// Method 2: Implement Runnable (Preferred)
class MyRunnable implements Runnable {
    @Override
    public void run() {
        System.out.println("Runnable running: " + Thread.currentThread().getName());
    }
}

Thread thread = new Thread(new MyRunnable());
thread.start();

// Method 3: Lambda (Java 8+)
Thread thread = new Thread(() -> {
    System.out.println("Lambda thread: " + Thread.currentThread().getName());
});
thread.start();

// Method 4: ExecutorService (Best for production)
ExecutorService executor = Executors.newFixedThreadPool(5);
executor.submit(() -> {
    System.out.println("Executor thread: " + Thread.currentThread().getName());
});
executor.shutdown();
```

### 🔒 Synchronization

#### synchronized Keyword

```java
public class Counter {
    private int count = 0;
    
    // ❌ WRONG: Race condition
    public void incrementUnsafe() {
        count++;  // Not atomic! (read, increment, write)
    }
    
    // ✅ CORRECT: synchronized method
    public synchronized void increment() {
        count++;  // Thread-safe
    }
    
    // ✅ CORRECT: synchronized block
    public void incrementBlock() {
        synchronized(this) {
            count++;
        }
    }
    
    public synchronized int getCount() {
        return count;
    }
}

// Usage:
Counter counter = new Counter();

// Without synchronization - race condition
for (int i = 0; i < 1000; i++) {
    new Thread(() -> counter.incrementUnsafe()).start();
}
Thread.sleep(1000);
System.out.println(counter.getCount());  // NOT 1000! (e.g., 987)

// With synchronization - correct
for (int i = 0; i < 1000; i++) {
    new Thread(() -> counter.increment()).start();
}
Thread.sleep(1000);
System.out.println(counter.getCount());  // 1000 ✅
```

**How synchronized works:**

```
synchronized Internals:

Object Monitor (Every object has one)
┌──────────────────────────────────┐
│  Owner: null                     │
│  Wait Set: []                    │
│  Entry Set: []                   │
└──────────────────────────────────┘

Thread T1 enters synchronized block:
┌──────────────────────────────────┐
│  Owner: T1 ✅ (acquired lock)    │
│  Wait Set: []                    │
│  Entry Set: []                   │
└──────────────────────────────────┘

Thread T2 tries to enter:
┌──────────────────────────────────┐
│  Owner: T1                       │
│  Wait Set: []                    │
│  Entry Set: [T2] ← Blocked!      │
└──────────────────────────────────┘

T1 exits:
┌──────────────────────────────────┐
│  Owner: T2 ✅ (T2 acquired)      │
│  Wait Set: []                    │
│  Entry Set: []                   │
└──────────────────────────────────┘
```

#### volatile Keyword

```java
public class SharedFlag {
    // ❌ WRONG: May not be visible to other threads
    private boolean flag = false;
    
    public void setFlagUnsafe() {
        flag = true;  // May stay in thread's cache!
    }
    
    public boolean isFlagSetUnsafe() {
        return flag;  // May read stale value!
    }
    
    // ✅ CORRECT: volatile ensures visibility
    private volatile boolean volatileFlag = false;
    
    public void setFlag() {
        volatileFlag = true;  // Immediately visible to all threads
    }
    
    public boolean isFlagSet() {
        return volatileFlag;  // Always reads latest value
    }
}

// Problem without volatile:
Thread 1: flag = true;    → Writes to CPU cache
Thread 2: if (flag)...    → Reads from its own cache (still false!)

// With volatile:
Thread 1: volatileFlag = true;  → Writes to main memory
Thread 2: if (volatileFlag)...  → Reads from main memory ✅
```

**synchronized vs volatile:**

| Feature | synchronized | volatile |
|---------|-------------|----------|
| **Mutual exclusion** | ✅ Yes | ❌ No |
| **Visibility** | ✅ Yes | ✅ Yes |
| **Atomicity** | ✅ Yes | ❌ No (except assignments) |
| **Use case** | Complex operations | Simple flags |
| **Performance** | Slower (locking) | Faster (no locking) |

### 🏊 Thread Pools (ExecutorService)

```java
// Fixed Thread Pool - Fixed number of threads
ExecutorService fixed = Executors.newFixedThreadPool(5);
for (int i = 0; i < 100; i++) {
    final int taskId = i;
    fixed.submit(() -> {
        System.out.println("Task " + taskId + " by " + Thread.currentThread().getName());
    });
}
fixed.shutdown();  // Stop accepting new tasks
fixed.awaitTermination(1, TimeUnit.MINUTES);  // Wait for completion

// Cached Thread Pool - Creates threads as needed, reuses idle
ExecutorService cached = Executors.newCachedThreadPool();
// Good for many short tasks

// Single Thread Executor - One thread, sequential execution
ExecutorService single = Executors.newSingleThreadExecutor();
// Guarantees order

// Scheduled Thread Pool - Delayed/periodic execution
ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(2);
scheduled.schedule(() -> System.out.println("After 5 sec"), 5, TimeUnit.SECONDS);
scheduled.scheduleAtFixedRate(() -> System.out.println("Every 1 sec"), 0, 1, TimeUnit.SECONDS);
```

**Thread Pool Architecture:**

```
ExecutorService (ThreadPoolExecutor)
┌─────────────────────────────────────────┐
│  Work Queue (BlockingQueue)            │
│  [Task1] [Task2] [Task3] [Task4] ...   │
└─────────────────────────────────────────┘
         ↓          ↓          ↓
    ┌────────┐ ┌────────┐ ┌────────┐
    │Thread 1│ │Thread 2│ │Thread 3│
    └────────┘ └────────┘ └────────┘
    (Worker threads - reused for multiple tasks)
```

### 🔄 Future & CompletableFuture

```java
// Future - Get result from async task
ExecutorService executor = Executors.newFixedThreadPool(5);

Future<Integer> future = executor.submit(() -> {
    Thread.sleep(1000);
    return 42;
});

System.out.println("Waiting for result...");
Integer result = future.get();  // Blocks until complete
System.out.println("Result: " + result);

// CompletableFuture - Modern async API (Java 8+)
CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> {
    // Runs in ForkJoinPool.commonPool()
    return "Hello";
});

CompletableFuture<String> future2 = future1.thenApply(s -> s + " World");

CompletableFuture<Void> future3 = future2.thenAccept(System.out::println);

future3.join();  // Wait for completion

// Chaining multiple async operations
CompletableFuture.supplyAsync(() -> fetchUser(userId))
    .thenApply(user -> user.getEmail())
    .thenApply(email -> sendEmail(email))
    .thenAccept(result -> System.out.println("Email sent: " + result))
    .exceptionally(ex -> {
        System.err.println("Error: " + ex);
        return null;
    });

// Combine multiple futures
CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> 10);
CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> 20);

CompletableFuture<Integer> combined = future1.thenCombine(future2, (a, b) -> a + b);
System.out.println(combined.get());  // 30
```

### ⚠️ Common Concurrency Problems

#### 1. Race Condition

```java
// ❌ Problem
public class RaceCondition {
    private int balance = 1000;
    
    public void withdraw(int amount) {
        if (balance >= amount) {  // Check
            Thread.yield();  // Simulate delay
            balance -= amount;  // Update
        }
    }
}

// Thread 1: withdraw(600) → Sees balance=1000, proceeds
// Thread 2: withdraw(600) → Sees balance=1000, proceeds
// Both succeed! Balance = -200 (should have blocked one)

// ✅ Solution: synchronized
public synchronized void withdraw(int amount) {
    if (balance >= amount) {
        balance -= amount;
    }
}
```

#### 2. Deadlock

```java
// ❌ Deadlock example
public class DeadlockExample {
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();
    
    public void method1() {
        synchronized(lock1) {
            System.out.println("Thread 1: Holding lock1...");
            Thread.sleep(10);
            synchronized(lock2) {  // Waiting for lock2
                System.out.println("Thread 1: Holding lock1 & lock2");
            }
        }
    }
    
    public void method2() {
        synchronized(lock2) {
            System.out.println("Thread 2: Holding lock2...");
            Thread.sleep(10);
            synchronized(lock1) {  // Waiting for lock1
                System.out.println("Thread 2: Holding lock1 & lock2");
            }
        }
    }
}

// Thread 1 holds lock1, waits for lock2
// Thread 2 holds lock2, waits for lock1
// → Deadlock! Both wait forever

// ✅ Solution: Lock ordering
public void method1Fixed() {
    synchronized(lock1) {
        synchronized(lock2) {  // Always lock1 → lock2
            // Work
        }
    }
}

public void method2Fixed() {
    synchronized(lock1) {  // Same order!
        synchronized(lock2) {
            // Work
        }
    }
}
```

#### 3. Thread Starvation

```java
// ❌ Problem: High-priority threads starve low-priority
Thread highPrio = new Thread(() -> {
    while (true) {
        // CPU-intensive work
    }
});
highPrio.setPriority(Thread.MAX_PRIORITY);

Thread lowPrio = new Thread(() -> {
    // Never gets CPU time!
});
lowPrio.setPriority(Thread.MIN_PRIORITY);

// ✅ Solution: Use fair locks or avoid priority
ReentrantLock fairLock = new ReentrantLock(true);  // Fair mode
```

### 🔐 Advanced Locks

```java
// ReentrantLock - More flexible than synchronized
ReentrantLock lock = new ReentrantLock();

public void method() {
    lock.lock();
    try {
        // Critical section
    } finally {
        lock.unlock();  // Always unlock in finally!
    }
}

// tryLock - Non-blocking attempt
if (lock.tryLock(1, TimeUnit.SECONDS)) {
    try {
        // Got lock
    } finally {
        lock.unlock();
    }
} else {
    // Couldn't get lock
}

// ReadWriteLock - Multiple readers, single writer
ReadWriteLock rwLock = new ReentrantReadWriteLock();

public void read() {
    rwLock.readLock().lock();
    try {
        // Multiple readers can enter
    } finally {
        rwLock.readLock().unlock();
    }
}

public void write() {
    rwLock.writeLock().lock();
    try {
        // Only one writer at a time
    } finally {
        rwLock.writeLock().unlock();
    }
}
```

### ⚛️ Atomic Variables

```java
// AtomicInteger - Thread-safe without locks
AtomicInteger counter = new AtomicInteger(0);

// Atomic operations
counter.incrementAndGet();  // count++
counter.decrementAndGet();  // count--
counter.addAndGet(5);       // count += 5
counter.compareAndSet(10, 20);  // if (count == 10) count = 20

// ❌ Without atomic - race condition
int count = 0;
for (int i = 0; i < 1000; i++) {
    new Thread(() -> count++).start();  // Not thread-safe!
}

// ✅ With atomic - correct
AtomicInteger atomicCount = new AtomicInteger(0);
for (int i = 0; i < 1000; i++) {
    new Thread(() -> atomicCount.incrementAndGet()).start();  // Thread-safe!
}
```

### 🎓 Interview Talking Points

✅ **Key Points:**
- "synchronized provides mutual exclusion and visibility. Only one thread enters synchronized block"
- "volatile ensures visibility across threads but doesn't provide atomicity - use for simple flags"
- "ExecutorService manages thread pool - submit tasks, threads reused. Better than creating threads manually"
- "Race condition: concurrent access without synchronization. Deadlock: circular wait on locks"
- "CompletableFuture for async programming - chain operations with thenApply, thenAccept"
- "Atomic classes (AtomicInteger) provide lock-free thread-safe operations using CAS (Compare-And-Swap)"

### 💡 Best Practices

```java
// ✅ Use ExecutorService, not manual threads
ExecutorService executor = Executors.newFixedThreadPool(10);
executor.submit(task);
executor.shutdown();

// ✅ Always unlock in finally
lock.lock();
try {
    // Critical section
} finally {
    lock.unlock();
}

// ✅ Use concurrent collections
Map<String, Integer> map = new ConcurrentHashMap<>();

// ✅ Prefer ReentrantLock over synchronized for advanced features
ReentrantLock lock = new ReentrantLock();
if (lock.tryLock()) {
    try {
        // Work
    } finally {
        lock.unlock();
    }
}

// ❌ Avoid: Synchronizing on public object
public String lock = "lock";  // Anyone can synchronize on this!

// ✅ Use: Private final object
private final Object lock = new Object();
```

---

[Content continues with remaining topics: J5-J8, S1-S5, SB1-SB5, M1-M6, R1-R5...]

**TO BE CONTINUED IN NEXT PART DUE TO LENGTH...**

---

**PROGRESS: 4 out of 27 topics completed**
- ✅ J1: Collections
- ✅ J2: HashMap
- ✅ J3: Streams
- ✅ J4: Multithreading

Remaining: J5-J8 (4), Spring Framework (5), Spring Boot (5), Microservices (6), REST API (5) = 25 topics

<a name="j5-jvm"></a>
## J5: 🖥️ JVM Architecture & Memory

### ⚡ Quick Answer (30 seconds)
> *"JVM has Class Loader (loads .class files), Runtime Data Areas (method area, heap, stack, PC register, native method stack), Execution Engine (interpreter + JIT compiler). Memory: Heap stores objects (shared), Stack stores local variables and method calls (per thread). Method Area stores class metadata. JIT compiles frequently-used bytecode to native code for performance. Stack stores frames (local variables, operand stack, return address). StackOverflowError when recursion too deep, OutOfMemoryError when heap full."*

### 🏗️ JVM Architecture

```
┌──────────────────────────────────────────────────────┐
│                   JVM Architecture                   │
├──────────────────────────────────────────────────────┤
│                                                      │
│  ┌────────────────────────────────────────────┐     │
│  │         Class Loader Subsystem             │     │
│  │  ┌──────────┬─────────────┬────────────┐  │     │
│  │  │ Loading  │  Linking    │ Initializ  │  │     │
│  │  │ (Load    │  (Verify,   │  (Static   │  │     │
│  │  │ .class)  │  Prepare,   │  blocks)   │  │     │
│  │  │          │  Resolve)   │            │  │     │
│  │  └──────────┴─────────────┴────────────┘  │     │
│  └────────────────────────────────────────────┘     │
│                      ↓                               │
│  ┌────────────────────────────────────────────┐     │
│  │      Runtime Data Areas                    │     │
│  │                                             │     │
│  │  ┌──────────────────────────────────────┐  │     │
│  │  │  Method Area (Class metadata)        │  │     │
│  │  │  - Class structures                  │  │     │
│  │  │  - Method code                       │  │     │
│  │  │  - Runtime constant pool             │  │     │
│  │  └──────────────────────────────────────┘  │     │
│  │                                             │     │
│  │  ┌──────────────────────────────────────┐  │     │
│  │  │  Heap (Objects, Arrays)              │  │     │
│  │  │  - Young Gen (Eden, S0, S1)          │  │     │
│  │  │  - Old Gen (Tenured)                 │  │     │
│  │  │  - Shared by all threads             │  │     │
│  │  └──────────────────────────────────────┘  │     │
│  │                                             │     │
│  │  Per Thread:                                │     │
│  │  ┌─────────────────┐  ┌─────────────────┐  │     │
│  │  │  Stack          │  │  PC Register    │  │     │
│  │  │  (Method frames)│  │  (Current inst) │  │     │
│  │  └─────────────────┘  └─────────────────┘  │     │
│  │  ┌─────────────────┐                       │     │
│  │  │ Native Method   │                       │     │
│  │  │ Stack           │                       │     │
│  │  └─────────────────┘                       │     │
│  └────────────────────────────────────────────┘     │
│                      ↓                               │
│  ┌────────────────────────────────────────────┐     │
│  │         Execution Engine                   │     │
│  │  ┌──────────────┬──────────────────────┐  │     │
│  │  │ Interpreter  │  JIT Compiler        │  │     │
│  │  │ (Execute     │  (Compile bytecode   │  │     │
│  │  │  bytecode)   │   to native)         │  │     │
│  │  └──────────────┴──────────────────────┘  │     │
│  │  ┌──────────────────────────────────────┐  │     │
│  │  │  Garbage Collector                   │  │     │
│  │  └──────────────────────────────────────┘  │     │
│  └────────────────────────────────────────────┘     │
│                      ↓                               │
│  ┌────────────────────────────────────────────┐     │
│  │   Native Method Interface (JNI)           │     │
│  └────────────────────────────────────────────┘     │
│                      ↓                               │
│  ┌────────────────────────────────────────────┐     │
│  │   Native Method Libraries                  │     │
│  └────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────┘
```

### 💾 Memory Areas

#### 1. Heap (Shared, Garbage Collected)

```
Heap Structure:
┌─────────────────────────────────────────────────────┐
│                   Young Generation                   │
│  ┌──────────┬───────────┬───────────┐               │
│  │  Eden    │ Survivor0 │ Survivor1 │               │
│  │  (New    │   (S0)    │   (S1)    │               │
│  │  objects)│           │           │               │
│  └──────────┴───────────┴───────────┘               │
│         ↓ Objects survive minor GC                  │
├─────────────────────────────────────────────────────┤
│                   Old Generation                     │
│  ┌───────────────────────────────────────────────┐  │
│  │  Tenured (Long-lived objects)                 │  │
│  │  - Objects that survived multiple GCs         │  │
│  │  - Large objects directly allocated here      │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘

// Object allocation flow:
new Object() → Allocated in Eden
    ↓
Minor GC (Eden full)
    ↓
Survivors moved to S0 → Age = 1
    ↓
Next Minor GC
    ↓
S0 → S1 → Age = 2
    ↓
Age >= 15 (default threshold)
    ↓
Promoted to Old Generation
```

#### 2. Stack (Per Thread, Not GC'd)

```
Stack Structure (Per Thread):

┌───────────────────────────────┐
│        Stack Frame 1          │
│  ┌─────────────────────────┐  │
│  │  Local Variable Array   │  │
│  │  [0]: this              │  │
│  │  [1]: param1            │  │
│  │  [2]: localVar          │  │
│  └─────────────────────────┘  │
│  ┌─────────────────────────┐  │
│  │  Operand Stack          │  │
│  │  (Temp values)          │  │
│  └─────────────────────────┘  │
│  ┌─────────────────────────┐  │
│  │  Frame Data             │  │
│  │  - Return address       │  │
│  │  - Exception table      │  │
│  └─────────────────────────┘  │
├───────────────────────────────┤
│        Stack Frame 2          │
│  (Calling method)             │
└───────────────────────────────┘

Example:
public void methodA() {
    int x = 10;  // Stored in local variables
    methodB(x);  // New frame pushed
}

public void methodB(int param) {
    String s = "hello";  // New frame's local var
}  // Frame popped on return
```

#### 3. Method Area (Shared, Class Data)

```
Method Area:
┌────────────────────────────────────┐
│  Class Metadata                    │
│  - Class name, superclass          │
│  - Interface names                 │
│  - Access modifiers                │
│                                    │
│  Method Information                │
│  - Method names, return types      │
│  - Parameter types                 │
│  - Method bytecode                 │
│                                    │
│  Field Information                 │
│  - Field names, types              │
│  - Access modifiers                │
│                                    │
│  Static Variables                  │
│  - Class-level variables           │
│                                    │
│  Runtime Constant Pool             │
│  - Literal constants               │
│  - Symbolic references             │
└────────────────────────────────────┘
```

### 🎯 Memory Configuration

```bash
# Heap size
-Xms2g    # Initial heap size: 2GB
-Xmx4g    # Maximum heap size: 4GB

# Young generation
-Xmn512m  # Young generation size: 512MB

# Metaspace (Method Area in Java 8+)
-XX:MetaspaceSize=256m
-XX:MaxMetaspaceSize=512m

# Stack size
-Xss1m    # Stack size per thread: 1MB

# Example startup:
java -Xms2g -Xmx4g -Xmn512m -Xss1m -jar app.jar
```

### 🚀 JIT Compiler

```
Execution Flow:

Source Code (.java)
    ↓ javac
Bytecode (.class)
    ↓
┌────────────────────────────────┐
│  Interpreter (First execution) │
│  - Line by line interpretation │
│  - Slow but starts quickly     │
└────────────┬───────────────────┘
             │
             ↓ Detects hot spots
┌────────────────────────────────┐
│  JIT Compiler                  │
│  - Compiles frequently used    │
│    bytecode to native code     │
│  - Optimizations applied       │
└────────────┬───────────────────┘
             │
             ↓
┌────────────────────────────────┐
│  Native Machine Code           │
│  - Directly executed by CPU    │
│  - Fast!                       │
└────────────────────────────────┘

Hot Spot Detection:
- Method called 10,000 times → Compile to native
- Loop executed 15,000 times → Compile loop
```

### ⚠️ Common Errors

```java
// StackOverflowError
public void recursiveMethod() {
    recursiveMethod();  // Infinite recursion
}
// Stack: Frame1 → Frame2 → Frame3 → ... → OutOfMemory!

// OutOfMemoryError: Java heap space
List<byte[]> list = new ArrayList<>();
while (true) {
    list.add(new byte[1024 * 1024]);  // 1MB each
}
// Heap fills up → OutOfMemoryError

// OutOfMemoryError: Metaspace
while (true) {
    // Dynamically load classes
    ClassLoader cl = new CustomClassLoader();
    cl.loadClass("MyClass");
}
// Method Area fills up → OutOfMemoryError
```

### 🎓 Interview Talking Points

✅ **Key Points:**
- "JVM has Class Loader, Runtime Data Areas, Execution Engine"
- "Heap stores objects (shared, GC'd). Stack stores method frames (per thread, not GC'd)"
- "Young Gen for new objects, Old Gen for long-lived. Minor GC cleans Young, Major GC cleans Old"
- "JIT compiles hot code (frequently executed) to native for performance"
- "StackOverflowError when recursion too deep, OutOfMemoryError when heap/metaspace full"

---

<a name="j6-gc"></a>
## J6: 🗑️ Garbage Collection

### ⚡ Quick Answer (30 seconds)
> *"GC automatically frees unused objects. Mark-and-Sweep algorithm: mark reachable objects from GC roots (stack, static fields), sweep unreachable. Generational GC: Young Gen (frequent minor GC), Old Gen (infrequent major GC). G1GC divides heap into regions, predicts pause times. ZGC ultra-low latency (<10ms pauses). GC roots: local variables, static fields, JNI references. Finalize() called before GC but deprecated. Use try-with-resources for cleanup."*

### 🔄 GC Algorithm: Mark-and-Sweep

```
Phase 1: Mark
┌────────────────────────────────────────┐
│  GC Roots (Stack, Static variables)   │
└───────┬────────────────────────────────┘
        │ Mark reachable
        ↓
   ┌─────────┐
   │ Object1 │ ✅ Reachable
   └────┬────┘
        │ References
        ↓
   ┌─────────┐
   │ Object2 │ ✅ Reachable
   └─────────┘

   ┌─────────┐
   │ Object3 │ ❌ Unreachable (No path from roots)
   └─────────┘

Phase 2: Sweep
   ┌─────────┐
   │ Object1 │ ✅ Keep
   └─────────┘
   
   ┌─────────┐
   │ Object2 │ ✅ Keep
   └─────────┘
   
   ┌─────────┐
   │ Object3 │ ❌ DELETED
   └─────────┘

Phase 3: Compact (Optional)
Before:  [Obj1] [    ] [Obj2] [    ] [Obj3]
After:   [Obj1] [Obj2] [Obj3] [           ]
         ↑ Compact to eliminate fragmentation
```

### 🌱 Generational Garbage Collection

```
Heap Memory:

Young Generation (Small, Frequent GC)
┌───────────────────────────────────────┐
│  Eden (8/10)    │ S0 (1/10) │ S1 (1/10)│
│  New objects    │ Survivors │ Survivors│
└───────────────────────────────────────┘
        ↓ Minor GC (Fast, ~milliseconds)
Old Generation (Large, Infrequent GC)
┌───────────────────────────────────────┐
│  Tenured (Long-lived objects)         │
│  - Survived 15+ minor GCs             │
└───────────────────────────────────────┘
        ↓ Major GC (Slow, ~seconds)

Object Lifecycle:
1. new Object() → Eden
2. Eden full → Minor GC
3. Survivors → S0 (age = 1)
4. Next Minor GC → S0 to S1 (age = 2)
5. Age >= threshold (default 15) → Old Gen
```

### 🎯 Types of Garbage Collectors

#### 1. Serial GC (Single thread)
```bash
-XX:+UseSerialGC

Use case: Single CPU, small heap (<100MB)
Pause time: ~100ms - 1s
```

#### 2. Parallel GC (Multiple threads)
```bash
-XX:+UseParallelGC

Use case: Multi-CPU, throughput important
Pause time: ~100ms - 1s
Throughput: High (more work, less GC)
```

#### 3. CMS (Concurrent Mark Sweep)
```bash
-XX:+UseConcMarkSweepGC

Use case: Low latency required
Pause time: ~100ms
Concurrent: Runs alongside application
Problem: Fragmentation (deprecated in Java 14)
```

#### 4. G1GC (Garbage First) - Default since Java 9
```bash
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200  # Target pause time

Heap divided into regions:
┌──┬──┬──┬──┬──┬──┬──┬──┐
│E │E │S │O │O │H │E │E │
└──┴──┴──┴──┴──┴──┴──┴──┘
E=Eden, S=Survivor, O=Old, H=Humongous

Use case: Large heap (>4GB), predictable pauses
Pause time: 100-200ms (predictable)
Concurrent: Most work concurrent
```

#### 5. ZGC (Ultra-low latency)
```bash
-XX:+UseZGC

Use case: Very large heap (>8GB), <10ms pauses
Pause time: <10ms (even for TB heaps!)
Concurrent: Almost entirely concurrent
```

### 📊 GC Comparison

| Collector | Threads | Pause Time | Throughput | Heap Size | Use Case |
|-----------|---------|------------|------------|-----------|----------|
| **Serial** | 1 | High (1s+) | Low | Small | Single CPU |
| **Parallel** | Multiple | Medium (100ms) | High | Medium | Batch jobs |
| **CMS** | Multiple | Low (100ms) | Medium | Medium | Low latency (deprecated) |
| **G1GC** | Multiple | Predictable (200ms) | Good | Large | General purpose |
| **ZGC** | Multiple | Ultra-low (<10ms) | Good | Huge | Ultra-low latency |

### 🔍 GC Roots

```java
// GC Roots - Starting points for reachability

// 1. Local variables on stack
public void method() {
    Object obj = new Object();  // obj is GC root
    // obj is reachable
}  // obj removed from stack → object unreachable

// 2. Static variables
public class MyClass {
    private static Object staticObj = new Object();  // GC root
    // staticObj always reachable (until class unloaded)
}

// 3. Active threads
Thread thread = new Thread();
thread.start();  // thread is GC root

// 4. JNI references (Native code)
// C/C++ code holding Java object references

// Object reachable if:
// - Direct reference from GC root, OR
// - Referenced by reachable object

Example:
Stack: [ref1] ← GC Root
         ↓
Heap: [Object A] → [Object B] → [Object C]
       ✅          ✅           ✅ All reachable

      [Object D]  ← No path from GC root
       ❌ Unreachable → Garbage collected
```

### 🧹 Manual Control

```java
// System.gc() - Suggests GC (not guaranteed!)
System.gc();  // Hint to JVM, may be ignored

// Better: Let JVM decide when to GC

// For monitoring:
Runtime runtime = Runtime.getRuntime();
long totalMemory = runtime.totalMemory();  // Total heap
long freeMemory = runtime.freeMemory();    // Free heap
long usedMemory = totalMemory - freeMemory;
System.out.println("Used: " + usedMemory / 1024 / 1024 + " MB");
```

### ⚠️ finalize() - Deprecated!

```java
// ❌ OLD WAY (Deprecated in Java 9)
@Override
protected void finalize() throws Throwable {
    try {
        // Cleanup before GC
        closeResource();
    } finally {
        super.finalize();
    }
}

// Problems:
// - Unpredictable when called
// - May never be called
// - Slows down GC
// - Can resurrect objects

// ✅ MODERN WAY: try-with-resources
try (FileInputStream fis = new FileInputStream("file.txt")) {
    // Use resource
}  // Automatically closed

// Or implement AutoCloseable
public class MyResource implements AutoCloseable {
    @Override
    public void close() {
        // Cleanup code
    }
}
```

### 🎓 Interview Talking Points

✅ **Key Points:**
- "GC uses Mark-and-Sweep: mark reachable from GC roots (stack, static fields), sweep unreachable"
- "Generational GC: Young Gen (Eden + 2 Survivors) for new objects, Old Gen for long-lived. Minor GC fast, Major GC slow"
- "G1GC default since Java 9 - divides heap into regions, predictable pauses (100-200ms)"
- "ZGC for ultra-low latency - <10ms pauses even for TB heaps"
- "GC roots: local variables, static fields, active threads, JNI references"
- "finalize() deprecated - use try-with-resources or AutoCloseable for cleanup"

---

[Content continues with J7, J8, Spring Framework, Spring Boot, Microservices, and REST API sections...]

**WOULD YOU LIKE ME TO CONTINUE WITH REMAINING TOPICS?**
This is getting quite large. I can:
1. Continue adding all topics to one file
2. Split into separate files by category
3. Create a condensed version with just essentials

Please let me know how you'd like me to proceed! 🎯
