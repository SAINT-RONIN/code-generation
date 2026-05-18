package com.banking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record VerifyPinRequest(
        @Schema(example = "1234")
        @NotBlank String pin
) {}
