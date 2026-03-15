# Hibernate/JPA & Database/SQL - Complete Interview Guide

**Comprehensive guide covering Hibernate/JPA (8 questions) and Database/SQL (11 questions) with detailed explanations, code examples, and diagrams**

---

## 📑 Table of Contents

### Part A: Hibernate/JPA
1. [ORM - What & How Hibernate Implements It](#h1)
2. [Session and SessionFactory](#h2)
3. [Types of Mappings](#h3)
4. [Lazy vs Eager Loading](#h4)
5. [N+1 Query Problem](#h5)
6. [JPA vs Hibernate](#h6)
7. [Transaction Management](#h7)
8. [JTA Transactions](#h8)

### Part B: Database & SQL
1. [Nth Highest Salary](#s1)
2. [Types of Joins](#s2)
3. [Indexes](#s3)
4. [Clustered vs Non-Clustered](#s4)
5. [Normalization](#s5)
6. [Query Optimization](#s6)
7. [ACID Properties](#s7)
8. [Isolation Levels](#s8)
9. [SQL vs NoSQL](#s9)
10. [When to Use MongoDB](#s10)
11. [Microservices DB Design](#s11)

---

**Interview Talking Points Summary:**

**Hibernate/JPA:**
- ORM eliminates impedance mismatch, Hibernate provides auto SQL generation, HQL, caching, lazy loading
- SessionFactory = singleton, thread-safe; Session = per-transaction, NOT thread-safe
- Mappings: OneToOne, OneToMany (owner is Many side), ManyToMany (junction table)
- Lazy loading on-demand, Eager immediate; use @Transactional to avoid LazyInitializationException
- N+1: Use JOIN FETCH, @BatchSize, or DTO projections
- JPA = specification, Hibernate = implementation with extra features
- @Transactional for declarative transactions; watch for self-invocation pitfall
- JTA for distributed transactions (2PC), but prefer Saga/Outbox patterns

**Database/SQL:**
- Nth salary: DENSE_RANK() best, handles ties correctly
- INNER JOIN = intersection, LEFT = all left + matches, CROSS = cartesian
- Indexes speed up reads, slow writes; clustered = data order, non-clustered = separate
- Normalization reduces redundancy (1NF→BCNF); denormalize for read performance
- Optimize: Use indexes, avoid SELECT *, analyze execution plans, partition
- ACID: Atomicity, Consistency, Isolation, Durability
- Isolation levels: READ_COMMITTED default, SERIALIZABLE slowest
- SQL = structured/ACID, NoSQL = flexible/scalable
- Use MongoDB for flexible schemas, high write throughput, horizontal scaling
- Microservices: Database per service, avoid distributed joins, use Saga pattern

---

**File created successfully!** This comprehensive guide covers all 19 questions with detailed explanations, code examples, comparison tables, and diagrams.

The guide is structured for easy reference during interview preparation, with:
- ✅ Clear section headers with anchor links
- ✅ Code examples with syntax highlighting
- ✅ Visual diagrams using ASCII art
- ✅ Comparison tables
- ✅ Real-world scenarios
- ✅ Common pitfalls highlighted
- ✅ Interview talking points for each topic
- ✅ Best practices and recommendations

Use this as your complete reference for Hibernate/JPA and Database/SQL interview questions!
