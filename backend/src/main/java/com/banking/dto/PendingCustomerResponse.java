package com.banking.dto;

public record PendingCustomerResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String bsn,
        String phoneNumber
) {}
