package com.dashboard.backend.dto;

import com.dashboard.backend.domain.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String name;
    private String domain;
    private String trackingKey;
    private LocalDateTime createdAt;

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDomain(),
                project.getTrackingKey(),
                project.getCreatedAt()
        );
    }
}
