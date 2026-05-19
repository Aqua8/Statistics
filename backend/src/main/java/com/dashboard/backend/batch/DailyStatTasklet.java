package com.dashboard.backend.batch;

import com.dashboard.backend.domain.DailyStat;
import com.dashboard.backend.domain.Project;
import com.dashboard.backend.repository.DailyStatRepository;
import com.dashboard.backend.repository.PageLogRepository;
import com.dashboard.backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DailyStatTasklet implements Tasklet {

    private final ProjectRepository projectRepository;
    private final PageLogRepository pageLogRepository;
    private final DailyStatRepository dailyStatRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        LocalDate targetDate = getTargetDate(chunkContext);
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.atTime(23, 59, 59, 999_999_999);

        List<Project> projects = projectRepository.findAll();
        for (Project project : projects) {
            String key = project.getTrackingKey();

            long totalViews = pageLogRepository
                    .countByTrackingKeyAndEventTypeAndCreatedAtBetween(key, "pageview", start, end);
            long uniqueVisitors = pageLogRepository
                    .countUniqueVisitorsByTrackingKeyAndPeriod(key, start, end);
            Double avgDuration = pageLogRepository
                    .avgDurationByTrackingKeyAndPeriod(key, start, end);

            // 재실행 시 중복 방지: 기존 데이터 삭제 후 재생성
            dailyStatRepository.deleteByProjectAndStatDate(project, targetDate);
            dailyStatRepository.save(new DailyStat(
                    project, targetDate,
                    totalViews,
                    uniqueVisitors,
                    avgDuration != null ? avgDuration : 0.0,
                    0.0  // TODO: 세션 기반 이탈률 계산
            ));

            contribution.incrementWriteCount(1);
        }
        return RepeatStatus.FINISHED;
    }

    private LocalDate getTargetDate(ChunkContext chunkContext) {
        String dateStr = (String) chunkContext.getStepContext().getJobParameters().get("targetDate");
        return LocalDate.parse(dateStr);
    }
}
