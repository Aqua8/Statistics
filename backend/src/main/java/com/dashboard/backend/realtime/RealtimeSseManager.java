package com.dashboard.backend.realtime;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RealtimeSseManager {

    private final ActiveVisitorStore activeVisitorStore;

    // trackingKey -> 연결된 emitter 목록
    private final ConcurrentHashMap<String, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String trackingKey) {
        // 브라우저 EventSource 기본 타임아웃보다 길게 설정해 불필요한 재연결 방지
        SseEmitter emitter = new SseEmitter(300_000L);
        emitters.computeIfAbsent(trackingKey, k -> ConcurrentHashMap.newKeySet()).add(emitter);

        // 연결 종료/타임아웃/에러 발생 시 emitter 목록에서 제거
        emitter.onCompletion(() -> remove(trackingKey, emitter));
        emitter.onTimeout(() -> remove(trackingKey, emitter));
        emitter.onError(e -> remove(trackingKey, emitter));

        // 연결 즉시 현재 카운트 전송 — 5초 브로드캐스트 전에 화면이 빈 상태로 있지 않도록
        try {
            emitter.send(SseEmitter.event().name("visitors").data(activeVisitorStore.count(trackingKey)));
        } catch (IOException e) {
            remove(trackingKey, emitter);
        }

        return emitter;
    }

    private void remove(String trackingKey, SseEmitter emitter) {
        Set<SseEmitter> set = emitters.get(trackingKey);
        if (set != null) set.remove(emitter);
    }

    // 5초마다 전체 구독 클라이언트에 현재 방문자 수 푸시; 전송 실패한 emitter는 즉시 제거
    @Scheduled(fixedRate = 5_000)
    public void broadcast() {
        emitters.forEach((trackingKey, emitterSet) -> {
            if (emitterSet.isEmpty()) return;
            int count = activeVisitorStore.count(trackingKey);
            Set<SseEmitter> dead = new HashSet<>();
            emitterSet.forEach(emitter -> {
                try {
                    emitter.send(SseEmitter.event().name("visitors").data(count));
                } catch (Exception e) {
                    dead.add(emitter);
                }
            });
            emitterSet.removeAll(dead);
        });
    }
}
