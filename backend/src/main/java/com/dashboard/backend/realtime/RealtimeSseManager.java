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
        SseEmitter emitter = new SseEmitter(300_000L); // 5분 타임아웃
        emitters.computeIfAbsent(trackingKey, k -> ConcurrentHashMap.newKeySet()).add(emitter);

        emitter.onCompletion(() -> remove(trackingKey, emitter));
        emitter.onTimeout(() -> remove(trackingKey, emitter));
        emitter.onError(e -> remove(trackingKey, emitter));

        // 연결 즉시 현재 카운트 전송
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
