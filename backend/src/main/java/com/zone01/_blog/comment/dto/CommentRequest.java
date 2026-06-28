package com.zone01._blog.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotBlank
        @Size(max = 1_000)
        String content) {
}
