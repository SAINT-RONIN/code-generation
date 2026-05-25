package com.banking.mapper;

import com.banking.dto.CustomerResponse;
import com.banking.model.User;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponse toResponse(User user) {
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
