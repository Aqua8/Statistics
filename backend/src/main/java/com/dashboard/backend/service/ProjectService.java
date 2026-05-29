package com.dashboard.backend.service;

import com.dashboard.backend.domain.Project;
import com.dashboard.backend.domain.User;
import com.dashboard.backend.dto.ProjectCreateRequest;
import com.dashboard.backend.dto.ProjectResponse;
import com.dashboard.backend.repository.ProjectRepository;
import com.dashboard.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectResponse create(Long userId, ProjectCreateRequest request) {
        User user = findUser(userId);
        String trackingKey = generateUniqueTrackingKey();
        Project project = new Project(user, request.getName(), request.getDomain(), trackingKey);
        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getMyProjects(Long userId) {
        User user = findUser(userId);
        return projectRepository.findByUser(user).stream()
                .map(ProjectResponse::from)
                .toList();
    }

    public void delete(Long userId, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
        if (!project.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        projectRepository.delete(project);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    // UUID 충돌 가능성은 극히 낮지만 tracker.js에 노출되는 키라 중복 없음을 보장
    private String generateUniqueTrackingKey() {
        String key;
        do {
            key = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        } while (projectRepository.existsByTrackingKey(key));
        return key;
    }
}
