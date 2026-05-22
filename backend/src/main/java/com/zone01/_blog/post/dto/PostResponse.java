package com.zone01._blog.post.dto;

import java.time.Instant;

public record PostResponse(
        Long id,
        UserPost author,
        String description,
        int likeCount,
        int commentCount,
        boolean isLiked,
        Instant createdAt
) {
}
