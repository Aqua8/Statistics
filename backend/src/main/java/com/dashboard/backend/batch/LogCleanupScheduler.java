package com.dashboard.backend.batch;

import com.dashboard.backend.repository.PageLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogCleanupScheduler {

    @Value("${log.retention.days:90}")
    private int retentionDays;

    private final PageLogRepository pageLogRepository;

    @Transactional
    @Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시 — Batch 집계(1시) 완료 후 실행
    public void cleanup() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        int deleted = pageLogRepository.deleteOlderThan(cutoff);
        log.info("page_logs 정리 완료: {}건 삭제 (보존 기간: {}일)", deleted, retentionDays);
    }
}
