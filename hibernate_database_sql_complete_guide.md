# Hibernate/JPA & Database/SQL - Complete Interview Guide
## All 19 Questions with Detailed Answers, Code Examples & Diagrams

---

# PART A: HIBERNATE/JPA

## H1: What is ORM? How does Hibernate implement it?

### What is ORM?

**ORM (Object-Relational Mapping)** bridges the gap between object-oriented programming and relational databases.

**The Problem:**
```
Java Objects              Relational Database
- Inheritance            - No inheritance
- Associations           - Foreign keys
- Polymorphism           - Joins
- Encapsulation          - Normalized tables
```

### Without ORM (JDBC Boilerplate)

```java
// ❌ Traditional JDBC - lots of code
public Employee findById(Long id) throws SQLException {
    Connection conn = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;
    
    try {
        conn = dataSource.getConnection();
        stmt = conn.prepareStatement("SELECT * FROM employees WHERE id = ?");
        stmt.setLong(1, id);
        rs = stmt.executeQuery();
        
        if (rs.next()) {
            Employee emp = new Employee();
            emp.setId(rs.getLong("id"));
            emp.setName(rs.getString("name"));
            emp.setSalary(rs.getBigDecimal("salary"));
            return emp;
        }
    } finally {
        if (rs != null) rs.close();
        if (stmt != null) stmt.close();
        if (conn != null) conn.close();
    }
    return null;
}
```

### With Hibernate ORM

```java
// ✅ Clean with Hibernate
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private BigDecimal salary;
    
    @ManyToOne
    @JoinColumn(name = "dept_id")
    private Department department;
}

// No implementation needed!
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Employee findByName(String name);
}
```

### Hibernate Architecture

```
Application Layer (Your Code)
        ↓
┌──────────────────────────┐
│   Hibernate Framework    │
│                          │
│ • SessionFactory         │
│ • Session                │
│ • HQL Translator         │
│ • First-Level Cache      │
│ • Dirty Checking         │
└──────────────────────────┘
        ↓
     JDBC Layer
        ↓
   Database (MySQL/PostgreSQL)
```

### Key Hibernate Features

**1. Automatic SQL Generation**
```java
Employee emp = new Employee("John", 75000);
session.save(emp);
// Generates: INSERT INTO employees (name, salary) VALUES ('John', 75000)
```

**2. HQL (Hibernate Query Language)**
```java
String hql = "FROM Employee WHERE salary > :min";
List<Employee> emps = session.createQuery(hql)
    .setParameter("min", 50000)
    .list();
```

**3. Caching**
```java
Employee e1 = session.get(Employee.class, 1L);  // DB hit
Employee e2 = session.get(Employee.class, 1L);  // From cache!
```

**4. Lazy Loading**
```java
@OneToMany(fetch = FetchType.LAZY)
private List<Order> orders;  // Loaded only when accessed
```

**Interview Answer:** *"ORM maps objects to database tables, eliminating boilerplate JDBC code. Hibernate implements ORM through automatic SQL generation, HQL for object queries, multi-level caching, lazy loading, and dirty checking. This reduces code by 80%+."*

---

## H2: Hibernate Session and SessionFactory

### SessionFactory
- **Heavy-weight**, created once per application
- **Thread-safe**, can be shared
- **Holds second-level cache**
- **Immutable** after creation

```java
@Configuration
public class HibernateConfig {
    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean sf = new LocalSessionFactoryBean();
        sf.setDataSource(dataSource);
        sf.setPackagesToScan("com.example.entity");
        return sf;
    }
}
```

### Session
- **Light-weight**, created per transaction
- **NOT thread-safe**, one per thread
- **Holds first-level cache**
- **Short-lived**

```java
@Service
public class EmployeeService {
    @Autowired
    private SessionFactory sessionFactory;
    
    public Employee save(Employee emp) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(emp);
            tx.commit();
            return emp;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }
}
```

### Comparison

| Feature | SessionFactory | Session |
|---------|---------------|---------|
| Thread-Safe | ✅ Yes | ❌ No |
| Scope | Application | Transaction |
| Cost | Expensive | Cheap |
| Instances | One per DB | Many |
| Cache | Second-level | First-level |

### Session Methods

```java
// save() - Insert
Long id = (Long) session.save(employee);

// get() - Fetch (returns null if not found)
Employee emp = session.get(Employee.class, 1L);

// load() - Lazy proxy
Employee emp = session.load(Employee.class, 1L);

// update() - Update
session.update(employee);

// delete() - Remove
session.delete(employee);

// merge() - Merge detached entity
Employee merged = (Employee) session.merge(detachedEmp);
```

### Entity States

```
Transient → save() → Persistent → close() → Detached
                          ↓
                      delete()
                          ↓
                       Removed
```

**Interview Answer:** *"SessionFactory is thread-safe singleton holding second-level cache, created once. Session is NOT thread-safe, per-transaction wrapper around DB connection with first-level cache. Spring's @Transactional manages Session automatically."*

---

## H3: Types of Mappings in Hibernate

### 1. One-to-One

```java
@Entity
public class User {
    @Id
    private Long id;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id")
    private UserProfile profile;
}

@Entity
public class UserProfile {
    @Id
    private Long id;
    
    @OneToOne(mappedBy = "profile")
    private User user;
}
```

### 2. One-to-Many / Many-to-One

```java
@Entity
public class Department {
    @Id
    private Long id;
    
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<Employee> employees = new ArrayList<>();
    
    public void addEmployee(Employee emp) {
        employees.add(emp);
        emp.setDepartment(this);
    }
}

@Entity
public class Employee {
    @Id
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "dept_id")
    private Department department;
}
```

### 3. Many-to-Many

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
    private Set<Course> courses = new HashSet<>();
}

@Entity
public class Course {
    @Id
    private Long id;
    
    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();
}
```

### 4. Inheritance - Single Table

```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class Payment {
    @Id
    private Long id;
    private BigDecimal amount;
}

@Entity
@DiscriminatorValue("CREDIT_CARD")
public class CreditCardPayment extends Payment {
    private String cardNumber;
}
```

### 5. Embedded Objects

```java
@Embeddable
public class Address {
    private String street;
    private String city;
}

@Entity
public class Company {
    @Id
    private Long id;
    
    @Embedded
    private Address address;
}
```

**Interview Answer:** *"Hibernate supports OneToOne (User-Profile), OneToMany/ManyToOne (Department-Employees where Many side owns relationship), ManyToMany (Students-Courses with junction table). For inheritance: SINGLE_TABLE (one table with discriminator), JOINED (normalized), TABLE_PER_CLASS. Use @Embeddable for value objects."*

---

## H4: Lazy Loading vs Eager Loading

### Lazy Loading (Default for collections)

```java
@Entity
public class Department {
    @OneToMany(fetch = FetchType.LAZY)  // Default
    private List<Employee> employees;
}

// Usage
Department dept = session.get(Department.class, 1L);
// SQL: SELECT * FROM departments WHERE id = 1

List<Employee> emps = dept.getEmployees();  // NOW loads
// SQL: SELECT * FROM employees WHERE dept_id = 1
```

### Eager Loading

```java
@Entity
public class Department {
    @OneToMany(fetch = FetchType.EAGER)
    private List<Employee> employees;
}

