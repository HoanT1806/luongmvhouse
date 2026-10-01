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
    
    private boolean active = true;
}
