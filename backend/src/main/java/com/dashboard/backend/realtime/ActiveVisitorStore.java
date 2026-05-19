package com.dashboard.backend.realtime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ActiveVisitorStore {

    private static final long ACTIVE_WINDOW_MS = 5 * 60 * 1000L;

    // trackingKey -> (visitorIp -> lastSeenEpochMs)
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> store = new ConcurrentHashMap<>();

    public void record(String trackingKey, String visitorIp) {
        store.computeIfAbsent(trackingKey, k -> new ConcurrentHashMap<>())
                .put(visitorIp, System.currentTimeMillis());
    }

    public int count(String trackingKey) {
        ConcurrentHashMap<String, Long> visitors = store.get(trackingKey);
        if (visitors == null) return 0;
        long cutoff = System.currentTimeMillis() - ACTIVE_WINDOW_MS;
        return (int) visitors.values().stream().filter(ts -> ts > cutoff).count();
    }

    @Scheduled(fixedRate = 60_000)
    public void cleanup() {
        long cutoff = System.currentTimeMillis() - ACTIVE_WINDOW_MS;
        store.values().forEach(visitors -> visitors.entrySet().removeIf(e -> e.getValue() <= cutoff));
    }
}