// Usage
Department dept = session.get(Department.class, 1L);
// SQL: SELECT d.*, e.* FROM departments d 
//      LEFT JOIN employees e ON d.id = e.dept_id
//      WHERE d.id = 1
```

### LazyInitializationException Problem

```java
// ❌ Problem
public Department getDepartment(Long id) {
    Session session = sessionFactory.openSession();
    Department dept = session.get(Department.class, id);
    session.close();  // Session closed!
    return dept;
}

Department dept = getDepartment(1L);
dept.getEmployees();  // ❌ LazyInitializationException!
```

### Solutions

**1. @Transactional**
```java
@Transactional  // Keeps session open
public Department getDepartment(Long id) {
    return repository.findById(id).orElse(null);
}
```

**2. JOIN FETCH**
```java
@Query("SELECT d FROM Department d LEFT JOIN FETCH d.employees WHERE d.id = :id")
Department findByIdWithEmployees(@Param("id") Long id);
```

**3. Entity Graph**
```java
@EntityGraph(attributePaths = {"employees"})
Department findById(Long id);
```

### Comparison

| Aspect | Lazy | Eager |
|--------|------|-------|
| When Loaded | On access | Immediately |
| Performance | ✅ Better initial | ❌ Slower |
| Memory | ✅ Less | ❌ More |
| Exception Risk | ❌ LazyInitializationException | ✅ None |

**Interview Answer:** *"Lazy loads on-demand (better performance but risks LazyInitializationException). Eager loads immediately (no exceptions but wastes resources). I use LAZY by default with @Transactional to keep session open, or JOIN FETCH when I know I need the data."*

---

## H5: N+1 Query Problem and Solutions

### The Problem

```java
// ❌ N+1 queries
List<Department> depts = session.createQuery("FROM Department").list();
// Query 1: SELECT * FROM departments (10 rows)

for (Department dept : depts) {
    dept.getEmployees().size();  // Lazy load!
}
// Query 2-11: SELECT * FROM employees WHERE dept_id = ?
// Total: 11 queries!
```

### Solution 1: JOIN FETCH (Best)

```java
// ✅ Single query
String jpql = "SELECT d FROM Department d LEFT JOIN FETCH d.employees";
List<Department> depts = session.createQuery(jpql).list();

// SQL: SELECT d.*, e.* FROM departments d 
//      LEFT JOIN employees e ON d.id = e.dept_id
```

### Solution 2: @BatchSize

```java
@Entity
public class Department {
    @OneToMany(mappedBy = "department")
    @BatchSize(size = 10)  // Fetch 10 at once
    private List<Employee> employees;
}

// Result: 1 + ceil(N/10) queries instead of 1 + N
```

### Solution 3: Entity Graph

```java
@EntityGraph(attributePaths = {"employees"})
@Query("SELECT d FROM Department d")
List<Department> findAllWithEmployees();
```

### Solution 4: DTO Projection

```java
@Query("SELECT new com.example.DepartmentDTO(d.id, d.name, COUNT(e)) " +
       "FROM Department d LEFT JOIN d.employees e GROUP BY d.id, d.name")
List<DepartmentDTO> findAllWithCount();
```

### Detection

```properties
# Enable SQL logging
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.generate_statistics=true
```

**Interview Answer:** *"N+1: 1 query for parents + N queries for children. Solutions: JOIN FETCH for single query (best), @BatchSize for large datasets, Entity Graphs for complex scenarios, DTOs for APIs. Always enable SQL logging to detect N+1 early."*

---

## H6: JPA vs Hibernate

### JPA - Specification

```
javax.persistence.*
- Annotations (@Entity, @Id, @OneToMany)
- EntityManager API
- JPQL
- Criteria API
```

### Hibernate - Implementation

```
org.hibernate.*
- Implements all JPA
- Adds: HQL, Filters, Formula, @Cache
- SessionFactory, Session
```

### Comparison

| Aspect | JPA | Hibernate |
|--------|-----|-----------|
| Type | Specification | Implementation |
| Package | javax.persistence.* | org.hibernate.* |
| API | EntityManager | Session |
| Query | JPQL | HQL |
| Portability | ✅ Switch providers | ❌ Vendor lock-in |

### Code Example

```java
// JPA (portable)
@Entity
public class Employee {
    @Id
    private Long id;
    
    @Column
    private String name;
}

@PersistenceContext
private EntityManager entityManager;

// Hibernate-specific
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@DynamicUpdate
public class Employee {
    @Formula("UPPER(name)")
    private String upperName;
}

@Autowired
private SessionFactory sessionFactory;
```

### When to Use

**JPA:** Portability, standard compliance  
**Hibernate-specific:** Advanced caching, filters, performance optimization

**Interview Answer:** *"JPA is specification (interface), Hibernate is implementation. JPA provides standard annotations/EntityManager for portability. Hibernate implements JPA plus extras like HQL, advanced caching, filters. I use JPA annotations for portability but leverage Hibernate features like second-level cache when needed."*

---

## H7: Transaction Management in Hibernate

### Without Transactions (Dangerous!)

```java
// ❌ No transaction - data inconsistency risk
public void transferMoney(Long from, Long to, BigDecimal amount) {
    Account fromAcc = accountRepo.findById(from);
    fromAcc.setBalance(fromAcc.getBalance().subtract(amount));
    accountRepo.save(fromAcc);  // ✅ Saved
    
    // 💥 Crash here!
    
    Account toAcc = accountRepo.findById(to);
    toAcc.setBalance(toAcc.getBalance().add(amount));
    accountRepo.save(toAcc);  // ❌ Never executes
    // Money lost!
}
```

### With Transactions (Safe!)

```java
// ✅ Transactional - all or nothing
@Transactional
public void transferMoney(Long from, Long to, BigDecimal amount) {
    Account fromAcc = accountRepo.findById(from);
    fromAcc.setBalance(fromAcc.getBalance().subtract(amount));
    
    // Crash anywhere → both rollback
    
    Account toAcc = accountRepo.findById(to);
    toAcc.setBalance(toAcc.getBalance().add(amount));
    
    // Both commit together
}
```

### Propagation Types

```java
@Transactional(propagation = Propagation.REQUIRED)  // Default - join or create
@Transactional(propagation = Propagation.REQUIRES_NEW)  // Always new transaction
@Transactional(propagation = Propagation.MANDATORY)  // Must have transaction
@Transactional(propagation = Propagation.NEVER)  // Must NOT have transaction
```

### Isolation Levels

```java
@Transactional(isolation = Isolation.READ_COMMITTED)  // Default
@Transactional(isolation = Isolation.REPEATABLE_READ)
@Transactional(isolation = Isolation.SERIALIZABLE)
```

### Rollback Rules

```java
@Transactional(rollbackFor = Exception.class)  // Rollback on checked exceptions too
@Transactional(noRollbackFor = NotFoundException.class)  // Don't rollback
```

### Common Pitfalls

**1. Self-invocation (No proxy!)**
```java
@Service
public class UserService {
    public void register(User user) {
        save(user);  // ❌ Transaction not applied!
    }
    
    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }
}

// ✅ Solution: Call from another bean or self-inject
@Autowired
private UserService self;

public void register(User user) {
    self.save(user);  // ✅ Goes through proxy
}
```

**2. Private methods**
```java
@Transactional  // ❌ Ignored on private!
private void save(User user) { }

