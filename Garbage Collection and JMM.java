🗑️ Garbage Collection (GC)
Garbage Collection is the process of automatically reclaiming memory occupied by objects that are no longer reachable/referenced by the program.
How It Works Internally
Step 1 — Object Allocation
Objects are created in the Heap memory, which is divided into regions:
Heap Memory
├── Young Generation
│   ├── Eden Space        ← new objects born here
│   ├── Survivor S0
│   └── Survivor S1
└── Old Generation (Tenured) ← long-lived objects promoted here
Step 2 — Minor GC (Young Gen)

New objects fill Eden space
When Eden is full → Minor GC triggers
Live objects move to Survivor spaces (S0 ↔ S1), alternating each cycle
Each surviving object's age counter increments
When age hits threshold (default: 15) → object promoted to Old Gen

Step 3 — Major / Full GC (Old Gen)

When Old Gen fills up → Major GC triggers (more expensive, Stop-the-World)
Algorithms like G1GC, ZGC, Shenandoah try to minimize pause times

Step 4 — Reachability Analysis
GC uses GC Roots (stack variables, static fields, JNI refs) and traces object graphs. Anything not reachable is eligible for collection.
GC Roots → Object A → Object B → Object C  ✅ reachable, kept
           Object X  (no reference)         ❌ unreachable, collected
Popular GC Algorithms
AlgorithmStrategyBest ForSerial GCSingle-threaded, Stop-the-WorldSmall appsParallel GCMulti-threaded throughputBatch processingG1GCRegion-based, low pauseGeneral purpose (Java 9+ default)ZGCSub-millisecond pausesLarge heaps, low latency
GC Phases (Mark-Sweep-Compact)

Mark — identify all live (reachable) objects
Sweep — reclaim memory of dead objects
Compact — defragment heap to avoid fragmentation


Q3: Garbage Collection in Java
Detailed Answer:
Garbage Collection (GC) is Java's automatic memory management process that identifies and removes objects from heap memory that are no longer reachable or referenced.
Memory Structure:
┌─────────────────────────────────────────────────────────────┐
│                     HEAP MEMORY                              │
├──────────────────┬──────────────────┬─────────────────────┤
│   Young Gen      │   Old Gen        │   Metaspace         │
│  (Eden + S0/S1)  │  (Tenured)       │  (Java 8+)          │
└──────────────────┴──────────────────┴─────────────────────┘
Object Lifecycle:
NEW OBJECT CREATION → OBJECT LIFECYCLE → GARBAGE COLLECTION

┌─────────┐   Minor GC  ┌─────────┐   Major GC   ┌─────────┐
│  Eden   │ ──────────▶ │Survivor │ ───────────▶ │   Old   │
│  Space  │   (frequent)│  Space  │  (infrequent)│   Gen   │
└─────────┘             └─────────┘              └─────────┘
   New objects         Young survivors          Long-lived
   created here        (age < threshold)        objects
How GC Works:
Step 1: Marking Phase
Heap Memory:
┌──────┬──────┬──────┬──────┬──────┐
│  A   │  B   │  C   │  D   │  E   │  Objects
└──────┴──────┴──────┴──────┴──────┘
   ✓      ✗      ✓      ✗      ✓     Referenced?
 (Keep) (Delete)(Keep)(Delete)(Keep)

GC Root → A → C → E  (Reachable objects marked)
B and D are unreachable (eligible for GC)
Step 2: Deletion Phase
After Deletion:
┌──────┬──────┬──────┐
│  A   │  C   │  E   │
└──────┴──────┴──────┘
Memory compacted (optional)
GC Roots:
Objects that are always reachable:

Local variables in active methods
Static variables
Active Java threads
JNI (Java Native Interface) references

