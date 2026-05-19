package com.dashboard.backend.controller;

import com.dashboard.backend.dto.BreakdownStatResponse;
import com.dashboard.backend.dto.DailyStatResponse;
import com.dashboard.backend.dto.PageStatResponse;
import com.dashboard.backend.dto.ReferrerStatResponse;
import com.dashboard.backend.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/stats")
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;

    @GetMapping("/daily")
    public ResponseEntity<List<DailyStatResponse>> getDailyStats(
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(statService.getDailyStats(projectId, from, to));
    }

    @GetMapping("/pages")
    public ResponseEntity<List<PageStatResponse>> getPageStats(
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(statService.getPageStats(projectId, from, to));
    }

    @GetMapping("/referrers")
    public ResponseEntity<List<ReferrerStatResponse>> getReferrerStats(
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(statService.getReferrerStats(projectId, from, to));
    }

    @GetMapping("/devices")
    public ResponseEntity<List<BreakdownStatResponse>> getDeviceStats(
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(statService.getDeviceStats(projectId, from, to));
    }

    @GetMapping("/browsers")
    public ResponseEntity<List<BreakdownStatResponse>> getBrowserStats(
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(statService.getBrowserStats(projectId, from, to));
    }
}
