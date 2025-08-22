package com.backend_dashboard.backend_dashboard.test;

import com.backend_dashboard.backend_dashboard.common.domain.dto.ApiResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    // api 테스트
    @GetMapping(value = "/test")
    public ApiResponseDto<String> test() {
        String response = "테스트입니다.";
        return ApiResponseDto.createOk(response);
    }
}