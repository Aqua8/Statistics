package com.dashboard.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BreakdownStatResponse {
    private String name;
    private Long count;

    public static BreakdownStatResponse from(Object[] row) {
        return new BreakdownStatResponse(
                (String) row[0],
                ((Number) row[1]).longValue()
        );
    }
}
