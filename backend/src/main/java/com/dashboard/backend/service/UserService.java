package com.dashboard.backend.service;

import com.dashboard.backend.domain.User;
import com.dashboard.backend.dto.UpdateNameRequest;
import com.dashboard.backend.dto.UpdatePasswordRequest;
import com.dashboard.backend.dto.UserProfileResponse;
import com.dashboard.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = findActiveUser(userId);
        return new UserProfileResponse(user);
    }

    public void updateName(Long userId, UpdateNameRequest request) {
        User user = findActiveUser(userId);
        user.updateName(request.getName());
    }

    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = findActiveUser(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    private User findActiveUser(Long userId) {
        return userRepository.findById(userId)
                .filter(u -> "N".equals(u.getDelYn()))
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
