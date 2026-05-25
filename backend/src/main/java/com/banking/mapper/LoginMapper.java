package com.banking.mapper;

import com.banking.dto.LoginResponse;
import com.banking.model.User;
import org.springframework.stereotype.Component;

@Component
public class LoginMapper {

    public LoginResponse toResponse(String token, User user) {
        return new LoginResponse(token, user.getRole().name());
    }
}
