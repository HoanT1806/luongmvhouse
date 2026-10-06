package com.salary.service;

import com.salary.dto.SalaryRecordDto;
import com.salary.model.Employee;
import com.salary.model.SalaryRecord;
import com.salary.model.SalaryRecord.SendStatus;
import com.salary.repository.SalaryRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalaryService {

    private final SalaryRecordRepository salaryRecordRepository;
    private final EmployeeService employeeService;
    private final EmailService emailService;

    public List<SalaryRecord> findByMonthYear(Integer month, Integer year) {
        return salaryRecordRepository.findBySalaryMonthAndSalaryYear(month, year);
    }

    public SalaryRecord findById(Long id) {
        return salaryRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bản ghi lương ID: " + id));
    }

    @Transactional
    public SalaryRecord save(SalaryRecordDto dto) {
        Employee employee = employeeService.findById(dto.getEmployeeId());

        SalaryRecord record;
        if (dto.getId() != null) {
            record = findById(dto.getId());
        } else {
            if (salaryRecordRepository.existsByEmployeeIdAndSalaryMonthAndSalaryYear(
                    dto.getEmployeeId(), dto.getSalaryMonth(), dto.getSalaryYear())) {
                throw new RuntimeException("Đã có phiếu lương tháng " + dto.getSalaryMonth() + "/" + dto.getSalaryYear()
                        + " cho nhân viên " + employee.getFullName());
            }
            record = new SalaryRecord();
        }

        record.setEmployee(employee);
        record.setSalaryMonth(dto.getSalaryMonth());
        record.setSalaryYear(dto.getSalaryYear());
        record.setBasicSalary(dto.getBasicSalary());
        record.setAllowance(dto.getAllowance());
        record.setBonus(dto.getBonus());
        record.setOvertime(dto.getOvertime());
        record.setSocialInsurance(dto.getSocialInsurance());
        record.setHealthInsurance(dto.getHealthInsurance());
        record.setUnemploymentInsurance(dto.getUnemploymentInsurance());
        record.setIncomeTax(dto.getIncomeTax());
        record.setOtherDeduction(dto.getOtherDeduction());
        record.calculateTotals();

        return salaryRecordRepository.save(record);
    }

    @Transactional
    public void sendPayslip(Long recordId) {
        SalaryRecord record = findById(recordId);
        record.setSendStatus(SendStatus.SENDING);
        salaryRecordRepository.save(record);

        try {
            emailService.sendPayslipEmail(record);
            record.setSendStatus(SendStatus.SENT);
            record.setSentAt(LocalDateTime.now());
            record.setErrorMessage(null);
            log.info("✅ Gửi phiếu lương thành công: {} - T{}/{}", 
                    record.getEmployee().getFullName(), record.getSalaryMonth(), record.getSalaryYear());
        } catch (Exception e) {
            record.setSendStatus(SendStatus.FAILED);
            record.setErrorMessage(e.getMessage());
            log.error("❌ Gửi phiếu lương thất bại: {} - {}", record.getEmployee().getEmail(), e.getMessage());
        }
        salaryRecordRepository.save(record);
    }

    /**
     * Gửi hàng loạt cho tất cả nhân viên trong tháng
     */
    @Transactional
    public void sendAllPayslips(Integer month, Integer year) {
        List<SalaryRecord> records = salaryRecordRepository.findBySalaryMonthAndSalaryYearAndSendStatus(
                month, year, SendStatus.PENDING);
        log.info("📨 Bắt đầu gửi {} phiếu lương tháng {}/{}", records.size(), month, year);

        for (SalaryRecord record : records) {
            try {
                sendPayslip(record.getId());
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("✅ Hoàn thành gửi phiếu lương tháng {}/{}", month, year);
    }

    /**
     * Gửi lại các phiếu bị lỗi
     */
    @Transactional
    public void retrySendFailed(Integer month, Integer year) {
        List<SalaryRecord> failedRecords = salaryRecordRepository.findBySalaryMonthAndSalaryYearAndSendStatus(
                month, year, SendStatus.FAILED);

        failedRecords.forEach(r -> {
            r.setSendStatus(SendStatus.PENDING);
            salaryRecordRepository.save(r);
        });

        sendAllPayslips(month, year);
    }

    public long countSent(Integer month, Integer year) {
        return salaryRecordRepository.countSentBySalaryMonthAndSalaryYear(month, year);
    }

    public long countTotal(Integer month, Integer year) {
        return salaryRecordRepository.countBySalaryMonthAndSalaryYear(month, year);
    }

    public void delete(Long id) {
        salaryRecordRepository.deleteById(id);
    }
}
