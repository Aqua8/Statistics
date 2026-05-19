package com.dashboard.backend.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class DailyStatJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DailyStatTasklet dailyStatTasklet;
    private final PageStatTasklet pageStatTasklet;
    private final ReferrerStatTasklet referrerStatTasklet;

    @Bean
    public Job dailyStatJob() {
        return new JobBuilder("dailyStatJob", jobRepository)
                .start(dailyAggregationStep())
                .next(pageAggregationStep())
                .next(referrerAggregationStep())
                .build();
    }

    @Bean
    public Step dailyAggregationStep() {
        return new StepBuilder("dailyAggregationStep", jobRepository)
                .tasklet(dailyStatTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step pageAggregationStep() {
        return new StepBuilder("pageAggregationStep", jobRepository)
                .tasklet(pageStatTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step referrerAggregationStep() {
        return new StepBuilder("referrerAggregationStep", jobRepository)
                .tasklet(referrerStatTasklet, transactionManager)
                .build();
    }
}
