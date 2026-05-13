package com.zone01._blog.auth.dto;

public record LoginResponse(String token, Long UserId, String username) {
}
