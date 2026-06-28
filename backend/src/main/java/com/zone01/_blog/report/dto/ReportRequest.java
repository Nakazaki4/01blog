package com.zone01._blog.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReportRequest(
        @NotBlank
        String postId,
        @NotBlank
        @Size(max = 500)
        String reason) {
}
