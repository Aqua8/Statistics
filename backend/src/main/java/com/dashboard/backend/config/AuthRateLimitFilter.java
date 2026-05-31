package com.dashboard.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// 로그인/회원가입 엔드포인트 브루트포스 방어 — IP당 분당 10회 제한
@RequiredArgsConstructor
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MS = 60_000L;

    private record Bucket(AtomicInteger count, long windowEnd) {}

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!isAllowed(request.getRemoteAddr())) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
            objectMapper.writeValue(response.getWriter(),
                    Map.of("success", false, "data", null, "message", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isAllowed(String ip) {
        long now = System.currentTimeMillis();
        Bucket bucket = buckets.compute(ip, (k, b) ->
                (b == null || now > b.windowEnd())
                        ? new Bucket(new AtomicInteger(0), now + WINDOW_MS)
                        : b
        );
        return bucket.count().incrementAndGet() <= MAX_REQUESTS;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        // 로그인, 회원가입에만 적용
        return !("POST".equals(method) && (uri.equals("/api/auth/login") || uri.equals("/api/auth/register")));
    }
}
