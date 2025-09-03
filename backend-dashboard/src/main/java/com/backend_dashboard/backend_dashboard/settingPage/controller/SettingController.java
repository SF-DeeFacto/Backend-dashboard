package com.backend_dashboard.backend_dashboard.settingPage.controller;

import com.backend_dashboard.backend_dashboard.common.domain.dto.ApiResponseDto;
import com.backend_dashboard.backend_dashboard.settingPage.domain.dto.*;
import com.backend_dashboard.backend_dashboard.redis.dto.UserCacheDto;
import com.backend_dashboard.backend_dashboard.settingPage.service.SensorSettingService;
import com.backend_dashboard.backend_dashboard.redis.service.UserRedisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    // zoneId: 3가지 종류 (a,b,c)
    @GetMapping("/sensor")
    public ApiResponseDto<Page<SensorResponseDto>> getSensorList(
            @RequestHeader("X-Employee-Id") String employeeId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String sensorType,
            @RequestParam(required = false) String zoneId,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        UserCacheDto userInfo = userRedisService.getUserInfo(employeeId);
        Page<SensorResponseDto> sensorList = sensorSettingService.getSensorList(userInfo, sensorType, zoneId, pageable);
        return ApiResponseDto.createOk(sensorList, "센서 목록 조회 성공");
    }

    // 센서 임계치 목록 조회
    @GetMapping("/sensor/threshold")
    public ApiResponseDto<List<SensorThresholdResponseDto>> getSensorThresholdList(
            @RequestHeader("X-Employee-Id") String employeeId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        UserCacheDto userInfo = userRedisService.getUserInfo(employeeId);
        List<SensorThresholdResponseDto> sensorThresholdList = sensorSettingService.getSensorThresholdList(userInfo);
        return ApiResponseDto.createOk(sensorThresholdList, "센서 임계치 목록 조회 성공");
    }

    // 센서 임계치 수정
    @PostMapping("/sensor/threshold/update")
    public ApiResponseDto<SensorThresholdResponseDto> updateSensorThreshold(
            @RequestHeader("X-Employee-Id") String employeeId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody @Valid SensorThresholdUpdateRequestDto request
            ) {
        UserCacheDto userInfo = userRedisService.getUserInfo(employeeId);
        SensorThresholdResponseDto updatedThreshold = sensorSettingService.updateSensorThreshold(userInfo, request);
        return ApiResponseDto.createOk(updatedThreshold, "센서 임계치 수정 성공");
    }

    // AI 추천된 센서 임계치 목록 조회 (Read)
    @GetMapping("/sensor/threshold/recommend")
    public ApiResponseDto<Page<SensorThresholdRecommendationDto>> readSensorThresholdRecommendation(
            @RequestHeader("X-Employee-Id") String employeeId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String sensorType,
            @RequestParam(required = false) String zoneId,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        UserCacheDto userInfo = userRedisService.getUserInfo(employeeId);
        Page<SensorThresholdRecommendationDto> response = sensorSettingService.readSensorThresholdRecommendation(userInfo, sensorType, zoneId, pageable);
        return ApiResponseDto.createOk(response, "AI 추천된 센서 임계치 목록 조회 성공");
    }

    // AI 추천된 센서 임계치 목록 적용 (Update: 승인 버튼)
    @PostMapping("/sensor/threshold/recommend/update/{recommendId}")
    public ApiResponseDto<SensorThresholdRecommendationUpdateDto> updateSensorThresholdRecommendation(
            @RequestHeader("X-Employee-Id") String employeeId,
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable(required = true) Long recommendId
    ) {
        UserCacheDto userInfo = userRedisService.getUserInfo(employeeId);
        SensorThresholdRecommendationUpdateDto result = sensorSettingService.updateSensorThresholdRecommendation(userInfo, recommendId);
        return ApiResponseDto.createOk(result, "AI 추천된 센서 임계치 목록 적용 성공 (승인 완료)");
    }

}
