package com.dashboard.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "page_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private LocalDate statDate;

    @Column(nullable = false)
    private String pageUrl;

    private Long views;

    private Long uniqueVisitors;

    public PageStat(Project project, LocalDate statDate, String pageUrl,
                    Long views, Long uniqueVisitors) {
        this.project = project;
        this.statDate = statDate;
        this.pageUrl = pageUrl;
        this.views = views;
        this.uniqueVisitors = uniqueVisitors;
    }
}
