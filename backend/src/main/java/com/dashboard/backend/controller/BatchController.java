package com.dashboard.backend.controller;

import com.dashboard.backend.batch.BatchService;
import com.dashboard.backend.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
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
            log.error("배치 수동 실행 실패: date={}", date, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.fail("배치 작업 실행에 실패했습니다."));
        }
    }
}
