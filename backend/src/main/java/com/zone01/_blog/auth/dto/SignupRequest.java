package com.zone01._blog.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignupRequest(@NotBlank String username,
                            @NotBlank @Email String email,
                            @NotBlank String password,
                            String bio,
                            String avatarUrl) {

}
