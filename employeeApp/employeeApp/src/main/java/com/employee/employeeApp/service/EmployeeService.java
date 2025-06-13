package com.employee.employeeApp.service;

import com.employee.employeeApp.dto.EmployeeDTO;
import com.employee.employeeApp.entity.Employee;
import com.employee.employeeApp.mapper.EmployeeMapper;
import com.employee.employeeApp.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public List<EmployeeDTO> getAllEmployees() {
        return repository.findAll().stream().map(EmployeeMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Employee createEmployee(Employee employee){
        return repository.save(employee);
    }

    public EmployeeDTO getEmployeeById(Long id) {
        Employee emp = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return EmployeeMapper.toDTO(emp);
    }
//    old
//    public Employee getEmployeeById(Long id){
//        return repository.findById(id)
//                .orElseThrow(()->new RuntimeException("Employee not found"));
//    }

    public Employee updateEmployee(Long id, Employee updateEmp){
        Employee emp = repository.findById(id)
                .orElseThrow(()->new RuntimeException("Emp not found"));

        emp.setFirstName(updateEmp.getFirstName());
        emp.setLastName(updateEmp.getLastName());
        emp.setEmail(updateEmp.getEmail());
        emp.setJobTitle(emp.getJobTitle());
        return repository.save(emp);
    }

    public void deleteEmployee(Long id){
        repository.deleteById(id);
    }

    public List<Employee> getEmpByJobTitle(String jobTitle){
       return repository.findByJobTitle(jobTitle);
    }

    public List<Employee> getByDepartMentNative(String department){
        return repository.getByDepartMentNative(department);
    }

    public Page<Employee> getAllEmployeesPaged(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findAll(pageable);
    }

    public Page<Employee> searchByFirstName(int page, int size, String sortBy, String sortDir, String name) {
      //sorting
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return repository.findByFirstNameContainingIgnoreCase(name, pageable);
    }

    public List<Employee> searchByLastName(String name) {
        return repository.findByLastNameContainingIgnoreCase(name);
    }
}
