package com.zone01._blog.user.dto;

public record UserProfileResponse(
        Long id,
        String username,
        String avatarUrl,
        int subscribersCount,
        int followingCount,
        boolean isSubscribed) {
}
