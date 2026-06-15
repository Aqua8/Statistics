package com.dashboard.backend.repository;

import com.dashboard.backend.domain.PageStat;
import com.dashboard.backend.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PageStatRepository extends JpaRepository<PageStat, Long> {

    @Query("SELECT ps.pageUrl, SUM(ps.views), SUM(ps.uniqueVisitors) " +
           "FROM PageStat ps " +
           "WHERE ps.project = :project AND ps.statDate BETWEEN :from AND :to AND ps.delYn = 'N' " +
           "GROUP BY ps.pageUrl " +
           "ORDER BY SUM(ps.views) DESC")
    List<Object[]> sumByPageUrlBetween(
            @Param("project") Project project,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    boolean existsByProjectAndStatDate(Project project, LocalDate statDate);

    void deleteByProjectAndStatDate(Project project, LocalDate statDate);
}
