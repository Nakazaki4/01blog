package com.zone01._blog.post.dto;

import java.time.Instant;

public record FeedPost(
    Long id,
    UserPost author,
    Instant createdAt,
    String description,
    Long likeCount,
    Long commentCount,
    Boolean isLiked
) {}
