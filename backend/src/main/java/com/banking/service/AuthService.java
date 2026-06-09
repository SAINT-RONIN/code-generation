package com.banking.service;

import com.banking.dto.LoginRequest;
import com.banking.dto.LoginResponse;
import com.banking.dto.RegisterRequest;
import com.banking.mapper.LoginMapper;
import com.banking.model.User;
import com.banking.model.User.UserStatus;
import com.banking.repository.UserRepository;
import com.banking.security.JwtUtil;
import com.banking.service.interfaces.IAuthService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user registration and login authentication.
 * New customers register with PENDING status and must be approved by an employee before they can log in.
 */
@Service // Registers this class as a Spring-managed service bean
public class AuthService implements IAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LoginMapper loginMapper;

    // Constructor injection — Spring auto-wires all dependencies
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, LoginMapper loginMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.loginMapper = loginMapper;
    }

    /**
     * Registers a new customer with PENDING status.
     * The customer cannot log in until an employee approves their account.
     * Throws EmailAlreadyInUseException (409) if the email is taken.
     */
    @Override
    public User register(RegisterRequest request) {
        // Check that no existing user has this email
        userRepository.ensureEmailAvailable(request.email());
        User user = new User(
                request.firstName(),
                request.lastName(),
                request.email(),
                // Hash the raw password with BCrypt before storing it in the database
                passwordEncoder.encode(request.password()),
                request.bsn(),
                request.phoneNumber(),
                User.Role.CUSTOMER
        );
        // New customers start as PENDING — they need employee approval to become ACTIVE
        user.setStatus(UserStatus.PENDING);
        return userRepository.save(user);
    }

    /**
     * Authenticates a user by email and password.
     * Returns a JWT token and the user's role so the frontend knows which dashboard to show.
     * Throws BadCredentialsException (401) for wrong password, pending, or closed accounts.
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findRequiredByEmail(request.email());
        // Block login if account is PENDING or CLOSED
        verifyAccountIsActive(user);
        // Compare the raw password against the stored BCrypt hash
        verifyPassword(request.password(), user.getPassword());
        // Generate a JWT token containing the user's ID and email
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return loginMapper.toResponse(token, user);
    }

    /**
     * Checks that the user's account is ACTIVE.
     * PENDING users get a specific message telling them to wait for approval.
     * CLOSED users are told their account has been deactivated.
     */
    private void verifyAccountIsActive(User user) {
        if (user.getStatus() == UserStatus.PENDING) {
            throw new BadCredentialsException("Account pending approval. Await employee activation.");
        }
        if (user.getStatus() == UserStatus.CLOSED) {
            throw new BadCredentialsException("Account has been closed.");
        }
    }

    // Compares the raw password against the BCrypt-encoded hash from the database
    private void verifyPassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            // Use a generic error message to avoid revealing whether the email exists
            throw new BadCredentialsException("Invalid email or password");
        }
    }

}