@Transactional  // ✅ Must be public
public void save(User user) { }
```

**Interview Answer:** *"Transactions ensure atomicity - all succeed or all fail. @Transactional provides declarative management with propagation (REQUIRED, REQUIRES_NEW), isolation (READ_COMMITTED), rollback rules. Pitfalls: self-invocation (no proxy), private methods (can't proxy), swallowing exceptions (no rollback)."*

---

## H8: JTA Transactions

### What is JTA?

**JTA (Java Transaction API)** manages distributed transactions across multiple resources (databases, queues).

```
Single DB Transaction:     Distributed (JTA):
┌──────┐                  ┌────────────┐
│ App  │                  │    App     │
└──┬───┘                  └─────┬──────┘
   │                            │
   ▼                            ▼
┌──────┐                  ┌────────────┐
│  DB  │                  │JTA Manager │
└──────┘                  └─────┬──────┘
                            ┌───┼───┐
                            ▼   ▼   ▼
                          ┌───┬───┬───┐
                          │DB1│DB2│MQ │
                          └───┴───┴───┘
```

### 2-Phase Commit

```
Phase 1: Prepare
Manager: "Can you commit?"
DB1: "Yes"
DB2: "Yes"

Phase 2: Commit
Manager: "Commit now!"
DB1: ✅ Committed
DB2: ✅ Committed

If any DB says "No" → ALL rollback
```

### Example

```java
@Service
public class OrderService {
    
    @PersistenceContext(unitName = "ordersDB")
    private EntityManager ordersEM;
    
    @PersistenceContext(unitName = "inventoryDB")
    private EntityManager inventoryEM;
    
    @Transactional  // JTA manages both databases
    public void createOrder(Order order) {
        ordersEM.persist(order);  // DB1
        
        Inventory inv = inventoryEM.find(Inventory.class, order.getProductId());
        inv.reduceQuantity(order.getQuantity());  // DB2
        
        // Both commit together (2PC) or both rollback
    }
}
```

### Modern Alternatives (Better!)

**Saga Pattern**
```java
@Transactional
public void createOrder(Order order) {
    orderRepository.save(order);  // Local transaction
    kafkaTemplate.send("order-created", order);  // Event
}

@KafkaListener(topics = "order-created")
@Transactional
public void updateInventory(OrderEvent event) {
    inventoryRepository.reduce(event.getProductId(), event.getQuantity());
}
```

**Outbox Pattern**
```java
@Transactional
public void createOrder(Order order) {
    orderRepository.save(order);
    
    OutboxEvent outbox = new OutboxEvent("OrderCreated", toJson(order));
    outboxRepository.save(outbox);
    // Both in same local transaction!
}

@Scheduled(fixedDelay = 1000)
public void publishOutbox() {
    List<OutboxEvent> events = outboxRepository.findUnpublished();
    events.forEach(e -> kafka.send(e.getEventType(), e.getPayload()));
}
```

**Interview Answer:** *"JTA manages distributed transactions across multiple databases/resources using 2-phase commit. It's slow (10-50x overhead) and complex. Modern microservices prefer eventual consistency with Saga or Outbox patterns. I'd only use JTA for legacy monoliths requiring strict ACID across databases."*

---

# PART B: DATABASE & SQL

## S1: Nth Highest Salary Query

### Problem
Find 2nd, 3rd, Nth highest salary from employees table.

### Solution 1: DENSE_RANK (Best!)

```sql
-- 2nd highest salary
SELECT salary
FROM (
    SELECT salary,
           DENSE_RANK() OVER (ORDER BY salary DESC) as rank
    FROM employees
) ranked
WHERE rank = 2;

-- Generic for Nth
WITH RankedSalaries AS (
    SELECT DISTINCT salary,
           DENSE_RANK() OVER (ORDER BY salary DESC) as rank
    FROM employees
)
SELECT salary
FROM RankedSalaries
WHERE rank = :n;
```

### Solution 2: LIMIT OFFSET

```sql
-- 2nd highest
SELECT DISTINCT salary
FROM employees
ORDER BY salary DESC
LIMIT 1 OFFSET 1;

-- Nth highest
SELECT DISTINCT salary
FROM employees
ORDER BY salary DESC
LIMIT 1 OFFSET :n-1;
```

### Solution 3: Subquery

```sql
-- 2nd highest
SELECT MAX(salary)
FROM employees
WHERE salary < (SELECT MAX(salary) FROM employees);
```

### Comparison

| Method | Performance | Handles Ties | Database |
|--------|-------------|--------------|----------|
| DENSE_RANK | ✅ Fast | ✅ Yes | Modern SQL |
| LIMIT OFFSET | ✅ Fast | ❌ No | MySQL, PostgreSQL |
| Subquery | ❌ Slow | ✅ Yes | All |

**Interview Answer:** *"I prefer DENSE_RANK() window function - fast and handles ties correctly. LIMIT OFFSET works but needs DISTINCT for ties. For older databases, subquery approach works everywhere but slower."*

---

## S2: Types of Joins with Examples

### Sample Tables

**employees:** id, name, dept_id  
**departments:** id, dept_name

```
employees:              departments:
1 | Alice  | 10        10 | Engineering
2 | Bob    | 20        20 | Sales
3 | Charlie| NULL      40 | Marketing
```

### 1. INNER JOIN (Intersection)

```sql
SELECT e.name, d.dept_name
FROM employees e
INNER JOIN departments d ON e.dept_id = d.id;

-- Result:
-- Alice   | Engineering
-- Bob     | Sales
```

### 2. LEFT JOIN (All Left + Matches)

```sql
SELECT e.name, d.dept_name
FROM employees e
LEFT JOIN departments d ON e.dept_id = d.id;

-- Result:
-- Alice   | Engineering
-- Bob     | Sales
-- Charlie | NULL
```

### 3. RIGHT JOIN (All Right + Matches)

```sql
SELECT e.name, d.dept_name
FROM employees e
RIGHT JOIN departments d ON e.dept_id = d.id;

-- Result:
-- Alice | Engineering
-- Bob   | Sales
-- NULL  | Marketing
```

### 4. FULL OUTER JOIN (All from Both)

```sql
SELECT e.name, d.dept_name
FROM employees e
FULL OUTER JOIN departments d ON e.dept_id = d.id;

-- Result:
-- Alice   | Engineering
-- Bob     | Sales
-- Charlie | NULL
-- NULL    | Marketing
```

### 5. CROSS JOIN (Cartesian Product)

```sql
SELECT e.name, d.dept_name
FROM employees e
CROSS JOIN departments d;

-- 3 employees × 3 departments = 9 rows
```

### 6. SELF JOIN

```sql
-- employees: id, name, manager_id
SELECT 
    e.name AS employee,
    m.name AS manager
FROM employees e
LEFT JOIN employees m ON e.manager_id = m.id;
```

### Visual

```
INNER JOIN:        LEFT JOIN:
  A ∩ B             A + (A ∩ B)

RIGHT JOIN:        FULL OUTER:
  B + (A ∩ B)       A ∪ B
```

**Interview Answer:** *"INNER JOIN = matching rows only. LEFT JOIN = all left + matches (NULL if no match). RIGHT JOIN = all right + matches. FULL OUTER = all from both. I use INNER for required relationships (orders→customers), LEFT for optionals (products→categories)."*

---

## S3: Database Indexes

### What are Indexes?

Data structures that speed up data retrieval at the cost of slower writes.

```
Without Index:          With Index:
Scan entire table      Look up in index tree
O(n)                   O(log n)
```

### Creating Indexes

```sql
-- Single column
CREATE INDEX idx_email ON users(email);

-- Composite (multi-column)
CREATE INDEX idx_name_age ON users(last_name, first_name, age);

