package com.dashboard.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "page_logs", indexes = {
        @Index(name = "idx_tracking_key", columnList = "tracking_key"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_tracking_created", columnList = "tracking_key, created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_key", nullable = false)
    private String trackingKey;

    @Column(nullable = false)
    private String pageUrl;

    private String referrer;

    private String userAgent;

    private String ipAddress;

    @Column(nullable = false)
    private String eventType;

    private Long duration;

    private String country;

    private String deviceType;

    private String browser;

    @Column(name = "session_id", length = 36)
    private String sessionId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public PageLog(String trackingKey, String pageUrl, String referrer,
                   String userAgent, String ipAddress, String eventType,
                   Long duration, String country, String deviceType, String browser,
                   String sessionId) {
        this.trackingKey = trackingKey;
        this.pageUrl = pageUrl;
        this.referrer = referrer;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
        this.eventType = eventType;
        this.duration = duration;
        this.country = country;
        this.deviceType = deviceType;
        this.browser = browser;
        this.sessionId = sessionId;
    }
}
