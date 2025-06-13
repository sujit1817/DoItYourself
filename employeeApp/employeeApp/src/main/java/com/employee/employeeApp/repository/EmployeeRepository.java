package com.employee.employeeApp.repository;

import com.employee.employeeApp.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
//    A native query means you're writing a raw SQL query — the exact SQL you'd write
//    directly in the database (like in SSMS for SQL Server or MySQL Workbench)
//    — instead of using JPQL (Java Persistence Query Language), which is object-based.

    // By convention
    List<Employee> findByJobTitle(String jobTitle);

    @Query(value = "select * from Employee where department = :dept", nativeQuery = true)
    public List<Employee> getByDepartMentNative(@Param("dept") String department);


//    Spring Data JPA auto-creates the query:
//    Containing → partial match (LIKE %...%)
//    IgnoreCase → case-insensitive
    Page<Employee> findByFirstNameContainingIgnoreCase(String name, Pageable pageable);

    List<Employee> findByLastNameContainingIgnoreCase(String lastName);
}
