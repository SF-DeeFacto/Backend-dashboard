package com.backend_dashboard.backend_dashboard.settingPage.service;

import com.backend_dashboard.backend_dashboard.common.domain.entity.SensorThreshold;
import com.backend_dashboard.backend_dashboard.common.domain.repository.SensorThresholdRepository;
import com.backend_dashboard.backend_dashboard.redis.dto.UserCacheDto;
import com.backend_dashboard.backend_dashboard.redis.service.UserCacheService;
import com.backend_dashboard.backend_dashboard.settingPage.domain.dto.SensorDisplayDto;
import com.backend_dashboard.backend_dashboard.settingPage.domain.dto.SensorInfoDto;
import com.backend_dashboard.backend_dashboard.settingPage.repository.SensorSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorSettingService {
    private final UserCacheService userCacheService;
    private final SensorSummaryRepository sensorSummaryRepository;
    private final SensorThresholdRepository sensorThresholdRepository;

    // 센서 목록 조회 함수
    public List<SensorDisplayDto> getSensorList(String sensorType, String zoneId) {
        List<SensorInfoDto> basicSensors;
        log.info("[센서 목록 조회]: sensorType: {} zoneId: {}", sensorType, zoneId);
        if(sensorType != null && zoneId != null) {
            basicSensors = sensorSummaryRepository.findDistinctByTypeAndZone(sensorType, zoneId);
        } else if(sensorType != null) {
            basicSensors = sensorSummaryRepository.findDistinctByType(sensorType);
        } else if(zoneId != null) {
            basicSensors = sensorSummaryRepository.findDistinctByZone(zoneId);
        } else {
            basicSensors = sensorSummaryRepository.findDistinctSensorIdAndZones();
        }

        List<SensorThreshold> thresholds = sensorThresholdRepository.findAll();

        Map<String, SensorThreshold> thresholdMap = thresholds.stream()
                .collect(Collectors.toMap(SensorThreshold::getSensorType, t->t));

        List<SensorDisplayDto> result = new ArrayList<>();

        for(SensorInfoDto sensor : basicSensors) {
            SensorDisplayDto displayDto = new SensorDisplayDto();
            displayDto.setSensorId(sensor.getSensorId());
            displayDto.setSensorType(sensor.getSensorType());
            displayDto.setZoneId(sensor.getZoneId());

            SensorThreshold t = thresholdMap.get(sensor.getSensorType());
            if(t != null) {
                displayDto.setWarningLow(t.getWarningLow());
                displayDto.setWarningHigh(t.getWarningHigh());
                displayDto.setAlertLow(t.getAlertLow());
                displayDto.setAlertHigh(t.getAlertHigh());
                displayDto.setUpdatedAt(t.getUpdatedAt());
                displayDto.setUpdatedUserId(t.getUpdatedUserId());
            }

            result.add(displayDto);
        }

        return result;
    }

    public UserCacheDto getUserByEmployeeId(String employeeId) {
        UserCacheDto user = userCacheService.getUserCache(employeeId);
        if(user == null) return null;

        return user;
    }
}