-- Unique
CREATE UNIQUE INDEX idx_username ON users(username);

-- Covering index
CREATE INDEX idx_user_details ON users(email, name, age);
```

### When to Use Indexes

**✅ Create index when:**
- Column in WHERE clause frequently
- Column in JOIN condition
- Column in ORDER BY
- Column used in GROUP BY
- High cardinality (many unique values)

**❌ Don't index when:**
- Small tables (<1000 rows)
- Low cardinality (few unique values like gender)
- Columns updated frequently
- Rarely queried columns

### Performance Impact

```sql
-- Without index
SELECT * FROM users WHERE email = 'john@example.com';
-- Execution: Full table scan (10,000 rows)

-- With index on email
SELECT * FROM users WHERE email = 'john@example.com';
-- Execution: Index seek (1 row)
```

### Composite Index Order Matters!

```sql
CREATE INDEX idx_name_age ON users(last_name, first_name, age);

-- ✅ Uses index
SELECT * FROM users WHERE last_name = 'Smith';
SELECT * FROM users WHERE last_name = 'Smith' AND first_name = 'John';

-- ❌ Doesn't use index
SELECT * FROM users WHERE first_name = 'John';
SELECT * FROM users WHERE age = 30;
```

### Index Types

```sql
-- B-Tree (default) - for =, >, <, BETWEEN
CREATE INDEX idx_salary ON employees(salary);

-- Hash - only for = (very fast)
CREATE INDEX idx_email USING HASH ON users(email);

-- Full-text - for text search
CREATE FULLTEXT INDEX idx_content ON articles(content);
```

**Interview Answer:** *"Indexes speed up reads (O(log n) vs O(n)) but slow writes. Create indexes on WHERE, JOIN, ORDER BY columns with high cardinality. Don't index small tables or frequently updated columns. Composite indexes follow left-prefix rule."*

---

## S4: Clustered vs Non-Clustered Indexes

### Clustered Index

**Physical order of data matches index order. Only ONE per table.**

```
Table with Clustered Index on ID:
┌────┬──────────┬────────┐
│ ID │   Name   │ Salary │
├────┼──────────┼────────┤
│  1 │  Alice   │ 70000  │  ← Row 1
│  2 │  Bob     │ 80000  │  ← Row 2
│  3 │  Charlie │ 90000  │  ← Row 3
└────┴──────────┴────────┘
Data physically sorted by ID
```

### Non-Clustered Index

**Separate structure pointing to data. Multiple allowed.**

```
Non-Clustered Index on Name:
┌──────────┬─────────┐         Table:
│   Name   │ Row Ptr │         ┌────┬──────────┐
├──────────┼─────────┤         │ ID │   Name   │
│  Alice   │ ──────►1│         ├────┼──────────┤
│  Bob     │ ──────►2│         │ 3  │ Charlie  │
│  Charlie │ ──────►3│         │ 1  │ Alice    │
└──────────┴─────────┘         │ 2  │ Bob      │
                               └────┴──────────┘
```

### Comparison

| Aspect | Clustered | Non-Clustered |
|--------|-----------|---------------|
| **Count** | One per table | Multiple |
| **Data Order** | Physical | Logical |
| **Speed** | Faster for ranges | Slower (extra lookup) |
| **Storage** | Data = Index | Separate structure |
| **Example** | Primary Key | Email, Name indexes |

### SQL Examples

```sql
-- Clustered (usually Primary Key)
CREATE TABLE employees (
    id INT PRIMARY KEY,  -- Clustered index automatically
    name VARCHAR(100),
    email VARCHAR(100)
);

-- Non-Clustered
CREATE INDEX idx_email ON employees(email);  -- Non-clustered
CREATE INDEX idx_name ON employees(name);    -- Non-clustered
```

### Query Performance

```sql
-- Clustered index (very fast)
SELECT * FROM employees WHERE id BETWEEN 100 AND 200;
-- Data physically together, sequential read

-- Non-Clustered index (slower)
SELECT * FROM employees WHERE email = 'john@example.com';
-- 1. Lookup in index
-- 2. Follow pointer to data row
```

**Interview Answer:** *"Clustered index determines physical data order (only one per table, usually PK). Non-clustered is separate structure with pointers (multiple allowed). Clustered faster for range queries. Non-clustered requires extra lookup. Primary key automatically gets clustered index."*

---

## S5: Database Normalization

### What is Normalization?

Process of organizing data to reduce redundancy and improve integrity.

### Unnormalized (Problems!)

```sql
-- ❌ Redundant data
orders
┌────┬─────────┬──────────────┬─────────┬──────────┐
│ ID │Customer │ Cust Email   │ Product │ Quantity │
├────┼─────────┼──────────────┼─────────┼──────────┤
│ 1  │ John    │ john@ex.com  │ Laptop  │ 1        │
│ 2  │ John    │ john@ex.com  │ Mouse   │ 2        │
│ 3  │ Jane    │ jane@ex.com  │ Laptop  │ 1        │
└────┴─────────┴──────────────┴─────────┴──────────┘

Problems:
- Customer data repeated
- Update anomaly (change email in multiple places)
- Deletion anomaly (delete order, lose customer)
```

### 1NF (First Normal Form)

**Atomic values, no repeating groups**

```sql
-- ❌ Violates 1NF
┌────┬──────────┬────────────────────┐
│ ID │   Name   │     Phone          │
├────┼──────────┼────────────────────┤
│ 1  │ John     │ 111-222, 333-444   │  ← Multiple values!
└────┴──────────┴────────────────────┘

-- ✅ 1NF
┌────┬──────────┬──────────┐
│ ID │   Name   │  Phone   │
├────┼──────────┼──────────┤
│ 1  │ John     │ 111-222  │
│ 1  │ John     │ 333-444  │
└────┴──────────┴──────────┘
```

### 2NF (Second Normal Form)

**1NF + No partial dependencies (all non-key columns depend on entire primary key)**

```sql
-- ❌ Violates 2NF
order_items (order_id, product_id are composite PK)
┌──────────┬────────────┬──────────────┬──────────┐
│ order_id │ product_id │ product_name │ quantity │
├──────────┼────────────┼──────────────┼──────────┤
│ 1        │ 101        │ Laptop       │ 2        │
│ 2        │ 101        │ Laptop       │ 1        │
└──────────┴────────────┴──────────────┴──────────┘
product_name depends only on product_id (partial dependency!)

-- ✅ 2NF
order_items                products
┌──────────┬────────────┬──────────┐   ┌────────────┬──────────────┐
│ order_id │ product_id │ quantity │   │ product_id │ product_name │
├──────────┼────────────┼──────────┤   ├────────────┼──────────────┤
│ 1        │ 101        │ 2        │   │ 101        │ Laptop       │
│ 2        │ 101        │ 1        │   │ 102        │ Mouse        │
└──────────┴────────────┴──────────┘   └────────────┴──────────────┘
```

### 3NF (Third Normal Form)

**2NF + No transitive dependencies (non-key columns don't depend on other non-key columns)**

```sql
-- ❌ Violates 3NF
employees
┌────┬──────┬────────────┬───────────────┐
│ ID │ Name │ Dept_ID    │ Dept_Location │
├────┼──────┼────────────┼───────────────┤
│ 1  │ John │ 10         │ Building A    │
│ 2  │ Jane │ 10         │ Building A    │
└────┴──────┴────────────┴───────────────┘
Dept_Location depends on Dept_ID (transitive dependency!)

