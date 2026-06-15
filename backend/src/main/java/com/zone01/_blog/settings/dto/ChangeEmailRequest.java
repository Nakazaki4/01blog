package com.zone01._blog.settings.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ChangeEmailRequest(@NotBlank @Email String email) {}
