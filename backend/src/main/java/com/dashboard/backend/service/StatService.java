package com.dashboard.backend.service;

import com.dashboard.backend.domain.Project;
import com.dashboard.backend.dto.BreakdownStatResponse;
import com.dashboard.backend.dto.DailyStatResponse;
import com.dashboard.backend.dto.PageStatResponse;
import com.dashboard.backend.dto.ReferrerStatResponse;
import com.dashboard.backend.repository.DailyStatRepository;
import com.dashboard.backend.repository.PageLogRepository;
import com.dashboard.backend.repository.PageStatRepository;
import com.dashboard.backend.repository.ProjectRepository;
import com.dashboard.backend.repository.ReferrerStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatService {

    private final DailyStatRepository dailyStatRepository;
    private final PageStatRepository pageStatRepository;
    private final ReferrerStatRepository referrerStatRepository;
    private final ProjectRepository projectRepository;
    private final PageLogRepository pageLogRepository;

    public List<DailyStatResponse> getDailyStats(Long projectId, LocalDate from, LocalDate to) {
        Project project = findProject(projectId);
        return dailyStatRepository
                .findByProjectAndStatDateBetweenOrderByStatDateAsc(project, from, to)
                .stream().map(DailyStatResponse::from).toList();
    }

    public List<PageStatResponse> getPageStats(Long projectId, LocalDate from, LocalDate to) {
        Project project = findProject(projectId);
        return pageStatRepository
                .findByProjectAndStatDateBetweenOrderByViewsDesc(project, from, to)
                .stream().map(PageStatResponse::from).toList();
    }

    public List<ReferrerStatResponse> getReferrerStats(Long projectId, LocalDate from, LocalDate to) {
        Project project = findProject(projectId);
        return referrerStatRepository
                .findByProjectAndStatDateBetweenOrderByVisitsDesc(project, from, to)
                .stream().map(ReferrerStatResponse::from).toList();
    }

    public List<BreakdownStatResponse> getDeviceStats(Long projectId, LocalDate from, LocalDate to) {
        Project project = findProject(projectId);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(23, 59, 59, 999_999_999);
        return pageLogRepository.groupByDeviceType(project.getTrackingKey(), start, end)
                .stream().map(BreakdownStatResponse::from).toList();
    }

    public List<BreakdownStatResponse> getBrowserStats(Long projectId, LocalDate from, LocalDate to) {
        Project project = findProject(projectId);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(23, 59, 59, 999_999_999);
        return pageLogRepository.groupByBrowser(project.getTrackingKey(), start, end)
                .stream().map(BreakdownStatResponse::from).toList();
    }

    public String getTrackingKey(Long projectId) {
        return findProject(projectId).getTrackingKey();
    }

    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
    }
}
