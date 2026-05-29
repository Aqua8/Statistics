package com.dashboard.backend.repository;

import com.dashboard.backend.domain.Project;
import com.dashboard.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByUserAndDelYn(User user, String delYn);

    // 활성 프로젝트만 (로그 수집 유효성 검사용)
    boolean existsByTrackingKeyAndDelYn(String trackingKey, String delYn);

    // 트래킹 키 중복 여부 확인 (삭제된 프로젝트 포함 — 키 재사용 방지)
    boolean existsByTrackingKey(String trackingKey);

    Optional<Project> findByTrackingKeyAndDelYn(String trackingKey, String delYn);

    // 배치 집계 대상: 활성 프로젝트만
    List<Project> findByDelYn(String delYn);
}
