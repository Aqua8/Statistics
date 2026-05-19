package com.dashboard.backend.controller;

import com.dashboard.backend.dto.ProjectCreateRequest;
import com.dashboard.backend.dto.ProjectResponse;
import com.dashboard.backend.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getMyProjects(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(projectService.getMyProjects(userId));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid ProjectCreateRequest request) {
        return ResponseEntity.ok(projectService.create(userId, request));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long projectId) {
        projectService.delete(userId, projectId);
        return ResponseEntity.noContent().build();
    }
}
