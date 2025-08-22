package com.backend_dashboard.backend_dashboard.settingPage.controller;

import com.backend_dashboard.backend_dashboard.common.domain.dto.ApiResponseDto;
import com.backend_dashboard.backend_dashboard.settingPage.domain.dto.SensorResponseDto;
import com.backend_dashboard.backend_dashboard.settingPage.domain.dto.SensorThresholdResponseDto;
import com.backend_dashboard.backend_dashboard.settingPage.domain.dto.SensorThresholdUpdateRequestDto;
import com.backend_dashboard.backend_dashboard.redis.dto.UserCacheDto;
import com.backend_dashboard.backend_dashboard.settingPage.service.SensorSettingService;
import com.backend_dashboard.backend_dashboard.redis.service.UserRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/home/setting")
public class SettingController {
    private final SensorSettingService sensorSettingService;
    private final UserRedisService userRedisService;

    // 센서 목록 조회
    @GetMapping("/sensor")
    public ApiResponseDto<List<SensorResponseDto>> getSensorList(
            @RequestHeader("X-Employee-Id") String employeeId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String sensorType,
            @RequestParam(required = false) String zoneId
    ) {
        UserCacheDto userInfo = userRedisService.getUserInfo(employeeId);
        List<SensorResponseDto> sensorList = sensorSettingService.getSensorList(userInfo);
        return ApiResponseDto.createOk(sensorList);
    }

    // 센서 임계치 목록 조회
    @GetMapping("/sensor/threshold")
    public ApiResponseDto<List<SensorThresholdResponseDto>> getSensorThresholdList(
            @RequestHeader("X-Employee-Id") String employeeId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        UserCacheDto userInfo = userRedisService.getUserInfo(employeeId);
        List<SensorThresholdResponseDto> sensorThresholdList = sensorSettingService.getSensorThresholdList(userInfo);
        return ApiResponseDto.createOk(sensorThresholdList);
    }

    // 센서 임계치 수정
    @PostMapping("/sensor/threshold/update")
    public ApiResponseDto<SensorThresholdResponseDto> updateSensorThreshold(
            @RequestHeader("X-Employee-Id") String employeeId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody SensorThresholdUpdateRequestDto request
            ) {
        UserCacheDto userInfo = userRedisService.getUserInfo(employeeId);
        SensorThresholdResponseDto updatedThreshold = sensorSettingService.updateSensorThreshold(request, userInfo);
        return ApiResponseDto.createOk(updatedThreshold);
    }

}
