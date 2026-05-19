package com.dashboard.backend.repository;

import com.dashboard.backend.domain.DailyStat;
import com.dashboard.backend.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyStatRepository extends JpaRepository<DailyStat, Long> {

    List<DailyStat> findByProjectAndStatDateBetweenOrderByStatDateAsc(
            Project project, LocalDate from, LocalDate to);

    Optional<DailyStat> findByProjectAndStatDate(Project project, LocalDate statDate);

    void deleteByProjectAndStatDate(Project project, LocalDate statDate);
}
