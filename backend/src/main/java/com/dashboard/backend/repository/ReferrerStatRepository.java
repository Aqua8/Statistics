package com.dashboard.backend.repository;

import com.dashboard.backend.domain.ReferrerStat;
import com.dashboard.backend.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReferrerStatRepository extends JpaRepository<ReferrerStat, Long> {

    List<ReferrerStat> findByProjectAndStatDateBetweenOrderByVisitsDesc(
            Project project, LocalDate from, LocalDate to);

    void deleteByProjectAndStatDate(Project project, LocalDate statDate);
}
