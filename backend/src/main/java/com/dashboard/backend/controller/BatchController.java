package com.dashboard.backend.controller;

import com.dashboard.backend.batch.BatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/batch")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    @PostMapping("/run")
    public ResponseEntity<Map<String, String>> run(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            batchService.runForDate(date);
            return ResponseEntity.ok(Map.of("status", "success", "date", date.toString()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "failed", "date", date.toString(), "message", e.getMessage()));
        }
    }
}
