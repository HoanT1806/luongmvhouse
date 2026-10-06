package com.salary.service;

import com.salary.dto.EmployeeDto;
import com.salary.model.Employee;
import com.salary.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public List<Employee> findAll() {
        return employeeRepository.findByActiveTrue();
    }

    public List<Employee> search(String keyword) {
        if (keyword == null || keyword.isBlank()) return findAll();
        return employeeRepository.searchByKeyword(keyword);
    }

    public Employee findById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên ID: " + id));
    }

    @Transactional
    public Employee save(EmployeeDto dto) {
        if (dto.getId() == null) {
            // Thêm mới

            if (employeeRepository.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email đã được sử dụng: " + dto.getEmail());
            }
            Employee employee = Employee.builder()

                    .fullName(dto.getFullName())
                    .email(dto.getEmail())
                    .department(dto.getDepartment())
                    .position(dto.getPosition())
                    .identityCard(dto.getIdentityCard())
                    .active(true)
                    .build();
            return employeeRepository.save(employee);
        } else {
            // Cập nhật
            Employee employee = findById(dto.getId());
            employee.setFullName(dto.getFullName());
            employee.setEmail(dto.getEmail());
            employee.setDepartment(dto.getDepartment());
            employee.setPosition(dto.getPosition());
            employee.setIdentityCard(dto.getIdentityCard());
            employee.setActive(dto.isActive());
            return employeeRepository.save(employee);
        }
    }

    @Transactional
    public void delete(Long id) {
        Employee employee = findById(id);
        employee.setActive(false); // Soft delete
        employeeRepository.save(employee);
    }

    public long countAll() {
        return employeeRepository.findByActiveTrue().size();
    }
}
