package com.dashboard.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindAccountRequest {

    @NotBlank(message = "이름을 입력해주세요.")
    private String name;
}
