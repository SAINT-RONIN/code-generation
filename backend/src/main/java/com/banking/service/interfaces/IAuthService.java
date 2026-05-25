package com.banking.service.interfaces;

import com.banking.dto.LoginRequest;
import com.banking.dto.LoginResponse;
import com.banking.dto.RegisterRequest;
import com.banking.model.User;

public interface IAuthService {
    User register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    void verifyPin(Long userId, String pin);
}
