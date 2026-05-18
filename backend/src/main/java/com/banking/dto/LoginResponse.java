package com.banking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "JWT bearer token")
        String token,
        @Schema(example = "CUSTOMER")
        String role
) {}
