package com.dashboard.backend.service;

import com.dashboard.backend.domain.Project;
import com.dashboard.backend.dto.DailyStatResponse;
import com.dashboard.backend.dto.PageStatResponse;
import com.dashboard.backend.dto.ReferrerStatResponse;
import com.dashboard.backend.repository.DailyStatRepository;
import com.dashboard.backend.repository.PageStatRepository;
import com.dashboard.backend.repository.ProjectRepository;
import com.dashboard.backend.repository.ReferrerStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatService {

    private final DailyStatRepository dailyStatRepository;
    private final PageStatRepository pageStatRepository;
    private final ReferrerStatRepository referrerStatRepository;
    private final ProjectRepository projectRepository;

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

    private Project findProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
    }
}
