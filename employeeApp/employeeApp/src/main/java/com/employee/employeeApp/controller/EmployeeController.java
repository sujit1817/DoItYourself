package com.employee.employeeApp.controller;

import com.employee.employeeApp.dto.EmployeeDTO;
import com.employee.employeeApp.entity.Employee;
import com.employee.employeeApp.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
//@CrossOrigin(origins = "http://localhost:3000")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<EmployeeDTO> getAllEmployees(){
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable Long id){
        return  ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @PostMapping
    public Employee createEmployee(@RequestBody Employee employee){
        return employeeService.createEmployee(employee);
    }

    @PutMapping("/{id}")
    public Employee updateEmployee(@PathVariable Long id, @RequestBody Employee employee){
        return employeeService.updateEmployee(id, employee);
    }

    @DeleteMapping("/{id}")
    public String deleteEmployee(@PathVariable Long id){
        employeeService.deleteEmployee(id);
        return "Deleted";
    }

    @GetMapping("/department/{department}")
    public List<Employee> getEmployeeByDept(@PathVariable String department) {
        return employeeService.getByDepartMentNative(department);
    }

    //@RequestParam is used to extract query parameters from the URL in a GET request.
    // GET /by-department?department=IT
    // by-department is your endpoint.
    // department=IT is a query parameter.
    // Spring extracts IT from the URL and passes it to your method using @RequestParam.

//    GET http://localhost:8080/employees/by-jobtitle?jobTitle=Manager

    @GetMapping("/by-jobtitle")
    public List<Employee> getByJobTitle(@RequestParam String jobTitle) {
        return employeeService.getEmpByJobTitle(jobTitle);
    }

    @GetMapping("/paged")
    public Page<Employee> getAllEmployeesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        return employeeService.getAllEmployeesPaged(page, size, sortBy, sortDir);
    }

    //http://localhost:8080/api/employee/search?name=s&page=0&size=2&sortBy=firstName&sortDir=asc trial api page 1
    //page=0 means first page
    @GetMapping("/search")
    public Page<Employee> searchByFirstName(  @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "5") int size,
                                              @RequestParam(defaultValue = "id") String sortBy,
                                              @RequestParam(defaultValue = "asc") String sortDir,
                                              @RequestParam String name) {
        return employeeService.searchByFirstName(page, size, sortBy, sortDir, name);
    }

    @GetMapping("/searchLast")
    public List<Employee> searchByLastName(@RequestParam String name) {
        return employeeService.searchByLastName(name);
    }
}
