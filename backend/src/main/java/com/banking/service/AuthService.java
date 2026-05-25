package com.banking.service;

import com.banking.dto.LoginRequest;
import com.banking.dto.LoginResponse;
import com.banking.dto.RegisterRequest;
import com.banking.model.User;
import com.banking.model.User.UserStatus;
import com.banking.repository.UserRepository;
import com.banking.security.JwtUtil;
import com.banking.service.interfaces.IAuthService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public User register(RegisterRequest request) {
        userRepository.ensureEmailAvailable(request.email());
        return userRepository.create(buildCustomerFrom(request));
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findRequiredByEmail(request.email());
        verifyAccountIsActive(user);
        verifyPassword(request.password(), user.getPassword());
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new LoginResponse(token, user.getRole().name());
    }

    private void verifyAccountIsActive(User user) {
        if (user.getStatus() == UserStatus.PENDING) {
            throw new BadCredentialsException("Account pending approval. Await employee activation.");
        }
        if (user.getStatus() == UserStatus.CLOSED) {
            throw new BadCredentialsException("Account has been closed.");
        }
    }

    @Override
    public void verifyPin(Long userId, String pin) {
        User user = userRepository.findRequiredById(userId);
        if (user.getPin() == null || !passwordEncoder.matches(pin, user.getPin())) {
            throw new BadCredentialsException("Incorrect PIN");
        }
    }

    private void verifyPassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    private User buildCustomerFrom(RegisterRequest request) {
        User user = new User(
                request.firstName(),
                request.lastName(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.bsn(),
                request.phoneNumber(),
                User.Role.CUSTOMER
        );
        user.setStatus(UserStatus.PENDING);
        return user;
    }
}
