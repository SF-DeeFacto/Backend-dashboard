package com.backend_dashboard.backend_dashboard.remote.dto;

import com.backend_dashboard.backend_dashboard.settingPage.domain.dto.SensorThresholdUpdateRequestDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class RecommendThresholdMessage {
    private String zoneId;
    private List<SensorThresholdUpdateRequestDto> sensorThresholdUpdateRequestDto;  // 센서 타입별 임계치 리스트
    private LocalDateTime recommendedAt;
}