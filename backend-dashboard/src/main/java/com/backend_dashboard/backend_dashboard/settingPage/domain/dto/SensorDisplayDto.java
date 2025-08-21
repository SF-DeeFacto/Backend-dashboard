package com.backend_dashboard.backend_dashboard.settingPage.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class SensorDisplayDto {
    private String sensorId;
    private String zoneId;
    private String sensorType;
    private LocalDateTime updatedAt;
    private String updatedUserId;
    private Double warningLow;
    private Double warningHigh;
    private Double alertLow;
    private Double alertHigh;
}
