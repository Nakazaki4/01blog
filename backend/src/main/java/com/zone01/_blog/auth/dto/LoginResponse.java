package com.zone01._blog.auth.dto;

import com.zone01._blog.user.Role;

public record LoginResponse(String token, Long userId, String username, Role role) {
}
