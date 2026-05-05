package com.banking.service.interfaces;

import com.banking.dto.LoginRequest;
import com.banking.dto.LoginResponse;
import com.banking.dto.RegisterRequest;
import com.banking.model.User;

/** Handles registration and authentication for all user roles. */
public interface AuthService {

    /** @return the newly created User in PENDING status */
    User register(RegisterRequest request);

    /** @return a JWT token and the caller's role on success */
    LoginResponse login(LoginRequest request);

    /** @return true if the supplied PIN matches the stored hash for this email */
    boolean verifyPin(String email, String pin);
}
