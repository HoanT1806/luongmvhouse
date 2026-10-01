package com.salary.controller;

import com.salary.dto.EmployeeDto;
import com.salary.model.Employee;
import com.salary.model.SalaryRecord;
import com.salary.repository.SalaryRecordRepository;
import com.salary.service.EmployeeService;
import com.salary.service.SalaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class MainController {

    private final EmployeeService employeeService;
    private final SalaryService salaryService;
    private final SalaryRecordRepository salaryRecordRepository;

    // ===== LOGIN =====
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {
        if (error != null) model.addAttribute("errorMsg", "Sai tên đăng nhập hoặc mật khẩu!");
        if (logout != null) model.addAttribute("logoutMsg", "Đã đăng xuất thành công.");
        return "login";
    }

    // ===== DASHBOARD =====
    @GetMapping({"/", "/dashboard"})
    public String dashboard(@RequestParam(defaultValue = "0") Integer month,
                            @RequestParam(defaultValue = "0") Integer year,
                            Model model) {
        LocalDate now = LocalDate.now();
        if (month == 0) month = now.getMonthValue();
        if (year == 0) year = now.getYear();

        model.addAttribute("totalEmployees", employeeService.countAll());
        model.addAttribute("currentMonth", month);
        model.addAttribute("currentYear", year);
        model.addAttribute("totalRecords", salaryService.countTotal(month, year));
        model.addAttribute("sentCount", salaryService.countSent(month, year));
        model.addAttribute("recentRecords", salaryRecordRepository.findBySalaryMonthAndSalaryYear(month, year));
        return "dashboard";
    }

    // ===== EMPLOYEE MANAGEMENT =====
    @GetMapping("/employees")
    public String employees(@RequestParam(required = false) String search, Model model) {
        List<Employee> employees = (search != null && !search.isBlank())
                ? employeeService.search(search)
                : employeeService.findAll();
        model.addAttribute("employees", employees);
        model.addAttribute("search", search);
        model.addAttribute("employeeDto", new EmployeeDto());
        return "employees";
    }

    @PostMapping("/employees/save")
    public String saveEmployee(@Valid @ModelAttribute EmployeeDto employeeDto,
                               BindingResult result,
                               RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            redirectAttrs.addFlashAttribute("error", "Dữ liệu không hợp lệ: " + result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/employees";
        }
        try {
            employeeService.save(employeeDto);
            redirectAttrs.addFlashAttribute("success",
                    employeeDto.getId() == null ? "Thêm nhân viên thành công!" : "Cập nhật thành công!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employees";
    }

    @PostMapping("/employees/delete/{id}")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            employeeService.delete(id);
            redirectAttrs.addFlashAttribute("success", "Đã xóa nhân viên!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employees";
    }

    // ===== SALARY MANAGEMENT =====
    @GetMapping("/salary")
    public String salaryList(@RequestParam(defaultValue = "0") Integer month,
                             @RequestParam(defaultValue = "0") Integer year,
                             Model model) {
        LocalDate now = LocalDate.now();
        if (month == 0) month = now.getMonthValue();
        if (year == 0) year = now.getYear();

        List<SalaryRecord> records = salaryService.findByMonthYear(month, year);
        model.addAttribute("records", records);
        model.addAttribute("month", month);
        model.addAttribute("year", year);
        model.addAttribute("employees", employeeService.findAll());
        model.addAttribute("salaryDto", new com.salary.dto.SalaryRecordDto());
        model.addAttribute("totalSent", salaryService.countSent(month, year));
        model.addAttribute("totalRecords", salaryService.countTotal(month, year));
        return "salary";
    }

    @PostMapping("/salary/save")
    public String saveSalary(@Valid @ModelAttribute com.salary.dto.SalaryRecordDto dto,
                             BindingResult result,
                             RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            redirectAttrs.addFlashAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/salary";
        }
        try {
            salaryService.save(dto);
            redirectAttrs.addFlashAttribute("success", "Lưu phiếu lương thành công!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/salary?month=" + dto.getSalaryMonth() + "&year=" + dto.getSalaryYear();
    }

    @PostMapping("/salary/send/{id}")
    public String sendPayslip(@PathVariable Long id,
                              @RequestParam Integer month,
                              @RequestParam Integer year,
                              RedirectAttributes redirectAttrs) {
        try {
            salaryService.sendPayslip(id);
            redirectAttrs.addFlashAttribute("success", "Đã gửi phiếu lương thành công!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Gửi thất bại: " + e.getMessage());
        }
        return "redirect:/salary?month=" + month + "&year=" + year;
    }

    @PostMapping("/salary/send-all")
    public String sendAllPayslips(@RequestParam Integer month,
                                  @RequestParam Integer year,
                                  RedirectAttributes redirectAttrs) {
        try {
            salaryService.sendAllPayslips(month, year);
            redirectAttrs.addFlashAttribute("success", "Đã gửi tất cả phiếu lương!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/salary?month=" + month + "&year=" + year;
    }

    @PostMapping("/salary/retry-failed")
    public String retryFailed(@RequestParam Integer month,
                              @RequestParam Integer year,
                              RedirectAttributes redirectAttrs) {
        try {
            salaryService.retrySendFailed(month, year);
            redirectAttrs.addFlashAttribute("success", "Đã gửi lại các phiếu bị lỗi!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/salary?month=" + month + "&year=" + year;
    }

    @PostMapping("/salary/delete/{id}")
    public String deleteSalary(@PathVariable Long id,
                               @RequestParam Integer month,
                               @RequestParam Integer year,
                               RedirectAttributes redirectAttrs) {
        salaryService.delete(id);
        redirectAttrs.addFlashAttribute("success", "Đã xóa phiếu lương!");
        return "redirect:/salary?month=" + month + "&year=" + year;
    }
}
