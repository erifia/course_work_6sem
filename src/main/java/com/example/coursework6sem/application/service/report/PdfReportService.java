package com.example.coursework6sem.application.service.report;

import com.example.coursework6sem.domain.RoleName;
import com.example.coursework6sem.infrastructure.persistence.jpa.entity.UserAccountEntity;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EstateRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.EvaluationRepository;
import com.example.coursework6sem.infrastructure.persistence.jpa.repository.UserAccountRepository;
import com.example.coursework6sem.application.service.evaluation.EvaluationQueryService;
import com.example.coursework6sem.security.SecurityUtils;
import com.example.coursework6sem.web.dto.admin.AdminStatsResponse;
import com.example.coursework6sem.web.dto.evaluation.EvaluationResponse;
import com.example.coursework6sem.web.dto.auth.AuthResponses;
import com.example.coursework6sem.web.dto.admin.UserSummaryResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class PdfReportService {

    private final UserAccountRepository users;
    private final EstateRepository estates;
    private final EvaluationRepository evaluations;
    private final EvaluationQueryService evaluationQueryService;
    private final SecurityUtils securityUtils;

    public PdfReportService(
            UserAccountRepository users,
            EstateRepository estates,
            EvaluationRepository evaluations,
            EvaluationQueryService evaluationQueryService,
            SecurityUtils securityUtils
    ) {
        this.users = users;
        this.estates = estates;
        this.evaluations = evaluations;
        this.evaluationQueryService = evaluationQueryService;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public byte[] adminReportPdf() {
        AdminStatsResponse stats = new AdminStatsResponse(
                users.count(),
                estates.count(),
                evaluations.count()
        );

        List<UserSummaryResponse> topUsers = users.findAll().stream()
                .sorted(Comparator.comparing(UserAccountEntity::getCreatedAt).reversed())
                .limit(10)
                .map(u -> new UserSummaryResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRoleName()))
                .toList();

        return buildPdf(doc -> {
            doc.title("Отчёт администратора");

            doc.addLine("ADMIN REPORT", 16, true);
            doc.addLine("", 12, false);
            doc.addLine("Generated at: " + nowRu(), 12, false);
            doc.addLine("Users total: " + stats.usersCount(), 12, false);
            doc.addLine("Estates total: " + stats.estatesCount(), 12, false);
            doc.addLine("Evaluations total: " + stats.evaluationsCount(), 12, false);
            doc.addLine("", 12, false);

            doc.addLine("Recent users (up to 10):", 12, true);
            for (UserSummaryResponse u : topUsers) {
                doc.addLine("- " + u.username() + " (" + u.role() + ", " + u.email() + ")", 11, false);
            }
        });
    }

    @Transactional(readOnly = true)
    public byte[] myReportPdf() {
        Long myId = securityUtils.currentUserAccountId()
                .orElseThrow(() -> new IllegalStateException("Не авторизован"));

        UserAccountEntity me = users.findById(myId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Page<EvaluationResponse> myEvals = evaluationQueryService.list(myId, null, PageRequest.of(0, 10));
        long myEvalCount = myEvals.getTotalElements();

        AuthResponses.ProfileResponse profile = new AuthResponses.ProfileResponse(
                me.getId(),
                me.getUsername(),
                me.getEmail(),
                me.getRoleName(),
                me.getCreatedAt()
        );

        return buildPdf(doc -> {
            doc.title("Мой отчёт");

            doc.addLine("MY REPORT", 16, true);
            doc.addLine("", 12, false);
            doc.addLine("User: " + profile.username(), 12, false);
            doc.addLine("Email: " + profile.email(), 12, false);
            doc.addLine("Role: " + profile.role(), 12, false);
            doc.addLine("Generated at: " + nowRu(), 12, false);
            doc.addLine("", 12, false);

            doc.addLine("My evaluations: " + myEvalCount, 12, true);
            if (myEvals.getContent().isEmpty()) {
                doc.addLine("No evaluations yet.", 12, false);
            } else {
                doc.addLine("Recent (up to 10):", 11, false);
                for (EvaluationResponse e : myEvals.getContent()) {
                    doc.addLine("- #" + e.id() + ": " + e.address() + " / " + formatMoney(e.estimatedValue()), 10, false);
                }
            }
        });
    }

    public byte[] fallbackPdf(String title, String message) {
        return buildPdf(doc -> {
            doc.addLine(title == null ? "REPORT" : title, 16, true);
            doc.addLine("", 12, false);
            doc.addLine(message == null ? "Failed to generate full report." : message, 12, false);
            doc.addLine("Generated at: " + nowRu(), 12, false);
        });
    }

    private static String formatMoney(java.math.BigDecimal v) {
        if (v == null) return "0";
        return v.toPlainString();
    }

    private static String nowRu() {
        Instant now = Instant.now();
        return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru"))
                .withZone(ZoneId.systemDefault())
                .format(now);
    }

    private interface PdfBuilder {
        void build(PdfDoc doc) throws IOException;
    }

    private static byte[] buildPdf(PdfBuilder builder) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                PdfDoc doc = new PdfDoc(document, cs);
                builder.build(doc);
            }

            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка генерации PDF", e);
        }
    }

    private static class PdfDoc {
        private final PDDocument document;
        private final PDPageContentStream cs;
        private final PDType1Font helvetica = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private float y = 750;
        private float left = 50;

        private PdfDoc(PDDocument document, PDPageContentStream cs) {
            this.document = document;
            this.cs = cs;
        }

        void title(String title) {
            // no-op: keep for future headers
        }

        void addLine(String text, int fontSize, boolean bold) throws IOException {
            if (text == null) text = "";
            text = toSafeAscii(text);

            // Simple "wrap": split by length (good enough for coursework demo).
            int maxChars = 85;
            String[] chunks = text.length() <= maxChars ? new String[]{text} : splitByChunks(text, maxChars);
            for (String chunk : chunks) {
                if (y < 60) {
                    // For brevity we don't implement multi-page in this coursework-sized report.
                    break;
                }
                cs.setFont(helvetica, fontSize);
                cs.beginText();
                cs.newLineAtOffset(left, y);
                cs.showText(chunk);
                cs.endText();
                y -= (fontSize + 4);
            }
        }

        private static String[] splitByChunks(String text, int maxChars) {
            int parts = (int) Math.ceil(text.length() / (double) maxChars);
            String[] res = new String[parts];
            for (int i = 0; i < parts; i++) {
                int start = i * maxChars;
                int end = Math.min(text.length(), start + maxChars);
                res[i] = text.substring(start, end);
            }
            return res;
        }

        private static String toSafeAscii(String text) {
            StringBuilder sb = new StringBuilder(text.length());
            for (char c : text.toCharArray()) {
                if (c >= 32 && c <= 126) {
                    sb.append(c);
                } else {
                    sb.append('?');
                }
            }
            return sb.toString();
        }
    }
}

