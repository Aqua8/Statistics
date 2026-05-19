package com.dashboard.backend.repository;

import com.dashboard.backend.domain.PageStat;
import com.dashboard.backend.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PageStatRepository extends JpaRepository<PageStat, Long> {

    List<PageStat> findByProjectAndStatDateBetweenOrderByViewsDesc(
            Project project, LocalDate from, LocalDate to);

    void deleteByProjectAndStatDate(Project project, LocalDate statDate);
}
