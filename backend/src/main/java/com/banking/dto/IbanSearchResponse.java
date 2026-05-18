package com.banking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record IbanSearchResponse(
        String firstName,
        String lastName,
        @Schema(example = "NL91ABNA0417164300")
        String checkingIban
) {}
