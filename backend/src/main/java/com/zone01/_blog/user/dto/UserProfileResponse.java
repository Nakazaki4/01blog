package com.zone01._blog.user.dto;

import java.util.List;

import com.zone01._blog.post.dto.PostResponse;

public record UserProfileResponse(
        Long id,
        String username,
        String avatarUrl,
        String bio,
        List<PostResponse> posts,
        int subscribersCount,
        int followingCount,
        boolean isSubscribed) {
}
