package com.salary.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalaryRecordDto {
    private Long id;

    @NotNull(message = "Nhân viên không được để trống")
    private Long employeeId;

    @NotNull(message = "Tháng không được để trống")
    @Min(value = 1, message = "Tháng từ 1-12")
    private Integer salaryMonth;

    @NotNull(message = "Năm không được để trống")
    private Integer salaryYear;

    private BigDecimal basicSalary = BigDecimal.ZERO;
    private BigDecimal allowance = BigDecimal.ZERO;
    private BigDecimal bonus = BigDecimal.ZERO;
    private BigDecimal overtime = BigDecimal.ZERO;

    private BigDecimal socialInsurance = BigDecimal.ZERO;
    private BigDecimal healthInsurance = BigDecimal.ZERO;
    private BigDecimal unemploymentInsurance = BigDecimal.ZERO;
    private BigDecimal incomeTax = BigDecimal.ZERO;
    private BigDecimal otherDeduction = BigDecimal.ZERO;
}
