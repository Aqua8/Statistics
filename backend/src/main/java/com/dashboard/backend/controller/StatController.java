package com.dashboard.backend.controller;

import com.dashboard.backend.dto.ApiResponse;
import com.dashboard.backend.dto.BreakdownStatResponse;
import com.dashboard.backend.dto.DailyStatResponse;
import com.dashboard.backend.dto.PageStatResponse;
import com.dashboard.backend.dto.ReferrerStatResponse;
import com.dashboard.backend.realtime.RealtimeSseManager;
import com.dashboard.backend.service.ProjectService;
import com.dashboard.backend.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/stats")
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;
    private final ProjectService projectService;
    private final RealtimeSseManager realtimeSseManager;

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<DailyStatResponse>>> getDailyStats(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        projectService.verifyOwnership(userId, projectId);
        return ResponseEntity.ok(ApiResponse.ok(statService.getDailyStats(projectId, from, to)));
    }

    @GetMapping("/pages")
    public ResponseEntity<ApiResponse<List<PageStatResponse>>> getPageStats(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        projectService.verifyOwnership(userId, projectId);
        return ResponseEntity.ok(ApiResponse.ok(statService.getPageStats(projectId, from, to)));
    }

    @GetMapping("/referrers")
    public ResponseEntity<ApiResponse<List<ReferrerStatResponse>>> getReferrerStats(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        projectService.verifyOwnership(userId, projectId);
        return ResponseEntity.ok(ApiResponse.ok(statService.getReferrerStats(projectId, from, to)));
    }

    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<List<BreakdownStatResponse>>> getDeviceStats(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        projectService.verifyOwnership(userId, projectId);
        return ResponseEntity.ok(ApiResponse.ok(statService.getDeviceStats(projectId, from, to)));
    }

    @GetMapping("/browsers")
    public ResponseEntity<ApiResponse<List<BreakdownStatResponse>>> getBrowserStats(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        projectService.verifyOwnership(userId, projectId);
        return ResponseEntity.ok(ApiResponse.ok(statService.getBrowserStats(projectId, from, to)));
    }

    @GetMapping("/countries")
    public ResponseEntity<ApiResponse<List<BreakdownStatResponse>>> getCountryStats(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        projectService.verifyOwnership(userId, projectId);
        return ResponseEntity.ok(ApiResponse.ok(statService.getCountryStats(projectId, from, to)));
    }

    @GetMapping(value = "/realtime", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter realtimeVisitors(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long projectId) {
        projectService.verifyOwnership(userId, projectId);
        String trackingKey = statService.getTrackingKey(projectId);
        return realtimeSseManager.subscribe(trackingKey);
    }
}
