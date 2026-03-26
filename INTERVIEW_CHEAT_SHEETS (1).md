# 🎯 HIBERNATE/JPA & DATABASE/SQL - INTERVIEW CHEAT SHEETS
## Quick Reference Guide for Technical Interviews

---

# 📚 TABLE OF CONTENTS

## Part A: Hibernate/JPA
- [H1: ORM & Hibernate Implementation](#h1-orm--hibernate)
- [H2: Session vs SessionFactory](#h2-session--sessionfactory)
- [H3: Mapping Types](#h3-mapping-types)
- [H4: Lazy vs Eager Loading](#h4-lazy-vs-eager)
- [H5: N+1 Query Problem](#h5-n1-problem)
- [H6: JPA vs Hibernate](#h6-jpa-vs-hibernate)
- [H7: Transaction Management](#h7-transactions)
- [H8: JTA Transactions](#h8-jta-distributed-transactions)

## Part B: Database/SQL
- [S1: Nth Highest Salary](#s1-nth-highest-salary)

---

# 🔷 HIBERNATE/JPA CHEAT SHEETS

<a name="h1-orm--hibernate"></a>
## H1: 🏗️ ORM & Hibernate Implementation

### ⚡ Quick Answer (30 seconds)
> *"ORM solves object-relational impedance mismatch by mapping objects to tables. Hibernate implements ORM through SessionFactory (factory holding metadata, connection pool, caches) and Session (persistence context with first-level cache). Key features: automatic SQL generation, HQL, dirty checking, lazy loading via proxies, multi-level caching."*

### 🎯 Key Concepts

#### Object-Relational Impedance Mismatch
```
Java Objects          vs.    Relational Database
• Inheritance                • Flat tables (no inheritance)
• Associations               • Foreign keys
• Identity (==)              • Primary keys
• Polymorphism              • Fixed schema
• Encapsulation             • Exposed columns
```

#### Hibernate Architecture
```
┌──────────────────────────────────────┐
│      Your Application Code           │
└──────────┬───────────────────────────┘
           ↓
┌──────────────────────────────────────┐
│    Hibernate Session/EntityManager   │
│  • save(), find(), persist()         │
└──────────┬───────────────────────────┘
           ↓
┌──────────────────────────────────────┐
│      Persistence Context             │
│  • First-level cache (identity map)  │
│  • Dirty checking                    │
│  • Entity state management           │
└──────────┬───────────────────────────┘
           ↓
┌──────────────────────────────────────┐
│      SessionFactory                  │
│  • Entity metadata                   │
│  • Second-level cache                │
│  • Connection pool                   │
│  • HQL → SQL translator              │
└──────────┬───────────────────────────┘
           ↓
         JDBC → Database
```

#### 🔑 Core Features

| Feature | How It Works | Benefit |
|---------|--------------|---------|
| **Auto SQL Generation** | Hibernate generates INSERT/UPDATE/DELETE/SELECT from entity operations | 80% less code than JDBC |
| **HQL** | Object-oriented query language: `FROM Employee WHERE salary > :min` | Query objects, not tables |
| **Dirty Checking** | Compares entity snapshots, auto-generates UPDATE for changes | No manual update() needed |
| **Lazy Loading** | Creates proxies, loads data on access | Better performance |
| **First-Level Cache** | Session-level identity map | Same row = same object instance |
| **Second-Level Cache** | SessionFactory-level shared cache | Reduce DB hits across sessions |

#### 💻 Code Comparison

**JDBC (Painful):**
```java
// 50+ lines of boilerplate
Connection conn = dataSource.getConnection();
PreparedStatement stmt = conn.prepareStatement("SELECT * FROM employees WHERE id = ?");
stmt.setLong(1, id);
ResultSet rs = stmt.executeQuery();
if (rs.next()) {
    Employee emp = new Employee();
    emp.setId(rs.getLong("id"));
    emp.setName(rs.getString("name"));
    // ... manual mapping
}
rs.close(); stmt.close(); conn.close();
```

**Hibernate (Clean):**
```java
// 1 line!
Employee emp = session.get(Employee.class, id);
```

### 🧠 How Hibernate Works Internally

**When you call `session.get(Employee.class, 1L)`:**

```
1. Check first-level cache → Cache hit? Return cached object
2. Check second-level cache → Cache hit? Hydrate and return
3. Generate SQL: SELECT id, name, salary FROM employees WHERE id = 1
4. Execute via JDBC PreparedStatement
5. Hydrate ResultSet → Create Employee object
6. Store in first-level cache
7. Store snapshot for dirty checking
8. Return object
```

**Dirty Checking Flow:**
```
Load: emp.getSalary() = 70000 → Snapshot saved: [1L, "John", 70000]
Modify: emp.setSalary(75000) → Object modified in memory
Flush: Compare current [1L, "John", 75000] vs snapshot [1L, "John", 70000]
      → Dirty! Generate: UPDATE employees SET salary = 75000 WHERE id = 1
```

### 🎓 Interview Talking Points

✅ **Mention these:**
- "SessionFactory is immutable, thread-safe, expensive to create (1-5 sec)"
- "Session is NOT thread-safe, cheap, one per transaction"
- "Dirty checking uses entity snapshots to detect changes automatically"
- "Lazy loading uses CGLib/Javassist proxies that trigger SQL on first access"
- "First-level cache ensures object identity: same DB row = same Java object"

❌ **Common Mistakes to Avoid:**
- Don't say "Hibernate is slow" - it's faster than hand-written JDBC when used correctly
- Don't confuse Session (lightweight) with SessionFactory (heavyweight)

---

<a name="h2-session--sessionfactory"></a>
## H2: ⚙️ Session vs SessionFactory

### ⚡ Quick Answer (30 seconds)
> *"SessionFactory is heavyweight, thread-safe singleton holding entity metadata, connection pool, and second-level cache - created once per application. Session is lightweight, NOT thread-safe, manages persistence context with first-level cache - one per transaction. SessionFactory builds Sessions; Session manages entity lifecycle and dirty checking."*

### 📊 Comparison Table

| Feature | SessionFactory | Session |
|---------|---------------|---------|
| **Weight** | Heavy (1-5 sec to build) | Light (milliseconds) |
| **Thread Safety** | ✅ Thread-safe | ❌ NOT thread-safe |
| **Scope** | Application-wide | Single transaction |
| **Lifecycle** | Startup → Shutdown | Begin TX → Commit/Close |
| **Instances** | One per database | Many (one per request) |
| **State** | Immutable (except caches) | Mutable |
| **Cache** | Second-level (shared) | First-level (private) |
| **Cost** | Expensive creation | Cheap creation |
| **Contains** | Metadata, SQL generators, pool | Persistence context, action queue |

### 🏗️ SessionFactory Internal Components

```
SessionFactory
├── Entity Metadata Repository
│   ├── EntityPersister per entity
│   ├── CollectionPersister per collection
│   ├── Field → Column mappings
│   └── SQL generators (INSERT, UPDATE, DELETE)
├── Second-Level Cache
│   ├── Entity cache regions
│   ├── Collection cache regions
│   └── Query cache
├── Connection Pool (HikariCP/C3P0)
│   └── 10-50 JDBC connections
├── Query Infrastructure
│   ├── HQL parser & translator
│   ├── Criteria API
│   └── Named query repository
└── SQL Dialect
    └── Database-specific SQL generation
```

### 🔄 Session Internal Components

```
Session
├── Persistence Context (First-Level Cache)
│   ├── Identity Map: EntityKey → Entity
│   ├── Entity Snapshots (for dirty checking)
│   └── Collection Snapshots
├── Action Queue
│   ├── EntityInsertAction[]
│   ├── EntityUpdateAction[]
│   ├── EntityDeleteAction[]
│   └── CollectionUpdateAction[]
├── JDBC Connection (borrowed from pool)
└── Transaction
    ├── Isolation level
    └── Flush mode
```

### 💻 Key Methods

#### Session Methods

| Method | Returns | Use Case | Notes |
|--------|---------|----------|-------|
| `save(entity)` | Serializable (ID) | Insert | Returns ID immediately |
| `persist(entity)` | void | Insert | JPA standard, delays ID |
| `get(Class, id)` | Entity or null | Fetch | Hits DB immediately |
| `load(Class, id)` | Proxy | Fetch (lazy) | Returns proxy, throws exception if not found |
| `update(entity)` | void | Reattach detached | Assumes entity exists |
| `merge(entity)` | Managed entity | Reattach detached | Checks existence, returns managed instance |
| `delete(entity)` | void | Remove | Schedules DELETE |
| `flush()` | void | Force sync | Executes pending SQL |

#### save() vs persist()
```java
// save() - returns ID immediately
Long id = (Long) session.save(emp);  // May execute INSERT now
System.out.println(id);  // Has value

// persist() - delays until flush
session.persist(emp);
Long id = emp.getId();  // May be null!
session.flush();
id = emp.getId();  // Now has value
```

#### get() vs load()
```java
// get() - immediate SELECT
Employee emp = session.get(Employee.class, 1L);
if (emp == null) { /* not found */ }

// load() - returns proxy (lazy)
Employee emp = session.load(Employee.class, 1L);  // No SELECT yet!
emp.getName();  // NOW executes SELECT
// Throws ObjectNotFoundException if not found
```

### 🔄 Entity Lifecycle States

```
┌─────────────┐
│  Transient  │ ← new Employee()
└──────┬──────┘
       │ save() / persist()
       ↓
┌─────────────┐
│ Persistent  │ ← Managed by Session, changes tracked
└──────┬──────┘
       │ close() / evict() / clear()
       ↓
┌─────────────┐
│  Detached   │ ← Was persistent, Session closed
└──────┬──────┘
       │ update() / merge()
       ↓
┌─────────────┐
│ Persistent  │ ← Managed again
└──────┬──────┘
       │ delete()
       ↓
┌─────────────┐
│   Removed   │ ← Scheduled for deletion
└─────────────┘
```

### 🎓 Interview Talking Points

✅ **Say this:**
- "SessionFactory created once, holds metadata and connection pool"
- "Session is NOT thread-safe - one per thread/transaction"
- "First-level cache in Session ensures object identity within transaction"
- "get() hits DB immediately, load() returns proxy"
- "save() returns ID, persist() is JPA standard"

### 💡 Spring Boot Integration

```java
// Manual (old way)
Session session = sessionFactory.openSession();
Transaction tx = session.beginTransaction();
try {
    session.save(emp);
    tx.commit();
} catch (Exception e) {
    tx.rollback();
} finally {
    session.close();
}

// Spring @Transactional (modern)
@Transactional
public void save(Employee emp) {
    entityManager.persist(emp);  // Spring manages Session automatically
}
```

---

<a name="h3-mapping-types"></a>
## H3: 🔗 Mapping Types

### ⚡ Quick Answer (30 seconds)
> *"Hibernate supports OneToOne (foreign key or shared PK), OneToMany/ManyToOne (Many side owns relationship with FK), ManyToMany (junction table with composite PK). For inheritance: SINGLE_TABLE (discriminator, fast), JOINED (normalized, JOINs), TABLE_PER_CLASS (duplicates). Always use Set for ManyToMany, maintain both sides with helper methods."*

### 📋 Mapping Quick Reference

| Mapping Type | Database Structure | Owning Side | Example |
|--------------|-------------------|-------------|---------|
| **@OneToOne** | FK in one table OR shared PK | Side with @JoinColumn | User ↔ Profile |
| **@ManyToOne** | FK in Many table | Many side (has @JoinColumn) | Employee → Department |
| **@OneToMany** | FK in Many table | Inverse side (has mappedBy) | Department → Employees |
| **@ManyToMany** | Junction table with 2 FKs | Side with @JoinTable | Students ↔ Courses |

### 🔹 OneToOne Mapping

#### Database Structure
```sql
users                      user_profiles
┌────┬──────┐             ┌────┬─────────┬─────┐
│ id │ name │             │ id │ user_id │ bio │
├────┼──────┤             ├────┼─────────┼─────┤
│ 1  │ John │             │ 1  │ 1 (FK)  │ ... │
└────┴──────┘             └────┴─────────┴─────┘
                          UNIQUE(user_id)
```

#### Code
```java
@Entity
public class User {
    @Id
    private Long id;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id", unique = true)  // Owning side
    private UserProfile profile;
}

@Entity
public class UserProfile {
    @Id
    private Long id;
    
    @OneToOne(mappedBy = "profile")  // Inverse side
    private User user;
}
```

### 🔹 ManyToOne / OneToMany Mapping

#### Database Structure
```sql
departments                employees
┌────┬──────┐             ┌────┬──────┬─────────┐
│ id │ name │             │ id │ name │ dept_id │
├────┼──────┤             ├────┼──────┼─────────┤
│ 10 │ Eng  │             │ 1  │ John │ 10 ────┼→ FK
│ 20 │ Sale │             │ 2  │ Jane │ 10 ────┘
└────┴──────┘             └────┴──────┴─────────┘
```

#### Code with Best Practices
```java
@Entity
public class Department {
    @Id
    private Long id;
    
    @OneToMany(mappedBy = "department",  // Points to Employee.department field
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<Employee> employees = new ArrayList<>();
    
    // ⭐ Helper method - maintains BOTH sides!
    public void addEmployee(Employee emp) {
        employees.add(emp);
        emp.setDepartment(this);  // Set other side
    }
    
    public void removeEmployee(Employee emp) {
        employees.remove(emp);
        emp.setDepartment(null);  // Clear other side
    }
}

@Entity
public class Employee {
    @Id
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)  // ⭐ Always LAZY for optional
    @JoinColumn(name = "dept_id")  // Owning side
    private Department department;
}
```

#### ⚠️ Why Many Side Owns
```
❌ Can't put collection in SQL column:
departments
┌────┬──────┬──────────────────────┐
│ id │ name │ employee_ids         │ ← No array type!
├────┼──────┼──────────────────────┤
│ 10 │ Eng  │ [1, 2, 3, 4]         │ ❌ Not standard SQL
└────┴──────┴──────────────────────┘

✅ FK on Many side:
employees
┌────┬──────┬─────────┐
│ id │ name │ dept_id │ ← Foreign key
├────┼──────┼─────────┤
│ 1  │ John │ 10      │ ✅ Standard SQL
└────┴──────┴─────────┘
```

### 🔹 ManyToMany Mapping

#### Database Structure
```sql
students              student_course            courses
┌────┬──────┐        ┌────────────┬───────────┐ ┌────┬──────┐
│ id │ name │        │ student_id │ course_id │ │ id │ name │
├────┼──────┤        ├────────────┼───────────┤ ├────┼──────┤
│ 1  │ John │        │ 1          │ 101       │ │101 │ Math │
│ 2  │ Jane │        │ 1          │ 102       │ │102 │ Phys │
└────┴──────┘        │ 2          │ 101       │ └────┴──────┘
                     └────────────┴───────────┘
                     PK: (student_id, course_id)
```

#### Code
```java
@Entity
public class Student {
    @Id
    private Long id;
    
    @ManyToMany
    @JoinTable(
        name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();  // ⭐ Use Set, not List!
    
    public void enrollInCourse(Course course) {
        courses.add(course);
        course.getStudents().add(this);
    }
}

@Entity
public class Course {
    @Id
    private Long id;
    
    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();
}
```

#### ⚠️ Set vs List for ManyToMany
```java
// ❌ With List - WRONG!
@ManyToMany
private List<Course> courses = new ArrayList<>();

student.getCourses().add(course);
student.getCourses().add(course);  // Duplicate allowed!
// Hibernate may insert duplicate rows

// ✅ With Set - CORRECT!
@ManyToMany
private Set<Course> courses = new HashSet<>();

student.getCourses().add(course);
student.getCourses().add(course);  // Ignored, Set prevents duplicate
```

### 🔹 Inheritance Strategies

#### Strategy Comparison Table

| Strategy | Tables | Discriminator | JOINs | NULL Columns | Use When |
|----------|--------|---------------|-------|--------------|----------|
| **SINGLE_TABLE** | 1 | ✅ Yes | ❌ None | ⚠️ Many | Small hierarchy, performance critical |
| **JOINED** | 1 per class | ❌ No | ✅ Required | ❌ None | Large hierarchy, normalization important |
| **TABLE_PER_CLASS** | 1 per concrete | ❌ No | ⚠️ UNION | ❌ None | Rarely used |

#### SINGLE_TABLE (Fast)
```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class Payment {
    @Id private Long id;
    private BigDecimal amount;
}

@Entity
@DiscriminatorValue("CREDIT_CARD")
public class CreditCardPayment extends Payment {
    private String cardNumber;
}

@Entity
@DiscriminatorValue("PAYPAL")
public class PayPalPayment extends Payment {
    private String email;
}
```

**Generated Table:**
```sql
payments
┌────┬──────┬────────┬─────────────┬───────┐
│ id │ type │ amount │ card_number │ email │
├────┼──────┼────────┼─────────────┼───────┤
│ 1  │ CC   │ 100    │ 1234...     │ NULL  │
│ 2  │ PP   │ 50     │ NULL        │ john@ │
└────┴──────┴────────┴─────────────┴───────┘
```

#### JOINED (Normalized)
```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Payment {
    @Id private Long id;
    private BigDecimal amount;
}

@Entity
public class CreditCardPayment extends Payment {
    private String cardNumber;
}
```

**Generated Tables:**
```sql
payments              credit_card_payments
┌────┬────────┐      ┌────┬─────────────┐
│ id │ amount │      │ id │ card_number │
├────┼────────┤      ├────┼─────────────┤
│ 1  │ 100    │      │ 1  │ 1234...     │
└────┴────────┘      └────┴─────────────┘
                      FK: id → payments(id)
```

### 🔹 Cascade Operations

```java
@OneToMany(cascade = CascadeType.ALL)  // All operations cascade
@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})  // Specific

// Cascade types:
CascadeType.PERSIST   // save() cascades
CascadeType.MERGE     // merge() cascades
CascadeType.REMOVE    // delete() cascades
CascadeType.REFRESH   // refresh() cascades
CascadeType.DETACH    // detach() cascades
CascadeType.ALL       // All of above
```

**Example:**
```java
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
private List<Employee> employees;

Department dept = new Department();
dept.addEmployee(new Employee("John"));
dept.addEmployee(new Employee("Jane"));

session.save(dept);  // Saves Department AND both Employees (cascade)

dept.removeEmployee(john);
session.flush();  // Deletes John (orphanRemoval)
```

### 🎓 Interview Talking Points

✅ **Key Points:**
- "Many side owns OneToMany relationship (has foreign key)"
- "Always use Set for ManyToMany to prevent duplicates"
- "Helper methods maintain bidirectional consistency"
- "SINGLE_TABLE fastest but wastes space with NULLs"
- "JOINED normalized but requires JOINs for queries"
- "orphanRemoval deletes child when removed from collection"

---

<a name="h4-lazy-vs-eager"></a>
## H4: 🐌 Lazy vs Eager Loading

### ⚡ Quick Answer (30 seconds)
> *"Lazy loading defers loading until accessed via proxy objects - saves memory but risks LazyInitializationException if Session closes. Eager loads immediately with JOIN - no exceptions but wastes resources. Default: @OneToMany/@ManyToMany are LAZY (collections can be huge), @ManyToOne/@OneToOne are EAGER. Solutions: @Transactional to keep Session open, JOIN FETCH for known access, Entity Graphs, or DTOs."*

### 📊 Comparison Table

| Aspect | Lazy Loading | Eager Loading |
|--------|--------------|---------------|
| **When Loaded** | On first access | Immediately with parent |
| **SQL** | Separate SELECT | JOIN in same query |
| **Performance** | ✅ Faster initial load | ❌ Slower, loads everything |
| **Memory** | ✅ Less | ❌ More |
| **Exception Risk** | ⚠️ LazyInitializationException | ✅ None |
| **Use Case** | Optional data, large collections | Always-needed data |

### 🔄 How Lazy Loading Works (Proxy Pattern)

```
When you load Department with lazy employees:

session.get(Department.class, 10L)
  ↓
SQL: SELECT id, name FROM departments WHERE id = 10
  ↓
Department Object Created:
┌──────────────────────────┐
│ id: 10                   │
│ name: "Engineering"      │
│ employees: PROXY ────────┼──→ PersistentBag Proxy
└──────────────────────────┘    ├─ session: Session@abc
                                ├─ initialized: false
                                └─ elements: null

When you call dept.getEmployees().size():
  ↓
Proxy detects: initialized = false
  ↓
Proxy checks: session.isOpen() = true
  ↓
SQL: SELECT * FROM employees WHERE dept_id = 10
  ↓
Proxy loads data, sets initialized = true
  ↓
Returns size
```

### ⚠️ LazyInitializationException

#### The Problem
```java
// Method with @Transactional
@Transactional
public Department getDepartment(Long id) {
    return deptRepo.findById(id);  // Session open
}  // ← @Transactional ends, Session CLOSES

// Controller (NO transaction)
Department dept = service.getDepartment(10L);
int count = dept.getEmployees().size();  
// 💥 LazyInitializationException: no Session!
```

**Timeline:**
```
@Transactional starts → Session opens
   Load Department
   employees = proxy (not initialized)
@Transactional ends → Session CLOSES ❌
   
Access employees.size()
   Proxy needs to load
   Proxy: "Session is closed!"
   💥 Exception
```

### ✅ Solutions to LazyInitializationException

#### Solution 1: @Transactional (Keep Session Open)
```java
@Transactional(readOnly = true)
public void processDepartment(Long id) {
    Department dept = deptRepo.findById(id);
    dept.getEmployees().forEach(e -> System.out.println(e.getName()));
    // ✅ Works! Session still open
}  // Session closes here
```

#### Solution 2: JOIN FETCH (Eager Load Selectively)
```java
@Query("SELECT d FROM Department d LEFT JOIN FETCH d.employees WHERE d.id = :id")
Department findByIdWithEmployees(@Param("id") Long id);

// SQL generated:
// SELECT d.*, e.* 
// FROM departments d 
// LEFT JOIN employees e ON d.id = e.dept_id 
// WHERE d.id = ?

// ✅ All data loaded in one query, can use outside transaction
```

#### Solution 3: Entity Graph
```java
@EntityGraph(attributePaths = {"employees", "employees.department"})
Department findById(Long id);

// Hibernate auto-generates JOIN FETCH
```

#### Solution 4: DTO Projection
```java
@Query("SELECT new com.example.DeptDTO(d.id, d.name, COUNT(e)) " +
       "FROM Department d LEFT JOIN d.employees e " +
       "GROUP BY d.id, d.name")
DeptDTO getDepartmentSummary(Long id);

// No entities, no lazy loading issues
```

#### Solution 5: Hibernate.initialize()
```java
@Transactional
public Department getDepartment(Long id) {
    Department dept = deptRepo.findById(id);
    Hibernate.initialize(dept.getEmployees());  // Force load
    return dept;  // Safe to use outside transaction
}
```

### 🚀 Eager Loading

```java
@Entity
public class Employee {
    @ManyToOne(fetch = FetchType.EAGER)  // Load immediately
    @JoinColumn(name = "dept_id")
    private Department department;
}

Employee emp = session.get(Employee.class, 1L);
// SQL: SELECT e.*, d.* FROM employees e 
//      LEFT JOIN departments d ON e.dept_id = d.id 
//      WHERE e.id = 1
```

#### ⚠️ Cascading Eager Problem
```java
Department (EAGER employees)
   → Employee (EAGER orders)
       → Order (EAGER products)
           → Product (EAGER category)
               → Category (EAGER parentCategory)
                   ...

// Loading ONE department loads ENTIRE database! 💥
```

### 🎯 Default Fetch Types

| Association | Default | Reason |
|-------------|---------|--------|
| @ManyToOne | EAGER | Usually small, FK typically not null |
| @OneToOne | EAGER | 1-to-1 often accessed together |
| @OneToMany | LAZY | Collection can be huge |
| @ManyToMany | LAZY | Collection can be huge |

### 🎓 Interview Talking Points

✅ **Say this:**
- "Lazy uses proxy pattern - loads on first access, risks LazyInitializationException"
- "Eager loads with JOIN immediately - no exceptions but wastes memory"
- "LazyInitializationException: Session closed before proxy initialized"
- "Solutions: @Transactional (keep Session open), JOIN FETCH (best), Entity Graphs"
- "Default: collections LAZY (can be large), single associations EAGER"
- "Avoid cascading EAGER - can load entire database"

### 💡 Best Practices

```java
// ✅ Good: Default LAZY for collections
@OneToMany(mappedBy = "department")  // LAZY by default
private List<Employee> employees;

// ✅ Good: Override to LAZY for optional ManyToOne
@ManyToOne(fetch = FetchType.LAZY, optional = true)
private Department department;

// ✅ Good: Use JOIN FETCH when you know you need data
@Query("SELECT d FROM Department d JOIN FETCH d.employees")
List<Department> findAllWithEmployees();

// ❌ Bad: EAGER collections
@OneToMany(fetch = FetchType.EAGER)  // Loads all employees always!
private List<Employee> employees;
```

---

<a name="h5-n1-problem"></a>
## H5: 🔁 N+1 Query Problem

### ⚡ Quick Answer (30 seconds)
> *"N+1 problem: 1 query loads N parents, then N separate queries load children for each parent = 1+N total queries. Example: Load 100 departments (1 query), access employees for each (100 queries) = 101 total! Solution: JOIN FETCH for single query with LEFT JOIN, @BatchSize for batching, SUBSELECT for 2 queries, or DTOs for APIs."*

### 🔴 The Problem

```java
// Load departments
List<Department> depts = session.createQuery("FROM Department").list();
// Query 1: SELECT * FROM departments (Returns 100 rows)

// Access employees for each
for (Department dept : depts) {
    System.out.println(dept.getEmployees().size());
    // Query 2:   SELECT * FROM employees WHERE dept_id = 1
    // Query 3:   SELECT * FROM employees WHERE dept_id = 2
    // ...
    // Query 101: SELECT * FROM employees WHERE dept_id = 100
}

// Total: 1 + 100 = 101 queries! 💥
```

**Performance Impact:**
```
100 departments → 101 queries
1000 departments → 1001 queries

Each query: ~5ms network latency
1001 queries × 5ms = 5 seconds just waiting!
```

### ✅ Solutions Ranked

| Solution | Queries | Best For | Complexity |
|----------|---------|----------|------------|
| **JOIN FETCH** | 1 | Small/medium data | Low |
| **Entity Graph** | 1 | Complex scenarios | Medium |
| **@BatchSize** | 1 + ceil(N/size) | Large datasets | Low |
| **SUBSELECT** | 2 | Many entities | Low |
| **DTO** | 1 | APIs, reports | Medium |

### 🥇 Solution 1: JOIN FETCH (Best)

```java
// ✅ Single query
List<Department> depts = session.createQuery(
    "SELECT DISTINCT d FROM Department d LEFT JOIN FETCH d.employees"
).list();

for (Department dept : depts) {
    dept.getEmployees().size();  // No additional queries!
}

// SQL:
// SELECT d.id, d.name, e.id, e.name, e.dept_id
// FROM departments d
// LEFT JOIN employees e ON d.id = e.dept_id
```

**⚠️ Important: Use DISTINCT!**
```
Without DISTINCT:
departments (10 rows) × employees (average 5 per dept) = 50 ResultSet rows
Returns: [Dept1, Dept1, Dept1, Dept1, Dept1, Dept2, Dept2, ...]
         (duplicate Department objects!)

With DISTINCT:
Hibernate deduplicates based on ID
Returns: [Dept1, Dept2, Dept3, ...] (10 unique objects)
```

### 🥈 Solution 2: @BatchSize

```java
@Entity
public class Department {
    @OneToMany(mappedBy = "department")
    @BatchSize(size = 10)  // Fetch for 10 departments at once
    private List<Employee> employees;
}

// Usage:
List<Department> depts = session.createQuery("FROM Department").list();
// Query 1: SELECT * FROM departments (100 rows)

for (Department dept : depts) {
    dept.getEmployees().size();
}
// Query 2:  SELECT * FROM employees WHERE dept_id IN (1,2,3...10)
// Query 3:  SELECT * FROM employees WHERE dept_id IN (11,12,13...20)
// ...
// Query 11: SELECT * FROM employees WHERE dept_id IN (91,92...100)

// Total: 1 + ceil(100/10) = 11 queries (much better than 101!)
```

**Performance:**
```
N = 100 departments
Batch size = 10

Without batching: 1 + 100 = 101 queries
With batching:    1 + 10 = 11 queries (90% reduction!)
```

### 🥉 Solution 3: SUBSELECT

```java
@Entity
public class Department {
    @OneToMany(mappedBy = "department")
    @Fetch(FetchMode.SUBSELECT)
    private List<Employee> employees;
}

// Usage:
List<Department> depts = session.createQuery("FROM Department").list();
// Query 1: SELECT * FROM departments

for (Department dept : depts) {
    dept.getEmployees().size();
}
// Query 2: SELECT * FROM employees 
//          WHERE dept_id IN (SELECT id FROM departments)

// Total: 2 queries!
```

### 🎯 Solution 4: Entity Graphs

```java
@EntityGraph(attributePaths = {"employees"})
@Query("SELECT d FROM Department d")
List<Department> findAllWithEmployees();

// Generates same JOIN FETCH SQL as Solution 1
```

**Complex nested loading:**
```java
@EntityGraph(attributePaths = {"employees", "employees.orders"})
List<Department> findAllWithEmployeesAndOrders();

// SQL:
// SELECT d.*, e.*, o.*
// FROM departments d
// LEFT JOIN employees e ON d.id = e.dept_id
// LEFT JOIN orders o ON e.id = o.emp_id
```

### 📊 Solution 5: DTO Projection

```java
@Query("SELECT new com.example.DeptDTO(d.id, d.name, COUNT(e)) " +
       "FROM Department d LEFT JOIN d.employees e " +
       "GROUP BY d.id, d.name")
List<DeptDTO> findAllWithEmployeeCount();

// Single optimized query with aggregate:
// SELECT d.id, d.name, COUNT(e.id)
// FROM departments d
// LEFT JOIN employees e ON d.id = e.dept_id
// GROUP BY d.id, d.name
```

### 🔍 Detection Techniques

#### Enable SQL Logging
```properties
# application.properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Statistics
spring.jpa.properties.hibernate.generate_statistics=true
logging.level.org.hibernate.stat=DEBUG
```

#### Test with Query Counter
```java
@Test
public void shouldNotHaveNPlusOne() {
    long startCount = getQueryCount();
    
    List<Department> depts = deptRepo.findAll();
    depts.forEach(d -> d.getEmployees().size());
    
    long endCount = getQueryCount();
    long totalQueries = endCount - startCount;
    
    assertThat(totalQueries).isLessThanOrEqualTo(2);  // Should be 1-2, not 101!
}
```

### 📈 Performance Comparison

```
Test: 100 departments, average 10 employees each

┌──────────────────┬──────────┬──────────────┐
│ Solution         │ Queries  │ Time         │
├──────────────────┼──────────┼──────────────┤
│ N+1 (Problem)    │ 101      │ 505ms        │
│ JOIN FETCH       │ 1        │ 45ms ✅      │
│ @BatchSize(10)   │ 11       │ 80ms         │
│ SUBSELECT        │ 2        │ 55ms         │
│ Entity Graph     │ 1        │ 45ms ✅      │
│ DTO Projection   │ 1        │ 40ms ✅      │
└──────────────────┴──────────┴──────────────┘
```

### 🎓 Interview Talking Points

✅ **Say this:**
- "N+1: 1 parent query + N child queries = terrible performance"
- "100 departments × 5ms = 500ms wasted in network latency alone"
- "JOIN FETCH best for most cases - single query with LEFT JOIN"
- "@BatchSize batches lazy loading - 1 + ceil(N/size) queries"
- "SUBSELECT uses IN with subquery - always 2 queries"
- "DTOs best for APIs - no lazy loading, optimized aggregates"
- "Enable hibernate statistics to detect N+1 in production"

### 💡 Best Practices

```java
// ✅ Good: Explicit JOIN FETCH for known access pattern
@Query("SELECT d FROM Department d JOIN FETCH d.employees")
List<Department> findAllWithEmployees();

// ✅ Good: @BatchSize for large optional collections
@OneToMany(mappedBy = "dept")
@BatchSize(size = 20)
private List<Employee> employees;

// ✅ Good: DTO for list endpoints
@Query("SELECT new DeptDTO(d.id, d.name, COUNT(e)) ...")
List<DeptDTO> findAllSummaries();

// ❌ Bad: EAGER loading (always loads)
@OneToMany(fetch = FetchType.EAGER)
private List<Employee> employees;

// ❌ Bad: No fetch strategy (N+1 guaranteed)
List<Department> depts = deptRepo.findAll();
depts.forEach(d -> d.getEmployees().size());  // N+1!
```

---

<a name="h6-jpa-vs-hibernate"></a>
## H6: 📜 JPA vs Hibernate

### ⚡ Quick Answer (30 seconds)
> *"JPA is specification (javax.persistence interfaces) defining standard annotations (@Entity, @Id), EntityManager API, and JPQL. Hibernate is implementation - provides concrete classes for all JPA plus extras: Session API, HQL (superset of JPQL), advanced second-level cache, @Filter for multi-tenancy, @Formula for computed fields. Relationship: JPA:Hibernate :: JDBC:MySQL Driver. I use JPA for portability, leverage Hibernate features when needed."*

### 📊 Core Comparison

| Aspect | JPA | Hibernate |
|--------|-----|-----------|
| **Type** | Specification | Implementation |
| **Package** | javax.persistence.* | org.hibernate.* |
| **API** | EntityManager | Session (+ EntityManager) |
| **Query** | JPQL, Criteria | HQL, JPQL, Criteria, Native |
| **Caching** | First-level (spec) | First + Second level + Query |
| **Portability** | ✅ Switch providers | ❌ Vendor lock-in |
| **Features** | Standard only | JPA + Hibernate-specific |

### 🔄 Relationship Diagram

```
┌─────────────────────────────────────┐
│         JPA Specification           │
│   (Interface / Contract)            │
│  • @Entity, @Id, @OneToMany         │
│  • EntityManager interface          │
│  • JPQL query language              │
└──────────────┬──────────────────────┘
               │ implements
        ┌──────┴───────┬────────────┐
        ↓              ↓            ↓
┌──────────────┐ ┌────────────┐ ┌─────────┐
│  Hibernate   │ │EclipseLink │ │ OpenJPA │
│(org.hibernate)│ │            │ │         │
└──────────────┘ └────────────┘ └─────────┘

Similar to:
JDBC (Interface) ← MySQL Driver, PostgreSQL Driver
Servlet API ← Tomcat, Jetty
```

### 📦 JPA - The Specification

**What JPA Defines:**

```java
// Standard annotations (portable)
@Entity
@Table(name = "employees")
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name = "emp_name", nullable = false)
@OneToMany(mappedBy = "department")
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "dept_id")

// Standard API (portable)
@PersistenceContext
private EntityManager em;

em.persist(entity);      // Insert
em.merge(entity);        // Update/merge
em.remove(entity);       // Delete
em.find(Employee.class, id);  // Find by ID
em.createQuery(jpql);    // JPQL query

// Standard JPQL (portable)
"SELECT e FROM Employee e WHERE e.salary > :min"
```

### 🔧 Hibernate - The Implementation

**JPA Implementation:**
```java
// Hibernate implements all JPA interfaces
public class SessionImpl implements EntityManager, Session {
    @Override
    public void persist(Object entity) {
        // Hibernate's actual implementation
    }
    
    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        // Hibernate's actual implementation
    }
}
```

**Plus Hibernate-Specific Features:**

#### 1. Session API (More Powerful)
```java
@Autowired
private SessionFactory sessionFactory;

Session session = sessionFactory.getCurrentSession();

// Hibernate-specific methods
session.save(entity);     // Returns generated ID
session.load(Entity.class, id);  // Returns proxy (lazy)
session.saveOrUpdate(entity);    // Flexible
session.setCacheMode(CacheMode.IGNORE);  // Cache control
```

#### 2. HQL (Superset of JPQL)
```java
// JPQL (works with any provider)
"SELECT e FROM Employee e WHERE e.salary > :min"

// HQL has more features
"SELECT e FROM Employee e WHERE extract(year from e.hireDate) = 2024"
"FROM Employee e"  // No SELECT needed
```

#### 3. Advanced Second-Level Cache
```java
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)  // Hibernate-specific!
@BatchSize(size = 10)  // Hibernate-specific!
public class Employee {
    // ...
}

// Cache regions
sessionFactory.getCache().evictEntityRegion(Employee.class);
```

#### 4. @Filter for Multi-Tenancy
```java
@Entity
@FilterDef(name = "tenantFilter",  // Hibernate-specific!
    parameters = @ParamDef(name = "tenantId", type = "long"))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Employee {
    private Long tenantId;
}

// Usage
session.enableFilter("tenantFilter").setParameter("tenantId", 123L);
List<Employee> emps = session.createQuery("FROM Employee").list();
// Automatically adds: WHERE tenant_id = 123
```

#### 5. @Formula (Computed Columns)
```java
@Entity
public class Employee {
    @Formula("UPPER(name)")  // Hibernate-specific!
    private String upperName;  // Computed in SELECT, not stored
    
    @Formula("(SELECT COUNT(*) FROM orders WHERE emp_id = id)")
    private long orderCount;  // Subquery!
}

// Generated SQL:
// SELECT id, name, UPPER(name) as upperName, 
//        (SELECT COUNT(*) FROM orders WHERE emp_id = id) as orderCount
// FROM employees
```

#### 6. @DynamicUpdate / @DynamicInsert
```java
@Entity
@DynamicUpdate  // Hibernate-specific!
public class Employee {
    // ...
}

// Normal:  UPDATE employees SET name = ?, salary = ?, dept_id = ? WHERE id = ?
// Dynamic: UPDATE employees SET salary = ? WHERE id = ?  (only changed columns!)
```

#### 7. Statistics and Monitoring
```java
Statistics stats = sessionFactory.getStatistics();
stats.setStatisticsEnabled(true);

// Query stats
long queryCacheHitCount = stats.getQueryCacheHitCount();
long queryCacheMissCount = stats.getQueryCacheMissCount();

// Entity stats
SecondLevelCacheStatistics empStats = 
    stats.getSecondLevelCacheStatistics("com.example.Employee");
long hitCount = empStats.getHitCount();
```

### 💻 Code Comparison

#### Portable JPA Code
```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne
    @JoinColumn(name = "dept_id")
    private Department department;
}

@Service
public class EmployeeService {
    @PersistenceContext  // JPA
    private EntityManager em;
    
    @Transactional
    public void save(Employee emp) {
        em.persist(emp);
    }
    
    public List<Employee> findByName(String name) {
        return em.createQuery(
            "SELECT e FROM Employee e WHERE e.name = :name", Employee.class)
            .setParameter("name", name)
            .getResultList();
    }
}
```
✅ **Works with Hibernate, EclipseLink, or OpenJPA!**

#### Hibernate-Specific Code
```java
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)  // Hibernate
@DynamicUpdate  // Hibernate
@FilterDef(name = "activeOnly",  // Hibernate
    parameters = @ParamDef(name = "active", type = "boolean"))
@Filter(name = "activeOnly", condition = "is_active = :active")
public class Employee {
    @Id
    private Long id;
    
    @Formula("UPPER(name)")  // Hibernate
    private String upperName;
    
    @Formula("(SELECT AVG(salary) FROM employees)")  // Hibernate
    private BigDecimal avgSalary;
}

@Service
public class EmployeeService {
    @Autowired
    private SessionFactory sessionFactory;  // Hibernate
    
    @Transactional
    public List<Employee> findActive() {
        Session session = sessionFactory.getCurrentSession();
        
        // Enable filter
        session.enableFilter("activeOnly")
            .setParameter("active", true);
        
        return session.createQuery("FROM Employee", Employee.class)
            .setCacheable(true)  // Use query cache
            .list();
    }
}
```
❌ **Only works with Hibernate!**

### 🔄 Migration Between Providers

**From Hibernate to EclipseLink:**

1. Change dependency:
```xml
<!-- Remove Hibernate -->
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-core</artifactId>
</dependency>

<!-- Add EclipseLink -->
<dependency>
    <groupId>org.eclipse.persistence</groupId>
    <artifactId>eclipselink</artifactId>
</dependency>
```

2. Change configuration:
```properties
# From
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# To
spring.jpa.properties.eclipselink.target-database=PostgreSQL
```

3. **Remove Hibernate-specific code:**
   - @Cache → Standard cache config
   - @Filter → Application-level filtering
   - @Formula → Database views or @Transient calculations
   - Session → EntityManager

### 📚 When to Use What

**Use JPA (Portable):**
```java
✅ Standard CRUD operations
✅ Basic queries with JPQL
✅ Simple relationships
✅ When you might switch providers
✅ Learning ORM (standard approach)

Example: Microservices with simple data access
```

**Use Hibernate Features:**
```java
✅ Advanced caching needs (read-heavy apps)
✅ Multi-tenancy with filters
✅ Computed fields without database changes
✅ Performance optimization (dynamic update)
✅ Complex legacy system integration

Example: Large enterprise app with complex requirements
```

### 🎓 Interview Talking Points

✅ **Say this:**
- "JPA is specification (interface), Hibernate is implementation (concrete classes)"
- "Relationship like JDBC:MySQL Driver or Servlet:Tomcat"
- "JPA provides portability - can switch from Hibernate to EclipseLink"
- "Hibernate adds: advanced caching, filters, formulas, HQL extensions"
- "I use JPA annotations for portability, leverage Hibernate when needed"
- "Package convention: javax.persistence = portable, org.hibernate = vendor-specific"

### 💡 Best Practices

```java
// ✅ Good: Use JPA annotations (portable)
@Entity
@Table(name = "employees")
@OneToMany(mappedBy = "department")

// ✅ Good: Inject EntityManager (JPA)
@PersistenceContext
private EntityManager entityManager;

// ✅ Good: Use Hibernate features when justified
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)  // For read-heavy
@Formula("(SELECT COUNT(*) FROM orders WHERE emp_id = id)")  // Avoid DB change

// ❌ Avoid: Unnecessary Hibernate-specific code
@Autowired
private SessionFactory sessionFactory;  // Use EntityManager instead
Session session = sessionFactory.getCurrentSession();
session.save(emp);  // Use em.persist() instead
```

---

<a name="h7-transactions"></a>
## H7: 💳 Transaction Management

### ⚡ Quick Answer (30 seconds)
> *"Transactions ensure ACID: Atomicity (all-or-nothing), Consistency (valid states), Isolation (concurrent transactions don't interfere), Durability (committed = permanent). Hibernate provides transaction API, Spring's @Transactional uses AOP proxies to manage declaratively. Propagation: REQUIRED (default, join/create), REQUIRES_NEW (new transaction). Isolation: READ_COMMITTED (default), SERIALIZABLE (strictest). Pitfalls: self-invocation bypasses proxy, private methods can't be proxied, swallowing exceptions prevents rollback."*

### 🎯 ACID Properties

```
┌─────────────────────────────────────────────────┐
│  A - Atomicity    │  All operations succeed     │
│                   │  or all fail (no partial)   │
├─────────────────────────────────────────────────┤
│  C - Consistency  │  Database moves from one    │
│                   │  valid state to another     │
├─────────────────────────────────────────────────┤
│  I - Isolation    │  Concurrent transactions    │
│                   │  don't interfere            │
├─────────────────────────────────────────────────┤
│  D - Durability   │  Committed changes survive  │
│                   │  crashes                    │
└─────────────────────────────────────────────────┘
```

#### Real Example: Bank Transfer
```java
@Transactional
public void transfer(Long from, Long to, BigDecimal amount) {
    Account fromAcc = accountRepo.findById(from);
    Account toAcc = accountRepo.findById(to);
    
    fromAcc.setBalance(fromAcc.getBalance().subtract(amount));  // Step 1
    accountRepo.save(fromAcc);
    
    // CRASH HERE?
    
    toAcc.setBalance(toAcc.getBalance().add(amount));  // Step 2
    accountRepo.save(toAcc);
    
    // ACID guarantees:
    // A - Both steps or neither (rollback on crash)
    // C - Balance constraints enforced (balance >= 0)
    // I - Other transactions see old values until commit
    // D - After commit, changes permanent (survives crash)
}
```

### ⚙️ How @Transactional Works

#### Without @Transactional (Manual)
```java
public void save(Employee emp) {
    Session session = null;
    Transaction tx = null;
    
    try {
        session = sessionFactory.openSession();
        tx = session.beginTransaction();  // BEGIN
        
        session.save(emp);
        
        tx.commit();  // COMMIT
        
    } catch (Exception e) {
        if (tx != null) {
            tx.rollback();  // ROLLBACK
        }
        throw e;
    } finally {
        if (session != null) {
            session.close();
        }
    }
}
```

#### With @Transactional (Declarative)
```java
@Service
public class EmployeeService {
    
    @Transactional  // Spring manages everything!
    public void save(Employee emp) {
        employeeRepo.save(emp);
    }
}
```

**Spring AOP Proxy:**
```java
// Spring creates proxy at runtime:
public class EmployeeService$Proxy extends EmployeeService {
    
    @Override
    public void save(Employee emp) {
        TransactionStatus status = null;
        try {
            // 1. Begin transaction
            status = txManager.getTransaction(definition);
            
            // 2. Call actual method
            super.save(emp);
            
            // 3. Commit
            txManager.commit(status);
            
        } catch (RuntimeException e) {
            // 4. Rollback on RuntimeException
            txManager.rollback(status);
            throw e;
        }
    }
}
```

### 🔄 Transaction Propagation

| Type | Behavior | Use Case |
|------|----------|----------|
| **REQUIRED** | Join existing or create new | Default, most common |
| **REQUIRES_NEW** | Always new, suspend outer | Independent operations |
| **MANDATORY** | Must have transaction | Ensure transactional context |
| **NEVER** | Must NOT have transaction | Non-transactional operations |
| **NOT_SUPPORTED** | Suspend transaction | Long-running read operations |
| **SUPPORTS** | Join if exists, else non-TX | Flexible operations |
| **NESTED** | Nested transaction (savepoint) | Partial rollback |

#### REQUIRED (Default)
```java
@Transactional  // Transaction T1
public void methodA() {
    methodB();  // Joins T1 (no new transaction)
}

@Transactional(propagation = Propagation.REQUIRED)
public void methodB() {
    // Participates in T1
}

Timeline:
methodA() → T1 starts
   methodB() → Joins T1
   methodB() ends → Still in T1
methodA() ends → T1 commits
```

#### REQUIRES_NEW
```java
@Transactional
public void methodA() {
    try {
        methodB();  // Creates new transaction T2
    } catch (Exception e) {
        // T2 rolled back, but T1 can still commit!
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void methodB() {
    // NEW transaction T2
}

Timeline:
methodA() → T1 starts
   methodB() → T1 SUSPENDED, T2 starts
   methodB() commits → T2 commits
   Back to methodA() → T1 RESUMES
methodA() commits → T1 commits
```

**Use Case: Audit Logging**
```java
@Transactional
public void updateEmployee(Employee emp) {
    employeeRepo.save(emp);  // Business operation
    
    try {
        auditService.log("Employee updated");  // REQUIRES_NEW
    } catch (Exception e) {
        // Audit failed, but employee update still succeeds!
    }
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void log(String message) {
    auditRepo.save(new AuditLog(message));
    // Independent transaction - commits even if outer fails
}
```

### 🔒 Isolation Levels

| Level | Dirty Read | Non-Repeatable | Phantom | Performance |
|-------|------------|----------------|---------|-------------|
| **READ_UNCOMMITTED** | ❌ Possible | ❌ Possible | ❌ Possible | ✅ Fastest |
| **READ_COMMITTED** | ✅ Prevented | ❌ Possible | ❌ Possible | ✅ Good (Default) |
| **REPEATABLE_READ** | ✅ Prevented | ✅ Prevented | ❌ Possible | ⚠️ Slower |
| **SERIALIZABLE** | ✅ Prevented | ✅ Prevented | ✅ Prevented | ❌ Slowest |

#### Problems Explained

**Dirty Read (READ_UNCOMMITTED):**
```java
// TX1
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public void tx1() {
    account.setBalance(1000);
    // NOT committed yet
    Thread.sleep(5000);
    throw new Exception();  // ROLLBACK!
}

// TX2 (simultaneous)
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public void tx2() {
    BigDecimal bal = account.getBalance();  // Sees 1000 (dirty!)
    // TX1 rolls back, balance never was 1000!
}
```

**Non-Repeatable Read (READ_COMMITTED):**
```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public void example() {
    BigDecimal bal1 = account.getBalance();  // 1000
    
    // Another TX updates and commits
    
    BigDecimal bal2 = account.getBalance();  // 1500 (different!)
    // Same query, different result!
}
```

**Phantom Read (REPEATABLE_READ):**
```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public void example() {
    long count1 = employeeRepo.count();  // 100
    
    // Another TX inserts and commits
    
    long count2 = employeeRepo.count();  // 101 (phantom row!)
}
```

### 🔁 Rollback Rules

**Default Behavior:**
```java
@Transactional
public void method1() {
    save(entity);
    throw new RuntimeException();  // ✅ ROLLBACK
}

@Transactional
public void method2() throws Exception {
    save(entity);
    throw new Exception();  // ❌ COMMIT (checked exception!)
}
```

**Customize:**
```java
@Transactional(rollbackFor = Exception.class)  // Rollback on ANY exception
public void method() throws Exception {
    save(entity);
    throw new Exception();  // ✅ Now ROLLBACK
}

@Transactional(noRollbackFor = NotFoundException.class)
public void method() {
    save(entity);
    throw new NotFoundException();  // ❌ COMMIT (explicitly excluded)
}
```

### ⚠️ Common Pitfalls

#### Pitfall 1: Self-Invocation (NO PROXY!)

```java
❌ WRONG:
@Service
public class UserService {
    
    public void register(User user) {
        validate(user);
        save(user);  // ❌ Transaction NOT applied!
    }
    
    @Transactional
    private void save(User user) {
        userRepo.save(user);
    }
}

Why: this.save() bypasses proxy!

✅ SOLUTION 1: Self-inject
@Service
public class UserService {
    @Autowired
    private UserService self;
    
    public void register(User user) {
        self.save(user);  // ✅ Goes through proxy
    }
    
    @Transactional
    public void save(User user) {
        userRepo.save(user);
    }
}

✅ SOLUTION 2: Separate service
@Service
public class UserService {
    @Autowired
    private UserPersistence persistence;
    
    public void register(User user) {
        persistence.save(user);  // ✅ Different bean
    }
}

@Service
class UserPersistence {
    @Transactional
    public void save(User user) {
        userRepo.save(user);
    }
}
```

#### Pitfall 2: Private Methods

```java
❌ WRONG:
@Service
public class UserService {
    
    @Transactional  // ❌ Ignored! Can't proxy private
    private void save(User user) {
        userRepo.save(user);
    }
}

✅ CORRECT:
@Service
public class UserService {
    
    @Transactional  // ✅ Must be public/protected
    public void save(User user) {
        userRepo.save(user);
    }
}
```

#### Pitfall 3: Swallowing Exceptions

```java
❌ WRONG:
@Transactional
public void method() {
    try {
        save(entity);
        throw new RuntimeException();
    } catch (Exception e) {
        log.error("Error", e);  // ❌ Swallowed, NO rollback!
    }
}

✅ CORRECT:
@Transactional
public void method() {
    try {
        save(entity);
    } catch (Exception e) {
        TransactionAspectSupport.currentTransactionStatus()
            .setRollbackOnly();  // ✅ Mark for rollback
        log.error("Error", e);
    }
}
```

### 🎓 Interview Talking Points

✅ **Say this:**
- "ACID: Atomicity (all-or-nothing), Consistency (valid states), Isolation (no interference), Durability (permanent after commit)"
- "@Transactional uses Spring AOP proxies - wraps method with begin/commit/rollback"
- "Propagation REQUIRED joins existing, REQUIRES_NEW suspends outer and creates new"
- "Isolation READ_COMMITTED default, prevents dirty reads but allows non-repeatable"
- "Pitfalls: self-invocation (no proxy), private methods (can't proxy), swallowing exceptions (no rollback)"
- "Default: rollback on RuntimeException only, use rollbackFor=Exception.class for all"

---

<a name="h8-jta-distributed-transactions"></a>
## H8: 🌐 JTA & Distributed Transactions

### ⚡ Quick Answer (30 seconds)
> *"JTA manages distributed transactions across multiple resources using Two-Phase Commit: Phase 1 - all resources vote to commit, Phase 2 - if all agree commit, else rollback. Example: Transfer between two databases - JTA ensures both commit or both rollback. Problems: 10-50x slower, complexity, not cloud-native. Modern alternatives: Saga Pattern (local transactions + compensating actions), Outbox Pattern (guaranteed event delivery). I avoid JTA in microservices, prefer eventual consistency."*

### 🎯 The Problem: Multiple Databases

```java
❌ Problem with standard @Transactional:

@Transactional
public void transfer(Long fromId, Long toId, BigDecimal amount) {
    // Database 1 - US accounts
    Account fromAcc = usAccountRepo.findById(fromId);
    fromAcc.deduct(amount);
    usAccountRepo.save(fromAcc);  // ✅ Commits to DB1
    
    // CRASH HERE?
    
    // Database 2 - EU accounts
    Account toAcc = euAccountRepo.findById(toId);
    toAcc.add(amount);
    euAccountRepo.save(toAcc);  // ❌ Never executes
}

Result: DB1 committed, DB2 didn't → Money lost! 💥
```

### 🔄 Two-Phase Commit Protocol

```
Coordinator: Transaction Manager (Atomikos, Bitronix, JBoss)
Participants: DB1, DB2, Message Queue, etc.

┌──────────────────────────────────────────────────┐
│           PHASE 1: PREPARE (Vote)                │
└──────────────────────────────────────────────────┘

Transaction Manager → DB1: "Can you commit?"
                      DB1: "Yes, prepared" ✅

Transaction Manager → DB2: "Can you commit?"
                      DB2: "Yes, prepared" ✅

Transaction Manager → Queue: "Can you commit?"
                      Queue: "Yes, prepared" ✅

All voted YES → Proceed to Phase 2
Any voted NO → ABORT (all rollback)

┌──────────────────────────────────────────────────┐
│           PHASE 2: COMMIT/ABORT                  │
└──────────────────────────────────────────────────┘

If all YES:
Transaction Manager → DB1: "COMMIT!" ✅
Transaction Manager → DB2: "COMMIT!" ✅
Transaction Manager → Queue: "COMMIT!" ✅

If any NO:
Transaction Manager → ALL: "ABORT!" ❌
```

### ⚙️ JTA Configuration

```java
@Configuration
public class JtaConfig {
    
    // Database 1
    @Bean
    public DataSource ordersDataSource() {
        MysqlXADataSource ds = new MysqlXADataSource();
        ds.setUrl("jdbc:mysql://localhost/orders_db");
        return ds;
    }
    
    // Database 2
    @Bean
    public DataSource inventoryDataSource() {
        MysqlXADataSource ds = new MysqlXADataSource();
        ds.setUrl("jdbc:mysql://localhost/inventory_db");
        return ds;
    }
    
    // JTA Transaction Manager
    @Bean
    public JtaTransactionManager transactionManager() {
        AtomikosJtaPlatform platform = new AtomikosJtaPlatform();
        return new JtaTransactionManager(
            platform.retrieveTransactionManager());
    }
    
    // Entity Manager for DB1
    @Bean
    public LocalContainerEntityManagerFactoryBean ordersEM() {
        LocalContainerEntityManagerFactoryBean em = 
            new LocalContainerEntityManagerFactoryBean();
        em.setJtaDataSource(ordersDataSource());
        em.setPackagesToScan("com.example.orders");
        return em;
    }
    
    // Entity Manager for DB2
    @Bean
    public LocalContainerEntityManagerFactoryBean inventoryEM() {
        LocalContainerEntityManagerFactoryBean em = 
            new LocalContainerEntityManagerFactoryBean();
        em.setJtaDataSource(inventoryDataSource());
        em.setPackagesToScan("com.example.inventory");
        return em;
    }
}
```

### 💻 Usage Example

```java
@Service
public class OrderService {
    
    @PersistenceContext(unitName = "orders")
    private EntityManager ordersEM;
    
    @PersistenceContext(unitName = "inventory")
    private EntityManager inventoryEM;
    
    @Transactional  // JTA manages both!
    public void createOrder(Order order) {
        // Save to orders database
        ordersEM.persist(order);
        
        // Update inventory database
        Inventory inv = inventoryEM.find(
            Inventory.class, order.getProductId());
        inv.reduceQuantity(order.getQuantity());
        
        // 2PC ensures:
        // Both databases commit together OR
        // Both databases rollback together
    }
}
```

### ⚠️ Problems with JTA

#### 1. Performance Overhead
```
Single DB Transaction: 10ms
JTA Two-Phase Commit:  100-500ms (10-50x slower!)

Why slow:
• Two round-trips per resource (prepare + commit)
• Locks held longer
• Coordination overhead
• Network latency multiplied
```

#### 2. In-Doubt Transactions
```
Phase 1: Prepare
- DB1: "Yes, prepared" ✅
- DB2: "Yes, prepared" ✅

Phase 2: Commit
- Transaction Manager sends "COMMIT" to DB1 ✅
- NETWORK FAILURE before reaching DB2 💥
- DB2 stuck in "prepared" state (in-doubt)
- Locks held indefinitely!
- Manual intervention required
```

#### 3. Not Cloud-Native
```
❌ DynamoDB doesn't support 2PC
❌ Cosmos DB doesn't support 2PC
❌ Most cloud databases don't support XA
❌ Microservices across HTTP can't use JTA
❌ Complex setup in Kubernetes
```

### ✅ Modern Alternative 1: Saga Pattern

**Choreography-Based:**

```java
// Order Service
@Service
public class OrderService {
    
    @Transactional  // LOCAL transaction only!
    public void createOrder(Order order) {
        order.setStatus(OrderStatus.PENDING);
        orderRepo.save(order);
        
        // Publish event
        eventPublisher.publish(new OrderCreatedEvent(order));
    }
    
    @EventListener
    @Transactional
    public void onInventoryReserved(InventoryReservedEvent event) {
        Order order = orderRepo.findById(event.getOrderId());
        order.setStatus(OrderStatus.CONFIRMED);  // ✅ Success
        orderRepo.save(order);
    }
    
    @EventListener
    @Transactional
    public void onInventoryFailed(InventoryFailedEvent event) {
        Order order = orderRepo.findById(event.getOrderId());
        order.setStatus(OrderStatus.CANCELLED);  // ❌ Compensate
        orderRepo.save(order);
    }
}

// Inventory Service (separate database!)
@Service
public class InventoryService {
    
    @EventListener
    @Transactional  // LOCAL transaction
    public void onOrderCreated(OrderCreatedEvent event) {
        try {
            Inventory inv = inventoryRepo.findByProductId(
                event.getProductId());
            inv.reduceQuantity(event.getQuantity());
            inventoryRepo.save(inv);
            
            // Success
            eventPublisher.publish(
                new InventoryReservedEvent(event.getOrderId()));
            
        } catch (OutOfStockException e) {
            // Failure - trigger compensation
            eventPublisher.publish(
                new InventoryFailedEvent(event.getOrderId()));
        }
    }
}
```

**Flow:**
```
1. Order Service: Create order (PENDING) → OrderCreatedEvent
2. Inventory Service: Reduce stock → InventoryReservedEvent
3. Order Service: Update order (CONFIRMED)

If step 2 fails:
2. Inventory Service: Can't reduce → InventoryFailedEvent
3. Order Service: Cancel order (CANCELLED) ← Compensating action
```

### ✅ Modern Alternative 2: Outbox Pattern

**Problem with Saga:** Event publishing can fail!

```java
❌ Problem:
@Transactional
public void createOrder(Order order) {
    orderRepo.save(order);  // ✅ Committed
    
    // CRASH before event sent!
    eventPublisher.publish(new OrderCreatedEvent(order));  // ❌ Lost
    
    // Inventory never updated! 💥
}
```

**Solution: Outbox Pattern**

```java
@Entity
public class OutboxEvent {
    @Id private Long id;
    private String eventType;
    private String payload;  // JSON
    private boolean published;
    private LocalDateTime createdAt;
}

@Service
public class OrderService {
    
    @Transactional  // ACID transaction
    public void createOrder(Order order) {
        // 1. Save order
        orderRepo.save(order);
        
        // 2. Save event in SAME transaction
        OutboxEvent event = new OutboxEvent();
        event.setEventType("OrderCreated");
        event.setPayload(toJson(order));
        event.setPublished(false);
        outboxRepo.save(event);
        
        // Both saved atomically!
    }
}

// Separate process publishes events
@Scheduled(fixedDelay = 1000)
@Transactional
public void publishOutboxEvents() {
    List<OutboxEvent> events = 
        outboxRepo.findByPublishedFalse();
    
    for (OutboxEvent event : events) {
        try {
            kafka.send(event.getEventType(), 
                      event.getPayload());
            
            event.setPublished(true);
            outboxRepo.save(event);
            
        } catch (Exception e) {
            // Retry later
        }
    }
}
```

**Guarantees:**
- ✅ Order and event saved together (ACID)
- ✅ Event guaranteed to be published (eventually)
- ✅ No distributed transaction needed!

### 📊 Comparison

| Aspect | JTA (2PC) | Saga Pattern | Outbox Pattern |
|--------|-----------|--------------|----------------|
| **Consistency** | Strong (ACID) | Eventual | Eventual |
| **Performance** | ❌ Slow (10-50x) | ✅ Fast | ✅ Fast |
| **Complexity** | ⚠️ High | ⚠️ Medium | ⚠️ Medium |
| **Cloud Support** | ❌ Limited | ✅ Yes | ✅ Yes |
| **Failure Handling** | Automatic rollback | Manual compensation | Retry |
| **Microservices** | ❌ Difficult | ✅ Good | ✅ Best |
| **Latency** | High | Low | Low |

### 🎓 Interview Talking Points

✅ **Say this:**
- "JTA uses Two-Phase Commit: Phase 1 vote (can you commit?), Phase 2 action (commit/abort)"
- "All resources vote YES → all commit, any NO → all rollback"
- "Problems: 10-50x slower, in-doubt transactions on network failure, not cloud-native"
- "Modern alternatives: Saga (local transactions + compensating actions), Outbox (guaranteed events)"
- "I avoid JTA in microservices - prefer eventual consistency with Saga + Outbox"
- "Outbox ensures atomicity: save business data + event in same local transaction"

### 💡 When to Use

**Use JTA when:**
```
✅ Legacy monolith with multiple databases
✅ Strict ACID absolutely required
✅ Java EE environment (built-in support)
✅ Small transaction volume
```

**Use Saga/Outbox when:**
```
✅ Microservices architecture
✅ Cloud environment
✅ Eventual consistency acceptable
✅ High throughput required
✅ Modern system design
```

---

# 🔷 DATABASE/SQL CHEAT SHEETS

<a name="s1-nth-highest-salary"></a>
## S1: 🔢 Nth Highest Salary

### ⚡ Quick Answer (30 seconds)
> *"For Nth highest salary, I use DENSE_RANK() window function - handles ties correctly, works for any N, single scan. Query: WITH ranked AS (SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) as rank FROM employees) SELECT salary FROM ranked WHERE rank = N. DENSE_RANK doesn't skip ranks for ties (90K rank 1, 80K rank 2) unlike RANK. Alternative: LIMIT OFFSET with DISTINCT."*

### 📊 Sample Data

```sql
employees
┌────┬─────────┬────────┐
│ id │  name   │ salary │
├────┼─────────┼────────┤
│ 1  │ Alice   │ 90000  │ ← Tied for 1st
│ 2  │ Bob     │ 80000  │ ← Tied for 2nd
│ 3  │ Charlie │ 90000  │ ← Tied for 1st
│ 4  │ David   │ 70000  │ ← 3rd
│ 5  │ Eve     │ 80000  │ ← Tied for 2nd
│ 6  │ Frank   │ 60000  │ ← 4th
└────┴─────────┴────────┘

Sorted unique salaries:
1st: 90000
2nd: 80000 ← 2nd highest
3rd: 70000
4th: 60000
```

### 🥇 Solution 1: DENSE_RANK (Best)

```sql
-- 2nd highest salary
WITH RankedSalaries AS (
    SELECT 
        salary,
        DENSE_RANK() OVER (ORDER BY salary DESC) as rank
    FROM employees
)
SELECT salary
FROM RankedSalaries
WHERE rank = 2;

-- Result: 80000 ✅

-- Generic for Nth:
WITH RankedSalaries AS (
    SELECT 
        salary,
        DENSE_RANK() OVER (ORDER BY salary DESC) as rank
    FROM employees
)
SELECT salary
FROM RankedSalaries
WHERE rank = :N;
```

**How DENSE_RANK Works:**

```
salary  | DENSE_RANK | RANK | ROW_NUMBER
--------|------------|------|------------
90000   | 1          | 1    | 1
90000   | 1          | 1    | 2  ← Tie
80000   | 2 ✅       | 3 ❌ | 3  ← No gap!
80000   | 2          | 3    | 4
70000   | 3          | 5    | 5
60000   | 4          | 6    | 6
```

**Why DENSE_RANK?**
- ✅ No gaps in ranking (handles ties correctly)
- ✅ 2nd rank is truly 2nd unique salary
- ✅ Works for any N
- ✅ Standard SQL (SQL:2003)

### 🥈 Solution 2: LIMIT OFFSET

```sql
-- 2nd highest (MySQL/PostgreSQL)
SELECT DISTINCT salary
FROM employees
ORDER BY salary DESC
LIMIT 1 OFFSET 1;  -- Skip 1, take 1

-- Nth highest:
SELECT DISTINCT salary
FROM employees
ORDER BY salary DESC
LIMIT 1 OFFSET :N-1;
```

**⚠️ Must use DISTINCT!**

```sql
-- ❌ Without DISTINCT:
SELECT salary
FROM employees
ORDER BY salary DESC
LIMIT 1 OFFSET 1;
-- Returns: 90000 (second occurrence of 90K)

-- ✅ With DISTINCT:
SELECT DISTINCT salary
FROM employees
ORDER BY salary DESC
LIMIT 1 OFFSET 1;
-- Returns: 80000 ✅
```

### 🥉 Solution 3: Subquery (Works Everywhere)

```sql
-- 2nd highest:
SELECT MAX(salary)
FROM employees
WHERE salary < (SELECT MAX(salary) FROM employees);

-- How it works:
-- SELECT MAX(salary) FROM employees → 90000
-- WHERE salary < 90000 → [80000, 70000, 60000]
-- MAX of those → 80000 ✅

-- 3rd highest (nested):
SELECT MAX(salary)
FROM employees
WHERE salary < (
    SELECT MAX(salary)
    FROM employees
    WHERE salary < (SELECT MAX(salary) FROM employees)
);
```

**Problems:**
- ❌ Ugly for N > 2 (nested subqueries)
- ❌ Multiple table scans
- ❌ Hard to parameterize N

### 📊 Performance Comparison

```sql
Test: 1,000,000 rows

┌──────────────────┬──────────┬────────┐
│ Solution         │ Queries  │ Time   │
├──────────────────┼──────────┼────────┤
│ DENSE_RANK       │ 1 scan   │ 150ms  │
│ LIMIT OFFSET     │ 1 scan   │ 140ms  │
│ Subquery (2nd)   │ 2 scans  │ 300ms  │
│ Self-Join        │ O(N²)    │ 5000ms │
└──────────────────┴──────────┴────────┘
```

### 🗄️ Database-Specific Syntax

```sql
-- PostgreSQL
SELECT DISTINCT salary
FROM employees
ORDER BY salary DESC
LIMIT 1 OFFSET :N-1;

-- MySQL (same as PostgreSQL)
SELECT DISTINCT salary
FROM employees
ORDER BY salary DESC
LIMIT 1 OFFSET :N-1;

-- SQL Server
SELECT DISTINCT salary
FROM employees
ORDER BY salary DESC
OFFSET :N-1 ROWS
FETCH NEXT 1 ROWS ONLY;

-- Oracle (modern)
SELECT salary
FROM (
    SELECT salary,
           ROW_NUMBER() OVER (ORDER BY salary DESC) as rn
    FROM (SELECT DISTINCT salary FROM employees)
)
WHERE rn = :N;
```

### 🎓 Interview Talking Points

✅ **Say this:**
- "DENSE_RANK() best - handles ties, no rank gaps, works for any N"
- "DENSE_RANK vs RANK: DENSE doesn't skip (90K rank 1, 80K rank 2 not 3)"
- "LIMIT OFFSET simpler syntax but needs DISTINCT for ties"
- "Subquery works everywhere but nested for N>2, multiple scans"
- "Performance: DENSE_RANK ~150ms on 1M rows"

### 💡 Best Practice

```sql
-- ✅ Production-ready with NULL handling:
WITH RankedSalaries AS (
    SELECT 
        salary,
        DENSE_RANK() OVER (ORDER BY salary DESC) as rank
    FROM employees
)
SELECT COALESCE(
    (SELECT salary FROM RankedSalaries WHERE rank = :N),
    NULL
) AS nth_highest_salary;

-- Returns NULL if N > total unique salaries
```

---

# 📖 QUICK REFERENCE SUMMARY

## Hibernate/JPA Key Points

| Topic | Remember This |
|-------|---------------|
| **ORM** | SessionFactory (heavy, thread-safe, one per app), Session (light, NOT thread-safe, one per TX) |
| **Mappings** | Many side owns OneToMany, Use Set for ManyToMany, Helper methods for bidirectional |
| **Lazy/Eager** | Lazy = proxy, Eager = JOIN, LazyInitializationException when Session closed |
| **N+1** | 1 + N queries = bad, JOIN FETCH = good (single query) |
| **JPA vs Hibernate** | JPA = spec, Hibernate = impl + extras (cache, filters, formulas) |
| **Transactions** | ACID, @Transactional = AOP proxy, Pitfalls: self-invocation, private methods |
| **JTA** | 2PC for multi-DB, 10-50x slower, Modern: Saga + Outbox |

## SQL Key Points

| Topic | Remember This |
|-------|---------------|
| **Nth Salary** | DENSE_RANK() best, handles ties, no gaps |

---

**END OF CHEAT SHEETS** - Total: 9 Topics Covered
Ready for interviews! 🚀
