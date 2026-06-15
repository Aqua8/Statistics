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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        List<DailyStatResponse> result = new ArrayList<>(
                dailyStatRepository
                        .findByProjectAndStatDateBetweenOrderByStatDateAsc(project, from, to)
                        .stream().map(DailyStatResponse::from).toList()
        );

        // Batch는 전날 데이터만 집계하므로, 오늘 날짜가 범위에 포함되고 집계 결과가 없으면 page_logs 직접 조회
        LocalDate today = LocalDate.now();
        boolean todayInRange = !today.isBefore(from) && !today.isAfter(to);
        boolean todayAlreadyAggregated = result.stream().anyMatch(r -> r.getStatDate().equals(today));

        if (todayInRange && !todayAlreadyAggregated) {
            String key = project.getTrackingKey();
            LocalDateTime start = today.atStartOfDay();
            LocalDateTime end = today.atTime(23, 59, 59, 999_999_999);

            long totalViews = pageLogRepository
                    .countByTrackingKeyAndEventTypeAndCreatedAtBetween(key, "pageview", start, end);
            long uniqueVisitors = pageLogRepository
                    .countUniqueVisitorsByTrackingKeyAndPeriod(key, start, end);
            Double avgDurationRaw = pageLogRepository
                    .avgDurationByTrackingKeyAndPeriod(key, start, end);
            long avgDuration = avgDurationRaw != null ? avgDurationRaw.longValue() : 0L;

            long totalSessions = pageLogRepository
                    .countTotalSessionsByTrackingKeyAndPeriod(key, start, end);
            long bounceSessions = pageLogRepository
                    .countBounceSessionsByTrackingKeyAndPeriod(key, start, end);
            double bounceRate = totalSessions > 0
                    ? (double) bounceSessions / totalSessions * 100.0
                    : 0.0;

            Double avgPagesPerSession = pageLogRepository
                    .avgPagesPerSessionByTrackingKeyAndPeriod(key, start, end);
            Double avgSessionDurationRaw = pageLogRepository
                    .avgSessionDurationByTrackingKeyAndPeriod(key, start, end);

            result.add(new DailyStatResponse(
                    today, totalViews, uniqueVisitors, avgDuration, bounceRate,
                    totalSessions,
                    avgPagesPerSession != null ? avgPagesPerSession : 0.0,
                    avgSessionDurationRaw != null ? avgSessionDurationRaw.longValue() : 0L
            ));
        }

        return result;
    }

    public List<PageStatResponse> getPageStats(Long projectId, LocalDate from, LocalDate to) {
        Project project = findProject(projectId);

        // URL 기준으로 views/uniqueVisitors 합산 (날짜별 중복 제거)
        Map<String, long[]> urlStats = new LinkedHashMap<>();
        pageStatRepository.sumByPageUrlBetween(project, from, to)
                .forEach(row -> urlStats.put(
                        (String) row[0],
                        new long[]{((Number) row[1]).longValue(), ((Number) row[2]).longValue()}
                ));

        LocalDate today = LocalDate.now();
        boolean todayInRange = !today.isBefore(from) && !today.isAfter(to);
        boolean todayAlreadyAggregated = pageStatRepository.existsByProjectAndStatDate(project, today);

        if (todayInRange && !todayAlreadyAggregated) {
            String key = project.getTrackingKey();
            LocalDateTime start = today.atStartOfDay();
            LocalDateTime end = today.atTime(23, 59, 59, 999_999_999);
            pageLogRepository.groupByPageUrl(key, start, end).forEach(row -> {
                String url = (String) row[0];
                long views = ((Number) row[1]).longValue();
                long unique = ((Number) row[2]).longValue();
                urlStats.merge(url, new long[]{views, unique},
                        (existing, added) -> new long[]{existing[0] + added[0], existing[1] + added[1]});
            });
        }

        return urlStats.entrySet().stream()
                .map(e -> new PageStatResponse(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .sorted((a, b) -> Long.compare(b.getViews(), a.getViews()))
                .collect(Collectors.toList());
    }

    public List<ReferrerStatResponse> getReferrerStats(Long projectId, LocalDate from, LocalDate to) {
        Project project = findProject(projectId);
        List<ReferrerStatResponse> result = new ArrayList<>(
                referrerStatRepository
                        .findByProjectAndStatDateBetweenOrderByVisitsDesc(project, from, to)
                        .stream().map(ReferrerStatResponse::from).toList()
        );

        LocalDate today = LocalDate.now();
        boolean todayInRange = !today.isBefore(from) && !today.isAfter(to);
        boolean todayAlreadyAggregated = referrerStatRepository.existsByProjectAndStatDate(project, today);

        if (todayInRange && !todayAlreadyAggregated) {
            String key = project.getTrackingKey();
            LocalDateTime start = today.atStartOfDay();
            LocalDateTime end = today.atTime(23, 59, 59, 999_999_999);
            pageLogRepository.groupByReferrer(key, start, end).stream()
                    .map(row -> new ReferrerStatResponse(
                            (String) row[0],
                            ((Number) row[1]).longValue()))
                    .forEach(result::add);
            result.sort((a, b) -> Long.compare(b.getVisits(), a.getVisits()));
        }

        return result;
    }

    // 디바이스/브라우저는 사전 집계 테이블이 없어 원시 page_logs를 직접 집계
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

    public List<BreakdownStatResponse> getCountryStats(Long projectId, LocalDate from, LocalDate to) {
        Project project = findProject(projectId);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(23, 59, 59, 999_999_999);
        return pageLogRepository.groupByCountry(project.getTrackingKey(), start, end)
                .stream().map(BreakdownStatResponse::from).toList();
    }

    public String getTrackingKey(Long projectId) {
        return findProject(projectId).getTrackingKey();
    }

    private Project findProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
        if ("Y".equals(project.getDelYn())) {
            throw new IllegalArgumentException("프로젝트를 찾을 수 없습니다.");
        }
        return project;
    }
}
