package com.backend_dashboard.backend_dashboard.settingPage.controller;

import com.backend_dashboard.backend_dashboard.common.domain.dto.ApiResponseDto;
import com.backend_dashboard.backend_dashboard.settingPage.domain.dto.SensorDisplayDto;
import com.backend_dashboard.backend_dashboard.settingPage.service.SensorSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/home/setting")
public class SettingController {
    private final SensorSettingService sensorSettingService;

    // 센서 목록 출력
    @GetMapping("/sensor-list")
    public ApiResponseDto<List<SensorDisplayDto>> getSensorList(
            @RequestParam(required = false) String sensorType,
            @RequestParam(required = false) String zoneId
    ){
        log.info("[센서 목록 조회] contoller 호출");
        List<SensorDisplayDto> data = sensorSettingService.getSensorList(sensorType, zoneId);
        return ApiResponseDto.createOk(data);
    }
    // 특정 타입의 센서 임계치 수정
}
