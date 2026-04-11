package com.freelanceflow.auth;

import com.freelanceflow.user.User;
import com.freelanceflow.user.UserRepository;
import com.freelanceflow.user.dto.*;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.refresh-expiration-days}")
    private int refreshExpirationDays;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setBusinessName(request.getBusinessName());
        user.setPhone(request.getPhone());
        user = userRepository.save(user);

        log.info("Registered new user id={} email={}", user.getId(), user.getEmail());
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (Exception ex) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        log.info("User logged in id={}", user.getId());
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new IllegalArgumentException("Refresh token expired — please login again");
        }

        User user = stored.getUser();

        // Rotate: delete old token, issue new one
        refreshTokenRepository.delete(stored);
        refreshTokenRepository.flush();

        log.info("Refreshed token for user id={}", user.getId());
        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
        log.info("Logged out user id={}", userId);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateToken(user.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plus(refreshExpirationDays, ChronoUnit.DAYS));
        refreshTokenRepository.save(refreshToken);

        UserResponse userResponse = UserResponse.from(user);
        return new AuthResponse(accessToken, refreshToken.getToken(),
                jwtUtil.getExpirationMs() / 1000, userResponse);
    }
}
