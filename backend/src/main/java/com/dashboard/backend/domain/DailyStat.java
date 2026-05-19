package com.dashboard.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "daily_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private LocalDate statDate;

    private Long totalViews;

    private Long uniqueVisitors;

    private Double avgDuration;

    private Double bounceRate;

    public DailyStat(Project project, LocalDate statDate, Long totalViews,
                     Long uniqueVisitors, Double avgDuration, Double bounceRate) {
        this.project = project;
        this.statDate = statDate;
        this.totalViews = totalViews;
        this.uniqueVisitors = uniqueVisitors;
        this.avgDuration = avgDuration;
        this.bounceRate = bounceRate;
    }
}
