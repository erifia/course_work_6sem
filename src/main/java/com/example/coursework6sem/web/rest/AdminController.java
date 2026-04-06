package com.example.coursework6sem.web.rest;

import com.example.coursework6sem.application.service.admin.AdminService;
import com.example.coursework6sem.application.service.report.PdfReportService;
import com.example.coursework6sem.domain.RoleName;
import com.example.coursework6sem.web.dto.admin.AdminStatsResponse;
import com.example.coursework6sem.web.dto.admin.UserSummaryResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final PdfReportService pdfReportService;

    public AdminController(AdminService adminService, PdfReportService pdfReportService) {
        this.adminService = adminService;
        this.pdfReportService = pdfReportService;
    }

    @GetMapping("/users")
    public List<UserSummaryResponse> getUsers() {
        return adminService.getAllUsers();
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<Void> changeRole(
            @PathVariable("id") Long id,
            @RequestParam("role") RoleName role
    ) {
        adminService.changeUserRole(id, role);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/stats")
    public AdminStatsResponse getStats() {
        return adminService.getStats();
    }

    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> downloadAdminReportPdf() {
        byte[] pdf;
        try {
            pdf = pdfReportService.adminReportPdf();
        } catch (Exception e) {
            pdf = pdfReportService.fallbackPdf("ADMIN REPORT", "Could not build full report: " + e.getMessage());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"admin-report.pdf\"")
                .body(pdf);
    }
}

