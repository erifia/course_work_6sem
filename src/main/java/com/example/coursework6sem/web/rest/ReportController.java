package com.example.coursework6sem.web.rest;

import com.example.coursework6sem.application.service.report.PdfReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final PdfReportService pdfReportService;

    public ReportController(PdfReportService pdfReportService) {
        this.pdfReportService = pdfReportService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me/pdf")
    public ResponseEntity<byte[]> downloadMyReportPdf() {
        byte[] pdf;
        try {
            pdf = pdfReportService.myReportPdf();
        } catch (Exception e) {
            pdf = pdfReportService.fallbackPdf("MY REPORT", "Could not build full report: " + e.getMessage());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"my-report.pdf\"")
                .body(pdf);
    }
}