Code Examples:
Making Objects Eligible for GC:
javapublic class GCDemo {
    public static void main(String[] args) {
        // 1. Nullifying reference
        Student s1 = new Student("John");
        s1 = null;  // Object eligible for GC
        
        // 2. Reassigning reference
        Student s2 = new Student("Jane");
        Student s3 = new Student("Bob");
        s2 = s3;  // Jane object eligible for GC
        
        // 3. Anonymous object
        new Student("Alice");  // Immediately eligible for GC
        
        // 4. Object created inside method
        createStudent();  // Object eligible after method completes
        
        // 5. Island of Isolation
        Student s4 = new Student("Tom");
        Student s5 = new Student("Jerry");
        s4.friend = s5;
        s5.friend = s4;
        s4 = null;
        s5 = null;  // Both objects eligible (circular reference)
        
        // Request GC (not guaranteed to run immediately)
        System.gc();
        Runtime.getRuntime().gc();
        
        // Give GC time to run
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public static void createStudent() {
        Student temp = new Student("Temp");
        // temp eligible for GC when method returns
    }
}

class Student {
    String name;
    Student friend;  // For island of isolation example
    
    Student(String name) {
        this.name = name;
        System.out.println(name + " created");
    }
    
    // finalize() called before GC (deprecated in Java 9+)
    @Override
    protected void finalize() throws Throwable {
        System.out.println(name + " is being garbage collected");
    }
}
Memory Leak Example:
java// Example 1: Static Collection causing memory leak
public class MemoryLeakDemo {
    private static List<Object> list = new ArrayList<>();
    
    public void addObject(Object obj) {
        list.add(obj);  // Objects never removed - memory leak!
    }
}

// Example 2: Unclosed Resources
public class ResourceLeak {
    public void readFile() throws IOException {
        FileInputStream fis = new FileInputStream("file.txt");
        // File not closed - memory leak!
    }
    
    // Correct way - try-with-resources
    public void readFileSafe() {
        try (FileInputStream fis = new FileInputStream("file.txt")) {
            // Auto-closed after block
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Example 3: ThreadLocal not cleaned
public class ThreadLocalLeak {
    private static ThreadLocal<List<Object>> threadLocal = new ThreadLocal<>();
    
    public void process() {
        List<Object> list = new ArrayList<>();
        threadLocal.set(list);
        // If not removed, memory leak in thread pool
        
        // Always remove
        threadLocal.remove();
    }
}
Types of Garbage Collectors:
1. Serial GC (-XX:+UseSerialGC)
Single-threaded collector
┌──────────────┐
│   GC Thread  │ → Stops application threads
└──────────────┘
Best for: Small applications, single-core systems
2. Parallel GC (-XX:+UseParallelGC)
Multiple threads for collection
┌───────┬───────┬───────┐
│  GC1  │  GC2  │  GC3  │ → Parallel minor GC
└───────┴───────┴───────┘
Best for: Throughput-oriented applications
Default: Java 8
3. CMS - Concurrent Mark Sweep (-XX:+UseConcMarkSweepGC)
Concurrent collection (low pause)
App Thread: ─────────────────
GC Thread:   ─  ───  ───  ─  (runs concurrently)
Best for: Low latency applications
Status: Deprecated (Java 9), Removed (Java 14)
4. G1 GC (-XX:+UseG1GC)
Heap divided into regions
┌───┬───┬───┬───┬───┬───┬───┬───┐
│ E │ S │ O │ E │ H │ O │ E │ S │
└───┴───┴───┴───┴───┴───┴───┴───┘
E=Eden, S=Survivor, O=Old, H=Humongous

Best for: Large heaps (>4GB), predictable pause times
Default: Java 9+
5. ZGC (-XX:+UseZGC)
Ultra-low latency (<10ms pauses)
Concurrent, scalable
Best for: Large heaps (TB), latency-sensitive apps
Available: Java 11+
6. Shenandoah GC (-XX:+UseShenandoahGC)
Similar to ZGC
Low pause times
Best for: Large heaps, low latency
Available: Java 12+
GC Configuration:
bash# Heap size configuration
java -Xms512m -Xmx2g MyApp
# -Xms: Initial heap size (512 MB)
# -Xmx: Maximum heap size (2 GB)

# Stack size per thread
java -Xss1m MyApp

# Young generation size
java -Xmn512m MyApp

# Select GC
java -XX:+UseG1GC MyApp
java -XX:+UseZGC MyApp

# GC logging (Java 9+)
java -Xlog:gc* MyApp
java -Xlog:gc*:file=gc.log MyApp

# GC logging (Java 8)
java -XX:+PrintGCDetails -XX:+PrintGCDateStamps MyApp

# Metaspace (Java 8+)
java -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=512m MyApp

# Example complete configuration
java -Xms1g -Xmx4g -Xss1m \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:MetaspaceSize=256m \
     -XX:MaxMetaspaceSize=512m \
     -Xlog:gc*:file=gc.log \
     MyApp
GC Performance Tuning:
java// Monitor GC
Runtime runtime = Runtime.getRuntime();
long totalMemory = runtime.totalMemory();
long freeMemory = runtime.freeMemory();
long usedMemory = totalMemory - freeMemory;
long maxMemory = runtime.maxMemory();

System.out.println("Used Memory: " + usedMemory / (1024*1024) + " MB");
System.out.println("Free Memory: " + freeMemory / (1024*1024) + " MB");
System.out.println("Total Memory: " + totalMemory / (1024*1024) + " MB");
System.out.println("Max Memory: " + maxMemory / (1024*1024) + " MB");
Best Practices:

Minimize object creation in loops
Use object pools for expensive objects
Close resources properly (try-with-resources)
Remove listeners when no longer needed
Clear collections when done
Use WeakReference for caches
Monitor and tune GC based on application needs

Interview Follow-ups:

Can we force GC? No, System.gc() is just a request
What happens during Full GC? Application threads pause, entire heap is collected
Difference between Minor and Major GC? Minor = Young Gen (fast), Major = Old Gen (slow)
What is Stop-The-World event? When all application threads pause for GC
