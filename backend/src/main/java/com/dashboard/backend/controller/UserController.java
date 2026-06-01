package com.dashboard.backend.controller;

import com.dashboard.backend.dto.ApiResponse;
import com.dashboard.backend.dto.UpdateNameRequest;
import com.dashboard.backend.dto.UpdatePasswordRequest;
import com.dashboard.backend.dto.UserProfileResponse;
import com.dashboard.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getProfile(userId)));
    }

    @PutMapping("/me/name")
    public ResponseEntity<ApiResponse<Void>> updateName(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid UpdateNameRequest request) {
        userService.updateName(userId, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid UpdatePasswordRequest request) {
        userService.updatePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