-- ✅ 3NF
employees                    departments
┌────┬──────┬─────────┐     ┌─────────┬───────────────┐
│ ID │ Name │ Dept_ID │     │ Dept_ID │ Dept_Location │
├────┼──────┼─────────┤     ├─────────┼───────────────┤
│ 1  │ John │ 10      │     │ 10      │ Building A    │
│ 2  │ Jane │ 10      │     │ 20      │ Building B    │
└────┴──────┴─────────┘     └─────────┴───────────────┘
```

### BCNF (Boyce-Codd Normal Form)

**3NF + Every determinant is a candidate key**

### Denormalization (When to Violate!)

**For performance in read-heavy systems:**

```sql
-- Normalized (3 JOINs)
SELECT o.id, c.name, p.product_name, oi.quantity
FROM orders o
JOIN customers c ON o.customer_id = c.id
JOIN order_items oi ON o.id = oi.order_id
JOIN products p ON oi.product_id = p.id;

-- Denormalized (1 table, no JOINs)
order_details
┌────┬───────────┬──────────────┬──────────┐
│ ID │ Cust_Name │ Product_Name │ Quantity │
├────┼───────────┼──────────────┼──────────┤
│ 1  │ John      │ Laptop       │ 1        │
└────┴───────────┴──────────────┴──────────┘
```

**When to denormalize:**
- Read-heavy workloads
- Reporting/analytics
- Frequent JOINs are slow
- Acceptable data redundancy

**Interview Answer:** *"Normalization reduces redundancy: 1NF (atomic values), 2NF (no partial dependencies), 3NF (no transitive dependencies), BCNF (every determinant is key). I denormalize for read-heavy systems where JOINs become bottleneck, like reporting tables or caches."*

---

## S6: How to Optimize Slow SQL Queries

### 1. Use EXPLAIN to Analyze

```sql
EXPLAIN SELECT * FROM employees WHERE email = 'john@example.com';

Result shows:
- type: ALL (bad - full table scan) vs ref (good - index)
- rows: 10000 (scanned rows)
- Extra: Using where, Using filesort
```

### 2. Add Indexes

```sql
-- Before (slow)
SELECT * FROM users WHERE email = 'john@example.com';
-- Execution plan: Full table scan (1,000,000 rows)

-- After adding index
CREATE INDEX idx_email ON users(email);
-- Execution plan: Index seek (1 row)
```

### 3. Avoid SELECT *

```sql
-- ❌ Slow (retrieves all columns)
SELECT * FROM employees WHERE dept_id = 10;

-- ✅ Fast (only needed columns)
SELECT id, name, salary FROM employees WHERE dept_id = 10;
```

### 4. Use WHERE Instead of HAVING

```sql
-- ❌ Slow (filters after aggregation)
SELECT dept_id, COUNT(*) 
FROM employees 
GROUP BY dept_id 
HAVING dept_id = 10;

-- ✅ Fast (filters before aggregation)
SELECT dept_id, COUNT(*) 
FROM employees 
WHERE dept_id = 10 
GROUP BY dept_id;
```

### 5. Avoid Functions on Indexed Columns

```sql
-- ❌ Index not used
SELECT * FROM employees WHERE YEAR(hire_date) = 2024;

-- ✅ Index used
SELECT * FROM employees 
WHERE hire_date >= '2024-01-01' AND hire_date < '2025-01-01';
```

### 6. Use EXISTS Instead of IN for Subqueries

```sql
-- ❌ Slow
SELECT * FROM employees 
WHERE dept_id IN (SELECT id FROM departments WHERE location = 'NYC');

-- ✅ Faster
SELECT * FROM employees e
WHERE EXISTS (
    SELECT 1 FROM departments d 
    WHERE d.id = e.dept_id AND d.location = 'NYC'
);
```

### 7. Limit Results

```sql
-- ❌ Returns millions of rows
SELECT * FROM orders ORDER BY order_date DESC;

-- ✅ Pagination
SELECT * FROM orders ORDER BY order_date DESC LIMIT 20 OFFSET 0;
```

### 8. Use UNION ALL Instead of UNION

```sql
-- ❌ Slow (removes duplicates)
SELECT name FROM employees_2023
UNION
SELECT name FROM employees_2024;

-- ✅ Fast (no duplicate check)
SELECT name FROM employees_2023
UNION ALL
SELECT name FROM employees_2024;
```

### 9. Partition Large Tables

```sql
-- Partition by year
CREATE TABLE orders (
    id INT,
    order_date DATE,
    amount DECIMAL
)
PARTITION BY RANGE (YEAR(order_date)) (
    PARTITION p2022 VALUES LESS THAN (2023),
    PARTITION p2023 VALUES LESS THAN (2024),
    PARTITION p2024 VALUES LESS THAN (2025)
);

-- Query only relevant partition
SELECT * FROM orders WHERE order_date >= '2024-01-01';
-- Only scans p2024 partition!
```

### 10. Denormalize for Read Performance

```sql
-- Normalized (3 JOINs - slow)
SELECT o.id, c.name, p.product_name
FROM orders o
JOIN customers c ON o.customer_id = c.id
JOIN order_items oi ON o.id = oi.order_id
JOIN products p ON oi.product_id = p.id;

-- Denormalized (no JOINs - fast)
SELECT id, customer_name, product_name FROM order_details;
```

### Optimization Checklist

```
✅ Add indexes on WHERE, JOIN, ORDER BY columns
✅ Use EXPLAIN to check execution plans
✅ Avoid SELECT *, fetch only needed columns
✅ Use WHERE instead of HAVING when possible
✅ Don't use functions on indexed columns
✅ Use covering indexes
✅ Partition large tables
✅ Use LIMIT for pagination
✅ Avoid N+1 queries (use JOINs)
✅ Cache frequently accessed data
```

**Interview Answer:** *"I optimize by: 1) EXPLAIN to find bottlenecks, 2) Add indexes on WHERE/JOIN columns, 3) Avoid SELECT *, 4) Don't use functions on indexed columns, 5) Use covering indexes, 6) Partition large tables, 7) Denormalize for read-heavy workloads. Always measure before/after with execution plans."*

---

## S7: ACID Properties

### A - Atomicity

**All or nothing - transaction completes fully or not at all**

```sql
BEGIN TRANSACTION;
    UPDATE accounts SET balance = balance - 100 WHERE id = 1;
    UPDATE accounts SET balance = balance + 100 WHERE id = 2;
COMMIT;

-- If second UPDATE fails → both rollback
-- Money not lost or created
```

### C - Consistency

**Database moves from one valid state to another valid state**

```sql
-- Constraint: balance >= 0
BEGIN TRANSACTION;
    UPDATE accounts SET balance = balance - 100 WHERE id = 1;
    -- If this violates constraint (balance < 0)
    -- Transaction rolls back
COMMIT;
```

### I - Isolation

**Concurrent transactions don't interfere with each other**

```sql
-- Transaction 1
BEGIN;
UPDATE accounts SET balance = balance - 100 WHERE id = 1;
-- Not yet committed

-- Transaction 2 (cannot see uncommitted changes)
SELECT balance FROM accounts WHERE id = 1;
-- Returns old balance (isolation!)
```

### D - Durability

**Once committed, changes are permanent (even after crash)**

```sql
BEGIN TRANSACTION;
    INSERT INTO orders VALUES (1, 'Product A', 100);
COMMIT;
-- Saved to disk

