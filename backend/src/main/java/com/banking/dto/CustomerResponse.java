package com.banking.dto;

import com.banking.model.User;

public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String bsn,
        String phoneNumber,
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
