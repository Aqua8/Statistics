package com.dashboard.backend.batch;

import com.dashboard.backend.domain.DailyStat;
import com.dashboard.backend.domain.PageStat;
import com.dashboard.backend.domain.Project;
import com.dashboard.backend.domain.ReferrerStat;
import com.dashboard.backend.repository.DailyStatRepository;
import com.dashboard.backend.repository.PageLogRepository;
import com.dashboard.backend.repository.PageStatRepository;
import com.dashboard.backend.repository.ProjectRepository;
import com.dashboard.backend.repository.ReferrerStatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

    private final JobOperator jobLauncher;
    private final Job dailyStatJob;
    private final ProjectRepository projectRepository;
    private final PageLogRepository pageLogRepository;
    private final DailyStatRepository dailyStatRepository;
    private final PageStatRepository pageStatRepository;
    private final ReferrerStatRepository referrerStatRepository;

    public void runForDate(LocalDate date) throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("targetDate", date.toString())
                .addLong("runTime", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(dailyStatJob, params);
        log.info("일별 통계 집계 완료: {}", date);
    }

    @Transactional
    public void runForProject(Long projectId, LocalDate date) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59, 999_999_999);
        String key = project.getTrackingKey();

        // DailyStat 재집계
        long totalViews = pageLogRepository.countByTrackingKeyAndEventTypeAndCreatedAtBetween(key, "pageview", start, end);
        long uniqueVisitors = pageLogRepository.countUniqueVisitorsByTrackingKeyAndPeriod(key, start, end);
        Double avgDuration = pageLogRepository.avgDurationByTrackingKeyAndPeriod(key, start, end);
        long totalSessions = pageLogRepository.countTotalSessionsByTrackingKeyAndPeriod(key, start, end);
        long bounceSessions = pageLogRepository.countBounceSessionsByTrackingKeyAndPeriod(key, start, end);
        double bounceRate = totalSessions > 0 ? (double) bounceSessions / totalSessions * 100.0 : 0.0;
        Double avgPagesPerSession = pageLogRepository.avgPagesPerSessionByTrackingKeyAndPeriod(key, start, end);
        Double avgSessionDuration = pageLogRepository.avgSessionDurationByTrackingKeyAndPeriod(key, start, end);

        dailyStatRepository.deleteByProjectAndStatDate(project, date);
        dailyStatRepository.save(new DailyStat(project, date, totalViews, uniqueVisitors,
                avgDuration != null ? avgDuration.longValue() : 0L, bounceRate, totalSessions,
                avgPagesPerSession != null ? avgPagesPerSession : 0.0,
                avgSessionDuration != null ? avgSessionDuration.longValue() : 0L));

        // PageStat 재집계
        pageStatRepository.deleteByProjectAndStatDate(project, date);
        List<PageStat> pageStats = pageLogRepository.groupByPageUrl(key, start, end).stream()
                .map(row -> new PageStat(project, date, (String) row[0],
                        ((Number) row[1]).longValue(), ((Number) row[2]).longValue()))
                .toList();
        pageStatRepository.saveAll(pageStats);

        // ReferrerStat 재집계
        referrerStatRepository.deleteByProjectAndStatDate(project, date);
        List<ReferrerStat> referrerStats = pageLogRepository.groupByReferrer(key, start, end).stream()
                .map(row -> new ReferrerStat(project, date, (String) row[0],
                        ((Number) row[1]).longValue()))
                .toList();
        referrerStatRepository.saveAll(referrerStats);

        log.info("프로젝트 {} 배치 재집계 완료: {}", projectId, date);
    }
}