-- System crashes and restarts
SELECT * FROM orders WHERE id = 1;
-- Data still there! (durability)
```

### Real-World Example

```sql
-- Banking transaction
BEGIN TRANSACTION;
    -- Atomicity: Both or neither
    UPDATE accounts SET balance = balance - 500 WHERE id = 1;
    UPDATE accounts SET balance = balance + 500 WHERE id = 2;
    
    -- Consistency: Constraints enforced
    -- (balance must be >= 0)
    
    -- Isolation: Other transactions see old values until commit
    
    -- Durability: Once committed, changes permanent
COMMIT;
```

**Interview Answer:** *"ACID ensures reliable transactions: Atomicity (all-or-nothing), Consistency (valid state always), Isolation (concurrent transactions don't interfere), Durability (committed changes permanent). Example: bank transfer must debit and credit together (atomicity), maintain positive balance (consistency), hide uncommitted changes (isolation), survive crashes (durability)."*

---

## S8: Database Transactions and Isolation Levels

### Isolation Problems

**1. Dirty Read**
```sql
-- Transaction A
BEGIN;
UPDATE accounts SET balance = 500 WHERE id = 1;
-- NOT committed yet

-- Transaction B
SELECT balance FROM accounts WHERE id = 1;
-- Sees 500 (dirty read!)

-- Transaction A
ROLLBACK;
-- Transaction B used wrong data!
```

**2. Non-Repeatable Read**
```sql
-- Transaction A
BEGIN;
SELECT balance FROM accounts WHERE id = 1;  -- Returns 1000

-- Transaction B
UPDATE accounts SET balance = 500 WHERE id = 1;
COMMIT;

-- Transaction A
SELECT balance FROM accounts WHERE id = 1;  -- Returns 500 (different!)
```

**3. Phantom Read**
```sql
-- Transaction A
BEGIN;
SELECT COUNT(*) FROM employees WHERE dept = 'Sales';  -- Returns 10

-- Transaction B
INSERT INTO employees (name, dept) VALUES ('John', 'Sales');
COMMIT;

-- Transaction A
SELECT COUNT(*) FROM employees WHERE dept = 'Sales';  -- Returns 11 (phantom!)
```

### Isolation Levels

**1. READ UNCOMMITTED** (Lowest isolation)
```sql
SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;

Problems:
✗ Dirty reads
✗ Non-repeatable reads
✗ Phantom reads

Use case: Reporting where approximate data acceptable
```

**2. READ COMMITTED** (Default in most databases)
```sql
SET TRANSACTION ISOLATION LEVEL READ COMMITTED;

Prevents:
✓ Dirty reads

Problems:
✗ Non-repeatable reads
✗ Phantom reads

Use case: Most applications (good balance)
```

**3. REPEATABLE READ**
```sql
SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;

Prevents:
✓ Dirty reads
✓ Non-repeatable reads

Problems:
✗ Phantom reads

Use case: When consistent reads within transaction needed
```

**4. SERIALIZABLE** (Highest isolation)
```sql
SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;

Prevents:
✓ Dirty reads
✓ Non-repeatable reads
✓ Phantom reads

Problems:
✗ Lowest concurrency (locks)
✗ Slowest

Use case: Financial transactions requiring strict isolation
```

### Comparison

| Level | Dirty Read | Non-Repeatable | Phantom | Performance |
|-------|------------|----------------|---------|-------------|
| READ UNCOMMITTED | ❌ Possible | ❌ Possible | ❌ Possible | ✅ Fastest |
| READ COMMITTED | ✅ Prevented | ❌ Possible | ❌ Possible | ✅ Good |
| REPEATABLE READ | ✅ Prevented | ✅ Prevented | ❌ Possible | ⚠️ Slower |
| SERIALIZABLE | ✅ Prevented | ✅ Prevented | ✅ Prevented | ❌ Slowest |

### Spring Boot Configuration

```java
@Transactional(isolation = Isolation.READ_COMMITTED)  // Default
public void transfer(Long from, Long to, BigDecimal amount) {
    // Transaction logic
}

@Transactional(isolation = Isolation.REPEATABLE_READ)
public BigDecimal calculateBalance(Long accountId) {
    // Consistent reads within transaction
}

@Transactional(isolation = Isolation.SERIALIZABLE)
public void processCriticalTransaction() {
    // Strict isolation for financial operations
}
```

**Interview Answer:** *"Isolation levels control concurrent transaction visibility: READ_UNCOMMITTED (dirty reads possible), READ_COMMITTED (default, no dirty reads), REPEATABLE_READ (consistent reads), SERIALIZABLE (complete isolation, slowest). Higher isolation = more consistency but less concurrency. I use READ_COMMITTED for most cases, SERIALIZABLE only for critical financial operations."*

---

## S9: SQL vs NoSQL Databases

### SQL (Relational)

**Structure:** Fixed schema, tables with rows/columns

```sql
-- Schema required
CREATE TABLE users (
    id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    age INT
);

INSERT INTO users VALUES (1, 'John', 'john@example.com', 30);
```

**Examples:** MySQL, PostgreSQL, Oracle, SQL Server

### NoSQL (Non-Relational)

**Structure:** Flexible schema, various data models

```javascript
// Document (MongoDB)
{
    "_id": "123",
    "name": "John",
    "email": "john@example.com",
    "age": 30,
    "address": {  // Nested!
        "city": "NYC",
        "zip": "10001"
    },
    "hobbies": ["reading", "coding"]  // Array!
}
```

**Types:**
- **Document:** MongoDB, CouchDB
- **Key-Value:** Redis, DynamoDB
- **Column-Family:** Cassandra, HBase
- **Graph:** Neo4j, ArangoDB

### Comparison

| Feature | SQL | NoSQL |
|---------|-----|-------|
| **Schema** | Fixed | Flexible |
| **Scaling** | Vertical (bigger server) | Horizontal (more servers) |
| **ACID** | ✅ Full | ⚠️ Eventual consistency |
| **Joins** | ✅ Powerful | ❌ Limited/None |
| **Query Language** | SQL (standard) | Varies by database |
| **Use Case** | Complex queries, transactions | High scale, flexible data |

### When to Use SQL

```
✅ Complex queries with JOINs
✅ ACID transactions required
✅ Structured data (finance, e-commerce)
✅ Data integrity critical
✅ Well-defined schema

Examples:
- Banking systems
- E-commerce orders
- Inventory management
- Traditional CRM
```

### When to Use NoSQL

```
✅ Flexible/evolving schema
✅ Massive scale (millions of ops/sec)
✅ Horizontal scaling needed
✅ Denormalized data
✅ High write throughput

Examples:
- Social media feeds
- Real-time analytics
- IoT sensor data
- Content management
- Session stores
```

### Real-World Examples

**SQL - E-commerce Order**
```sql
SELECT 
    o.order_id,
    c.name AS customer,
    p.product_name,
    oi.quantity,
    oi.price
FROM orders o
JOIN customers c ON o.customer_id = c.id
JOIN order_items oi ON o.order_id = oi.order_id
JOIN products p ON oi.product_id = p.id
WHERE o.order_date >= '2024-01-01';
```

**NoSQL - Social Media Post**
```javascript
// MongoDB - all data in one document
{
    "post_id": "123",
    "author": {
        "id": "456",
        "name": "John Doe",
        "avatar": "http://..."
    },
    "content": "Hello World!",
    "created_at": ISODate("2024-01-01"),
    "likes": 42,
    "comments": [
        {
            "user": "Jane",
            "text": "Great post!",
            "created_at": ISODate("2024-01-01")
        }
    ]
}
```

### Hybrid Approach (Polyglot Persistence)

```
Application
    ├── PostgreSQL (orders, payments)
    ├── MongoDB (product catalog)
    ├── Redis (session cache)
    └── Elasticsearch (search)
