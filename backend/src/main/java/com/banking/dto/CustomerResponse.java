package com.banking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        @Schema(example = "john@example.com")
        String email,
        String bsn,
        String phoneNumber,
        @Schema(example = "ACTIVE")
        String status
) {}
