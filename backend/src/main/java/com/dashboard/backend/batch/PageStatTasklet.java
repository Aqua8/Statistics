package com.dashboard.backend.batch;

import com.dashboard.backend.domain.PageStat;
import com.dashboard.backend.domain.Project;
import com.dashboard.backend.repository.PageLogRepository;
import com.dashboard.backend.repository.PageStatRepository;
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
public class PageStatTasklet implements Tasklet {

    private final ProjectRepository projectRepository;
    private final PageLogRepository pageLogRepository;
    private final PageStatRepository pageStatRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        LocalDate targetDate = getTargetDate(chunkContext);
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.atTime(23, 59, 59, 999_999_999);

        List<Project> projects = projectRepository.findAll();
        for (Project project : projects) {
            // 멱등성 보장: 재실행 시 중복 방지
            pageStatRepository.deleteByProjectAndStatDate(project, targetDate);

            // row[0]=pageUrl, row[1]=totalViews, row[2]=uniqueVisitors (JPQL groupBy 결과)
            List<PageStat> stats = pageLogRepository
                    .groupByPageUrl(project.getTrackingKey(), start, end)
                    .stream()
                    .map(row -> new PageStat(
                            project,
                            targetDate,
                            (String) row[0],
                            ((Number) row[1]).longValue(),
                            ((Number) row[2]).longValue()
                    ))
                    .toList();

            pageStatRepository.saveAll(stats);
            contribution.incrementWriteCount(stats.size());
        }
        return RepeatStatus.FINISHED;
    }

    private LocalDate getTargetDate(ChunkContext chunkContext) {
        return LocalDate.parse((String) chunkContext.getStepContext().getJobParameters().get("targetDate"));
    }
}
