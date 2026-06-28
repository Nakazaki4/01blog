package com.zone01._blog.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotBlank
        @Size(min = 2_000, max = 10_000,
                message = "Description must be between 2000 and 10000 characters")
        String description) {
}
