package com.dashboard.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogCollectRequest {

    @NotBlank
    @Size(max = 64)
    private String trackingKey;

    @NotBlank
    @Size(max = 2048)
    private String pageUrl;

    @Size(max = 2048)
    private String referrer;

    @Size(max = 512)
    private String userAgent;

    @NotBlank
    @Size(max = 32)
    private String eventType;

    private Long duration;

    @Size(max = 10)
    private String country;

    @Size(max = 20)
    private String deviceType;

    @Size(max = 50)
    private String browser;

    @Size(max = 36)
    private String sessionId;
}
