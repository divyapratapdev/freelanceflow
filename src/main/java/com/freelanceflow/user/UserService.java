package com.freelanceflow.user;

import com.freelanceflow.common.SecurityUtils;
import com.freelanceflow.user.dto.UpdateProfileRequest;
import com.freelanceflow.user.dto.UserResponse;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        MDC.put("userId", String.valueOf(userId));
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
            return UserResponse.from(user);
        } finally {
            MDC.clear();
        }
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        MDC.put("userId", String.valueOf(userId));
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

            if (request.getFullName() != null && !request.getFullName().isBlank()) {
                user.setFullName(request.getFullName());
            }
            if (request.getBusinessName() != null) {
                user.setBusinessName(request.getBusinessName());
            }
            if (request.getPhone() != null) {
                user.setPhone(request.getPhone());
            }

            user = userRepository.save(user);
            return UserResponse.from(user);
        } finally {
            MDC.clear();
        }
    }
}
