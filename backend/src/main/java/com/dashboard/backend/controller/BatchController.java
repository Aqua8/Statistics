package com.dashboard.backend.controller;

import com.dashboard.backend.batch.BatchService;
import com.dashboard.backend.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/batch")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<Void>> run(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            batchService.runForDate(date);
            return ResponseEntity.ok(ApiResponse.ok());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail(e.getMessage()));
        }
    }
}
