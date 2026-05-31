package com.dashboard.backend.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CollectRateLimiter {

    @Value("${rate-limit.collect.max-requests:60}")
    private int maxRequests;

    @Value("${rate-limit.collect.window-ms:60000}")
    private long windowMs;

    private record Bucket(AtomicInteger count, long windowEnd) {}

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean isAllowed(String ip) {
        long now = System.currentTimeMillis();
        Bucket bucket = buckets.compute(ip, (k, b) ->
                (b == null || now > b.windowEnd())
                        ? new Bucket(new AtomicInteger(0), now + windowMs)
                        : b
        );
        return bucket.count().incrementAndGet() <= maxRequests;
    }

    // 5분마다 만료된 버킷 제거 — 메모리 누수 방지
    @Scheduled(fixedRate = 300_000)
    public void cleanup() {
        long now = System.currentTimeMillis();
        buckets.entrySet().removeIf(e -> now > e.getValue().windowEnd());
    }
}
