package com.dashboard.backend.controller;

import com.dashboard.backend.dto.ApiResponse;
import com.dashboard.backend.dto.LogCollectRequest;
import com.dashboard.backend.service.LogCollectService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/collect")
@RequiredArgsConstructor
public class LogCollectController {

    private final LogCollectService logCollectService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> collect(
            @RequestBody @Valid LogCollectRequest request,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getHeader("X-Real-IP");
        if (ip == null || ip.isBlank()) ip = httpRequest.getRemoteAddr();
        logCollectService.collect(request, ip);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
