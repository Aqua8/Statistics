package com.dashboard.backend.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobOperator jobLauncher;
    private final Job dailyStatJob;

    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시 — 전날 로그가 모두 쌓인 후 집계
    public void runDailyStatJob() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        JobParameters params = new JobParametersBuilder()
                .addString("targetDate", yesterday.toString())
                // Spring Batch는 동일 파라미터 조합의 Job 재실행을 막으므로 runTime으로 유니크하게 만듦
                .addLong("runTime", System.currentTimeMillis())
                .toJobParameters();
        try {
            jobLauncher.run(dailyStatJob, params);
            log.info("일별 통계 집계 완료: {}", yesterday);
        } catch (Exception e) {
            log.error("일별 통계 집계 실패: {}", yesterday, e);
        }
    }
}
