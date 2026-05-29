package com.dashboard.backend.service;

import com.dashboard.backend.domain.PageLog;
import com.dashboard.backend.dto.LogCollectRequest;
import com.dashboard.backend.realtime.ActiveVisitorStore;
import com.dashboard.backend.repository.PageLogRepository;
import com.dashboard.backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LogCollectService {

    private final PageLogRepository pageLogRepository;
    private final ProjectRepository projectRepository;
    private final ActiveVisitorStore activeVisitorStore;

    public void collect(LogCollectRequest request, String ipAddress) {
        // 등록되지 않은 트래킹 키로 오는 스팸 로그 차단
        if (!projectRepository.existsByTrackingKey(request.getTrackingKey())) {
            throw new IllegalArgumentException("유효하지 않은 트래킹 키입니다.");
        }
        pageLogRepository.save(new PageLog(
                request.getTrackingKey(),
                request.getPageUrl(),
                request.getReferrer(),
                request.getUserAgent(),
                ipAddress,
                request.getEventType(),
                request.getDuration(),
                request.getCountry(),
                request.getDeviceType(),
                request.getBrowser()
        ));
        // 로그 저장과 동시에 실시간 방문자 카운트 갱신
        activeVisitorStore.record(request.getTrackingKey(), ipAddress);
    }
}
