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
        activeVisitorStore.record(request.getTrackingKey(), ipAddress);
    }
}
