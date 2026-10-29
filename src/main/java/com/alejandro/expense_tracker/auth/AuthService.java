package com.alejandro.expense_tracker.auth;

import com.alejandro.expense_tracker.auth.dto.RegisterRequest;
import com.alejandro.expense_tracker.auth.dto.RegisterResponse;
import com.alejandro.expense_tracker.exceptions.EmailAlreadyExistsException;
import com.alejandro.expense_tracker.auth.dto.LoginRequest;
import com.alejandro.expense_tracker.auth.dto.LoginResponse;
import com.alejandro.expense_tracker.exceptions.InvalidCredentialsException;
import com.alejandro.expense_tracker.security.JwtService;
import com.alejandro.expense_tracker.user.User;
import com.alejandro.expense_tracker.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String dummyPasswordHash;

    //Constructor
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;

        this.dummyPasswordHash = passwordEncoder.encode(
                java.util.UUID.randomUUID().toString()
        );
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = request.email()
                .strip()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }

        String passwordHash = passwordEncoder.encode(request.password());

        User user = new User(
                request.username().strip(),
                email,
                passwordHash
        );

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );
    }
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = request.email()
                .strip()
                .toLowerCase(Locale.ROOT);

        var optionalUser = userRepository.findByEmail(email);

        String hashToCheck = optionalUser
                .map(User::getPasswordHash)
                .orElse(dummyPasswordHash);

        boolean passwordMatches = passwordEncoder.matches(
                request.password(),
                hashToCheck
        );

        if (optionalUser.isEmpty() || !passwordMatches) {
            throw new InvalidCredentialsException();
        }

        User user = optionalUser.orElseThrow();

        String token = jwtService.generateToken(user.getId());

        return new LoginResponse(token, "Bearer");
    }
}
