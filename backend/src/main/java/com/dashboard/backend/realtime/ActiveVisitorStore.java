package com.dashboard.backend.realtime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ActiveVisitorStore {

    // 마지막 요청으로부터 5분 이내를 "활성 방문자"로 정의
    private static final long ACTIVE_WINDOW_MS = 5 * 60 * 1000L;

    // trackingKey -> (visitorIp -> lastSeenEpochMs): DB 없이 인메모리로 실시간 카운트
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> store = new ConcurrentHashMap<>();

    // 로그 수집 시마다 호출 — IP 기준으로 마지막 접속 시각 갱신
    public void record(String trackingKey, String visitorIp) {
        store.computeIfAbsent(trackingKey, k -> new ConcurrentHashMap<>())
                .put(visitorIp, System.currentTimeMillis());
    }

    // 5분 창 안에 요청이 있었던 고유 IP 수 반환
    public int count(String trackingKey) {
        ConcurrentHashMap<String, Long> visitors = store.get(trackingKey);
        if (visitors == null) return 0;
        long cutoff = System.currentTimeMillis() - ACTIVE_WINDOW_MS;
        return (int) visitors.values().stream().filter(ts -> ts > cutoff).count();
    }

    // 1분마다 만료된 IP 정리 — 메모리 누수 방지
    @Scheduled(fixedRate = 60_000)
    public void cleanup() {
        long cutoff = System.currentTimeMillis() - ACTIVE_WINDOW_MS;
        store.values().forEach(visitors -> visitors.entrySet().removeIf(e -> e.getValue() <= cutoff));
    }
}
