package com.backend_dashboard.backend_dashboard.settingPage.controller;

import com.backend_dashboard.backend_dashboard.common.domain.dto.ApiResponseDto;
import com.backend_dashboard.backend_dashboard.settingPage.service.WeatherService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@AllArgsConstructor
public class WeatherController {
    final private WeatherService weatherService;

    @GetMapping(value = "/home/weather")
    public ApiResponseDto<Map<String, Object>> getWeatherData() {
        Map<String, Object> weatherData = weatherService.getWeatherData();
        return ApiResponseDto.createOk(weatherData);
    }
}