package com.dashboard.backend.batch;

import com.dashboard.backend.domain.Project;
import com.dashboard.backend.domain.ReferrerStat;
import com.dashboard.backend.repository.PageLogRepository;
import com.dashboard.backend.repository.ProjectRepository;
import com.dashboard.backend.repository.ReferrerStatRepository;
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
public class ReferrerStatTasklet implements Tasklet {

    private final ProjectRepository projectRepository;
    private final PageLogRepository pageLogRepository;
    private final ReferrerStatRepository referrerStatRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        LocalDate targetDate = getTargetDate(chunkContext);
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.atTime(23, 59, 59, 999_999_999);

        List<Project> projects = projectRepository.findAll();
        for (Project project : projects) {
            referrerStatRepository.deleteByProjectAndStatDate(project, targetDate);

            List<ReferrerStat> stats = pageLogRepository
                    .groupByReferrer(project.getTrackingKey(), start, end)
                    .stream()
                    .map(row -> new ReferrerStat(
                            project,
                            targetDate,
                            (String) row[0],
                            ((Number) row[1]).longValue()
                    ))
                    .toList();

            referrerStatRepository.saveAll(stats);
            contribution.incrementWriteCount(stats.size());
        }
        return RepeatStatus.FINISHED;
    }

    private LocalDate getTargetDate(ChunkContext chunkContext) {
        return LocalDate.parse((String) chunkContext.getStepContext().getJobParameters().get("targetDate"));
    }
}
