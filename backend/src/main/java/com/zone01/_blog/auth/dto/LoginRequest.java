package com.zone01._blog.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String username,
                           @NotBlank String password) {

}
