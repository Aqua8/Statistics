package com.dashboard.backend.dto;

import com.dashboard.backend.domain.DailyStat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DailyStatResponse {
    private LocalDate statDate;
    private Long totalViews;
    private Long uniqueVisitors;
    private Long avgDuration;
    private Double bounceRate;
    private Long sessionCount;
    private Double avgPagesPerSession;
    private Long avgSessionDuration;

    public static DailyStatResponse from(DailyStat stat) {
        return new DailyStatResponse(
                stat.getStatDate(),
                stat.getTotalViews(),
                stat.getUniqueVisitors(),
                stat.getAvgDuration(),
                stat.getBounceRate(),
                stat.getSessionCount(),
                stat.getAvgPagesPerSession(),
                stat.getAvgSessionDuration()
        );
    }
}
