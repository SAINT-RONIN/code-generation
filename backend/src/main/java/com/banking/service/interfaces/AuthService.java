package com.banking.service.interfaces;

import com.banking.dto.RegisterRequest;
import com.banking.model.User;

public interface AuthService {
    User register(RegisterRequest request);
}
