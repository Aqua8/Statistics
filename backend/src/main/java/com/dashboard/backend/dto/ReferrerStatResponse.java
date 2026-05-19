package com.dashboard.backend.dto;

import com.dashboard.backend.domain.ReferrerStat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReferrerStatResponse {
    private String referrer;
    private Long visits;

    public static ReferrerStatResponse from(ReferrerStat stat) {
        return new ReferrerStatResponse(
                stat.getReferrer(),
                stat.getVisits()
        );
    }
}
