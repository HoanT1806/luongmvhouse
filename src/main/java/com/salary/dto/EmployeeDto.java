package com.salary.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmployeeDto {
    private Long id;


    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String email;

    private String department;
    private String position;
    
    @NotBlank(message = "CCCD không được để trống")
    private String identityCard;
    
    @jakarta.validation.constraints.NotNull(message = "Lương cơ bản không được để trống")
    @jakarta.validation.constraints.Min(value = 0, message = "Lương cơ bản không hợp lệ")
    private java.math.BigDecimal basicSalary = java.math.BigDecimal.ZERO;
    
    private boolean active = true;
}
