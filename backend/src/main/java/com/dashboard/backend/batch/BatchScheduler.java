package com.dashboard.backend.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final BatchService batchService;

    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시 — 전날 로그가 모두 쌓인 후 집계
    public void runDailyStatJob() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        try {
            batchService.runForDate(yesterday);
        } catch (Exception e) {
            log.error("일별 통계 집계 실패: {}", yesterday, e);
        }
    }
}
