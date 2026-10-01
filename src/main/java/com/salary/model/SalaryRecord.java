package com.salary.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "salary_records",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "month", "year"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull(message = "Tháng không được để trống")
    @Min(value = 1, message = "Tháng phải từ 1-12")
    @Column(name = "salary_month")
    private Integer salaryMonth;

    @NotNull(message = "Năm không được để trống")
    @Column(name = "salary_year")
    private Integer salaryYear;

    // === Các khoản thu nhập ===
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal basicSalary = BigDecimal.ZERO;       // Lương cơ bản

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal allowance = BigDecimal.ZERO;          // Phụ cấp

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal bonus = BigDecimal.ZERO;              // Thưởng

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal overtime = BigDecimal.ZERO;           // Làm thêm giờ

    // === Các khoản khấu trừ ===
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal socialInsurance = BigDecimal.ZERO;    // BHXH (8%)

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal healthInsurance = BigDecimal.ZERO;    // BHYT (1.5%)

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal unemploymentInsurance = BigDecimal.ZERO; // BHTN (1%)

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal incomeTax = BigDecimal.ZERO;          // Thuế TNCN

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal otherDeduction = BigDecimal.ZERO;     // Khấu trừ khác

    // === Tổng kết ===
    @Column(precision = 15, scale = 2)
    private BigDecimal grossSalary;                          // Tổng thu nhập

    @Column(precision = 15, scale = 2)
    private BigDecimal totalDeduction;                       // Tổng khấu trừ

    @Column(precision = 15, scale = 2)
    private BigDecimal netSalary;                            // Lương thực nhận

    // === Trạng thái gửi email ===
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SendStatus sendStatus = SendStatus.PENDING;

    private LocalDateTime sentAt;
    private String errorMessage;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateTotals();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateTotals();
    }

    /**
     * Tự động tính toán lương khi save
     */
    public void calculateTotals() {
        BigDecimal base = basicSalary != null ? basicSalary : BigDecimal.ZERO;
        this.grossSalary = base
                .add(allowance != null ? allowance : BigDecimal.ZERO)
                .add(bonus != null ? bonus : BigDecimal.ZERO)
                .add(overtime != null ? overtime : BigDecimal.ZERO);

        // Tự động tính bảo hiểm nếu chưa có
        if (socialInsurance == null || socialInsurance.compareTo(BigDecimal.ZERO) == 0) {
            socialInsurance = base.multiply(new BigDecimal("0.08"));
        }
        if (healthInsurance == null || healthInsurance.compareTo(BigDecimal.ZERO) == 0) {
            healthInsurance = base.multiply(new BigDecimal("0.015"));
        }
        if (unemploymentInsurance == null || unemploymentInsurance.compareTo(BigDecimal.ZERO) == 0) {
            unemploymentInsurance = base.multiply(new BigDecimal("0.01"));
        }

        this.totalDeduction = socialInsurance
                .add(healthInsurance)
                .add(unemploymentInsurance)
                .add(incomeTax != null ? incomeTax : BigDecimal.ZERO)
                .add(otherDeduction != null ? otherDeduction : BigDecimal.ZERO);

        this.netSalary = grossSalary.subtract(totalDeduction);
    }

    public enum SendStatus {
        PENDING,    // Chưa gửi
        SENT,       // Đã gửi thành công
        FAILED,     // Gửi thất bại
        SENDING     // Đang gửi
    }
}
