package com.zone01._blog.user.dto;

import com.zone01._blog.post.Post;

public record UserProfileResponse(
        String username, String avatarUrl, String bio,
        Post[] posts, int subscribersCount, int followingCount) {
}
