package com.dashboard.backend.service;

import com.dashboard.backend.domain.RefreshToken;
import com.dashboard.backend.domain.User;
import com.dashboard.backend.dto.CheckEmailRequest;
import com.dashboard.backend.dto.FindAccountRequest;
import com.dashboard.backend.dto.LoginRequest;
import com.dashboard.backend.dto.RefreshRequest;
import com.dashboard.backend.dto.RegisterRequest;
import com.dashboard.backend.dto.ResetPasswordRequest;
import com.dashboard.backend.dto.TokenResponse;
import com.dashboard.backend.repository.RefreshTokenRepository;
import com.dashboard.backend.repository.UserRepository;
import com.dashboard.backend.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${guest.user-id}")
    private Long guestUserId;

    @Transactional(readOnly = true)
    public String guestLogin() {
        userRepository.findById(guestUserId)
                .filter(u -> "N".equals(u.getDelYn()))
                .orElseThrow(() -> new IllegalStateException("게스트 사용자가 설정되지 않았습니다."));
        return jwtTokenProvider.generateGuestToken(guestUserId);
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmailAndDelYn(request.getEmail(), "N")) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        userRepository.save(new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getName()
        ));
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDelYn(request.getEmail(), "N")
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        String accessToken = jwtTokenProvider.generateToken(user.getId());
        String rawRefreshToken = issueRefreshToken(user);
        return new TokenResponse(accessToken, rawRefreshToken, user.getEmail(), user.getName());
    }

    public TokenResponse refresh(RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("만료된 리프레시 토큰입니다. 다시 로그인해주세요.");
        }
        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.generateToken(user.getId());
        // 기존 리프레시 토큰 교체 (Refresh Token Rotation)
        refreshTokenRepository.delete(refreshToken);
        String newRawRefreshToken = issueRefreshToken(user);
        return new TokenResponse(newAccessToken, newRawRefreshToken, user.getEmail(), user.getName());
    }

    public void logout(RefreshRequest request) {
        refreshTokenRepository.deleteByToken(request.getRefreshToken());
    }

    @Transactional(readOnly = true)
    public List<String> findAccountsByName(FindAccountRequest request) {
        List<User> users = userRepository.findByNameAndDelYn(request.getName(), "N");
        if (users.isEmpty()) {
            throw new IllegalArgumentException("해당 이름으로 가입된 계정이 없습니다.");
        }
        return users.stream().map(u -> maskEmail(u.getEmail())).toList();
    }

    @Transactional(readOnly = true)
    public void checkEmail(CheckEmailRequest request) {
        userRepository.findByEmailAndDelYn(request.getEmail(), "N")
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmailAndDelYn(request.getEmail(), "N")
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String domain = email.substring(at + 1);

        // local: 첫 1자만 노출, 나머지 *
        String maskedLocal = local.charAt(0) + "*".repeat(Math.max(1, local.length() - 1));

        // domain: 점(.) 앞 부분 첫 1자만 노출, 나머지 * (예: gmail.com → g***.com)
        int dot = domain.indexOf('.');
        String domainName = dot > 0 ? domain.substring(0, dot) : domain;
        String domainSuffix = dot > 0 ? domain.substring(dot) : "";
        String maskedDomain = domainName.charAt(0) + "*".repeat(Math.max(1, domainName.length() - 1)) + domainSuffix;

        return maskedLocal + "@" + maskedDomain;
    }

    private String issueRefreshToken(User user) {
        String raw = jwtTokenProvider.generateRefreshToken();
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshExpiration() / 1000);
        refreshTokenRepository.save(new RefreshToken(user, raw, expiresAt));
        return raw;
    }
}
