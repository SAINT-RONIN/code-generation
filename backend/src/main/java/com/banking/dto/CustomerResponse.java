package com.banking.dto;

import com.banking.model.User;
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
) {
    public static CustomerResponse from(User user) {
        return new CustomerResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getBsn(),
                user.getPhoneNumber(),
                user.getStatus().name()
        );
    }
}
