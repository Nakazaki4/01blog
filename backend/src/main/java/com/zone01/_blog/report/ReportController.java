package com.zone01._blog.report;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.zone01._blog.report.dto.ReportRequest;
import com.zone01._blog.report.dto.ReportResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    private static Long requireUserId(String principal) {
        if (principal == null || "anonymousUser".equals(principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return Long.parseLong(principal);
    }

    @PostMapping
    public ResponseEntity<ReportResponse> postReport(@AuthenticationPrincipal String userId,
            @Valid @RequestBody ReportRequest requestBody) {
        return ResponseEntity.ok(reportService.ValidateAndSaveReport(requireUserId(userId), Long.parseLong(requestBody.postId()),
                requestBody.reason()));
    }
}
