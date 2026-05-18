package com.banking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @Schema(example = "John")
        @NotBlank String firstName,
        @Schema(example = "Doe")
        @NotBlank String lastName,
        @Schema(example = "john@example.com")
        @Email @NotBlank String email,
        @NotBlank String password,
        @Schema(example = "123456789")
        @NotBlank String bsn,
        @Schema(example = "0612345678")
        @NotBlank String phoneNumber
) {}
