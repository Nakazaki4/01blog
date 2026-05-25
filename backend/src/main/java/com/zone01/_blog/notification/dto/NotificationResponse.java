package com.zone01._blog.notification.dto;

import com.zone01._blog.notification.NotificationType;
import com.zone01._blog.post.dto.UserPost;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        UserPost actor,
        NotificationType type,
        Long postId,
        boolean isRead,
        Instant createdAt
) {
}