package com.dashboard.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LogCollectRequest {

    @NotBlank
    private String trackingKey;

    @NotBlank
    private String pageUrl;

    private String referrer;
    private String userAgent;

    @NotBlank
    private String eventType;

    private Long duration;
    private String country;
    private String deviceType;
    private String browser;
}
