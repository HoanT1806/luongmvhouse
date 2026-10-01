package com.salary.util;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.EncryptionConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.salary.model.SalaryRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class PdfGenerator {

    @Value("${app.company.name}")
    private String companyName;

    @Value("${app.company.logo-path}")
    private String logoPath;

    @Value("${app.company.address}")
    private String companyAddress;

    @Value("${app.company.phone}")
    private String companyPhone;

    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(13, 110, 253);
    private static final DeviceRgb HEADER_BG = new DeviceRgb(13, 110, 253);
    private static final DeviceRgb ROW_ALT = new DeviceRgb(248, 249, 250);
    private static final DeviceRgb INCOME_COLOR = new DeviceRgb(25, 135, 84);
    private static final DeviceRgb DEDUCT_COLOR = new DeviceRgb(220, 53, 69);
    private static final DeviceRgb NET_BG = new DeviceRgb(13, 110, 253);

    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    /**
     * Tạo PDF phiếu lương có mật khẩu = mã nhân viên
     */
    public byte[] generatePayslipPdf(SalaryRecord record) throws Exception {
        String password = record.getEmployee().getIdentityCard();
        byte[] userPassword = password.getBytes();
        byte[] ownerPassword = (password + "_OWNER").getBytes();

        WriterProperties writerProperties = new WriterProperties()
                .setStandardEncryption(
                        userPassword,
                        ownerPassword,
                        EncryptionConstants.ALLOW_PRINTING,
                        EncryptionConstants.ENCRYPTION_AES_256
                );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos, writerProperties);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(30, 40, 30, 40);

        PdfFont boldFont;
        PdfFont regularFont;
        try {
            org.springframework.core.io.ClassPathResource regularRes = new org.springframework.core.io.ClassPathResource("fonts/Roboto-Regular.ttf");
            org.springframework.core.io.ClassPathResource boldRes = new org.springframework.core.io.ClassPathResource("fonts/Roboto-Bold.ttf");
            boldFont = PdfFontFactory.createFont(boldRes.getInputStream().readAllBytes(), PdfEncodings.IDENTITY_H);
            regularFont = PdfFontFactory.createFont(regularRes.getInputStream().readAllBytes(), PdfEncodings.IDENTITY_H);
        } catch (Exception e) {
            boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        }

        // ===== HEADER =====
        addHeader(document, boldFont, regularFont, record);

        // ===== THÔNG TIN NHÂN VIÊN =====
        addEmployeeInfo(document, boldFont, regularFont, record);

        // ===== BẢNG CHI TIẾT (Thay cho addIncomeTable, addDeductionTable, addNetSalarySection) =====
        addMainTable(document, boldFont, regularFont, record);

        // ===== FOOTER =====
        addFooter(document, boldFont, regularFont, record);

        document.close();
        return baos.toByteArray();
    }

    private void addHeader(Document doc, PdfFont bold, PdfFont regular, SalaryRecord record) throws Exception {
        // Logo + Company info in a table
        float[] headerCols = {80f, 420f};
        Table headerTable = new Table(headerCols);
        headerTable.setWidth(UnitValue.createPercentValue(100));
        headerTable.setMarginBottom(4);

        // Logo cell
        Cell logoCell = new Cell().setBorder(Border.NO_BORDER).setPadding(0).setPaddingRight(10);
        try {
            ClassPathResource logoResource = new ClassPathResource("static/images/logo.png");
            byte[] logoBytes = logoResource.getInputStream().readAllBytes();
            Image logo = new Image(ImageDataFactory.create(logoBytes));
            logo.setWidth(70);
            logo.setHeight(70);
            logoCell.add(logo);
        } catch (Exception e) {
            // Logo not found — skip
            logoCell.add(new Paragraph(""));
        }
        headerTable.addCell(logoCell);

        // Company info cell
        Cell infoCell = new Cell().setBorder(Border.NO_BORDER).setPadding(0).setPaddingTop(4);
        infoCell.add(new Paragraph(companyName.toUpperCase())
                .setFont(bold).setFontSize(18)
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(2));
        infoCell.add(new Paragraph(companyAddress + " | Tel: " + companyPhone)
                .setFont(regular).setFontSize(9)
                .setFontColor(ColorConstants.GRAY));
        headerTable.addCell(infoCell);
        doc.add(headerTable);

        // Divider
        doc.add(new Paragraph("─────────────────────────────────────────────────────────────────")
                .setFont(regular).setFontSize(9)
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER));

        // Title
        doc.add(new Paragraph("PHIẾU LƯƠNG THÁNG " + record.getSalaryMonth() + "/" + record.getSalaryYear())
                .setFont(bold).setFontSize(16)
                .setFontColor(PRIMARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5).setMarginBottom(5));

        doc.add(new Paragraph(" ").setFontSize(4));
    }

    private void addEmployeeInfo(Document doc, PdfFont bold, PdfFont regular, SalaryRecord record) throws Exception {
        float[] colWidths = {200f, 200f};
        Table table = new Table(colWidths);
        table.setWidth(UnitValue.createPercentValue(100));
        table.setBorder(new SolidBorder(PRIMARY_COLOR, 1.5f));
        table.setBorderRadius(null);


        addInfoRow(table, bold, regular, "Họ và tên:", record.getEmployee().getFullName());
        addInfoRow(table, bold, regular, "Email:", record.getEmployee().getEmail());
        addInfoRow(table, bold, regular, "Phòng ban:", record.getEmployee().getDepartment() != null ? record.getEmployee().getDepartment() : "—");
        addInfoRow(table, bold, regular, "Chức vụ:", record.getEmployee().getPosition() != null ? record.getEmployee().getPosition() : "—");
        addInfoRow(table, bold, regular, "Kỳ lương:", "Tháng " + record.getSalaryMonth() + "/" + record.getSalaryYear());

        doc.add(table);
        doc.add(new Paragraph(" ").setFontSize(6));
    }

    private void addInfoRow(Table table, PdfFont bold, PdfFont regular, String label, String value) throws Exception {
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setFont(bold).setFontSize(10).setFontColor(new DeviceRgb(60, 60, 60)))
                .setBackgroundColor(ROW_ALT)
                .setPadding(6).setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(new DeviceRgb(220, 220, 220), 0.5f));
        Cell valueCell = new Cell()
                .add(new Paragraph(value).setFont(regular).setFontSize(10))
                .setPadding(6).setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(new DeviceRgb(220, 220, 220), 0.5f));
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addMainTable(Document doc, PdfFont bold, PdfFont regular, SalaryRecord record) throws Exception {
        float[] cols = {40f, 310f, 150f};
        Table table = new Table(cols);
        table.setWidth(UnitValue.createPercentValue(100));
        
        // Header
        Cell h1 = new Cell().add(new Paragraph("STT").setFont(bold).setFontSize(10))
            .setTextAlignment(TextAlignment.CENTER).setBackgroundColor(new DeviceRgb(240, 240, 240));
        Cell h2 = new Cell().add(new Paragraph("MỤC LỤC").setFont(bold).setFontSize(10))
            .setTextAlignment(TextAlignment.CENTER).setBackgroundColor(new DeviceRgb(240, 240, 240));
        Cell h3 = new Cell().add(new Paragraph("SỐ TIỀN").setFont(bold).setFontSize(10))
            .setTextAlignment(TextAlignment.CENTER).setBackgroundColor(new DeviceRgb(240, 240, 240));
        table.addCell(h1); table.addCell(h2); table.addCell(h3);

        DeviceRgb redColor = new DeviceRgb(255, 0, 0);

        // A. CÁC KHOẢN THU NHẬP
        addSectionHeader(table, bold, "A.", "CÁC KHOẢN THU NHẬP", redColor);
        addRow(table, regular, "1", "Lương cơ bản", record.getBasicSalary());
        addRow(table, regular, "2", "Phụ cấp", record.getAllowance());
        addRow(table, regular, "3", "Thưởng", record.getBonus());
        addRow(table, regular, "4", "Làm thêm giờ", record.getOvertime());
        addTotalRow(table, bold, "", "TỔNG THU NHẬP", record.getGrossSalary(), redColor);

        // B. CÁC KHOẢN KHẤU TRỪ
        addSectionHeader(table, bold, "B.", "CÁC KHOẢN KHẤU TRỪ", redColor);
        addRow(table, regular, "1", "Bảo hiểm xã hội (8%)", record.getSocialInsurance());
        addRow(table, regular, "2", "Bảo hiểm y tế (1.5%)", record.getHealthInsurance());
        addRow(table, regular, "3", "Bảo hiểm thất nghiệp (1%)", record.getUnemploymentInsurance());
        addRow(table, regular, "4", "Thuế thu nhập cá nhân", record.getIncomeTax());
        addRow(table, regular, "5", "Khấu trừ khác", record.getOtherDeduction());
        addTotalRow(table, bold, "", "TỔNG KHẤU TRỪ", record.getTotalDeduction(), redColor);

        // C. THỰC NHẬN
        addTotalRow(table, bold, "C.", "THỰC NHẬN (A - B)", record.getNetSalary(), redColor);

        doc.add(table);
        doc.add(new Paragraph(" ").setFontSize(6));
        
        // Note about password
        doc.add(new Paragraph("⚠ Tài liệu này được bảo mật. Mật khẩu mở file: Số CCCD của bạn (" + record.getEmployee().getIdentityCard() + ")")
                .setFont(regular).setFontSize(9)
                .setFontColor(new DeviceRgb(108, 117, 125))
                .setItalic()
                .setTextAlignment(TextAlignment.CENTER));
    }

    private void addFooter(Document doc, PdfFont bold, PdfFont regular, SalaryRecord record) throws Exception {
        doc.add(new Paragraph(" ").setFontSize(10));

        // Signature section
        float[] cols = {250f, 250f};
        Table sigTable = new Table(cols);
        sigTable.setWidth(UnitValue.createPercentValue(100));

        Cell empSig = new Cell()
                .add(new Paragraph("Nhân viên xác nhận").setFont(bold).setFontSize(10).setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("\n\n\n").setFontSize(8))
                .add(new Paragraph(record.getEmployee().getFullName())
                        .setFont(regular).setFontSize(9).setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(ColorConstants.GRAY))
                .setBorder(Border.NO_BORDER);

        Cell mgrSig = new Cell()
                .add(new Paragraph("Phòng Nhân sự").setFont(bold).setFontSize(10).setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("\n\n\n").setFontSize(8))
                .add(new Paragraph("(Ký, đóng dấu)")
                        .setFont(regular).setFontSize(9).setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(ColorConstants.GRAY))
                .setBorder(Border.NO_BORDER);

        sigTable.addCell(empSig);
        sigTable.addCell(mgrSig);
        doc.add(sigTable);

        doc.add(new Paragraph("─────────────────────────────────────────────────────────────────")
                .setFont(regular).setFontSize(9)
                .setFontColor(new DeviceRgb(200, 200, 200))
                .setTextAlignment(TextAlignment.CENTER));

        String generatedTime = "Phiếu lương được tạo tự động bởi hệ thống vào " +
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
        doc.add(new Paragraph(generatedTime)
                .setFont(regular).setFontSize(8)
                .setFontColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private void addSectionHeader(Table table, PdfFont bold, String stt, String title, DeviceRgb color) {
        Cell c1 = new Cell().add(new Paragraph(stt).setFont(bold).setFontSize(10).setFontColor(color))
            .setTextAlignment(TextAlignment.CENTER).setPadding(6);
        Cell c2 = new Cell(1, 2).add(new Paragraph(title).setFont(bold).setFontSize(10).setFontColor(color))
            .setPadding(6);
        table.addCell(c1); table.addCell(c2);
    }

    private void addRow(Table table, PdfFont regular, String stt, String title, BigDecimal amount) {
        table.addCell(new Cell().add(new Paragraph(stt).setFont(regular).setFontSize(10))
            .setTextAlignment(TextAlignment.CENTER).setPadding(6));
        table.addCell(new Cell().add(new Paragraph(title).setFont(regular).setFontSize(10)).setPadding(6));
        table.addCell(new Cell().add(new Paragraph(formatMoney(amount)).setFont(regular).setFontSize(10))
            .setTextAlignment(TextAlignment.RIGHT).setPadding(6));
    }

    private void addTotalRow(Table table, PdfFont bold, String stt, String title, BigDecimal amount, DeviceRgb color) {
        table.addCell(new Cell().add(new Paragraph(stt).setFont(bold).setFontSize(10).setFontColor(color))
            .setTextAlignment(TextAlignment.CENTER).setPadding(6));
        table.addCell(new Cell().add(new Paragraph(title).setFont(bold).setFontSize(10).setFontColor(color)).setPadding(6));
        table.addCell(new Cell().add(new Paragraph(formatMoney(amount)).setFont(bold).setFontSize(10).setFontColor(color))
            .setTextAlignment(TextAlignment.RIGHT).setPadding(6));
    }

    private String formatMoney(BigDecimal amount) {
        if (amount == null) return "0";
        return currencyFormat.format(amount) + " đ";
    }
}
