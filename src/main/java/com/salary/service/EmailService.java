package com.salary.service;

import com.salary.model.Employee;
import com.salary.model.SalaryRecord;
import com.salary.util.PdfGenerator;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final PdfGenerator pdfGenerator;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.company.name}")
    private String companyName;

    /**
     * Gửi phiếu lương PDF qua email cho nhân viên
     */
    public void sendPayslipEmail(SalaryRecord record) throws Exception {
        Employee employee = record.getEmployee();
        byte[] pdfBytes = pdfGenerator.generatePayslipPdf(record);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail, companyName);
        helper.setTo(employee.getEmail());
        helper.setSubject(buildSubject(record));
        helper.setText(buildHtmlBody(record), true);

        // Đính kèm file PDF
        String fileName = String.format("PhieuLuong_T%02d_%d.pdf",
                record.getSalaryMonth(), record.getSalaryYear());
        helper.addAttachment(fileName, new ByteArrayResource(pdfBytes), "application/pdf");

        mailSender.send(message);
        log.info("✅ Đã gửi phiếu lương cho {} - {} ({})", employee.getFullName(), employee.getEmail(), fileName);
    }

    private String buildSubject(SalaryRecord record) {
        return String.format("[%s] Phiếu lương tháng %d/%d - %s",
                companyName,
                record.getSalaryMonth(),
                record.getSalaryYear(),
                record.getEmployee().getFullName());
    }

    private String buildHtmlBody(SalaryRecord record) {
        Employee emp = record.getEmployee();
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 30px auto; background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                        .header { background: linear-gradient(135deg, #0d6efd, #6610f2); padding: 30px; text-align: center; }
                        .header h1 { color: white; margin: 0; font-size: 22px; }
                        .header p { color: rgba(255,255,255,0.85); margin: 8px 0 0; font-size: 14px; }
                        .body { padding: 30px; }
                        .greeting { font-size: 16px; color: #333; margin-bottom: 20px; }
                        .info-box { background: #f8f9fa; border-left: 4px solid #0d6efd; padding: 16px 20px; border-radius: 0 8px 8px 0; margin: 20px 0; }
                        .info-box p { margin: 6px 0; font-size: 14px; color: #555; }
                        .info-box strong { color: #333; }
                        .password-box { background: #fff3cd; border: 1px solid #ffc107; border-radius: 8px; padding: 16px; margin: 20px 0; }
                        .password-box p { margin: 0; font-size: 14px; color: #856404; }
                        .password-box .pass-code { font-size: 18px; font-weight: bold; color: #0d6efd; letter-spacing: 3px; font-family: monospace; }
                        .note { font-size: 13px; color: #6c757d; margin-top: 24px; line-height: 1.6; }
                        .footer { background: #f8f9fa; padding: 20px 30px; text-align: center; border-top: 1px solid #dee2e6; }
                        .footer p { margin: 4px 0; font-size: 12px; color: #aaa; }
                    </style>
                </head>
                <body>
                <div class="container">
                    <div class="header">
                        <h1>📧 PHIẾU LƯƠNG ĐIỆN TỬ</h1>
                        <p>Tháng %d/%d | %s</p>
                    </div>
                    <div class="body">
                        <p class="greeting">Xin chào <strong>%s</strong>,</p>
                        <p>Phiếu lương tháng <strong>%d/%d</strong> của bạn đã được đính kèm trong email này dưới định dạng PDF.</p>

                        <div class="info-box">

                            <p><strong>Phòng ban:</strong> %s</p>
                            <p><strong>Kỳ lương:</strong> Tháng %d/%d</p>
                        </div>

                        <div class="password-box">
                            <p>🔐 <strong>Mật khẩu để mở File là CCCD/CMT của Anh/Chị.</strong></p>
                            <p style="margin-top:8px; font-size:12px;">Vui lòng giữ bí mật thông tin này.</p>
                        </div>

                        <p class="note">
                            Nếu bạn có thắc mắc về phiếu lương, vui lòng liên hệ phòng Nhân sự.<br>
                            Email này được gửi tự động, vui lòng không phản hồi trực tiếp.
                        </p>
                    </div>
                    <div class="footer">
                        <p>%s</p>
                        <p>Email được tạo tự động - Hệ thống quản lý lương</p>
                    </div>
                </div>
                </body>
                </html>
                """.formatted(
                record.getSalaryMonth(), record.getSalaryYear(), companyName,
                emp.getFullName(),
                record.getSalaryMonth(), record.getSalaryYear(),
                emp.getDepartment() != null ? emp.getDepartment() : "—",
                record.getSalaryMonth(), record.getSalaryYear(),
                companyName
        );
    }
}