```

**Interview Answer:** *"SQL has fixed schema, ACID transactions, powerful JOINs - good for structured data and complex queries. NoSQL has flexible schema, horizontal scaling, eventual consistency - good for high scale and flexible data. I use SQL for transactional data (orders), NoSQL for flexible/high-scale data (user sessions, logs). Often polyglot persistence using both."*

---

## S10: When Would You Choose MongoDB Over Relational Database?

### Use MongoDB When:

**1. Flexible/Evolving Schema**

```javascript
// Different users can have different fields
{
    "_id": "1",
    "name": "John",
    "email": "john@example.com"
}

{
    "_id": "2",
    "name": "Jane",
    "email": "jane@example.com",
    "phone": "123-456-7890",  // Extra field
    "address": {              // Nested object
        "city": "NYC"
    }
}

// SQL would require:
// - ALTER TABLE (schema change)
// - NULL columns
// - Separate tables with JOINs
```

**2. Denormalized Data**

```javascript
// Everything in one document (no JOINs!)
{
    "order_id": "123",
    "customer": {
        "name": "John",
        "email": "john@example.com"
    },
    "items": [
        {
            "product": "Laptop",
            "price": 1000,
            "quantity": 1
        }
    ],
    "total": 1000
}

// SQL would require 3 tables + JOINs:
// orders, customers, order_items
```

**3. High Write Throughput**

```
MongoDB (horizontal scaling):
Write 1M records/sec
- Shard across 10 servers
- Each handles 100K writes/sec

SQL (vertical scaling):
Write 100K records/sec
- Need bigger/expensive server
```

**4. Rapid Development**

```javascript
// Day 1
db.users.insert({name: "John", email: "john@example.com"})

// Day 2 - add new field (no migration!)
db.users.insert({
    name: "Jane",
    email: "jane@example.com",
    preferences: {theme: "dark"}  // New field
})

// SQL Day 2:
// ALTER TABLE users ADD COLUMN preferences JSON;
```

**5. Hierarchical Data**

```javascript
// Nested categories (natural in MongoDB)
{
    "category": "Electronics",
    "subcategories": [
        {
            "name": "Computers",
            "subcategories": [
                {"name": "Laptops"},
                {"name": "Desktops"}
            ]
        },
        {
            "name": "Phones"
        }
    ]
}

// SQL requires recursive queries or multiple tables
```

### Don't Use MongoDB When:

**❌ Complex JOINs Required**

```sql
-- SQL handles this well
SELECT 
    o.order_id,
    c.name,
    p.product_name,
    s.shipping_status
FROM orders o
JOIN customers c ON o.customer_id = c.id
JOIN products p ON o.product_id = p.id
JOIN shipments s ON o.order_id = s.order_id
WHERE o.order_date >= '2024-01-01';

// MongoDB requires multiple queries or $lookup (slow)
```

**❌ ACID Transactions Critical**

```sql
-- SQL ACID transaction
BEGIN TRANSACTION;
    UPDATE accounts SET balance = balance - 100 WHERE id = 1;
    UPDATE accounts SET balance = balance + 100 WHERE id = 2;
COMMIT;
-- Both succeed or both fail

// MongoDB: ACID added in 4.0 but less mature
```

**❌ Complex Aggregations**

```sql
-- SQL
SELECT 
    dept,
    AVG(salary),
    MAX(salary),
    COUNT(*)
FROM employees
GROUP BY dept
HAVING AVG(salary) > 50000;

// MongoDB aggregation possible but more verbose
```

### Real-World Use Cases

**✅ Good for MongoDB:**
```
- Content Management Systems (flexible content types)
- Product Catalogs (varying attributes)
- User Profiles (different user types)
- Real-time Analytics (high write throughput)
- IoT Data (massive scale, simple queries)
- Session Stores (fast key-value access)
- Mobile Apps (flexible schema for rapid iteration)
```

**❌ Not Good for MongoDB:**
```
- Banking Systems (ACID critical)
- Financial Trading (complex transactions)
- Inventory Management (relational integrity)
- Complex Reporting (many JOINs)
```

### Example: E-commerce Product Catalog

**MongoDB (Better choice!):**
```javascript
{
    "product_id": "123",
    "name": "Laptop",
    "category": "Electronics",
    "specs": {
        "cpu": "Intel i7",
        "ram": "16GB",
        "storage": "512GB SSD"
    },
    "reviews": [
        {"user": "John", "rating": 5, "text": "Great!"}
    ],
    "related_products": ["124", "125"]
}

// Different products, different specs (no schema change needed!)
{
    "product_id": "200",
    "name": "T-Shirt",
    "category": "Clothing",
    "specs": {
        "size": "M",
        "color": "Blue",
        "material": "Cotton"
    }
}
```

**SQL (Would need):**
```sql
-- Multiple tables
products (id, name, category)
product_specs (product_id, spec_name, spec_value)
reviews (id, product_id, user, rating, text)
related_products (product_id, related_id)

-- Requires JOINs to get all data
```

**Interview Answer:** *"I choose MongoDB for: flexible schemas (different user types), high write throughput (IoT, analytics), denormalized data (avoid JOINs), rapid development (no migrations), hierarchical data. Examples: product catalogs with varying attributes, user profiles, content management. I avoid MongoDB for: complex JOINs, strict ACID requirements (banking), complex aggregations with GROUP BY/HAVING."*

---

## S11: How Do You Design Database Schemas for Microservices?

### Core Principle: Database Per Service

```
Microservices Architecture:

┌─────────────────┐       ┌─────────────────┐
│ Order Service   │       │ Inventory Svc   │
│ ┌─────────────┐ │       │ ┌─────────────┐ │
│ │  Orders DB  │ │       │ │Inventory DB │ │
│ └─────────────┘ │       │ └─────────────┘ │
└─────────────────┘       └─────────────────┘

❌ DON'T: Share database across services
✅ DO: Each service owns its database
```

### Why Database Per Service?

```
✅ Independent scaling
✅ Technology choice (SQL/NoSQL)
✅ Independent deployment
✅ Failure isolation
✅ Clear boundaries
```

### Design Patterns

**1. Database Per Service**

```
Order Service → order_db (PostgreSQL)
User Service → user_db (PostgreSQL)
Product Service → product_db (MongoDB)
Inventory Service → inventory_db (MySQL)
```

**2. Shared Database (Anti-pattern!)**

```
❌ DON'T DO THIS:
Order Service ─┐
User Service  ─┼─→ shared_db
Product Service ─┘

Problems:
- Tight coupling
- Schema changes affect all services
- No independent scaling
- Single point of failure
```

### Handling Data Joins

**Problem:** Can't JOIN across databases!

```sql
-- Monolith (easy)
SELECT 
    o.order_id,
    u.name AS customer,
    p.product_name
