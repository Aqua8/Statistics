package com.dashboard.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ProjectCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String domain;
}
