package com.dashboard.backend.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "referrer_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReferrerStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private LocalDate statDate;

    private String referrer;

    private Long visits;

    public ReferrerStat(Project project, LocalDate statDate, String referrer, Long visits) {
        this.project = project;
        this.statDate = statDate;
        this.referrer = referrer;
        this.visits = visits;
    }
}
