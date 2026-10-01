package com.salary.repository;

import com.salary.model.SalaryRecord;
import com.salary.model.SalaryRecord.SendStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, Long> {

    Optional<SalaryRecord> findByEmployeeIdAndSalaryMonthAndSalaryYear(Long employeeId, Integer salaryMonth, Integer salaryYear);

    List<SalaryRecord> findBySalaryMonthAndSalaryYear(Integer salaryMonth, Integer salaryYear);

    List<SalaryRecord> findBySalaryMonthAndSalaryYearAndSendStatus(Integer salaryMonth, Integer salaryYear, SendStatus status);

    List<SalaryRecord> findByEmployeeId(Long employeeId);

    @Query("SELECT sr FROM SalaryRecord sr WHERE sr.sendStatus = :status ORDER BY sr.createdAt DESC")
    List<SalaryRecord> findBySendStatus(SendStatus status);

    @Query("SELECT COUNT(sr) FROM SalaryRecord sr WHERE sr.salaryMonth = :salaryMonth AND sr.salaryYear = :salaryYear AND sr.sendStatus = 'SENT'")
    long countSentBySalaryMonthAndSalaryYear(Integer salaryMonth, Integer salaryYear);

    @Query("SELECT COUNT(sr) FROM SalaryRecord sr WHERE sr.salaryMonth = :salaryMonth AND sr.salaryYear = :salaryYear")
    long countBySalaryMonthAndSalaryYear(Integer salaryMonth, Integer salaryYear);

    boolean existsByEmployeeIdAndSalaryMonthAndSalaryYear(Long employeeId, Integer salaryMonth, Integer salaryYear);
}
