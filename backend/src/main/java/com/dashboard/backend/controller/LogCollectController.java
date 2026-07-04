package com.dashboard.backend.controller;

import com.dashboard.backend.dto.ApiResponse;
import com.dashboard.backend.dto.LogCollectRequest;
import com.dashboard.backend.service.LogCollectService;
import com.dashboard.backend.util.GeoIpService;
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
    private final GeoIpService geoIpService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> collect(
            @RequestBody @Valid LogCollectRequest request,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getHeader("X-Real-IP");
        if (ip == null || ip.isBlank()) ip = httpRequest.getRemoteAddr();
        // GeoIP 해석은 트랜잭션 밖에서 수행 — 외부 HTTP 호출이 DB 커넥션을 점유하지 않도록
        String country = geoIpService.getCountry(ip);
        logCollectService.collect(request, ip, country);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
