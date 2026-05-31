package com.dashboard.backend.controller;

import com.dashboard.backend.dto.ApiResponse;
import com.dashboard.backend.dto.LoginRequest;
import com.dashboard.backend.dto.RefreshRequest;
import com.dashboard.backend.dto.RegisterRequest;
import com.dashboard.backend.dto.TokenResponse;
import com.dashboard.backend.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody @Valid RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(
            @RequestBody @Valid LoginRequest request, HttpServletResponse response) {
        TokenResponse token = authService.login(request);
        setTokenCookies(response, token.getToken(), token.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok(token));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = resolveRefreshToken(request);
        if (refreshToken == null) {
            throw new IllegalArgumentException("리프레시 토큰이 없습니다. 다시 로그인해주세요.");
        }
        RefreshRequest refreshRequest = new RefreshRequest();
        refreshRequest.setRefreshToken(refreshToken);
        TokenResponse token = authService.refresh(refreshRequest);
        setTokenCookies(response, token.getToken(), token.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = resolveRefreshToken(request);
        if (refreshToken != null) {
            RefreshRequest refreshRequest = new RefreshRequest();
            refreshRequest.setRefreshToken(refreshToken);
            authService.logout(refreshRequest);
        }
        clearTokenCookies(response);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader("Set-Cookie", buildCookie("accessToken", accessToken, "/api", 3600));
        response.addHeader("Set-Cookie", buildCookie("refreshToken", refreshToken, "/api/auth/refresh", 604800));
    }

    private void clearTokenCookies(HttpServletResponse response) {
        response.addHeader("Set-Cookie", buildCookie("accessToken", "", "/api", 0));
        response.addHeader("Set-Cookie", buildCookie("refreshToken", "", "/api/auth/refresh", 0));
    }

    private String buildCookie(String name, String value, String path, int maxAge) {
        return name + "=" + value
                + "; Path=" + path
                + "; Max-Age=" + maxAge
                + "; HttpOnly"
                + "; SameSite=Lax"
                + (cookieSecure ? "; Secure" : "");
    }

    private String resolveRefreshToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) return cookie.getValue();
            }
        }
        return null;
    }
}