FROM orders o
JOIN users u ON o.user_id = u.id
JOIN products p ON o.product_id = p.id;
```

**Solution 1: API Composition**

```java
// Order Service
@GetMapping("/orders/{id}")
public OrderDTO getOrder(@PathVariable Long id) {
    Order order = orderRepository.findById(id);
    
    // Call User Service
    User user = userServiceClient.getUser(order.getUserId());
    
    // Call Product Service
    Product product = productServiceClient.getProduct(order.getProductId());
    
    // Compose response
    OrderDTO dto = new OrderDTO();
    dto.setOrderId(order.getId());
    dto.setCustomerName(user.getName());
    dto.setProductName(product.getName());
    
    return dto;
}
```

**Solution 2: Data Replication / Denormalization**

```java
// Order Service database
orders
┌────────┬─────────┬────────────┬──────────────┬────────────┐
│ id     │ user_id │ user_name  │ product_id   │product_name│
├────────┼─────────┼────────────┼──────────────┼────────────┤
│ 1      │ 101     │ John       │ 201          │ Laptop     │
└────────┴─────────┴────────────┴──────────────┴────────────┘

// Denormalized: Store user_name and product_name locally
// Update via events when user/product changes
```

**Solution 3: CQRS (Command Query Responsibility Segregation)**

```
Write Side (Commands):
Order Service → order_db
User Service → user_db
Product Service → product_db

Read Side (Queries):
Read Model Service → denormalized_db
    ↑
    └─ Listens to events from all services
    └─ Builds optimized read views
```

### Handling Transactions

**Problem:** Distributed transactions are complex!

**Solution: Saga Pattern**

```java
// Choreography-based Saga
@Service
public class OrderService {
    
    @Transactional
    public void createOrder(Order order) {
        // 1. Save order (local transaction)
        orderRepository.save(order);
        
        // 2. Publish event
        eventPublisher.publish(new OrderCreatedEvent(order));
    }
}

@Service
public class InventoryService {
    
    @KafkaListener(topics = "order-created")
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Reduce inventory (separate transaction)
        inventoryRepository.reduceStock(event.getProductId(), event.getQuantity());
        
        // Publish success/failure event
        if (success) {
            eventPublisher.publish(new InventoryReservedEvent(event.getOrderId()));
        } else {
            eventPublisher.publish(new InventoryReservationFailedEvent(event.getOrderId()));
        }
    }
}

// Order Service compensates on failure
@KafkaListener(topics = "inventory-reservation-failed")
@Transactional
public void handleInventoryFailed(InventoryReservationFailedEvent event) {
    Order order = orderRepository.findById(event.getOrderId());
    order.setStatus(OrderStatus.CANCELLED);  // Compensating action
    orderRepository.save(order);
}
```

### Schema Design Examples

**Order Service (PostgreSQL)**

```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(100),  -- Denormalized
    total_amount DECIMAL(10,2),
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200),  -- Denormalized
    quantity INT,
    price DECIMAL(10,2)
);
```

**Product Catalog Service (MongoDB)**

```javascript
{
    "_id": "123",
    "name": "Laptop",
    "category": "Electronics",
    "price": 999.99,
    "specs": {
        "cpu": "Intel i7",
        "ram": "16GB"
    },
    "inventory_service_id": "INV-123"  // Reference to Inventory Service
}
```

**Inventory Service (MySQL)**

```sql
CREATE TABLE inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    warehouse_location VARCHAR(100),
    last_updated TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_product ON inventory(product_id);
```

### Event-Driven Updates

```java
// When user updates their name
@Service
public class UserService {
    
    @Transactional
    public void updateName(Long userId, String newName) {
        User user = userRepository.findById(userId);
        user.setName(newName);
        userRepository.save(user);
        
        // Publish event
        eventPublisher.publish(new UserNameChangedEvent(userId, newName));
    }
}

// Order Service updates denormalized data
@Service
public class OrderService {
    
    @KafkaListener(topics = "user-name-changed")
    @Transactional
    public void handleUserNameChanged(UserNameChangedEvent event) {
        // Update denormalized user_name in orders
        orderRepository.updateUserName(event.getUserId(), event.getNewName());
    }
}
```

### Data Consistency Strategies

**1. Eventual Consistency**
```
- Accept temporary inconsistency
- Data converges to consistent state
- Use for non-critical data
```

**2. Strong Consistency (Saga)**
```
- Orchestrate transactions across services
- Compensating transactions on failure
- Use for critical operations (payments)
```

### Best Practices

```
✅ Database per service (clear boundaries)
✅ Denormalize data to avoid distributed JOINs
✅ Use events for cross-service communication
✅ Implement Saga pattern for distributed transactions
✅ CQRS for complex queries
✅ Choose right database per service (SQL/NoSQL)
✅ Monitor data consistency

❌ Don't share databases
❌ Don't use distributed transactions (2PC)
❌ Don't over-normalize in microservices
❌ Don't make synchronous calls for every query
```

### Complete Example: E-commerce

```
Order Service (PostgreSQL)
├── orders (id, user_id, user_name*, total, status)
└── order_items (id, order_id, product_id, product_name*, qty, price)

User Service (PostgreSQL)
└── users (id, name, email, address)

Product Service (MongoDB)
└── products {_id, name, description, price, category}

Inventory Service (MySQL)
└── inventory (id, product_id, quantity, warehouse)

Payment Service (PostgreSQL)
└── payments (id, order_id, amount, status, gateway_ref)

Communication: Kafka for events
Read Model: Elasticsearch for search
```

**Interview Answer:** *"I design database-per-service for independence and isolation. Each service owns its data schema and storage technology. For data joins, I use API composition for real-time or denormalization for performance. For transactions, Saga pattern with compensating actions. For queries, CQRS with read-optimized models. Example: Order Service stores denormalized user_name, updates via User Service events. This trades strong consistency for scalability and resilience."*

---

## 🎯 Summary - Quick Interview Answers

### Hibernate/JPA

1. **ORM:** Maps objects to tables, Hibernate provides auto SQL, HQL, caching, lazy loading
2. **Session:** Light-weight, per-transaction; SessionFactory: heavy, singleton, thread-safe
3. **Mappings:** OneToOne, OneToMany (Many owns), ManyToMany, Inheritance (SINGLE_TABLE/JOINED)
4. **Lazy:** Load on-demand; Eager: immediate; Use @Transactional or JOIN FETCH
5. **N+1:** Use JOIN FETCH (1 query), @BatchSize, or DTOs
6. **JPA vs Hibernate:** JPA = spec, Hibernate = implementation with extras
7. **Transactions:** @Transactional for ACID; Watch self-invocation pitfall
8. **JTA:** Distributed transactions (2PC); Prefer Saga/Outbox patterns

### Database/SQL

1. **Nth Salary:** DENSE_RANK() best, handles ties
2. **Joins:** INNER (intersection), LEFT (all left), CROSS (cartesian)
3. **Indexes:** Speed reads (log n), slow writes; Use on WHERE/JOIN columns
4. **Clustered:** Physical order, one per table; Non-clustered: separate, multiple
5. **Normalization:** 1NF→3NF reduces redundancy; Denormalize for read performance
6. **Optimization:** EXPLAIN, indexes, avoid SELECT *, no functions on indexed columns
7. **ACID:** Atomicity, Consistency, Isolation, Durability
8. **Isolation:** READ_COMMITTED default, SERIALIZABLE strictest
9. **SQL vs NoSQL:** SQL = structured/ACID, NoSQL = flexible/scalable
10. **MongoDB:** Flexible schema, high write throughput, horizontal scaling
11. **Microservices:** Database per service, Saga pattern, denormalize, events

---

**File Complete!** All 19 questions answered with code examples, diagrams, and interview talking points. Good luck with your interviews! 🚀
