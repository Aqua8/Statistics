package com.dashboard.backend.config;

import com.dashboard.backend.util.CollectRateLimiter;
import tools.jackson.databind.ObjectMapper;
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

@RequiredArgsConstructor
public class CollectRateLimitFilter extends OncePerRequestFilter {

    private final CollectRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!rateLimiter.isAllowed(request.getRemoteAddr())) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
            objectMapper.writeValue(response.getWriter(),
                    Map.of("success", false, "data", null, "message", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."));
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // /api/collect POST 요청에만 적용
        return !("POST".equals(request.getMethod()) && "/api/collect".equals(request.getRequestURI()));
    }
}
