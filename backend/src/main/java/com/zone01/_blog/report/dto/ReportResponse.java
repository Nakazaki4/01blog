package com.zone01._blog.report.dto;

import java.time.Instant;

import com.zone01._blog.report.ReportStatus;

public record ReportResponse(
    Long id,
    Long reportedUserId,
    String reason,
    ReportStatus status,
    Instant created_at
) {
}
