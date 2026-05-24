package com.zone01._blog.comment.dto;

import com.zone01._blog.post.dto.UserPost;

import java.time.Instant;

public record CommentResponse(
        Long id,
        UserPost author,
        String content,
        Instant createdAt
) {
}
