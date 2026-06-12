package com.zone01._blog.admin.dto;

import java.time.Instant;

import com.zone01._blog.post.dto.UserPost;

public record AdminPostDto(
        Long id,
        UserPost author,
        String description,
        long likeCount,
        long commentCount,
        long reportCount,
        boolean deleted,
        Instant createdAt
) {
}
