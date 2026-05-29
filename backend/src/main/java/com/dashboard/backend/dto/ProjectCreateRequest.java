package com.dashboard.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String domain;
}
