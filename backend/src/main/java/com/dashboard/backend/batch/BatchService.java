package com.dashboard.backend.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

    private final JobOperator jobLauncher;
    private final Job dailyStatJob;

    public void runForDate(LocalDate date) throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("targetDate", date.toString())
                .addLong("runTime", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(dailyStatJob, params);
        log.info("일별 통계 집계 완료: {}", date);
    }
}
