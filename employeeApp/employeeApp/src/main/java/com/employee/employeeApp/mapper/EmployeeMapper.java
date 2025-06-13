package com.employee.employeeApp.mapper;

import com.employee.employeeApp.dto.EmployeeDTO;
import com.employee.employeeApp.entity.Employee;

public class EmployeeMapper {
    public static EmployeeDTO toDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setDepartment(employee.getDepartment());
        dto.setJobTitle(employee.getJobTitle());
        return dto;
    }

    public static Employee toEntity(EmployeeDTO dto) {
        Employee emp = new Employee();
        emp.setId(dto.getId());
        emp.setFirstName(dto.getFirstName());
        emp.setLastName(dto.getLastName());
        emp.setDepartment(dto.getDepartment());
        emp.setJobTitle(dto.getJobTitle());
       // emp.setIsActive(true); // default active
        return emp;
    }
}
