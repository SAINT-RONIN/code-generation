package com.banking.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyPinRequest(@NotBlank String pin) {}
