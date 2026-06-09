package com.banking.service.interfaces;

import com.banking.dto.LoginRequest;
import com.banking.dto.LoginResponse;
import com.banking.dto.RegisterRequest;
import com.banking.model.User;

/**
 * Contract for authentication operations: registration and login.
 * Implemented by AuthService. Using an interface allows easy mocking in unit tests
 * and supports swapping implementations without changing the controller.
 */
public interface IAuthService {

    // Registers a new customer with PENDING status — returns the created User entity
    User register(RegisterRequest request);

    // Authenticates by email/password and returns a JWT token with the user's role
    LoginResponse login(LoginRequest request);
}
