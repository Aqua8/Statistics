package com.dashboard.backend.dto;

import com.dashboard.backend.domain.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserProfileResponse {

    private final String email;
    private final String name;
    private final LocalDateTime createdAt;

    public UserProfileResponse(User user) {
        this.email = user.getEmail();
        this.name = user.getName();
        this.createdAt = user.getCreatedAt();
    }
}
