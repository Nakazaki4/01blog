package com.zone01._blog.admin.dto;

import java.time.Instant;

import com.zone01._blog.post.dto.UserPost;
import com.zone01._blog.report.ReportStatus;

public record AdminReportDto(
        Long id,
        UserPost reporter,
        UserPost reportedUser,
        String reason,
        ReportStatus status,
        Instant createdAt
) {
}
