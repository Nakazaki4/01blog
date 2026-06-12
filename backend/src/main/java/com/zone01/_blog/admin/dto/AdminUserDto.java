package com.zone01._blog.admin.dto;

import com.zone01._blog.user.Role;

public record AdminUserDto(
        Long id,
        String username,
        String email,
        Role role,
        boolean banned,
        long postCount,
        long reportCount
) {
}
