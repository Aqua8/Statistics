package com.dashboard.backend.dto;

import com.dashboard.backend.domain.PageStat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageStatResponse {
    private String pageUrl;
    private Long views;
    private Long uniqueVisitors;

    public static PageStatResponse from(PageStat stat) {
        return new PageStatResponse(
                stat.getPageUrl(),
                stat.getViews(),
                stat.getUniqueVisitors()
        );
    }
}
