package com.salary.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Tự động gửi phiếu lương theo lịch định sẵn
 * Mặc định: ngày 25 hàng tháng lúc 08:00 sáng
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PayslipScheduler {

    private final SalaryService salaryService;

    @Value("${app.scheduler.enabled:false}")
    private boolean schedulerEnabled;

    /**
     * Cron: 0 0 8 25 * ? = lúc 08:00 ngày 25 mỗi tháng
     */
    @Scheduled(cron = "${app.scheduler.send-payslip-cron:0 0 8 25 * ?}")
    public void scheduledSendPayslips() {
        if (!schedulerEnabled) {
            log.debug("⏸ Scheduler đang tắt, bỏ qua lần chạy tự động");
            return;
        }

        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        log.info("⏰ [Scheduler] Bắt đầu gửi phiếu lương tháng {}/{} tự động", month, year);
        try {
            salaryService.sendAllPayslips(month, year);
            log.info("✅ [Scheduler] Hoàn thành gửi phiếu lương tháng {}/{}", month, year);
        } catch (Exception e) {
            log.error("❌ [Scheduler] Lỗi khi gửi phiếu lương: {}", e.getMessage(), e);
        }
    }
}
